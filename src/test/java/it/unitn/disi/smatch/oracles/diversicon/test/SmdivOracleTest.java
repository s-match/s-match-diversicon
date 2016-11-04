package it.unitn.disi.smatch.oracles.diversicon.test;

import static eu.kidf.diversicon.core.internal.Internals.newArrayList;
import static eu.kidf.diversicon.core.test.DivTester.tid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.Diversicons;
import eu.kidf.diversicon.core.test.LmfBuilder;
import eu.kidf.diversicon.core.test.DivTester;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import it.unitn.disi.smatch.oracles.SenseMatcherException;
import it.unitn.disi.smatch.oracles.diversicon.SmdivOracle;
import it.unitn.disi.smatch.oracles.diversicon.SmdivSense;

/**
 * @since 0.1.0
 */
public class SmdivOracleTest {

    private static final Logger log = LoggerFactory.getLogger(SmdivOracleTest.class);

    private DBConfig dbConfig;

    /**
     * @since 0.1.0
     */
    @Before
    public void beforeMethod() {
        dbConfig = DivTester.createNewDbConfig();
    }

    /**
     * @since 0.1.0
     */
    @After
    public void afterMethod() {
        dbConfig = null;
    }

    /**
     * Creates the classifications, matches them and processes the results.
     *
     * @throws SMatchException
     *             SMatchException
     *             
     * @since 0.1.0
     */
    @Test
    public void testGetSenses() throws SMatchException {

        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("ab")
                                           .synset()
                                           .lexicalEntry("a")
                                           .build();

        Diversicon div = oracle.getDiversicon();

        div.importResource(lexRes, DivTester.createLexResPackage(lexRes), true);

        DivTester.checkDb(lexRes, div);

        assertEquals(tid("synset-1"), oracle.getSenses("ab")
                                            .get(0)
                                            .getId());

        assertEquals(1, div.getLexicalEntries("a", null)
                           .size());

        assertEquals(new ArrayList<>(), oracle.getSenses("c"));

        List<ISense> senses = oracle.getSenses("a");

        SmdivSense sense = (SmdivSense) senses.get(0);
        assertEquals(1, senses.size());
        assertEquals(tid("synset-2"), sense.getSynset()
                                           .getId());
        assertEquals("a", sense.getLemmas()
                               .get(0));

        oracle.getDiversicon()
              .getSession()
              .close();

    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testIsSourceOppositeThanTarget() throws LinguisticOracleException, SenseMatcherException {

        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("a")
                                           .synset()
                                           .lexicalEntry("b")
                                           .synsetRelation(ERelNameSemantics.ANTONYM, 2, 1)
                                           .synsetRelation(ERelNameSemantics.ANTONYM, 1, 2)
                                           .synset()
                                           .lexicalEntry("c")
                                           .synsetRelation(ERelNameSemantics.HYPONYM, 1)

                                           .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        DivTester.checkDb(lexRes, div);

        ISense sense1 = oracle.getSenses("a")
                              .get(0);
        ISense sense2 = oracle.getSenses("b")
                              .get(0);
        ISense sense3 = oracle.getSenses("c")
                              .get(0);

        assertTrue(oracle.isSourceOppositeToTarget(sense1, sense2));
        assertTrue(oracle.isSourceOppositeToTarget(sense2, sense1));
        assertFalse(oracle.isSourceOppositeToTarget(sense1, sense3));
        assertFalse(oracle.isSourceOppositeToTarget(sense2, sense3));

    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testIsSourceSimilarTarget() throws LinguisticOracleException, SenseMatcherException {

        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("a")
                                           .synset()
                                           .lexicalEntry("b")
                                           .synsetRelation(ERelNameSemantics.SYNONYM, 2, 1)
                                           .synsetRelation(ERelNameSemantics.SYNONYM, 1, 2)
                                           .synset()
                                           .lexicalEntry("c")
                                           .synsetRelation(ERelNameSemantics.HYPERNYM, 3, 2)
                                           .synsetRelation(ERelNameSemantics.SYNONYMNEAR, 3, 1)
                                           .synsetRelation(ERelNameSemantics.SYNONYMNEAR, 1, 3)

                                           .build();

        Diversicon div = oracle.getDiversicon();

        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        DivTester.checkDb(lexRes, div);

        ISense sense1 = oracle.getSenses("a")
                              .get(0);
        ISense sense2 = oracle.getSenses("b")
                              .get(0);
        ISense sense3 = oracle.getSenses("c")
                              .get(0);

        assertTrue(oracle.isSourceSynonymTarget(sense1, sense2));
        assertTrue(oracle.isSourceSynonymTarget(sense2, sense1));
        assertTrue(oracle.isSourceSynonymTarget(sense3, sense1));
        assertTrue(oracle.isSourceSynonymTarget(sense1, sense3));

        assertFalse(oracle.isSourceSynonymTarget(sense3, sense2));
        assertFalse(oracle.isSourceSynonymTarget(sense2, sense3));

    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testGetMultiwords() throws LinguisticOracleException {
        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("a")
                                           .lexicalEntry("a b")
                                           .synset()
                                           .lexicalEntry("c")
                                           .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        assertEquals(newArrayList(newArrayList("a", "b")), oracle.getMultiwords("a"));
        assertEquals(newArrayList(newArrayList("a", "b")), oracle.getMultiwords("a b"));
        assertEquals(newArrayList(), oracle.getMultiwords("c"));
        assertEquals(new ArrayList<>(), oracle.getMultiwords("666"));

        div.getSession()
           .close();

    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testGetBaseforms() throws LinguisticOracleException {
        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("a")
                                           .lexicalEntry("a b")
                                           .synset()
                                           .lexicalEntry("c")
                                           .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        assertEquals(newArrayList("a"), oracle.getBaseForms("a"));
        assertEquals(newArrayList("a b"), oracle.getBaseForms("a b"));
        assertEquals(new ArrayList<>(), oracle.getBaseForms("666"));

        oracle.getDiversicon()
              .getSession()
              .close();
    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testIsSourceMoreGeneralThanTarget() throws SenseMatcherException, LinguisticOracleException {

        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("a")
                                           .synset()
                                           .lexicalEntry("b")
                                           .synsetRelation(ERelNameSemantics.HYPERNYM, 1)
                                           .synset()
                                           .lexicalEntry("c")
                                           .synsetRelation(ERelNameSemantics.HYPERNYM, 2)
                                           .synsetRelation(ERelNameSemantics.HYPERNYM, 1)
                                           .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        SmdivSense sense1 = (SmdivSense) oracle.getSenses("a")
                                               .get(0);
        SmdivSense sense2 = (SmdivSense) oracle.getSenses("b")
                                               .get(0);
        SmdivSense sense3 = (SmdivSense) oracle.getSenses("c")
                                               .get(0);

        assertTrue(oracle.isSourceMoreGeneralThanTarget(sense1, sense2));
        assertTrue(oracle.isSourceMoreGeneralThanTarget(sense2, sense3));
        assertTrue(oracle.isSourceMoreGeneralThanTarget(sense1, sense3));
        assertFalse(oracle.isSourceMoreGeneralThanTarget(sense1, sense1));
        assertFalse(oracle.isSourceMoreGeneralThanTarget(sense2, sense1));
        assertFalse(oracle.isSourceMoreGeneralThanTarget(sense3, sense1));

        assertTrue(oracle.isSourceLessGeneralThanTarget(sense2, sense1));
        assertTrue(oracle.isSourceLessGeneralThanTarget(sense3, sense2));
        assertTrue(oracle.isSourceLessGeneralThanTarget(sense3, sense1));
        assertFalse(oracle.isSourceLessGeneralThanTarget(sense1, sense1));
        assertFalse(oracle.isSourceLessGeneralThanTarget(sense1, sense2));
        assertFalse(oracle.isSourceLessGeneralThanTarget(sense1, sense3));

        div.getSession()
           .close();
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testCreateSense() throws LinguisticOracleException {

        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("a")
                                           .synset()
                                           .lexicalEntry("b")
                                           .synsetRelation(ERelNameSemantics.HYPERNYM, 1)
                                           .synset()
                                           .lexicalEntry("c")
                                           .synsetRelation(ERelNameSemantics.HYPERNYM, 2)
                                           .synsetRelation(ERelNameSemantics.HYPERNYM, 1)
                                           .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        SmdivSense sense1 = (SmdivSense) oracle.createSense(tid("synset-1"));

        SmdivSense sense2 = (SmdivSense) oracle.createSense(tid("synset-2"));

        try {
            oracle.createSense("666");
            Assert.fail("Shouldn't arrive here!");
        } catch (Exception ex) {

        }

        div.getSession()
           .close();
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testGetBaseFormsWordForm() throws LinguisticOracleException {

        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("a")
                                           .wordform("b")
                                           .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        List<String> v = new ArrayList<>();
        
        v.add("a");
        
        assertEquals(v,  oracle.getBaseForms("b"));
        assertEquals(new ArrayList<>(),  oracle.getBaseForms("c"));
        
        div.getSession().close();
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testGetBaseFormsLemmatizer() throws LinguisticOracleException {

        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
                                           .lexicon()
                                           .synset()
                                           .lexicalEntry("love")                                          
                                           .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);

        List<String> v = new ArrayList<>();
                
        v.add("love");
        
        assertEquals(v,  oracle.getBaseForms("loves"));
        
        assertEquals(new ArrayList<>(),  oracle.getBaseForms("fasdfasfasdf"));
        
        div.getSession().close();
    }
    
    
}
