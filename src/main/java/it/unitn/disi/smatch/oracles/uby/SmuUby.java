package it.unitn.disi.smatch.oracles.uby;

import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;
import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotNull;
import java.io.File;
import java.util.List;
import java.util.Objects;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.api.Uby;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;

/**
 * Version of Uby with some additional fields in the db to speed up computations
 *
 * @since 0.1
 */
public class SmuUby extends Uby {

    private static final Logger log = LoggerFactory.getLogger(SmuUby.class);
    
    
    /**
     * Amount of items to flush when writing into db with Hibernate.
     */
    private static final int BATCH_FLUSH_COUNT = 20;

    public SmuUby(DBConfig dbConfig) {
        super(dbConfig);

        if (dbConfig == null) {
            throw new IllegalArgumentException("database configuration is null");
        }

        this.dbConfig = dbConfig;

        // dav: note here we are overwriting cfg and sessionFactory
        cfg = SmuUtils.getHibernateConfig(dbConfig);

        ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder().applySettings(cfg.getProperties());
        sessionFactory = cfg.buildSessionFactory(serviceRegistryBuilder.buildServiceRegistry());
        session = sessionFactory.openSession();
    }

    /**
     * Augments the graph with is-a transitive closure and eventally adds
     * symmetric hyperym/hyponim relations.
     */
    // todo what about provenance? todo instances?
    public void augmentGraph() {

        normalizeGraph();

        computeTransitiveClosure();

    }

    /**
     * Returns {@code true} if {@code source} contains a relation toward
     * {@code target} synset.
     * Returns false otherwise.
     */
    private static boolean containsRel(Synset source, Synset target, String relName) {
        checkNotNull(source, "Invalid source!");
        checkNotNull(target, "Invalid target!");
        checkNotEmpty(relName, "Invalid relName!");

        for (SynsetRelation synRel : source.getSynsetRelations()) {
            if (relName.equals(synRel.getRelName())
                    && Objects.equals(synRel.getTarget()
                                            .getId(),
                            (target.getId()))) {
                return true;
            }
        }
        return false;
    }

    private static String quote(String s) {
        return "'"+s+"'";
    }
    
    private static String makeSqlList(Iterable<String> iterable) {
        StringBuilder retb = new StringBuilder("(");

        boolean first = true;
        for (String s : iterable) {
            if (first) {
                retb.append( quote(s));
                first = false;
            } else {
                retb.append(", " + quote(s));
            }
        }
        retb.append(")");
        return retb.toString();
    }

    /*
     * Adds missing edges of depth 1 for relations we consider as canonical.
     */
    private void normalizeGraph() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        String hql = "FROM Synset";
        Query query = session.createQuery(hql);

        ScrollableResults synsets = query
                                         .setCacheMode(CacheMode.IGNORE)
                                         .scroll(ScrollMode.FORWARD_ONLY);
        int count = 0;

        InsertionStats relStats = new InsertionStats();

        while (synsets.next()) {

            Synset synset = (Synset) synsets.get(0);
            log.info("Processing synset with id " + synset.getId() + " ...");

            List<SynsetRelation> relations = synset.getSynsetRelations();

            for (SynsetRelation sr : relations) {
                SmuSynsetRelation ssr = (SmuSynsetRelation) sr;

                if (SmuUtils.hasInverse(ssr.getRelName())) {
                    String inverseRelName = SmuUtils.getInverse(ssr.getRelName());
                    if (SmuUtils.isCanonical(inverseRelName)
                            && !containsRel(ssr.getTarget(),
                                    ssr.getSource(),
                                    inverseRelName)) {
                        SmuSynsetRelation newSsr = new SmuSynsetRelation();

                        newSsr.setDepth(1);
                        newSsr.setProvenance(SmuUby.getProvenanceId());
                        newSsr.setRelName(inverseRelName);
                        newSsr.setRelType(SmuUtils.getCanonicalRelationType(inverseRelName));
                        newSsr.setSource(ssr.getTarget());
                        newSsr.setTarget(ssr.getSource());

                        ssr.getTarget()
                           .getSynsetRelations()
                           .add(newSsr);
                        session.save(newSsr);
                        session.saveOrUpdate(ssr.getTarget());
                        relStats.inc(inverseRelName);
                    }
                }

            }

            if (++count % BATCH_FLUSH_COUNT == 0) {
                // flush a batch of updates and release memory:
                session.flush();
                session.clear();
            }
        }

