package it.unitn.disi.smatch.oracles.uby;

import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;
import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotNull;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.hibernate.cfg.Configuration;
import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.lmf.hibernate.HibernateConnect;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
import de.tudarmstadt.ukp.lmf.model.miscellaneous.ConstraintSet;
import de.tudarmstadt.ukp.lmf.model.multilingual.SenseAxis;
import de.tudarmstadt.ukp.lmf.model.semantics.SemanticPredicate;
import de.tudarmstadt.ukp.lmf.model.semantics.SynSemCorrespondence;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.syntax.SubcategorizationFrame;
import de.tudarmstadt.ukp.lmf.model.syntax.SubcategorizationFrameSet;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;

import de.tudarmstadt.ukp.lmf.transform.LMFDBTransformer;

/**
 * 
 * Simple transformer to directly put into the db a LexicalResource complete with all the
 * lexicons, synsets, etc.
 * 
 * @since 0.1
 * @author David Leoni
 */
class JavaToDbTransformer extends LMFDBTransformer {
	
	private static final Logger log = LoggerFactory.getLogger(JavaToDbTransformer.class);

	private LexicalResource lexicalResource;
	private String lexicalResourceId;
	private Iterator<Lexicon> lexiconIter;
	private Iterator<LexicalEntry> lexicalEntryIter;
	private Iterator<SubcategorizationFrame> subcategorizationFrameIter;
	private Iterator<SubcategorizationFrameSet> subcategorizationFrameSetIter;
	private Iterator<SemanticPredicate> semanticPredicateIter;
	private Iterator<SynSemCorrespondence> synSemCorrespondenceIter;
	private Iterator<Synset> synsetIter;
	private Iterator<SenseAxis> senseAxisIter;
	private Iterator<ConstraintSet> constraintSetIter;

	/**
	 *
	 * 
	 * @param resource
	 *            a LexicalResource complete with all the lexicons, synsets, etc
	 * @param lexicalResourceId
	 *            todo don't know well the meaning
	 * @throws FileNotFoundException 
	 */
	public JavaToDbTransformer(DBConfig dbConfig, LexicalResource lexicalResource, String lexicalResourceId) throws FileNotFoundException {
		super(dbConfig);
		
		Configuration cfg = SmuUtils.getHibernateConfig(dbConfig);
		sessionFactory = cfg.buildSessionFactory(
				new ServiceRegistryBuilder().applySettings(
				cfg.getProperties()).buildServiceRegistry());		
		
		checkNotNull(lexicalResource);
		checkNotEmpty(lexicalResourceId, "Invalid lexicalResourceId!");

		// copying to avoid double additions by LMFDBTransformer		
		this.lexicalResource = SmuUtils.deepCopy(lexicalResource);
		
		this.lexicalResourceId = lexicalResourceId;

		this.lexiconIter = this.lexicalResource.getLexicons().iterator();
		this.lexicalResource.setLexicons(new ArrayList());
		
		this.senseAxisIter = lexicalResource.getSenseAxes().iterator();
		this.lexicalResource.setSenseAxes(new ArrayList());

	}

	@Override
	protected LexicalResource createLexicalResource() {
		return lexicalResource;
	}

	@Override	
	protected Lexicon createNextLexicon() {
		// resetting lexicon array properties to avoid double additions by LMFDBTransformer
		if (lexiconIter.hasNext()) {
			Lexicon lexicon = lexiconIter.next();
			
			log.info("Creating Lexicon " + lexicon.getId());
			
			subcategorizationFrameIter = lexicon.getSubcategorizationFrames().iterator();
			lexicon.setSubcategorizationFrames(new ArrayList());
			subcategorizationFrameSetIter = lexicon.getSubcategorizationFrameSets().iterator();
			lexicon.setSubcategorizationFrameSets(new ArrayList());
			lexicalEntryIter = lexicon.getLexicalEntries().iterator();
			lexicon.setLexicalEntries(new ArrayList());
			semanticPredicateIter = lexicon.getSemanticPredicates().iterator();
			lexicon.setSemanticPredicates(new ArrayList());
			synSemCorrespondenceIter = lexicon.getSynSemCorrespondences().iterator();
			lexicon.setSynSemCorrespondences(new ArrayList());
			constraintSetIter = lexicon.getConstraintSets().iterator();
			lexicon.setConstraintSets(new ArrayList());
			return lexicon;
		} else {
			return null;
		}

	}

	@Override
	protected LexicalEntry getNextLexicalEntry() {
		if (lexicalEntryIter.hasNext()) {
			LexicalEntry lexicalEntry = lexicalEntryIter.next();			
			synsetIter = lexicalEntry.getSynsets().iterator();			
			return lexicalEntry;
		} else {
			return null;
		}
	}

	/**
	 * Return the next element of the iterator or {@code null} if there is none
	 */
	@Nullable
	private static <T> T next(Iterator<T> iter) {
		if (iter != null && iter.hasNext()) {
			return iter.next();
		} else {
			return null;
		}
	}

	@Override
	protected SubcategorizationFrame getNextSubcategorizationFrame() {
		return next(subcategorizationFrameIter);
	}

	@Override
	protected SubcategorizationFrameSet getNextSubcategorizationFrameSet() {
		return next(subcategorizationFrameSetIter);
	}

	@Override
	protected SemanticPredicate getNextSemanticPredicate() {
		return next(semanticPredicateIter);
	}

	@Override
	protected Synset getNextSynset() {
		return next(synsetIter);
	}

	@Override
	protected SynSemCorrespondence getNextSynSemCorrespondence() {
		return next(synSemCorrespondenceIter);
	}

	@Override
	protected ConstraintSet getNextConstraintSet() {
		return next(constraintSetIter);
	}

	@Override
	protected SenseAxis getNextSenseAxis() {
		return next(senseAxisIter);
	}

	@Override
	protected void finish() {

	}

	@Override
	protected String getResourceAlias() {
		return lexicalResourceId;
	}

}
