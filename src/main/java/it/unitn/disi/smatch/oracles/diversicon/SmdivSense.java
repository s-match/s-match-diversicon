package it.unitn.disi.smatch.oracles.diversicon;

import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.internal.Internals;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;

/**
 * 
 * Diversicon-based sense implementation.
 * 
 * @since 0.1.0
 * @author <a rel="author" href="http://davidleoni.it/">David Leoni</a>
 */
public class SmdivSense implements ISense, Serializable {
    
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(SmdivSense.class);

    private Synset synset;

    private Diversicon diversicon;

    private SmdivOracle oracle;

    /**
     * @since 0.1.0
     * 
     */
    public SmdivSense(Synset synset, SmdivOracle oracle) {
        Objects.requireNonNull(synset);
        Objects.requireNonNull(oracle);
        this.synset = synset;
        this.oracle = oracle;
    }

    @Override
    public String getId() {
        return synset.getId();
    }

    @Override
    public String getGloss() {
        // todo not using getGloss because it's a mashup of sense glosses.
        // Definition seems more
        // convenient, but maybe a fallback to glosses would be useful
        String ret = synset.getDefinitionText();
        if (ret == null) {
            log.debug("Found null in synset " + synset.getId() + ", returning empty string as gloss");
            return "";
        }
        return ret;
    }

    @Override
    public List<String> getLemmas() {
        ArrayList<String> ret = new ArrayList();
        // uby.
        try {
            for (de.tudarmstadt.ukp.lmf.model.core.Sense ubySense : synset.getSenses()) {
                LexicalEntry ubyLexicalEntry = null;
                try {
                    ubyLexicalEntry = ubySense.getLexicalEntry();
                    Objects.requireNonNull(ubyLexicalEntry);

                    try {
                        String lemma = ubyLexicalEntry.getLemmaForm();
                        Objects.requireNonNull(lemma);
                        ret.add(lemma);
                    } catch (Exception ex) {
                        throw new SmdivException("Error while retrieving lemma!");
                    }
                } catch (Exception ex) {
                    throw new SmdivException("Error while retrieving lexical entry for sense " + ubySense.getId(), ex);
                }
            }
        } catch (Exception ex) {
            throw new SmdivException("Error while retrieving lemmas for synset " + synset.getId() + ", returning an empty list", ex);
        }
        return Collections.unmodifiableList(ret);
        
    }

    // todo g SenseAxis, SynsetRelations or both?
    @Override
    public List<ISense> getParents() throws LinguisticOracleException {
        return getRelationTargets(
                -1,
                ERelNameSemantics.HYPERNYM,
                ERelNameSemantics.HYPERNYMINSTANCE);
    }

    @Override
    public List<ISense> getParents(int depth) throws LinguisticOracleException {
        return getRelationTargets(
                depth,
                ERelNameSemantics.HYPERNYM,
                ERelNameSemantics.HYPERNYMINSTANCE);
    }

    @Override
    public List<ISense> getChildren() throws LinguisticOracleException {
        return getRelationTargets(
                -1,
                ERelNameSemantics.HYPONYM,
                ERelNameSemantics.HYPONYMINSTANCE);
    }

    @Override
    public List<ISense> getChildren(int depth) throws LinguisticOracleException {
        return getRelationTargets(
                depth,
                ERelNameSemantics.HYPONYM,
                ERelNameSemantics.HYPONYMINSTANCE);
    }

    /**
     * Returns a list of all senses to which this sense is connected to with 
     *       edges tagged {@code relNamesArr}
     * 
     * @param depth
     *            Only edges with {@code depth} equal or less than depth are retrieved. 
     *            If -1 all edges are returned
     * @throws LinguisticOracleException
     * @since 0.1.0 
     */
    public List<ISense> getRelationTargets(int depth, String... relNamesArr) throws LinguisticOracleException {

        try {
            Internals.checkNotEmpty(relNamesArr, "invalid relation names!");
            Internals.checkDepth(depth);

            ArrayList<ISense> ret = new ArrayList();

            ArrayList<String> relNames = new ArrayList<String>(Arrays.asList(relNamesArr));

            Iterator<Synset> iter = oracle.getDiversicon()
                                          .getConnectedSynsets(getId(), depth, relNames);
            while (iter.hasNext()) {
                Synset syn = iter.next();
                ret.add(new SmdivSense(syn, oracle));
            }
            return Collections.unmodifiableList(ret);
            
        } catch (Exception ex) {
            throw new LinguisticOracleException("Error while getting relation targets!", ex);
        }
        
    }

    /**
     * @since 0.1.0
     */
    public Synset getSynset() {
        return synset;
    }

    /**
     * @since 0.1.0 
     */
    public void setSynset(Synset synset) {
        this.synset = synset;
    }

}