        tx.commit();
        session.close();

        log.info("");
        log.info("Done normalizing SynsetRelations:");
        log.info("");
        log.info(relStats.toString());
    }

    /**
     * Before calling this, the graph has to be normalized by calling {@link #normalizeGraph()}
     */
    private void computeTransitiveClosure() {

        log.info("Computing transitive closure for SynsetRelations ...");

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();        

        InsertionStats relStats = new InsertionStats();

        int depthToSearch = 1;
        int count = 0;

        // As: the edges computed so far
        // Bs: original edges
        
        String hqlSelect = "    SELECT SR_A.source, SR_B.target,  SR_A.relName"
                + "      FROM SynsetRelation SR_A, SynsetRelation SR_B"
                + "      WHERE"
                + "          SR_A.relName IN " + makeSqlList(SmuUtils.getCanonicalRelations())
                + "      AND SR_A.depth = :depth"
                + "      AND SR_B.depth = 1"
                + "      AND SR_A.relName = SR_B.relName"
                + "      AND SR_A.target = SR_B.source"
                // don't want to add twice edges                
                + "      AND NOT EXISTS" 
                + "             ("
                + "               "
                + "               FROM SynsetRelation SR_C"
                + "               WHERE "
                + "                         SR_A.relName = SR_C.relName"
                + "                    AND  SR_A.source=SR_C.source"
                + "                    AND  SR_B.target=SR_C.target"                                            
                + "             )";

        
        int processedRelationsInCurLevel = 0;
        
        do {
            processedRelationsInCurLevel = 0;

            // log.info("Augmenting SynsetRelation graph with edges of depth " + (depthToSearch + 1) + "  ...");

            Query query = session.createQuery(hqlSelect);
            query.setParameter("depth", depthToSearch);            

            
            ScrollableResults results = query
                                             .setCacheMode(CacheMode.IGNORE)
                                             .scroll(ScrollMode.FORWARD_ONLY);
            while (results.next()) {

                Synset source = (Synset) results.get(0);
                Synset target = (Synset) results.get(1);
                String relName = (String) results.get(2);
                
                SmuSynsetRelation ssr = new SmuSynsetRelation();
                ssr.setDepth(depthToSearch + 1);
                ssr.setProvenance(SmuUby.getProvenanceId());
                ssr.setRelName(relName);
                ssr.setRelType(SmuUtils.getCanonicalRelationType(relName));
                ssr.setSource(source);
                ssr.setTarget(target);

                source.getSynsetRelations().add(ssr);
                session.save(ssr);
                session.saveOrUpdate(source);
                // log.info("Inserted " + ssr.toString());
                relStats.inc(relName);
                processedRelationsInCurLevel += 1;
                
                if (++count % BATCH_FLUSH_COUNT == 0) {
                    // flush a batch of updates and release memory:
                    session.flush();
                    session.clear();
                }
            }
            
            depthToSearch += 1;
            
        } while (processedRelationsInCurLevel > 0);
        
        tx.commit();
        session.close();
        
        log.info("");
        log.info("Done computing transitive closure for SynsetRelations:");
        log.info("");
        log.info(relStats.toString());        
    }

    /**
     * 
     * @param filepath
     * @param lexicalResourceName
     *            todo meaning? name seems not be required to be in the xml
     */
    public void loadLmfXml(String filepath, String lexicalResourceName) {

        XMLToDBTransformer trans = new XMLToDBTransformer(dbConfig);

        try {
            trans.transform(new File(filepath), lexicalResourceName);
        } catch (Exception ex) {
            throw new RuntimeException("Error while loading lmf xml " + filepath, ex);
        }

        try {
            augmentGraph();
        } catch (Exception ex) {
            log.error("Error while augmenting graph with computed edges!", ex);
        }

    }

    /**
     * Returns the fully qualified package name.
     */
    public static String getProvenanceId() {
        return SmuUby.class.getPackage()
                           .getName();
    }
}
