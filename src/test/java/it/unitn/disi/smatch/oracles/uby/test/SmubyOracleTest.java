package it.unitn.disi.smatch.oracles.uby.test;

import static it.unitn.disi.diversicon.internal.Internals.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.test.LmfBuilder;
import it.unitn.disi.diversicon.test.DivTester;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import it.unitn.disi.smatch.oracles.SenseMatcherException;
import it.unitn.disi.smatch.oracles.uby.SmubyOracle;
import it.unitn.disi.smatch.oracles.uby.SmubySense;

public class SmubyOracleTest {

    private static final Logger log = LoggerFactory.getLogger(SmubyOracleTest.class);

    private DBConfig dbConfig;

    @Before
    public void beforeMethod() {
        dbConfig = DivTester.createNewDbConfig();
    }

    @After
    public void afterMethod() {
        dbConfig = null;
    }

    /**
     * Creates the classifications, matches them and processes the results.
     *
     * @throws SMatchException
     *             SMatchException
     */
    @Test
    public void testGetSenses() throws SMatchException {

        Diversicons.dropCreateTables(dbConfig);
        
        SmubyOracle oracle = new SmubyOracle(dbConfig, null);
        
        LexicalResource lexicalResource = LmfBuilder.lmf()
                                            .lexicon()
                                            .synset()                                            
                                            .lexicalEntry("ab")
                                            .synset()
                                            .lexicalEntry("a")
                                            .build();
                                                                                               
        Diversicon div = oracle.getDiversicon();
        div.importResource(lexicalResource, true);
        
        DivTester.checkDb(lexicalResource, div);

        assertEquals("synset 1", oracle.getSenses("ab").get(0).getId());
        
        assertEquals(1, div.getLexicalEntries("a", null).size());
        
        assertEquals(new ArrayList(), oracle.getSenses("c"));
        
        List<ISense> senses = oracle.getSenses("a");
                
        SmubySense sense = (SmubySense) senses.get(0);
        assertEquals(1, senses.size());        
        assertEquals("synset 2", sense.getSynset().getId());
        assertEquals("a", sense.getLemmas().get(0));
        
        oracle.getDiversicon().getSession().close();
        
    }
    
    @Test
    public void testIsSourceOppositeThanTarget() throws LinguisticOracleException, SenseMatcherException{
        
        Diversicons.dropCreateTables(dbConfig);
        
        SmubyOracle oracle = new SmubyOracle(dbConfig, null);
        
        LexicalResource lexicalResource = LmfBuilder.lmf()
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
        div.importResource( lexicalResource, true);
        
        DivTester.checkDb(lexicalResource, div);

        ISense sense1 = oracle.getSenses("a").get(0);
        ISense sense2 = oracle.getSenses("b").get(0);
        ISense sense3 = oracle.getSenses("c").get(0);
        
        assertTrue(oracle.isSourceOppositeToTarget(sense1, sense2));
        assertTrue(oracle.isSourceOppositeToTarget(sense2, sense1));
        assertFalse(oracle.isSourceOppositeToTarget(sense1, sense3));
        assertFalse(oracle.isSourceOppositeToTarget(sense2, sense3));
        
    }

    @Test
    public void testIsSourceSimilarTarget() throws LinguisticOracleException, SenseMatcherException{
        
        Diversicons.dropCreateTables(dbConfig);
        
        SmubyOracle oracle = new SmubyOracle(dbConfig, null);
        
        LexicalResource lexicalResource = LmfBuilder.lmf()
                                            .lexicon()
                                            .synset()                                            
                                            .lexicalEntry("a")
                                            .synset()
                                            .lexicalEntry("b")                                            
                                            .synsetRelation(ERelNameSemantics.SYNONYM, 2, 1)
                                            .synsetRelation(ERelNameSemantics.SYNONYM, 1, 2)
                                            .synset()
                                            .lexicalEntry("c")
                                            .synsetRelation(ERelNameSemantics.HYPERNYM, 3,2)
                                            .synsetRelation(ERelNameSemantics.SYNONYMNEAR, 3,1)
                                            .synsetRelation(ERelNameSemantics.SYNONYMNEAR, 1,3)                                            
                                            
                                            .build();
                                                                                               
        Diversicon div = oracle.getDiversicon();
        div.importResource( lexicalResource,  true);
        
        DivTester.checkDb(lexicalResource, div);

        ISense sense1 = oracle.getSenses("a").get(0);
        ISense sense2 = oracle.getSenses("b").get(0);
        ISense sense3 = oracle.getSenses("c").get(0);
                
        
        assertTrue(oracle.isSourceSynonymTarget(sense1, sense2));
        assertTrue(oracle.isSourceSynonymTarget(sense2, sense1));
        assertTrue(oracle.isSourceSynonymTarget(sense3, sense1));
        assertTrue(oracle.isSourceSynonymTarget(sense1, sense3));
        
        assertFalse(oracle.isSourceSynonymTarget(sense3, sense2));
        assertFalse(oracle.isSourceSynonymTarget(sense2, sense3));
        
    }
    
