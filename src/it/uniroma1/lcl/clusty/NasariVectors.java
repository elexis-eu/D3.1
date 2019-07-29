package it.uniroma1.lcl.clusty;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import it.uniroma1.lcl.clusty.data.NasariVector;
import it.uniroma1.lcl.clusty.data.POS;
import it.uniroma1.lcl.clusty.data.Synset;

/**
 * 
 * @author
 *
 */
public class NasariVectors {

	private Map<String, NasariVector> nasariVectors = new HashMap<>();
	public static final String NASARI_PATH = "resources/synset_nasari_vectors.txt";

	private static NasariVectors instance;

	private NasariVectors() {
		load();
	}

	public static NasariVectors getInstance() {
		if (instance == null) {
			synchronized (NasariVectors.class) {
				if (instance == null)
					instance = new NasariVectors();
			}
		}
		return instance;
	}

	private void load() {

//		System.out.println("Loading lexical vectors...");

		try {
			Files.lines(Paths.get(NASARI_PATH)).forEach(line -> {

				String[] fields = line.split("\t");

				Map<String, Double> lexicalVector = new LinkedHashMap<>();

				for (int i = 1; i < fields.length; i++) {
					Integer underscoreIdx = fields[i].lastIndexOf("_");
					String lemma = fields[i].substring(0, underscoreIdx);
					Double score = Double.parseDouble(fields[i].substring(underscoreIdx + 1));
					lexicalVector.put(lemma, score);
				}

				nasariVectors.put(fields[0], new NasariVector(fields[0], lexicalVector));
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a NASARI vector give as input a specific synsetID
	 * @param synsetID
	 * @return NASARI vector
	 */
	public NasariVector getNasariVector(String synsetID) {
		return nasariVectors.get(synsetID);
	}

	/**
	 * Returns a map from ID to NASARI vector
	 * @return
	 */
	public Map<String, NasariVector> getNasariVectors() {
		return nasariVectors;
	}

	public static void main(String[] args) {

		if (args.length > 0) {
			String lemma = args[0];
			System.out.println("Querying: " + lemma);

			List<Synset> synsets = WordNet.getInstance().getSynsets(lemma, POS.ALL);

			for (Synset s1 : synsets) {
				System.out.println(s1);
				NasariVector lv1 = NasariVectors.getInstance().getNasariVector(s1.getID());
				Map<Double, String> scores = new TreeMap<>(Comparator.reverseOrder());
				for (Synset s2 : synsets) {

					NasariVector lv2 = NasariVectors.getInstance().getNasariVector(s2.getID());
					double sim = lv1.weightedOverlap(lv2);
					scores.put(sim, s2.toString());
				}

				scores.keySet().stream().forEach(k -> System.out.println("\t" + k + "\t" + scores.get(k)));
			}
		} else
			for (Map.Entry<String, NasariVector> e : NasariVectors.getInstance().getNasariVectors().entrySet())
				System.out.println(e.getValue());
	}
}
