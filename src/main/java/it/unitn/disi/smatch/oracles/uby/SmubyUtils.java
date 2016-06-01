package it.unitn.disi.smatch.oracles.uby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import it.unitn.disi.smatch.data.ling.ISense;

/**
 * Utility class for S-match Uby
 * 
 * @since 0.1
 */
public final class SmubyUtils {


    private static final Logger log = LoggerFactory.getLogger(SmubyUtils.class);




    private static Map<DBConfig, Configuration> cachedHibernateConfigurations = new HashMap();

    
    private SmubyUtils() {
    }

 
    public static ArrayList<String> getIds(Iterable<ISense> senses){
        ArrayList<String> ret = new ArrayList();
        
        for (ISense sense : senses){
            ret.add(sense.getId());
        }
        return ret;
    }
    
    

}