    @Test
    public void testGetMultiwords() throws LinguisticOracleException{
        Diversicons.dropCreateTables(dbConfig);
        
        SmubyOracle oracle = new SmubyOracle(dbConfig, null);
        
        LexicalResource lexicalResource = LmfBuilder.lmf()
                .lexicon()
                .synset()
                .lexicalEntry("a")
                .lexicalEntry("a b")
                .synset()                                            
                .lexicalEntry("c")
                .build();
        
        Diversicon div = oracle.getDiversicon();
        div.importResource( lexicalResource, true);
        
        
        assertEquals(newArrayList(), oracle.getMultiwords("a"));
        assertEquals(newArrayList(), oracle.getMultiwords("a b"));
        assertEquals(new ArrayList(), oracle.getMultiwords("666"));
        
        div.getSession().close();

    }
    
    @Test
    public void testGetBaseforms() throws LinguisticOracleException{
        Diversicons.dropCreateTables(dbConfig);
        
        SmubyOracle oracle = new SmubyOracle(dbConfig, null);
        
        LexicalResource lexicalResource = LmfBuilder.lmf()
                .lexicon()
                .synset()
                .lexicalEntry("a")
                .lexicalEntry("a b")
                .synset()                                            
                .lexicalEntry("c")
                .build();
        
        Diversicon div = oracle.getDiversicon();
        div.importResource( lexicalResource, true);
        
        
        assertEquals(newArrayList("a"), oracle.getBaseForms("a"));
        assertEquals(newArrayList("a b"), oracle.getBaseForms("a b"));
        assertEquals(new ArrayList(), oracle.getBaseForms("666"));
        
        oracle.getDiversicon().getSession().close();
    }
    
    
    @Test
    public void testIsSourceMoreGeneralThanTarget() throws SenseMatcherException, LinguisticOracleException{
        
        Diversicons.dropCreateTables(dbConfig);
        
        SmubyOracle oracle = new SmubyOracle(dbConfig, null);
        
        LexicalResource lexicalResource = LmfBuilder.lmf()
                .lexicon()
                .synset()
                .lexicalEntry("a")
                .synset()                                            
                .lexicalEntry("b")
                .synsetRelation(ERelNameSemantics.HYPERNYM,1)                                            
                .synset()
                .lexicalEntry("c")                                            
                .synsetRelation(ERelNameSemantics.HYPERNYM,2)
                .synsetRelation(ERelNameSemantics.HYPERNYM,1)
                .build();
                                                                                               
        Diversicon div = oracle.getDiversicon();
        div.importResource( lexicalResource, true);
        
        SmubySense sense1 = (SmubySense) oracle.getSenses("a").get(0);
        SmubySense sense2 = (SmubySense) oracle.getSenses("b").get(0);
        SmubySense sense3 = (SmubySense) oracle.getSenses("c").get(0);   
                       
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
        
        div.getSession().close();
    }
    
    @Test
    public void testCreateSense() throws LinguisticOracleException{
        Diversicons.dropCreateTables(dbConfig);
        
        SmubyOracle oracle = new SmubyOracle(dbConfig, null);
        
        LexicalResource lexicalResource = LmfBuilder.lmf()
                .lexicon()
                .synset()
                .lexicalEntry("a")
                .synset()                                            
                .lexicalEntry("b")
                .synsetRelation(ERelNameSemantics.HYPERNYM,1)                                            
                .synset()
                .lexicalEntry("c")                                            
                .synsetRelation(ERelNameSemantics.HYPERNYM,2)
                .synsetRelation(ERelNameSemantics.HYPERNYM,1)
                .build();
                                                                                               
        Diversicon div = oracle.getDiversicon();
        div.importResource( lexicalResource, true);
        
        SmubySense sense1 = (SmubySense) oracle.createSense("synset 1");
        
        SmubySense sense2 = (SmubySense) oracle.createSense("synset 2");
        
        try {
            oracle.createSense("666");
            Assert.fail("Shouldn't arrive here!");
        } catch (Exception ex){
            
        }       
        
        
        div.getSession().close();
    }
    


    @Test
    public void test(){
        /*
         * System.out.println("Starting example...");
         * System.out.println("Creating MatchManager...");
         * IMatchManager mm = MatchManager.getInstanceFromResource(
         * "/it/unitn/disi/smatch/oracles/uby/test/conf/s-match.xml");
         * 
         * String example = "Courses";
         * System.out.println("Creating source context...");
         * IContext s = mm.createContext();
         * s.createRoot(example);
         * 
         * System.out.println("Creating target context...");
         * IContext t = mm.createContext();
         * INode root = t.createRoot("Course");
         * INode node = root.createChild("College of Arts and Sciences");
         * node.createChild("English");
         * 
         * node = root.createChild("College Engineering");
         * node.createChild("Civil and Environmental Engineering");
         * 
         * System.out.println("Preprocessing source context...");
         * mm.offline(s);
         * 
         * System.out.println("Preprocessing target context...");
         * mm.offline(t);
         * 
         * System.out.println("Matching...");
         * IContextMapping<INode> result = mm.online(s, t);
         * 
         * System.out.println("Processing results...");
         * System.out.println("Printing matches:");
         * for (IMappingElement<INode> e : result) {
         * System.out.println(e.getSource().nodeData().getName() + "\t" +
         * e.getRelation() + "\t" + e.getTarget().nodeData().getName());
         * }
         * 
         * System.out.println("Done");
         */

    }
   

}
