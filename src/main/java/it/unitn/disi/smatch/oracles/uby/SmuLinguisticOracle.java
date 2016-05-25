package it.unitn.disi.smatch.oracles.uby;

import java.io.File;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.api.Uby;
import de.tudarmstadt.ukp.lmf.api.UbyQuickAPI;
import de.tudarmstadt.ukp.lmf.hibernate.UBYH2Dialect;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.LMFDBUtils;
import de.tudarmstadt.ukp.lmf.transform.LMFXmlWriter;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;
import it.unitn.disi.common.DISIException;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import it.unitn.disi.smatch.oracles.SenseMatcherException;

/**
 * Oracle to access LMF XMLs via <a href="https://dkpro.github.io/dkpro-uby/" target="_blank">DKPRO UBY framework</a>.
 * todo talk about db
 * 
 * This oracle strives to give back some info regardless of possible inconsistencies or 
 * errors coming from the underlying LMF database
 * 
 * @since 0.1
 * @author David Leoni
 *
 */
public class SmuLinguisticOracle implements ILinguisticOracle, ISenseMatcher {

	private static final Logger log = LoggerFactory.getLogger(SmuLinguisticOracle.class);
	
	private SmuUby uby;

	/**
	 * Creates an empty H2 in-memory database
	 */
	public SmuLinguisticOracle() {
		this((String) null);
	}
	
	/**
	 * todo H2 in-memory database
	 * @param lmfXmlPath a path to an lmf xml to load. If {@code null} database 
	 * will be empty.
	 * todo throws what?
	 */
	public SmuLinguisticOracle(String lmfXmlPath) {
		this(new DBConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "org.h2.Driver",
			 UBYH2Dialect.class.getName(), "root", "pass", true),
			 lmfXmlPath);			
	}

	public SmuLinguisticOracle(DBConfig dbConfig, String lmfXmlPath) {
		
		Objects.requireNonNull(dbConfig);	

		uby = new SmuUby(dbConfig);
		
		if (lmfXmlPath != null){
			uby.loadLmfXml(lmfXmlPath, "UbyTestTodo"); // todo name meaning ?
		}
				
	}
	


	@Override
	public char getRelation(List<ISense> sourceSenses, List<ISense> targetSenses) throws SenseMatcherException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}

	@Override
	public boolean isSourceMoreGeneralThanTarget(ISense source, ISense target) throws SenseMatcherException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}

	@Override
	public boolean isSourceLessGeneralThanTarget(ISense source, ISense target) throws SenseMatcherException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}

	@Override
	public boolean isSourceSynonymTarget(ISense source, ISense target) throws SenseMatcherException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}

	@Override
	public boolean isSourceOppositeToTarget(ISense source, ISense target) throws SenseMatcherException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}

	// todo g here inflections should be dealt with by the oracle
	@Override
	public boolean isEqual(String str1, String str2) throws LinguisticOracleException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}

	/**
	 * NOTE: input word is supposed to be a lemma
	 */
	@Override
	public List<ISense> getSenses(String word) throws LinguisticOracleException {
		return null;
	}

	
	@Override
	public List<String> getBaseForms(String derivation) throws LinguisticOracleException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}

	// the create is misleading, it's actually retrieving a sense from oracle
	@Override
	public ISense createSense(String id) throws LinguisticOracleException {
		
		Synset ubysyn = null;
		
		try {
			ubysyn = uby.getSynsetById(id);
		} catch (IllegalArgumentException ex){
			throw new LinguisticOracleException("Couldn't find provided id!", ex);
		}
		try {		
			return new SmuSense(ubysyn, this);
		} catch (Exception ex){
			throw new LinguisticOracleException("Error while creating a UbySense!", ex);
		}
	
	}

	@Override
	public List<List<String>> getMultiwords(String beginning) throws LinguisticOracleException {
		throw new UnsupportedOperationException("TODO - developer forgot to implement the method!");
	}


	public SmuUby getUby(){
		return uby;
	}

}
