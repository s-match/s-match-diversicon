package it.unitn.disi.smatch.oracles.diversicon;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;

/**
 * The suffix model used by the lemmatizer.
 * <p>
 * The format of the model is a plain text file that contains on each line the definition of the
 * inflected forms of a given POS followed by the respective uninflected forms:
 * 
 * <pre>
 * NOUN_INFLECTED=','s,s,ses,xes,zes,ches,shes,men,ies
 * NOUN_UNINFLECTED= , , ,s,x,z,ch,sh,man,y
 * VERB_INFLECTED=s,ies,es,es,ed,ed,ing,ing
 * VERB_UNINFLECTED= ,y,e, ,e, , ,e
 * ADJECTIVE_INFLECTED=er,est,er,est
 * ADJECTIVE_UNINFLECTED= , ,e,e
 * ADVERB_INFLECTED=
 * ADVERB_UNINFLECTED=
 * </pre>
 * 
 * Notice that the number of inflected forms should match the number on uninflected forms.
 * 
 * @author Marco Marasca, marasca@disi.unitn.it
 */
class SuffixModel {

    /**
     * Inflected suffix
     */
    private static final String INFLECTED = "INFLECTED";

    /**
     * Uninflected suffix
     */
    private static final String UNINFLECTED = "UNINFLECTED";

    /**
     * Maps the part of speech to the array of inflected forms.
     */
    private Map<String, String[]> inflectedMap = new HashMap<String, String[]>(1);

    /**
     * Maps the part of speech to the array of uninflected forms.
     */
    private Map<String, String[]> uninflectedMap = new HashMap<String, String[]>(1);

    /**
     * Loads the model from the given input stream.
     * 
     * @param inputStream An input stream opened on a lemmatizer model file
     */
    public SuffixModel(InputStream inputStream) {
        InputStreamReader isReader = null;
        BufferedReader buffReader = null;
        try {
            isReader = new InputStreamReader(inputStream, "UTF-8");
            buffReader = new BufferedReader(isReader);
            String line;
            while ((line = buffReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                StringTokenizer lineTokenizer = new StringTokenizer(line, "=");
                if (!lineTokenizer.hasMoreTokens()) {
                    continue;
                }
                String type = lineTokenizer.nextToken().trim();
                String values = "";
                if (lineTokenizer.hasMoreTokens()) {
                    values = lineTokenizer.nextToken();
                }
                StringTokenizer typeTokenizer = new StringTokenizer(type, "_");
                String pos = typeTokenizer.nextToken();
                String formtype = typeTokenizer.nextToken();
                if (formtype.equalsIgnoreCase(INFLECTED)) {
                    inflectedMap.put(pos, parseValues(values));
                } else if (formtype.equalsIgnoreCase(UNINFLECTED)) {
                    uninflectedMap.put(pos, parseValues(values));
                }
            }
            modelCheck();
        } catch (IOException e) {
            throw new SmdivException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(buffReader);
            IOUtils.closeQuietly(isReader);
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Parses the values of a line of text read from the model
     * 
     * @param string The line to parse
     * @return An array of values from the given line
     */
    private String[] parseValues(String string) {
        List<String> values = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(string, ",");
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken().trim());
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Checks whether the model is consistent (e.g. errors in the size of inflected/uninflected
     * arrays).
     */
    private void modelCheck() {
        if (inflectedMap.size() != uninflectedMap.size()) {
            throw new SmdivException("Model format is incorrect.");
        }
        for (Entry<String, String[]> entry : inflectedMap.entrySet()) {
            String[] uninflected = uninflectedMap.get(entry.getKey());
            if (uninflected == null) {
                throw new SmdivException("Model format is incorrect: no uninflected forms for "
                                       + entry.getKey());
            }
            if (uninflected.length != entry.getValue().length) {
                throw new SmdivException("Model format is incorrect: the number of uninflected "
                                       + "forms is different from the number of inflected forms for "
                                       + entry.getKey());
            }
        }
    }

    /**
     * Returns the forms for the given part of speech.
     * 
     * @param pos The part of speech
     * @param isInflected True if the inflected forms should be returned, false if the uninflected
     *        forms should be returned instead
     * @return The inflected or uninflected forms for the given part of speech
     */
    public String[] getForms(String pos, boolean isInflected) {
        Map<String, String[]> model;
        if (isInflected) {
            model = inflectedMap;
        } else {
            model = uninflectedMap;
        }
        String[] values = model.get(pos);
        if (values == null) {
            return new String[0];
        } else {
            return values;
        }
    }

    /**
     * Returns the inflected forms for the given part of speech.
     * 
     * @param pos The part of speech
     * @return The array of inflected forms
     */
    public String[] getInflectedForms(String pos) {
        return getForms(pos, true);
    }

    /**
     * Returns the uninflected forms for the given part of speech.
     * 
     * @param pos The part of speech
     * @return The array of uninflected forms
     */
    public String[] getUninflectedForms(String pos) {
        return getForms(pos, false);
    }

}
