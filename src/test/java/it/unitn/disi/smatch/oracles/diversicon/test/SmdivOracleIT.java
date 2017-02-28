package it.unitn.disi.smatch.oracles.diversicon.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import eu.kidf.diversicon.core.DivConfig;
import eu.kidf.diversicon.core.test.DivTester;
import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.classifiers.CNFContextClassifier;
import it.unitn.disi.smatch.data.mappings.HashMapping;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.IMappingFactory;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.deciders.CachingSolver;
import it.unitn.disi.smatch.deciders.SAT4J;
import it.unitn.disi.smatch.loaders.context.SimpleXMLContextLoader;
import it.unitn.disi.smatch.loaders.context.TabContextLoader;
import it.unitn.disi.smatch.loaders.mapping.PlainMappingLoader;
import it.unitn.disi.smatch.matchers.element.ElementMatcher;
import it.unitn.disi.smatch.matchers.element.string.EditDistanceOptimized;
import it.unitn.disi.smatch.matchers.element.string.NGram;
import it.unitn.disi.smatch.matchers.element.string.Prefix;
import it.unitn.disi.smatch.matchers.element.string.Suffix;
import it.unitn.disi.smatch.matchers.element.string.Synonym;
import it.unitn.disi.smatch.matchers.structure.node.DefaultNodeMatcher;
import it.unitn.disi.smatch.matchers.structure.tree.def.DefaultTreeMatcher;
import it.unitn.disi.smatch.oracles.diversicon.SmdivOracle;
import it.unitn.disi.smatch.preprocessors.DefaultContextPreprocessor;
import it.unitn.disi.smatch.renderers.context.SimpleXMLContextRenderer;
import it.unitn.disi.smatch.renderers.mapping.PlainMappingRenderer;

/**
 * @since 0.1.0
 */
public class SmdivOracleIT {

    private static final Logger log = LoggerFactory.getLogger(SmdivOracleIT.class);

    private DivConfig divConfig;

    /**
     * @since 0.1.0
     */
    @Before
    public void beforeMethod() {
        divConfig = DivTester.createNewDivConfig();
    }

    /**
     * @since 0.1.0
     */
    @After
    public void afterMethod() {
        divConfig = null;
    }  
    
    /**
     * @throws IOException 
     * @since 0.1.0
     */
    @Test
    public void testSmdiv() throws SMatchException, IOException {
        
          log.info("Starting example...");
          log.info("Creating MatchManager...");
          
          
          // IMatchManager mm = MatchManager.getInstanceFromResource(
           // "/it/unitn/disi/smatch/oracles/uby/test/conf/s-match.xml");
          
          IMappingFactory mappingFactory = new HashMapping<>();
          
          File cacheDir = new File(System.getProperty("user.home") + File.separator
                  + SmdivOracle.DEFAULT_CACHE_PATH);
          
          log.info("Cleaning cache ... " + cacheDir);
          
          if (cacheDir.exists()){
              // security check
              if (cacheDir.getAbsolutePath().endsWith("diversicon/cache")){
                  FileUtils.deleteDirectory(cacheDir);    
                  log.info("Done cleaning.");
              } else{
                 Assert.fail("Failed security check to clean cache ! Tried to delete wrong cache: " + cacheDir);
              }                               
          }              
                    
          SmdivOracle oracle = new SmdivOracle();
          
          ElementMatcher elementMatcher = new ElementMatcher(
                  mappingFactory, 
                  oracle, 
                  true,
                  Arrays.asList(new Synonym(), 
                          new Prefix(), 
                          new Suffix(), 
                          new NGram(), 
                          new EditDistanceOptimized()),
                  null);
                               
          SimpleXMLContextLoader contextLoader = new SimpleXMLContextLoader(oracle);
          
          IMatchManager mm = new MatchManager(
                  contextLoader,                 
                  new SimpleXMLContextRenderer(), 
                  new PlainMappingLoader(mappingFactory), 
                  new PlainMappingRenderer(), 
                  null, 
                  mappingFactory, 
                  new DefaultContextPreprocessor(
                          oracle, 
                          oracle),
                  new CNFContextClassifier(), 
                  elementMatcher, 
                  new DefaultTreeMatcher(
                          new DefaultNodeMatcher(new CachingSolver(new SAT4J())),
                          mappingFactory)); 
          
          
          
          String example = "Courses";
          log.info("Creating source context...");
          IContext s = mm.createContext();
          s.createRoot(example);
          
          log.info("Creating target context...");
          IContext t = mm.createContext();
          INode root = t.createRoot("Course");
          INode node = root.createChild("College of Arts and Sciences");
          node.createChild("English");
          
          node = root.createChild("College Engineering");
          node.createChild("Civil and Environmental Engineering");
          
          log.info("Preprocessing source context...");
          mm.offline(s);
          
          log.info("Preprocessing target context...");
          mm.offline(t);
          
          log.info("Matching...");
          IContextMapping<INode> result = mm.online(s, t);
          
          log.info("Processing results...");
          log.info("Printing matches:");
          for (IMappingElement<INode> e : result) {
              log.info(e.getSource().nodeData().getName() + "\t" +
          e.getRelation() + "\t" + e.getTarget().nodeData().getName());
          }
          
          log.info("Done");
         

    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testCw() throws SMatchException {
        
        log.info("Starting example...");
        log.info("Creating MatchManager...");
        
        
        // IMatchManager mm = MatchManager.getInstanceFromResource(
         // "/it/unitn/disi/smatch/oracles/uby/test/conf/s-match.xml");
        
        IMappingFactory mappingFactory = new HashMapping();
        
        SmdivOracle oracle = new SmdivOracle();
        
        ElementMatcher elementMatcher = new ElementMatcher(
                mappingFactory, 
                oracle, 
                true,
                Arrays.asList(new Synonym(), 
                        new Prefix(), 
                        new Suffix(), 
                        new NGram(), 
                        new EditDistanceOptimized()),
                null);
                             
        TabContextLoader contextLoader = new TabContextLoader();
        
        IMatchManager mm = new MatchManager(
                contextLoader,                 
                new SimpleXMLContextRenderer(), 
                new PlainMappingLoader(mappingFactory), 
                new PlainMappingRenderer(), 
                null, 
                mappingFactory, 
                new DefaultContextPreprocessor(
                        oracle, 
                        oracle),
                new CNFContextClassifier(), 
                elementMatcher, 
                new DefaultTreeMatcher(
                        new DefaultNodeMatcher(new CachingSolver(new SAT4J())),
                        mappingFactory));         
        
        
        log.info("Creating source context...");                      
        IContext s = (IContext) mm.loadContext("src/test/resources/cw/c.txt");
        
        log.info("Creating target context...");
        IContext t = (IContext) mm.loadContext("src/test/resources/cw/w.txt");
        
        log.info("Preprocessing source context...");
        mm.offline(s);
        
        log.info("Preprocessing target context...");
        mm.offline(t);
        
        log.info("Matching...");
        IContextMapping<INode> result = mm.online(s, t);
        
        log.info("Processing results...");
        log.info("Printing matches:");
        for (IMappingElement<INode> e : result) {
            log.info(e.getSource().nodeData().getName() + "\t" +
        e.getRelation() + "\t" + e.getTarget().nodeData().getName());
        }
        
        log.info("Done");
               
    }
  

    
    
}
