package it.uniroma1.lcl.clusty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.uniroma1.lcl.clusty.data.NasariVector;
import it.uniroma1.lcl.clusty.data.POS;
import it.uniroma1.lcl.clusty.data.Pair;

/**
 * 
 * @author
 *
 */
public class Clusty {
	
	public enum Similarity {
		WOV, // weighted overlap
		COS; // cosine similarity
	}
	
	private static final double THRESHOLD_1 = 0.50;
	private static final double THRESHOLD_2 = 0.40;
	private static final double THRESHOLD_3 = 0.30;
	
	private static boolean DEBUG = false;
	
	private static Clusty instance;
	
	private WordNet wn;
	private NasariVectors nasariVectors;
	public Similarity similarity;
	private POS pos;
	
	private Clusty(Similarity similarity, POS pos) {
		this.wn = WordNet.getInstance();
		this.nasariVectors = NasariVectors.getInstance();
		this.similarity = similarity;
		this.pos = pos;
	}
	
	public static Clusty getInstance() {
		if(instance == null) {
			synchronized (Clusty.class) {
				if(instance == null)
					instance = new Clusty(Similarity.COS, POS.ALL);
			}
		}
		return instance;
	}
	
	
	private static int findCluster(List<Set<String>> clusters, String synsetId) {
		for(int i = 0; i < clusters.size(); i++)
			if(clusters.get(i).contains(synsetId))
				return i;
		return -1;
	}
	
	private static void printClusters(String lemma, List<Set<String>> clusters, WordNet wn) {
		for(int i=0; i < clusters.size(); i++) {
			for(String s: clusters.get(i)) {
				System.out.println(lemma + ":" + i + "\t" + wn.getSynsetFromID(s));
			}
			System.out.println();
		}
	}
	
	private double getAverageSimilarity(String s1, Set<String> cluster) {
		double sumSimilarity = 0.0;
		NasariVector nv1 = nasariVectors.getNasariVector(s1);
		for(String s2: cluster)
			sumSimilarity = sumSimilarity + computeSimilarity(nv1, nasariVectors.getNasariVector(s2));
		return sumSimilarity / cluster.size();
		
	}
	
	private double computeSimilarity(NasariVector nv1, NasariVector nv2) {
		switch (this.similarity) {
		case COS:
			return nv1.cosineSimilarity(nv2);
		case WOV:
		default:
			return nv1.weightedOverlap(nv2);
		}
	}
	
	private double computeSimilarity(String s1, String s2) {
		return computeSimilarity(nasariVectors.getNasariVector(s1), nasariVectors.getNasariVector(s2));
	}
	
	public void setPos(POS pos) {
		this.pos = pos;
	}
	
	public POS getPos() {
		return this.pos;
	}
	
