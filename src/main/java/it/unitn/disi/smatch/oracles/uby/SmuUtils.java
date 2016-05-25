package it.unitn.disi.smatch.oracles.uby;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.rits.cloning.Cloner;

import javax.annotation.Nullable;

import de.tudarmstadt.ukp.lmf.hibernate.HibernateConnect;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.model.enums.ERelTypeSemantics;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;

import static de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics.*;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;

/**
 * Utility class for S-match Uby
 * 
 * @since 0.1
 */
public final class SmuUtils {


    private static final List<String> SMATCH_CANONICAL_RELATIONS = Collections.unmodifiableList(
            Arrays.asList(ERelNameSemantics.HYPERNYM,
                    ERelNameSemantics.HYPERNYMINSTANCE,
                    ERelNameSemantics.HOLONYM,
                    ERelNameSemantics.HOLONYMCOMPONENT,
                    ERelNameSemantics.HOLONYMMEMBER,
                    ERelNameSemantics.HOLONYMPART,
                    ERelNameSemantics.HOLONYMPORTION,
                    ERelNameSemantics.HOLONYMSUBSTANCE));
    
    private static final Map<String, ERelTypeSemantics> SMATCH_CANONICAL_RELATION_TYPES = Collections.unmodifiableMap(
            SmuUtils.newMap(ERelNameSemantics.HYPERNYM, ERelTypeSemantics.taxonomic,
                    ERelNameSemantics.HYPERNYMINSTANCE, ERelTypeSemantics.taxonomic,
                    ERelNameSemantics.HOLONYM, ERelTypeSemantics.partWhole,
                    ERelNameSemantics.HOLONYMCOMPONENT, ERelTypeSemantics.partWhole,
                    ERelNameSemantics.HOLONYMMEMBER, ERelTypeSemantics.partWhole,
                    ERelNameSemantics.HOLONYMPART, ERelTypeSemantics.partWhole,
                    ERelNameSemantics.HOLONYMPORTION, ERelTypeSemantics.partWhole,
                    ERelNameSemantics.HOLONYMSUBSTANCE, ERelTypeSemantics.partWhole) );                                                                       
    

    private static Map<String, String> inverseRelations = new HashMap();

    static {
        putInverseRelations(ANTONYM, ANTONYM);
        putInverseRelations(HYPERNYM, HYPONYM);
        putInverseRelations(HYPERNYMINSTANCE, HYPONYMINSTANCE);
        putInverseRelations(HOLONYM, MERONYM);
        putInverseRelations(HOLONYMCOMPONENT, MERONYMCOMPONENT);
        putInverseRelations(HOLONYMMEMBER, MERONYMMEMBER);
        putInverseRelations(HOLONYMPART, MERONYMPART);
        putInverseRelations(HOLONYMPORTION, MERONYMPORTION);
        putInverseRelations(HOLONYMSUBSTANCE, MERONYMSUBSTANCE);

    }

    private static Map<DBConfig, Configuration> cachedHibernateConfigurations = new HashMap();

    private SmuUtils() {
    }

    /**
     * Sets {@code a} as {@code b}'s symmetric type, and vice versa.
     *
     * @param a
     *            pointer type
     * @param b
     *            pointer type
     */
    private static void putInverseRelations(String a, String b) {
        checkNotEmpty(a, "Invalid first relation!");
        checkNotEmpty(b, "Invalid second relation!");

        inverseRelations.put(a, b);
        inverseRelations.put(b, a);
    }

    /**
     * @throws SmuNotFoundException
     *             if {code relation} does not have an inverse
     */
    public static String getInverse(String relation) {
        checkNotEmpty(relation, "Invalid relation!");

        String ret = inverseRelations.get(relation);
        if (ret == null) {
            throw new SmuNotFoundException("Couldn't find the relation " + relation);
        }
        return ret;
    }

    /**
     * Returns true if provided relation has a known inverse, otherwise returns
     * false.
     */
    public static boolean hasInverse(String relation) {
        checkNotEmpty(relation, "Invalid relation!");

        String ret = inverseRelations.get(relation);
        if (ret == null) {
            return false;
        }
        return true;
    }

