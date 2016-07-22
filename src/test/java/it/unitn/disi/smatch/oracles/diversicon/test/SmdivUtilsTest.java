package it.unitn.disi.smatch.oracles.diversicon.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.smatch.oracles.diversicon.SmdivNotFoundException;



public class SmdivUtilsTest {

		
	private static final Logger log = LoggerFactory.getLogger(SmdivOracleTest.class);
	
	private DBConfig dbConfig;
		
	
	@Before
	public void beforeMethod(){
		 dbConfig = new DBConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver",
				UBYH2Dialect.class.getName(), "root", "pass", true);		 		
	}
	
	@After
	public void afterMethod(){
		dbConfig = null;
	}

    
    /**
     * Returns a new ArrayList.
     * 
     * (Note {@code Arrays.asList(T...)} returns an {@code Arrays.ArrayList} instead)
     * 
     * @since 0.1
     */
    public static <T> ArrayList<T> newArrayList(T... objs) {
        ArrayList<T> ret = new ArrayList();
        
        for (T obj : objs){
            ret.add(obj);            
        }
        return ret;
    }
    
    /**
     * Returns a new HashSet, filled with provided objects.
     * 
     * @since 0.1
     */
    public static <T> HashSet<T> newHashSet(T... objs) {
        HashSet<T> ret = new HashSet();
        
        for (T obj : objs){
            ret.add(obj);            
        }
        return ret;
    }    
    
	
}