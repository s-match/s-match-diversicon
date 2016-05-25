package it.unitn.disi.smatch.oracles.uby.test;



import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.smatch.oracles.uby.SmuUby;
import it.unitn.disi.smatch.oracles.uby.SmuUtils;


/**
 * Experiments in Hibernate Hell. 
 *
 */
public class HibernateExperimentsTest {

    private static final Logger log = LoggerFactory.getLogger(HibernateExperimentsTest.class);
    
    /**
     * Adds missing edges for relations we consider as canonical
     * 
     * Keeping this here just to remember what a horror it was
     * Adds missing edges for relations we consider as canonical
     */
    private void normalizeGraphWithSqlCrap() {

        log.info("Going to normalizing graph with canonical relations ...");

        DBConfig dbConfig = new DBConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver",
                UBYH2Dialect.class.getName(), "root", "pass", true);    
        
        SmuUby uby = new SmuUby(dbConfig);
        
        Session session = uby.getSession();

        Transaction tx = session.beginTransaction();

        for (String relName : SmuUtils.getCanonicalRelations()) {

            log.info("Normalizing graph with canonical relation " + relName + " ...");

            String inverseRelName = SmuUtils.getInverse(relName);
            log.info("inverse relation name = " + inverseRelName);

/*            String hqlInsert = "INSERT INTO SynsetRelation (source, target, relType, relName, depth, provenance) "
                    + "  SELECT SR.target, SR.source,  SR.relType, :relName,  1, :provenance"
                    + "  FROM SynsetRelation SR"; */
            /* String hqlInsert = "INSERT INTO SynsetRelation (source, target,  relType, relName, idx, depth, provenance) "
                    + "  SELECT SR.target, SR.source,  SR.relType, '" + relName + "', ROWNUM + 100, 1, '" + getProvenance() + "'"
                    + "  FROM SynsetRelation SR"; */           

         /*   query.setParameter("relName", "'" + relName + "'")
                 .setParameter("inverseRelName", "'" +inverseRelName + "'")
                 .setParameter("provenance", "'" + getProvenance() + "'");
*/
                                   
            
            //log.info("Inserted " + createdEntities + " " + relName + " edges.");            
            
            String hqlInsert = "INSERT INTO SynsetRelation (synsetId, target,  relType, relName,  depth, provenance) "
                    + "  SELECT SR.target, SR.synsetId,  SR.relType, " 
                    + "         '" + relName + "', 1, '" + SmuUby.getProvenanceId() + "'"
                    + "  FROM SynsetRelation SR"    
                   +  "  WHERE" 
                    + "        SR.relName='" + inverseRelName + "'"; 
  /*                  + "      AND SR.depth=1"  
                    + "    AND SR.provenance=''"; */  
/*                    + "      AND (SR.target, SR.synsetId) NOT IN " // so
                    + "         (" 
                    + "                SELECT (SR2.synsetId, SR2.target)"
                    + "                FROM SynsetRelation SR2" 
                    + "                WHERE      "
                    + "                      SR2.relName='"+relName + "'" 
                    + "                  AND SR2.depth=1"
                    + "         )"; 
*/
            // Query query = session.createQuery(hqlInsert);
            Query query = session.createSQLQuery(hqlInsert);

         /*   query.setParameter("relName", "'" + relName + "'")
                 .setParameter("inverseRelName", "'" +inverseRelName + "'")
                 .setParameter("provenance", "'" + getProvenance() + "'");
*/
            int createdEntities = query.executeUpdate();
            log.info("Inserted " + createdEntities + " " + relName + " edges.");

        }

        tx.commit();
        session.close();

        log.info("Done normalizing graph with canonical relations.");

    }

}