    /**
     * Note: if false is returned it means we <i> don't know </i> the relations
     * are actually inverses.
     */
    public static boolean isInverse(String a, String b) {
        checkNotEmpty(a, "Invalid first relation!");
        checkNotEmpty(b, "Invalid second relation!");

        String inverse = inverseRelations.get(a);
        if (inverse == null) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * Mappings from Uby classes to out own custom ones.
     */
    private static LinkedHashMap<String, String> customClassMappings;

    static {
        customClassMappings = new LinkedHashMap();
        customClassMappings.put(de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation.class.getCanonicalName(),
                SmuSynsetRelation.class.getCanonicalName());
    }

    private static final Logger log = LoggerFactory.getLogger(SmuUtils.class);

    /**
     * Create all LMF Tables in the database based on the hibernate mapping.
     * Eventual existing tables are dropped.
     * 
     * (adapted from LMFDBUtils.createTables(dbConfig) )
     * 
     * @param dbConfig
     * @since 0.1
     */
    public static void createTables(DBConfig dbConfig) {

        log.info("CREATE S-MATCH UBY TABLES");

        Configuration hcfg = getHibernateConfig(dbConfig);

        hcfg.setProperty("hibernate.hbm2ddl.auto", "none");
        SchemaExport se = new SchemaExport(hcfg);
        se.create(true, true);

    }

    /**
     * Returns true a uby database already exists.
     */
    public static boolean checkExists(DBConfig dbConfig) {
        throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
    }

    /**
     * Loads a given xml hibernate configuration xml into {@code hcfg}
     *
     * @since 0.1
     */
    public static void loadHibernateXml(Configuration hcfg, Resource xml) {

        log.info("Loading config " + xml.getDescription() + " ...");

        try {

            java.util.Scanner sc = new java.util.Scanner(xml.getInputStream()).useDelimiter("\\A");
            String s = sc.hasNext() ? sc.next() : "";
            sc.close();

            for (Map.Entry<String, String> e : customClassMappings.entrySet()) {
                s = s.replace(e.getKey(), e.getValue());
            }
            hcfg.addXML(s);

        } catch (Exception e) {
            throw new RuntimeException("Error while reading file at path: " + xml.getDescription(), e);
        }

    }

    /**
     * 
     * NOTE: returned configuration is set to create db if it doesn't already
     * exist.
     * 
     * @since 0.1
     */
    public static Configuration getHibernateConfig(DBConfig dbConfig) {

        if (cachedHibernateConfigurations.get(dbConfig) != null) {
            log.debug("Returning cached configuration.");
            return cachedHibernateConfigurations.get(dbConfig);
        }

        log.info("Going to load configuration...");

        Configuration hcfg = new Configuration()
                                                .addProperties(HibernateConnect.getProperties(dbConfig.getJdbc_url(),
                                                        dbConfig.getJdbc_driver_class(),
                                                        dbConfig.getDb_vendor(), dbConfig.getUser(),
                                                        dbConfig.getPassword(), dbConfig.isShowSQL()));

        hcfg.setProperty("hibernate.hbm2ddl.auto", "update");

        log.info("Going to load default UBY hibernate mappings...");

        ClassLoader cl = HibernateConnect.class.getClassLoader();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] mappings = null;
        try {
            mappings = resolver.getResources("hibernatemap/access/**/*.hbm.xml");
            for (Resource mapping : mappings) {
                boolean isCustomized = false;
                for (String c : customClassMappings.keySet()) {
                    String[] cs = c.split("\\.");
                    String cn = cs[cs.length - 1];
                    if (mapping.getFilename()
                               .replace(".hbm.xml", "")
                               .contains(cn)) {
                        isCustomized = true;
                    }
                }
                if (isCustomized) {
                    log.info("Skipping class customized by Smatch Uby: " + mapping.getDescription());
                } else {
                    loadHibernateXml(hcfg, mapping);

                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error while loading hibernate mappings!", e);
        }
        log.info("Done loading default UBY hibernate mappings...");

        log.info("Loading custom S-Match Uby hibernate mappings... ");

        try {

            Resource[] resources = new PathMatchingResourcePatternResolver(SmuUtils.class.getClassLoader())
                                                                                                           .getResources(
                                                                                                                   "hybernatemap/access/**/*.hbm.xml");

            SmuUtils.checkArgument(resources.length == 1,
                    "Resource should be equals to 1, found instead " + resources.length);

            for (Resource r : resources) {
                // loadHibernateXml(hcfg, r);
                hcfg.addURL(r.getURL());
            }

        } catch (Exception ex) {
            throw new RuntimeException("Error while loading hibernate mappings!", ex);
        }

        log.info("Done loading custom mappings. ");

        cachedHibernateConfigurations.put(dbConfig, hcfg);
        return hcfg;
    }

    /**
     *
     * Checks if provided string is non null and non empty.
     *
     * @param prependedErrorMessage
     *            the exception message to use if the check fails; will be
     *            converted to a string using String.valueOf(Object) and
     *            prepended to more specific error messages.
     *
     * @throws IllegalArgumentException
     *             if provided string fails validation
     *
     * @return the non-empty string that was validated
     * @since 0.1
     */
    public static String checkNotEmpty(String string, @Nullable Object prependedErrorMessage) {
        checkArgument(string != null, "%s -- Reason: Found null string.", prependedErrorMessage);
        if (string.length() == 0) {
            throw new IllegalArgumentException(
                    String.valueOf(prependedErrorMessage) + " -- Reason: Found empty string.");
        }
        return string;
    }

    /**
     *
     * Checks if provided string is non null and non empty.
     *
     * @param errorMessageTemplate
     *            a template for the exception message should the check fail.
     *            The message is formed by replacing each {@code %s} placeholder
     *            in the template with an argument. These are matched by
     *            position - the first {@code %s} gets {@code
     *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to
     *            the formatted message in square braces. Unmatched placeholders
     *            will be left as-is.
     * @param errorMessageArgs
     *            the arguments to be substituted into the message template.
     *            Arguments are converted to strings using
     *            {@link String#valueOf(Object)}.
     * @throws IllegalArgumentException
     *             if {@code expression} is false
     * @throws NullPointerException
     *             if the check fails and either {@code errorMessageTemplate} or
     *             {@code errorMessageArgs} is null (don't let this happen)
     *
     *
     * @throws IllegalArgumentException
     *             if provided string fails validation
     *
     * @return the non-empty string that was validated
     * @since 0.1
     */
    public static String checkNotEmpty(String string, @Nullable String errorMessageTemplate,
            @Nullable Object... errorMessageArgs) {
        String formattedMessage = SmuUtils.format(errorMessageTemplate, errorMessageArgs);
        checkArgument(string != null, "%s -- Reason: Found null string.", formattedMessage);
        if (string.length() == 0) {
            throw new IllegalArgumentException(formattedMessage + " -- Reason: Found empty string.");
        }
        return string;
    }

    /**
     *
     * Substitutes each {@code %s} in {@code template} with an argument. These
     * are matched by position: the first {@code %s} gets {@code args[0]}, etc.
     * If there are more arguments than placeholders, the unmatched arguments
     * will be appended to the end of the formatted message in square braces.
     * <br/>
     * <br/>
     * (Copied from Guava's
     * {@link com.google.common.base.Preconditions#format(java.lang.String, java.lang.Object...) }
     * )
     *
     * @param template
     *            a non-null string containing 0 or more {@code %s}
     *            placeholders.
     * @param args
     *            the arguments to be substituted into the message template.
     *            Arguments are converted to strings using
     *            {@link String#valueOf(Object)}. Arguments can be null.
     *
     * @since 0.1
     */
    public static String format(String template, @Nullable Object... args) {
        if (template == null) {
            log.warn("Found null template while formatting, converting it to \"null\"");
        }
        template = String.valueOf(template); // null -> "null"

        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to
     * the calling method.
     *
     * (Copied from Guava Preconditions)
     * 
     * @param expression
     *            a boolean expression
     * @throws IllegalArgumentException
     *             if {@code expression} is false
     * @since 0.1
     */
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to
     * the calling method.
     *
     * (Copied from Guava Preconditions)
     * 
     * @param expression
     *            a boolean expression
     * @param errorMessage
     *            the exception message to use if the check fails; will be
     *            converted to a string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException
     *             if {@code expression} is false
     * @since 0.1
     */
    public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to
     * the calling method.
     *
     * (Copied from Guava Preconditions)
     *
     * @param expression
     *            a boolean expression
     * @param errorMessageTemplate
     *            a template for the exception message should the check fail.
     *            The message is formed by replacing each {@code %s} placeholder
     *            in the template with an argument. These are matched by
     *            position - the first {@code %s} gets {@code
     *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to
     *            the formatted message in square braces. Unmatched placeholders
     *            will be left as-is.
     * @param errorMessageArgs
     *            the arguments to be substituted into the message template.
     *            Arguments are converted to strings using
     *            {@link String#valueOf(Object)}.
     * @throws IllegalArgumentException
     *             if {@code expression} is false
     * @throws NullPointerException
     *             if the check fails and either {@code errorMessageTemplate} or
     *             {@code errorMessageArgs} is null (don't let this happen)
     * @since 0.1
     */
    public static void checkArgument(boolean expression, @Nullable String errorMessageTemplate,
            @Nullable Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling
     * method is not null. (Copied from Guava)
     * 
     * @param reference
     *            an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException
     *             if {@code reference} is null
     * @since 0.1
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling
     * method is not null.
     * 
     * (Copied from Guava)
     * 
     * @param reference
     *            an object reference
     * @param errorMessage
     *            the exception message to use if the check fails; will be
     *            converted to a string using {@link String#valueOf(Object)}
     * @return the non-null reference that was validated
     * @throws NullPointerException
     *             if {@code reference} is null
     * @since 0.1
     */
    public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    /**
     * 
     * Saves a LexicalResource complete with all the lexicons, synsets, etc into
     * a database. This method is suitable only for small lexical resources and
     * generally for testing purposes. If you have a big resource, stream the
     * loading by providing your implementation of <a href=
     * "https://github.com/dkpro/dkpro-uby/blob/master/de.tudarmstadt.ukp.uby.persistence.transform-asl/src/main/java/de/tudarmstadt/ukp/lmf/transform/LMFDBTransformer.java"
     * target="_blank"> LMFDBTransformer</a> and call {@code transform()} on it
     * instead.
     * 
     * @param lexicalResourceId
     *            todo don't know well the meaning
     * 
     * @throws SmuException
     * @since 0.1
     */
    public static void saveLexicalResourceToDb(DBConfig dbConfig, LexicalResource lexicalResource,
            String lexicalResourceId) {
        log.info("Going to save lexical resource to database...");
        try {
            new JavaToDbTransformer(dbConfig, lexicalResource, lexicalResourceId).transform();
        } catch (Exception ex) {
            throw new SmuException("Error when importing lexical resource " + lexicalResourceId + " !", ex);
        }
        log.info("Done saving.");
    }

    /**
     * Returns a deep copy of any object, including non-serializable ones.
     * 
     * @since 0.1
     */
    static <T> T deepCopy(T orig) {
        Cloner cloner = new Cloner();
        return cloner.deepClone(orig);
    }

    /**
     * Returns true if provided relation is canonical
     */
    public static boolean isCanonical(String relName) {
        checkNotEmpty(relName, "Invalid relation name!");
        return SMATCH_CANONICAL_RELATIONS.contains(relName);
    }

    /**
     * Returns true if provided relation is canonical
     * @throws SmuNotFoundException
     */
    public static ERelTypeSemantics getCanonicalRelationType(String relName) {
        
        ERelTypeSemantics ret = SMATCH_CANONICAL_RELATION_TYPES.get(relName);
        
        if (ret == null){
            throw new SmuNotFoundException("There is no reltaion type associated to relation " + relName);
        }
        return ret;
    }
    
    
    public static String toString(@Nullable SynsetRelation sr) {

        if (sr == null) {
            return "null";
        } else {
            if (sr instanceof SmuSynsetRelation) {
                return sr.toString();
            } else {
                String sourceId = sr.getSource() == null ? "null" : sr.getSource()
                                                                      .getId();
                String targetId = sr.getTarget() == null ? "null" : sr.getTarget()
                                                                      .getId();
                return "SynsetRelation [source=" + sourceId + ", target="
                        + targetId + ", relType=" + sr.getRelType() + ", relName="
                        + sr.getRelName() + ", frequencies=" + sr.getFrequencies() + "]";
            }

        }

    }
    
    /**
     * Creates a map from key value pairs
     * 
     * @since 0.1
     */
    public static <V,W> HashMap<V, W> newMap(V v, W w, Object... data){
        HashMap<V, W> result = new HashMap();

        if(data.length % 2 != 0) 
            throw new IllegalArgumentException("Odd number of arguments");      

        V key = null;
        Integer step = -1;

        if (v == null){
            throw new IllegalArgumentException("Null key value");
        }
        
        result.put(v, w);
        
        for(Object d : data){
            step++;
            switch(step % 2){
            case 0: 
                if(d == null) {
                    throw new IllegalArgumentException("Null key value");
                }
                
                if (!v.getClass().isInstance(d)){
                    throw new IllegalArgumentException("Expected key " + d + " to be instance of class " + v.getClass());
                }
                key = (V) d;
                continue;
            case 1:     
                if (w != null && !w.getClass().isInstance(d)){
                    throw new IllegalArgumentException("Expected value " + d + " to be instance of class " + w.getClass());
                }
                
                W val = (W) d;
                result.put(key, val);
                break;
            }
        }

        return result;
    }

    /**
     * Returns a list of relations used by Smatch, in
     * {@link de.tudarmstadt.ukp.uby.lmf.model.ERelNameSemantics Uby format}
     * The list will contain only the canonical relations and not their inverse.
     */
    public static List<String> getCanonicalRelations() {
        return SMATCH_CANONICAL_RELATIONS;
    }

}
