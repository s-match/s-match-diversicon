package it.unitn.disi.smatch.oracles.diversicon;

import java.util.HashSet;
import java.util.Set;

/**
 * Processor for the inflected/non-inflected forms in a given language.
 * 
 * @author Marco Marasca, marasca@disi.unitn.it
 */
class SuffixProcessor {

    /**
     * The model instance
     */
    private SuffixModel model;

    /**
     * Initializes the processor to use the given model.
     * 
     * @param model The suffix forms model
     */
    public SuffixProcessor(SuffixModel model) {
        this.model = model;
    }

    /**
     * Returns the model used by this processor.
     * 
     * @return The model used by this instance
     */
    public SuffixModel getModel() {
        return model;
    }

    /**
     * Returns the set of strings that represent dictionary forms of the given token.
     * 
     * @param token The token to process
     * @param pos The part of speech of the token
     * @return The set of strings that represent dictionary forms of the given token
     *         i.e., possible lemmas)
     */
    public Set<String> process(String token, String pos) {
        Set<String> result = new HashSet<String>();
        int i = 0;
        String[] inflectedForms = model.getInflectedForms(pos);
        String[] uninflectedForms = model.getUninflectedForms(pos);
        for (String inflected : inflectedForms) {
            if (token.endsWith(inflected)) {
                result.add(token.substring(0, token.length() - inflectedForms[i].length())
                           + uninflectedForms[i]);
            }
            i++;
        }
        return result;
    }

}