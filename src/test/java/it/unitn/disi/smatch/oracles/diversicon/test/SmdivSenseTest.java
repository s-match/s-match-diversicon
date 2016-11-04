package it.unitn.disi.smatch.oracles.diversicon.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unitn.disi.smatch.oracles.diversicon.SmdivUtils.*;
import static it.unitn.disi.smatch.oracles.diversicon.test.SmdivUtilsTest.*;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.Diversicons;
import eu.kidf.diversicon.core.test.LmfBuilder;
import eu.kidf.diversicon.core.test.DivTester;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import it.unitn.disi.smatch.oracles.diversicon.SmdivOracle;
import it.unitn.disi.smatch.oracles.diversicon.SmdivSense;
import it.unitn.disi.smatch.oracles.diversicon.SmdivUtils;

public class SmdivSenseTest {

    private static final Logger log = LoggerFactory.getLogger(SmdivSenseTest.class);

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

    @Test
    public void testGetChildrenParents() throws LinguisticOracleException {
        Diversicons.dropCreateTables(dbConfig);

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
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
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                false);                

        SmdivSense sense1 = (SmdivSense) oracle.getSenses("a")
                                               .get(0);
        SmdivSense sense2 = (SmdivSense) oracle.getSenses("b")
                                               .get(0);
        SmdivSense sense3 = (SmdivSense) oracle.getSenses("c")
                                               .get(0);

        assertEquals(
                newHashSet("synset 2", "synset 3"),
                new HashSet<>(getIds(sense1.getChildren())));
        assertEquals(
                newArrayList("synset 2"),
                SmdivUtils.getIds(sense1.getChildren(1)));

        assertEquals(
                newHashSet("synset 1", "synset 2"),
                new HashSet<>(getIds(sense3.getParents())));
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

        SmdivOracle oracle = new SmdivOracle(dbConfig);

        LexicalResource lexRes = LmfBuilder.lmf()
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
        div.importResource(lexRes,
                DivTester.createLexResPackage(lexRes),
                true);
        
        DivTester.checkDb(lexRes, div);
        
        
        SmdivSense sense1 = (SmdivSense) oracle.getSenses("a")
                                               .get(0);
        SmdivSense sense2 = (SmdivSense) oracle.getSenses("b")
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