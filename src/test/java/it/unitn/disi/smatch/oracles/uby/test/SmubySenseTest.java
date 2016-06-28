package it.unitn.disi.smatch.oracles.uby.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unitn.disi.smatch.oracles.uby.test.SmubyUtilsTest.*;
import static it.unitn.disi.smatch.oracles.uby.SmubyUtils.*;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.test.LmfBuilder;
import it.unitn.disi.diversicon.test.DivTester;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import it.unitn.disi.smatch.oracles.uby.SmubyOracle;
import it.unitn.disi.smatch.oracles.uby.SmubySense;
import it.unitn.disi.smatch.oracles.uby.SmubyUtils;

public class SmubySenseTest {

    private static final Logger log = LoggerFactory.getLogger(SmubySenseTest.class);

    private DBConfig dbConfig;

    @Before
    public void beforeMethod() {
        dbConfig = DivTester.createNewDbConfig();
    }

    @After
    public void afterMethod() {
        dbConfig = null;
    }

    @Test
    public void testGetChildrenParents() throws LinguisticOracleException {
        Diversicons.dropCreateTables(dbConfig);

        SmubyOracle oracle = new SmubyOracle(dbConfig, null);

        LexicalResource lexicalResource = LmfBuilder.lmf()
                                                    .lexicon()
                                                    .synset()
                                                    .lexicalEntry("a")
                                                    .synset()
                                                    .lexicalEntry("b")
                                                    .synsetRelation(ERelNameSemantics.HYPERNYM, 1)
                                                    .synsetRelation(ERelNameSemantics.HOLONYM, 1)
                                                    .synset()
                                                    .lexicalEntry("c")
                                                    .synsetRelation(ERelNameSemantics.HYPERNYM, 2)
                                                    .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexicalResource, false);

        SmubySense sense1 = (SmubySense) oracle.getSenses("a")
                                               .get(0);
        SmubySense sense2 = (SmubySense) oracle.getSenses("b")
                                               .get(0);
        SmubySense sense3 = (SmubySense) oracle.getSenses("c")
                                               .get(0);

        assertEquals(
                newHashSet("synset 2", "synset 3"),
                new HashSet(getIds(sense1.getChildren())));
        assertEquals(
                newArrayList("synset 2"),
                SmubyUtils.getIds(sense1.getChildren(1)));

        assertEquals(
                newHashSet("synset 1", "synset 2"),
                new HashSet(getIds(sense3.getParents())));
        assertEquals(
                newArrayList("synset 2"),
                getIds(sense3.getParents(1)));

        oracle.getDiversicon()
              .getSession()
              .close();

    }

    @Test
    public void testGetGlossLemmas() throws LinguisticOracleException {
        Diversicons.dropCreateTables(dbConfig);

        SmubyOracle oracle = new SmubyOracle(dbConfig, null);

        LexicalResource lexicalResource = LmfBuilder.lmf()
                                                    .lexicon()
                                                    .synset()
                                                    .definition("da1")
                                                    .definition("da2")
                                                    .lexicalEntry("a")
                                                    .synset()
                                                    .definition("db3")
                                                    .lexicalEntry("b")
                                                    .build();

        Diversicon div = oracle.getDiversicon();
        div.importResource(lexicalResource, true);
        
        DivTester.checkDb(lexicalResource, div);
        
        
        SmubySense sense1 = (SmubySense) oracle.getSenses("a")
                                               .get(0);
        SmubySense sense2 = (SmubySense) oracle.getSenses("b")
                                               .get(0);
                
        
        assertEquals("da1", sense1.getGloss());
        assertEquals("db3", sense2.getGloss());

        assertEquals(newArrayList("a"), sense1.getLemmas());
        assertEquals(newArrayList("b"), sense2.getLemmas());
        
        
        oracle.getDiversicon()
              .getSession()
              .close();

    }

}