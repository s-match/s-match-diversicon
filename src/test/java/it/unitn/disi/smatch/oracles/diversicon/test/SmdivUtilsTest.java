package it.unitn.disi.smatch.oracles.diversicon.test;


import java.util.ArrayList;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;


/**
 * @since 0.1.0
 */
public class SmdivUtilsTest {

		
	private static final Logger log = LoggerFactory.getLogger(SmdivOracleTest.class);
	
	private DBConfig dbConfig;
		
	
	/**
	 * @since 0.1.0
	 */
	@Before
	public void beforeMethod(){
		 dbConfig = new DBConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver",
				UBYH2Dialect.class.getName(), "root", "pass", true);		 		
	}
	
	/**
	 * @since 0.1.0
	 */
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