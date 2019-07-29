package it.uniroma1.lcl.clusty.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
/**
 * Represents a NASARI lexical vector of a synset
 * @author
 *
 */
public class NasariVector {
	private String ID;
	private Map<String, Double> lexicalVector;

	public NasariVector(String ID, Map<String, Double> lexicalVector) {
		this.ID = ID;
		this.lexicalVector = lexicalVector;
	}

	/**
	 * @return Returns the lexical vector of this synset
	 */
	public Map<String, Double> getlexicalVector() {
		return lexicalVector;
	}

	@Override
	public String toString() {
		return "" + ID + "\t" + lexicalVector;
	}



	/**
	 * Compute the cosine similarity from this lexical vector and another given as input
	 * @param nasariVector lexical vector given as input
	 * @return the cosine similarity
	 */
	public double cosineSimilarity(NasariVector nasariVector) {
		Set<String> interset = Sets.intersection(lexicalVector.keySet(), nasariVector.lexicalVector.keySet());
		Double num = interset.stream().mapToDouble(k -> lexicalVector.get(k) * nasariVector.lexicalVector.get(k)).sum();
		Double n1 = Math.sqrt(lexicalVector.values().stream().mapToDouble(x -> x * x).sum());
		Double n2 = Math.sqrt(nasariVector.lexicalVector.values().stream().mapToDouble(x -> x * x).sum());
		return num / (n1 * n2);
	}

	
	private Map<String, Integer> getRankMap() {
		Map<String, Integer> m = new HashMap<>();
		int i = 1;
		for (String k : lexicalVector.keySet())
			m.put(k, i++);
		return m;
	}
	
	/**
	 * Compute the weighted overlap from this lexical vector and another given as input
	 * @param nasariVector lexical vector given as input
	 * @return the weighted overlap
	 */
	public double weightedOverlap(NasariVector nasariVector) {

		Map<String, Integer> r1 = getRankMap();
		Map<String, Integer> r2 = nasariVector.getRankMap();

		double num = 0.0;
		double den = 0.0;
		double i = 1;

		for (String k : r1.keySet()) {
			if (r2.containsKey(k)) {
				num += 1.0 / (r1.get(k) + r2.get(k));
				den += 1.0 / (2 * i++);
			}
		}

		return num == 0.0 && den == 0.0 ? 0.0 : num / den;
	}
}
