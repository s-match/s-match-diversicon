package it.unitn.disi.smatch.oracles.uby.test;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
import de.tudarmstadt.ukp.lmf.model.semantics.Synset;
import de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation;
import it.unitn.disi.smatch.oracles.uby.SmuUtils;
import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;

/**
 * 
 * Builder helper for {@link LexicalResource} data structures. To use for testing purposes. 
 * 
 * The builder will automatically crete necessary ids for you like 'lexical
 * resource 1', 'synset 3', ... according to the order of insertion.
 * 
 * Start building with {@link #lmf()} and finish with {@link #build()i}. Each builder instance 
 * can build only one object. 
 * 
 * @since 0.1
 *
 */
public class LmfBuilder {

	private LexicalResource lexicalResource;
	private boolean built;

	private LmfBuilder() {
		this.lexicalResource = new LexicalResource();
		this.lexicalResource.setName("lexicalResource 1");
		this.built = false;
	}

	public LmfBuilder lexicon() {
		checkBuilt();
		Lexicon lexicon = new Lexicon();
		lexicon.setId("lexicon " + (lexicalResource.getLexicons().size() + 1));
		lexicalResource.addLexicon(lexicon);
		return this;
	}

	private Synset getSynset(int idNum) {
		for (Lexicon lex : lexicalResource.getLexicons()) {
			for (Synset synset : lex.getSynsets()) {
				if (synset.getId().equals("synset " + idNum)) {
					return synset;
				}
			}
		}
		throw new IllegalStateException("Couldn't find a synset with id: 'synset " + idNum + "'");
	}

	public LmfBuilder synset() {
		checkBuilt();
		Synset synset = new Synset();
		Lexicon lexicon = getCurLexicon();
		synset.setId("synset " + (lexicon.getSynsets().size() + 1));
		lexicon.getSynsets().add(synset);
		return this;
	}

	/**
	 * 
	 * @param targetIdNum
	 *            must be > 0.
	 */
	public LmfBuilder synsetRelation(String relName, int targetIdNum) {
		checkBuilt();
		checkNotEmpty(relName, "Invalid relation name!");
		SmuUtils.checkArgument(targetIdNum > 0, "Expected idNum greater than zero, found " + targetIdNum + " instead!");
		SynsetRelation sr = new SynsetRelation();
		sr.setTarget(getSynset(targetIdNum));
		Synset curSynset = getCurSynset();
		sr.setSource(curSynset);
		sr.setRelName(relName);
		curSynset.getSynsetRelations().add(sr);
		return this;

	}
	
    /**
     * 
     * @param targetIdNum
     *            must be > 0.
     */
    public LmfBuilder synsetRelation(String relName, int sourceIdNum, int targetIdNum) {
        checkBuilt();
        checkNotEmpty(relName, "Invalid relation name!");
        SmuUtils.checkArgument(targetIdNum > 0, "Expected idNum greater than zero, found " + targetIdNum + " instead!");
        SynsetRelation sr = new SynsetRelation();
        sr.setTarget(getSynset(targetIdNum));
        Synset source = getSynset(sourceIdNum);
        sr.setSource(getSynset(sourceIdNum));
        sr.setRelName(relName);
        source.getSynsetRelations().add(sr);
        return this;

    }
	

	private Synset getCurSynset() {
		checkBuilt();
		Lexicon lexicon = getCurLexicon();
		int size = lexicon.getSynsets().size();
		if (size == 0) {
			throw new IllegalStateException("There are no synsets in current lexicon " + lexicon.getId() + "!");
		}
		return lexicon.getSynsets().get(size - 1);
	}

	public Lexicon getCurLexicon() {
		checkBuilt();
		int size = lexicalResource.getLexicons().size();
		if (size == 0) {
			throw new IllegalStateException("There are no lexicons!");
		}
		return lexicalResource.getLexicons().get(size - 1);
	}

	public static LmfBuilder lmf() {
		return new LmfBuilder();
	};

	public LexicalResource build() {
		checkBuilt();
		built = true;
		return lexicalResource;
	}

	private void checkBuilt() {
		if (built) {
			throw new IllegalStateException("A LexicalResource was already built with this !");
		}
	}
}
