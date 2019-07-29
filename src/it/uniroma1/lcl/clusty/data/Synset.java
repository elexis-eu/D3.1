package it.uniroma1.lcl.clusty.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a WordNet synset
 *
 * @author
 *
 */
public class Synset {

	/**
	 * synset's ID
	 */
	private String ID;
	/**
	 * set of synset's synonyms
	 */
	private Set<String> synonyms = new HashSet<>();
	/**
	 * synset's gloss
	 */
	private String gloss;

	public Synset(String ID, Set<String> sinonimi, String glossa) {
		this.ID = ID;
		this.synonyms = sinonimi;
		this.gloss = glossa;
	}

	/**
	 * Returns the synset's ID
	 * @return this synset's ID
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Returns the synset's part-of-speech 
	 * @return this synset's POS
	 */
	public POS getPOS() {
		return POS.valueOf(ID.substring(ID.length() - 1));
	}

	/**
	 * Returns the synset's gloss
	 * @return this synset's gloss
	 */
	public String getGloss() {
		return gloss;
	}
	
	/**
	 * Returns the set of synset's synonyms
	 * @return this synset's synonyms
	 */
	public Set<String> getSynonyms() {
		return synonyms;
	}

	@Override
	public String toString() {
		return ID + " " + synonyms + " " + gloss;
	}

}