	/**
	 * Returns a list of cluster for a lemma given as input
	 * @param lemma
	 * @return list of cluster
	 */
	public List<Set<String>> clustify(String lemma){
		
		// init the empty list of clusters
		List<Set<String>> clusters = new ArrayList<Set<String>>();
		
		// retrieve a list of synsetID for a give lemma
		List<String> synsets = wn.getSynsets(lemma, pos).stream().map(s -> s.getID()).collect(Collectors.toList());
		
		// build all the combinations given a list of synsetsID
		List<Pair<String, String>> pairs = synsets.stream()
				.flatMap(s1 -> synsets.stream().map(s2 -> new Pair<String,String>(s1,s2))
				.filter(p -> p.getFirst().compareTo(p.getSecond()) < 0)).collect(Collectors.toList());
		if(DEBUG) System.out.println(pairs);
		
		// compute the similarity measure for each pair and sort them about it
		List<Pair<Pair<String, String>,Double>> triples = pairs.stream()
				.map(p -> new Pair<Pair<String, String>,Double>(p, computeSimilarity(p.getFirst(), p.getSecond())))
				.sorted(Comparator.comparing(Pair::getSecond, Comparator.reverseOrder())).collect(Collectors.toList());
		if(DEBUG) System.out.println(triples);
		
		for(Pair<Pair<String, String>,Double> t: triples) {
			
			double score = t.getSecond();
			if(score < THRESHOLD_1) break;
			
			String s1 = t.getFirst().getFirst();
			String s2 = t.getFirst().getSecond();
			
		    int c1 = findCluster(clusters, s1);
		    int c2 = findCluster(clusters, s2);
		    
		    if(c1 == -1 && c2 == -1) {
		    	Set<String> cluster = new HashSet<String>();
		    	cluster.add(s1);
		    	cluster.add(s2);
		    	clusters.add(cluster);
		    	if(DEBUG) System.out.println(String.format("1) %s and %s aren't in clusters",s1, s2));
		    }
		    else if(c1 == c2) {
		    	// do nothing
		    	if(DEBUG) System.out.println(String.format("2) %s and %s are just in the same cluster.",s1, s2));
		    }
		    else if(c2 == -1) {
		    	Set<String> cluster1 = clusters.get(c1);
		    	if(getAverageSimilarity(s2, cluster1) >= THRESHOLD_2) { // make it in the explicit way but it is't necessary
		    		cluster1.add(s2);
		    	}
		    	else {
		    		HashSet<String> cluster2 = new HashSet<String>();
		    		cluster2.add(s2);
		    		clusters.add(cluster2);
		    	}
		    	if(DEBUG) System.out.println(String.format("3) only %s is in a cluster.",s1));
		    }
		    else if(c1 == -1) {
		    	Set<String> cluster2 = clusters.get(c2);
		   
		    	if(getAverageSimilarity(s1, cluster2) >= THRESHOLD_2) { // make it in the explicit way but it is't necessary
		    		cluster2.add(s1);
		    	}
		    	else {
		    		HashSet<String> cluster1 = new HashSet<String>();
		    		cluster2.add(s1);
		    		clusters.add(cluster1);
		    	}
		    	if(DEBUG) System.out.println(String.format("4) only %s is in a cluster.",s2));
		    }
		    else if(c1 != -1 && c2 != -1) {
		    	Set<String> cluster1 = clusters.get(c1);
		    	Set<String> cluster2 = clusters.get(c2);
		    	if(getAverageSimilarity(s1, cluster2) >= THRESHOLD_3 && getAverageSimilarity(s2, cluster1) >= THRESHOLD_3) {
		    		cluster1.addAll(cluster2);
		    		clusters.remove(c2);
		    	}
		    }
		    if(DEBUG) System.out.println(String.format("5) %s and %s are in different clusters.",s1,s2));
		    
		}
		
	    for(String s: synsets) {
	    	if(findCluster(clusters, s) == -1) {
	    		Set<String> cluster = new HashSet<String>();
	    		cluster.add(s);
	    		clusters.add(cluster);
	    	}
	    }
		
		return clusters;
	}
	
	public static void allClusters(Clusty c) {
		
		Set<String> lemmas = WordNet.getInstance().getLemmas(c.getPos());
		
		lemmas.stream().forEach(lemma -> {
			List<Set<String>> clusters = c.clustify(lemma);
			printClusters(lemma, clusters, c.wn);
			
		});
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("==================================================");
		System.out.println("Clusty v1.0 - Sense clustering");
		System.out.println("Linguistic Computing Laboratory");
		System.out.println("Sapienza University of Rome");
		System.out.println("http://lcl.uniroma1.it");
		System.out.println("--------------------------------------------------");
		System.out.println("Released under CC-BY-NC-SA 4.0");
		System.out.println("https://creativecommons.org/licenses/by-nc-sa/4.0/");
		System.out.println("==================================================");
		System.out.println();
		if(args.length < 2 || args.length > 3) {
			System.out.println("Usage: java -classpath lib -jar clusty-1.0.jar <lemma>|--all <n,v,r,a,all>");
			System.out.println();
			return ;
		}
		
		String lemma = args[0];
		POS pos = POS.ALL;
		try {
			pos = POS.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		Clusty c = Clusty.getInstance();
		c.setPos(pos);
		
		if(!lemma.equalsIgnoreCase("--all")) {
			List<Set<String>> clusters = c.clustify(lemma);
			printClusters(lemma, clusters, c.wn);
			return ;
		}
		else {
			allClusters(c);
			return ;
		}
	}
}
