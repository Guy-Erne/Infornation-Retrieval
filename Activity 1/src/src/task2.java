import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class task2 {
	
	
	public static void main(String[] args) {
		
		try {
			String engineEvaluations = args[0];
			String engineWeightsx = args[1];

			
			List<String> lines = Files.readAllLines(Paths.get(".\\"+engineEvaluations));
			String weights = Files.readAllLines(Paths.get(".\\"+engineWeightsx)).get(0);
			Map<String, HashMap<Integer, Float>> dataStructure = new HashMap<String, HashMap<Integer, Float>>();
			Map<String, String> engineWeights = new HashMap<String, String>();
			createDataStructure(dataStructure, lines);
			for (String weightEngineAssoc : weights.split("\t")) {
				String engineID = weightEngineAssoc.split(";")[0]; 
				String weight = weightEngineAssoc.split(";")[1];
				engineWeights.put(engineID, weight);
			}
			System.out.println("Interleaving...");
			performFusionWithInterleaving(dataStructure);
			System.out.println("CombSum...");
			performFusionWithCombSum(dataStructure, engineWeights);
			System.out.println("LCM...");
			performFusionWithLCM(dataStructure, engineWeights);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private static void performFusionWithLCM(Map<String, HashMap<Integer, Float>> dataStructure, Map<String, String> engineWeights) {
		
		// perform normalisation
		for(Entry<String, HashMap<Integer, Float>> engineResults : dataStructure.entrySet()) {
			
			ArrayList<Float> relevanceScores = new ArrayList<Float>(engineResults.getValue().values());
			float minScore = (relevanceScores.get(relevanceScores.size()-1));
			float maxScore = (relevanceScores.get(0));
			
			engineWeights.put("maxWeight"+engineResults.getKey(), Float.toString(maxScore));
			engineWeights.put("minWeight"+engineResults.getKey(), Float.toString(minScore));
			
			HashMap<Integer, Float> results = engineResults.getValue();
			
			for(Integer i : results.keySet()) {
				results.put(i, (engineResults.getValue().get(i) - minScore)/(maxScore - minScore));
			}
			dataStructure.put(engineResults.getKey(), results);
			
		}
		
		//after normalisation
		HashMap<Integer, Float> lcm = new HashMap<Integer, Float>();
		for( Entry<String, HashMap<Integer, Float>>dataStructureEntries : dataStructure.entrySet()) {
			String key = dataStructureEntries.getKey();
			Float weight = Float.parseFloat(engineWeights.get(key));
			HashMap<Integer, Float> results = dataStructureEntries.getValue();
			
			for( Entry<Integer, Float> result : results.entrySet()) {
				
				
				if(lcm.containsKey(result.getKey())) {
					lcm.put(result.getKey(), result.getValue() * weight +  lcm.get(result.getKey()));
				}else {
					lcm.put(result.getKey(), result.getValue() * weight);
				}
			}
		}
		
		
		//sorting
		HashMap<Integer, Float> lcmSorted = lcm
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                    LinkedHashMap::new));
		
		HashMap<Integer, Float> lcmSortedTop100 = lcmSorted
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .limit(100)
		        .collect(
		                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                    LinkedHashMap::new));
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("LCMOutput.txt"));
			String resultString = "";
			
			for(Integer documentID: lcmSortedTop100.keySet()) {
				resultString += documentID + "\t" + Float.toString(lcmSortedTop100.get(documentID)) + "\n";
			}
			writer.write(resultString);
			writer.close();
			System.out.println(resultString);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		
	}
	
	
	private static void performFusionWithCombSum(Map<String, HashMap<Integer, Float>> dataStructure, Map<String, String> engineWeights) {
		
		// perform normalisation
		for(Entry<String, HashMap<Integer, Float>> engineResults : dataStructure.entrySet()) {
			
			ArrayList<Float> relevanceScores = new ArrayList<Float>(engineResults.getValue().values());
			float minScore = (relevanceScores.get(relevanceScores.size()-1));
			float maxScore = (relevanceScores.get(0));
			
			engineWeights.put("maxWeight"+engineResults.getKey(), Float.toString(maxScore));
			engineWeights.put("minWeight"+engineResults.getKey(), Float.toString(minScore));
			
			HashMap<Integer, Float> results = engineResults.getValue();
			
			for(Integer i : results.keySet()) {
				results.put(i, (engineResults.getValue().get(i) - minScore)/(maxScore - minScore));
			}
			dataStructure.put(engineResults.getKey(), results);
			
		}
		
		
		//after normalisation
		HashMap<Integer, Float> combSum = new HashMap<Integer, Float>();
		for(Map<Integer, Float> results : dataStructure.values()) {
			for( Entry<Integer, Float> result : results.entrySet()) {
				if(combSum.containsKey(result.getKey())) {
					combSum.put(result.getKey(), result.getValue() +  combSum.get(result.getKey()));
				}else {
					combSum.put(result.getKey(), result.getValue());
				}
			}
		}
		
		//sorting
		HashMap<Integer, Float> combSumSorted = combSum
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                    LinkedHashMap::new));
		
		HashMap<Integer, Float> combSumSortedTop100 = combSumSorted
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .limit(100)
		        .collect(
		                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                    LinkedHashMap::new));
			
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("combSumOutput.txt"));
			String resultString = "";
			
			for(Integer documentID: combSumSortedTop100.keySet()) {
				resultString += documentID + "\t" + Float.toString(combSumSortedTop100.get(documentID)) + "\n";
			}
			writer.write(resultString);
			writer.close();
			System.out.println(resultString);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		

		
	}
	
	
	/*
	 * 0 from engine-A, 1 from engineB, 2 from engineC and so on in roundrobin fashion.
	 * */
	public static void performFusionWithInterleaving(Map<String, HashMap<Integer, Float>> dataStructure) {
		//list of engine IDS [A, B, C etc]
		List<String> engineIDs = new ArrayList<String>(dataStructure.keySet());
		List<String> interleavingResult = new ArrayList<String>();
		int index=1;
		for(int i=0; i < 100; i++) {
			try {
				// associating i with engine ids such that each i will get alternating engine ID;s which can be approached by formula just below.
				String fromEngineID = engineIDs.get( i - engineIDs.size() * ( i/engineIDs.size() ) );
				//
				HashMap<Integer, Float> engineData = dataStructure.get(fromEngineID);
				List<Integer> documentIDs = new ArrayList<Integer>(engineData.keySet());
				interleavingResult.add(fromEngineID+";"+documentIDs.get(index-1)+";"+engineData.get(documentIDs.get(index-1)));
				if(i % engineIDs.size() == 0) {
					index++;
				}
				
			} catch (Exception e) {
				System.out.println("exception");
				System.out.println(i/engineIDs.size());
			}
		}
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("interleavingResult.txt"));
			String resultString = "";
			
			for(String documentID: interleavingResult) {
				resultString += documentID + "\n";
			}
			
			writer.write(resultString);
			writer.close();
			System.out.println(resultString);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}
	
	public static void createDataStructure(Map<String, HashMap<Integer, Float>> dataStructure, List<String> lines) {
		for (String line : lines) {
			String[] engines = (line.split("\t"));
			for (String evaluation : engines) {
				
				String[] parts = evaluation.split(";");
				
				String engineID = parts[0];
				String documentID = parts[1];
				String relevanceScore = parts[2];
				HashMap<Integer, Float> engineData;
				if(dataStructure.containsKey(engineID)) {
					//append here
					engineData = dataStructure.get(engineID);
					engineData.put(Integer.parseInt(documentID), Float.parseFloat(relevanceScore));

				}else {
					//create a key for this engineLetter
					engineData = new HashMap<Integer, Float>();
					engineData.put(Integer.parseInt(documentID), Float.parseFloat(relevanceScore));
					
				}
				dataStructure.put(engineID, engineData);
				
			}
			
		}
		
		List<String> engineIDs = new ArrayList<String>(dataStructure.keySet());
		for (String string : engineIDs) {
			HashMap<Integer, Float> sorted = dataStructure.get(string)
			        .entrySet()
			        .stream()
			        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
			        .collect(
			                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
			                    LinkedHashMap::new));
			
			dataStructure.put(string, sorted);
		}
	
	}

}