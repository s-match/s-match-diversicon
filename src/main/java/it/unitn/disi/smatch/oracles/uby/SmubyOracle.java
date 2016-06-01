package it.unitn.disi.smatch.oracles.uby;

import it.unitn.disi.diversicon.DivException;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.Diversicons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import it.unitn.disi.smatch.oracles.SenseMatcherException;


/**
 * Oracle to access LMF XMLs via
 * <a href="https://dkpro.github.io/dkpro-uby/" target="_blank">DKPRO UBY
 * framework</a>.
 * todo talk about db
 * 
 * This oracle strives to give back some info regardless of possible
 * inconsistencies or errors coming from the underlying LMF database
 * 
 * @since 0.1
 * @author David Leoni
 *
 */
public class SmubyOracle implements ILinguisticOracle, ISenseMatcher {

    private static final Logger log = LoggerFactory.getLogger(SmubyOracle.class);

    private Diversicon diversicon;

    /**
     * Creates an empty H2 in-memory database
     */
    public SmubyOracle() {
        this((String) null);
    }

    /**
     * todo H2 in-memory database
     * 
     * @param lmfXmlPath
     *            a path to an lmf xml to load. If {@code null} database
     *            will be empty.
     *            todo throws what?
     */
    public SmubyOracle(String lmfXmlPath) {
        this(new DBConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver",
                UBYH2Dialect.class.getName(), "root", "pass", true),
                lmfXmlPath);
    }

    public SmubyOracle(DBConfig dbConfig, String lmfXmlPath) {

        Objects.requireNonNull(dbConfig);

        diversicon = Diversicon.create(dbConfig);

        if (lmfXmlPath != null) {
            diversicon.importFiles(lmfXmlPath, "UbyTestTodo"); // todo name
                                                               // meaning ?
        }

    }

    @Override
    public char getRelation(List<ISense> sourceSenses, List<ISense> targetSenses) throws SenseMatcherException {
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.EQUIVALENCE)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Found = using & (SIMILAR_TO) between " +
                                sourceSense.getId() + Arrays.toString(sourceSense.getLemmas()
                                                                                 .toArray())
                                + " and " +
                                targetSense.getId() + Arrays.toString(targetSense.getLemmas()
                                                                                 .toArray()));
                    }
                    return IMappingElement.EQUIVALENCE;
                }
            }
        }
        // Check for less general than
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.LESS_GENERAL)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Found < using @,#m,#s,#p (HYPERNYM, MEMBER_, SUBSTANCE_, PART_HOLONYM) between " +
                                sourceSense.getId() + Arrays.toString(sourceSense.getLemmas()
                                                                                 .toArray())
                                + " and " +
                                targetSense.getId() + Arrays.toString(targetSense.getLemmas()
                                                                                 .toArray()));
                    }
                    return IMappingElement.LESS_GENERAL;
                }
            }
        }
        // Check for more general than
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.MORE_GENERAL)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Found > using @,#m,#s,#p (HYPERNYM, MEMBER_, SUBSTANCE_, PART_HOLONYM) between " +
                                sourceSense.getId() + Arrays.toString(sourceSense.getLemmas()
                                                                                 .toArray())
                                + " and " +
                                targetSense.getId() + Arrays.toString(targetSense.getLemmas()
                                                                                 .toArray()));
                    }
                    return IMappingElement.MORE_GENERAL;
                }
            }
        }
        // Check for opposite meaning
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.DISJOINT)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Found ! using ! (ANTONYM) between " +
                                sourceSense.getId() + Arrays.toString(sourceSense.getLemmas()
                                                                                 .toArray())
                                + " and " +
                                targetSense.getId() + Arrays.toString(targetSense.getLemmas()
                                                                                 .toArray()));
                    }
                    return IMappingElement.DISJOINT;
                }
            }
        }
        return IMappingElement.IDK;
    }

    /**
     * Method which returns whether particular type of relation between
     * two senses holds(according to oracle).
     *
     * @param source
     *            the string of source
     * @param target
     *            the string of target
     * @param rel
     *            the relation between source and target
     * @return whether particular type of relation holds between two senses
     *         according to oracle
     * @throws it.unitn.disi.smatch.oracles.SenseMatcherException
     *             SenseMatcherException
     */
    // copied from wordnet oracle and removed caching. todo review to check caching is actually needed  
    private boolean getRelationFromOracle(ISense source, ISense target, char rel) throws SenseMatcherException {

        if (isSourceSynonymTarget(source, target)) {
            return rel == IMappingElement.EQUIVALENCE;
        } else {
            if (isSourceOppositeToTarget(source, target)) {                
                return rel == IMappingElement.DISJOINT;
            } else {
                if (isSourceLessGeneralThanTarget(source, target)) {                    
                    return rel == IMappingElement.LESS_GENERAL;
                } else {
                    if (isSourceMoreGeneralThanTarget(source, target)) {
                        return rel == IMappingElement.MORE_GENERAL;
                    } else {
                        return IMappingElement.IDK == rel;
                    }
                }
            }
        }
    }

    @Override
    public boolean isSourceMoreGeneralThanTarget(ISense source, ISense target) throws SenseMatcherException {

        checkSourceTarget(source, target);

        if ((source instanceof SmubySense) && (target instanceof SmubySense)) {
            SmubySense sourceSyn = (SmubySense) source;
            SmubySense targetSyn = (SmubySense) target;

            // todo g - do we need POS? if ((POS.NOUN == sourceSyn.getPOS() &&
            // POS.NOUN == targetSyn.getPOS()) || (POS.VERB ==
            // sourceSyn.getPOS() && POS.VERB == targetSyn.getPOS())) {
            if (source.getId()
                      .equals(target.getId())) {
                return false;
            }

            return diversicon.isConnected(
                    targetSyn.getId(),
                    sourceSyn.getId(),
                    -1,
                    Diversicons.getCanonicalTransitiveRelations());

        } else {
            return false;
        }
    }

    private static void checkSourceTarget(ISense source, ISense target) throws SenseMatcherException {

        if (source == null) {
            throw new SenseMatcherException("ERROR: received null source!");
        }

        if (source.getId() == null) {
            throw new SenseMatcherException("ERROR: received null source id!");
        }

        if (source.getId()
                  .isEmpty()) {
            throw new SenseMatcherException("ERROR: received empty source id!");
        }

        if (target == null) {
            throw new SenseMatcherException("ERROR: received null target!");
        }

        if (target.getId() == null) {
            throw new SenseMatcherException("ERROR: received null target id!");
        }

        if (target.getId()
                  .isEmpty()) {
            throw new SenseMatcherException("ERROR: received empty target id!");
        }

    }

    @Override
    public boolean isSourceLessGeneralThanTarget(ISense source, ISense target) throws SenseMatcherException {
        checkSourceTarget(source, target);
        return isSourceMoreGeneralThanTarget(target, source);
    }

    @Override
    public boolean isSourceSynonymTarget(ISense source, ISense target) throws SenseMatcherException {
        checkSourceTarget(source, target);
        
        if (source.equals(target)) {
            return true;
        }
        if ((source instanceof SmubySense) && (target instanceof SmubySense)) {
            try {
                return diversicon.isConnected(source.getId(), target.getId(), 1, ERelNameSemantics.SYNONYM, ERelNameSemantics.SYNONYMNEAR);
            } catch (Exception e) {
                throw new SenseMatcherException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public boolean isSourceOppositeToTarget(ISense source, ISense target) throws SenseMatcherException {

        checkSourceTarget(source, target);

        if (source.getId()
                  .equals(target.getId())) {
            return false;
        }

        if ((source instanceof SmubySense) && (target instanceof SmubySense)) {
            try {
                return diversicon.isConnected(source.getId(), target.getId(), 1, ERelNameSemantics.ANTONYM);
            } catch (DivException ex) {
                throw new SenseMatcherException(ex.getClass()
                                                  .getSimpleName()
                        + ": " + ex.getMessage(), ex);
            }
        }
        return false;
    }

    // todo g here inflections should be dealt with by the oracle
    @Override
    public boolean isEqual(String str1, String str2) throws LinguisticOracleException {
        log.warn("CALLED isEquals(str1, str2), WHICH IS NOT WELL SUPPORTED BY SmubyOracle.");
        return diversicon.getLemmaStringsByWrittenForm(str1).equals(diversicon.getLemmaStringsByWrittenForm(str2));
    }

    /**
     * {@inheritDoc}
     * 
     * NOTE: input word is supposed to be a lemma
     */
    @Override
    public List<ISense> getSenses(String word) throws LinguisticOracleException {
        List<LexicalEntry> lexEntries = diversicon.getLexicalEntries(word, null);

        HashSet<String> foundIds = new HashSet();

        List<ISense> ret = new ArrayList();

        // avoid dups
        for (LexicalEntry lexEntry : lexEntries) {
            List<Synset> synsets = lexEntry.getSynsets();
            for (Synset synset : synsets) {
                if (!foundIds.contains(synset.getId())) {
                    foundIds.add(synset.getId());
                    ret.add(new SmubySense(synset, this));
                }
            }
        }

        return ret;
    }

    @Override
    public List<String> getBaseForms(String derivation) throws LinguisticOracleException {
        log.warn("CALLED getBaseForms, WHICH IS NOT WELL SUPPORTED BY SmubyOracle.");
        return diversicon.getLemmaStringsByWrittenForm(derivation);
    }

    // the create is misleading, it's actually retrieving a sense from oracle
    @Override
    public ISense createSense(String id) throws LinguisticOracleException {

        Synset ubysyn = null;

        try {
            ubysyn = diversicon.getSynsetById(id);
        } catch (IllegalArgumentException ex) {
            throw new LinguisticOracleException("Couldn't find provided id!", ex);
        }
        try {
            return new SmubySense(ubysyn, this);
        } catch (Exception ex) {
            throw new LinguisticOracleException("Error while creating a UbySense!", ex);
        }

    }

    // IF we really need this look at UBY's Component class
    @Override
    public List<List<String>> getMultiwords(String beginning) throws LinguisticOracleException {
        log.warn("CALLED getMultiwords WHICH IS NOT SUPPORTED BY SmubyOracle, RETURNING AN EMPTY LIST");
        return new ArrayList();
    }

    public Diversicon getDiversicon() {
        return diversicon;
    }

}
