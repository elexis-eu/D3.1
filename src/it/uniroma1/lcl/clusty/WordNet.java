package it.uniroma1.lcl.clusty;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.uniroma1.lcl.clusty.data.POS;
import it.uniroma1.lcl.clusty.data.Synset;

/**
 * Represents a WordNet instance
 * @author
 *
 */
public class WordNet {
	
	private static WordNet instance;
	private static final String FOLDER_PATH = "wordnet-releases/WordNet-";
	private static final String WN_VERSION = "3.0";

	protected Map<String, Synset> id2synset = new HashMap<>();

	private WordNet() {
		load();
	}

	public static WordNet getInstance() {
		if (instance == null) {
			synchronized (WordNet.class) {
				if (instance == null)
					instance = new WordNet();
			}
		}
		return instance;
	}

	private void load() {
		
//		System.out.println("Loading WordNet...");

		try {
			Files.walk(Paths.get(FOLDER_PATH + WN_VERSION + "/dict/")).filter(Files::isRegularFile)
					.filter(x -> x.getFileName().toString().startsWith("da")).forEach(file -> {
						try {
							Files.lines(file).skip(29).forEach(line -> {

								String[] fields = line.split(" ");
								String ID = fields[0] + fields[2];
								if (ID.endsWith("s"))
									ID = ID.substring(0, ID.length() - 1) + "a";
								String gloss = line.substring(line.indexOf('|'));

								int synonyms_num = Integer.parseInt(fields[3], 16);
								Set<String> synonyms = new HashSet<>();

								if (synonyms_num != 0) {
									for (int i = 0; i < synonyms_num; i++)
										synonyms.add(fields[i * 2 + 4]);
								}

								Synset s = new Synset(ID, synonyms, gloss);

								id2synset.put(ID, s);
							});

						} catch (Exception e) {
							e.printStackTrace();
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns all lemmas given a specific POS
	 * @param pos Part-Of-Speech
	 * @return set of lemmas
	 */
	public Set<String> getLemmas(POS pos) {

		return id2synset.values().stream()
				.filter(s -> s.getID().endsWith(pos.name().toLowerCase()) || pos == POS.ALL)
				.flatMap(s -> s.getSynonyms().stream()).collect(Collectors.toSet());
	}


	/**
	 * Returns all synsets given a specific lemma and a POS
	 * @param lemma
	 * @param pos
	 * @return list of synsets
	 */
	public List<Synset> getSynsets(String lemma, POS pos)
    {

         return id2synset.values().stream()
        		 		.filter(s -> s.getID().endsWith(pos.name().toLowerCase()) || pos == POS.ALL)
                        .filter(x->x.getSynonyms().contains(lemma))
                        .collect(Collectors.toList());
    }

	/**
	 * Returns a synset given as input a specific ID
	 * @param ID synset's ID
	 * @return synset
	 */
	public Synset getSynsetFromID(String ID)
	{
		return id2synset.get(ID);
	}

	public List<Synset> getRelatedSynset(String ID, String r) {

		return getRelatedSynset(ID, Collections.singleton(r));
	}

	public List<Synset> getRelatedSynset(String ID, Set<String> r) {
		List<String> relatedSynsets = new ArrayList<>();

		try {
			Files.walk(Paths.get(FOLDER_PATH + WN_VERSION + "/dict")).filter(Files::isRegularFile)
					.filter(file -> file.getFileName().toString().startsWith("da")).forEach(file -> {
						try {
							Files.lines(file).skip(29).forEach(line -> {

								String[] fields = line.split(" ");


								if ((fields[0] + fields[2]).equals(ID)) {
									for (int i = 0; i < fields.length; i++) {
										if (r.contains(fields[i])
												&& fields[i + 1].chars().allMatch(c -> Character.isDigit((char) c))
												&& fields[i + 2].length() == 1)
											relatedSynsets.add(fields[i + 1] + fields[i + 2]);
									}
								}

							});

						} catch (Exception e) {
							e.printStackTrace();
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Synset> synseys = new ArrayList<>();

		for (String s : relatedSynsets)
			synseys.add(getSynsetFromID(s));

		return synseys;

	}

}
