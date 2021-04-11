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
import java.util.Scanner;
import java.util.stream.Collectors;

public class task3 {
	
	public static void main(String[] args) {
		try {
			//geting arguments from terminal
			String sourceFile = args[0];
			String segmentation = args[1];
			String liveFile = args[2];
			
			// importing source file (runNumber;engineLetter;results;totalRelDocs \n+)	
			List<String> lines = Files.readAllLines(Paths.get(".\\"+sourceFile));
			String live = Files.readAllLines(Paths.get(".\\"+liveFile)).get(0);

			Map<String, HashMap<String, HashMap<String, String>>> dataStructure = new HashMap<String, HashMap<String, HashMap<String, String>>>();
			
			//creating data structure to frame all from files
			createDataStructure(dataStructure, lines);
			
			//datastructure after segmentation and probfuse modeling
			HashMap<String, HashMap<String, String>> kDataStructure = segmentDataStructure(dataStructure, Integer.parseInt(segmentation));
			
			//live documents rankings when fitted to obtained probfuse model (kDataStructure)
			HashMap<String, Float> documentsRanks = pluginLiveData(kDataStructure,live, Integer.parseInt(segmentation));
			
			// write to file
			BufferedWriter writer = new BufferedWriter(new FileWriter("probfuseResult.txt"));
			
			String resultString = "";
			
			for(String documentID: documentsRanks.keySet()) {
				resultString += documentID + "\t" + Float.toString(documentsRanks.get(documentID)) + "\n";
			}
			
			writer.write(resultString);
			
			writer.close();
			
			System.out.println(resultString);
			
			
		} catch (IOException e) {
			System.out.println("file not found");
		}	
		
	}
	
	
	
	private static HashMap<String, Float> pluginLiveData(HashMap<String, HashMap<String, String>> kDataStructure, String liveData, Integer numberOfSegments) {
		//engine - documentid - score 
		Map<String, HashMap<String, String>> pluginStructure = new HashMap<String, HashMap<String,String>>();
		
		String[] perEngineData = liveData.split("\t");
		
		for (int i = 0; i < perEngineData.length; i++) {
			
			String engineId = perEngineData[i].split(";")[0];
			pluginStructure.put(engineId, new HashMap<String, String>());
			String[] documentIds = perEngineData[i].split(";")[1].substring(1, perEngineData[i].split(";")[1].length()-1).split(",");
			int segmentSize = documentIds.length/numberOfSegments;
			HashMap<String, String> documentScores = pluginStructure.get(engineId);
			for (int j = 0; j < documentIds.length; j++) {
				int documentSegment = j / segmentSize;
				if(documentSegment == numberOfSegments) {
					documentSegment = documentSegment - 1; 
				}
				
				//applying ranking score of document d formula (ie) dividing by segment number it appears in
				// in this case (documentSegment+1) because documentSegment starts from 0 in our case. Hence adding 1 to match segment number,
				Float documentScore = Float.parseFloat(kDataStructure.get(engineId).get(Integer.toString(documentSegment)))/(documentSegment+1);
				
				documentScores.put(documentIds[j], Float.toString(documentScore));
			}
		}
		
		
		//key value pair structure for document id and its score
		HashMap<String, Float> documentRankings = new HashMap<String,Float>();
		for(String engineID : pluginStructure.keySet()) {
			HashMap<String, String> engineRetrievals = pluginStructure.get(engineID) ;
			for(String documentID : engineRetrievals.keySet()) {
				if(documentRankings.containsKey(documentID)) {
					documentRankings.put(documentID, documentRankings.get(documentID) + Float.parseFloat(engineRetrievals.get(documentID)));
				}else {
					documentRankings.put(documentID, Float.parseFloat(engineRetrievals.get(documentID)));
				}
			}
		}
		
		//now sorting documents according to their ranks from fusion
		HashMap<String, Float> documentRankingsSorted = documentRankings
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                    LinkedHashMap::new));
		
		
		return documentRankingsSorted;
		
			
	}



	public static HashMap<String, HashMap<String, String>> segmentDataStructure(Map<String, HashMap<String, HashMap<String, String>>> dataStructure, int numberOfSegments) {
		
		Map<String, HashMap<String, HashMap<String, String>>> segmentedStructure = new HashMap<String, HashMap<String,HashMap<String,String>>>();
		
		List<String> engines = new ArrayList<String>(dataStructure.keySet());
		
		// get count of search results
		int totalResults = dataStructure.get(engines.get(0)).get("1").get("searchResults").length();
		
		float segmentSize = (float)totalResults/numberOfSegments;
		
		int averageSize = (int) segmentSize;
		
		
		for(String engineID : dataStructure.keySet()) {
			
			HashMap<String, HashMap<String, String>> value = dataStructure.get(engineID);
			segmentedStructure.put(engineID, new HashMap<String, HashMap<String,String>>());
			for( String runNumber: value.keySet()) {
				HashMap<String, HashMap<String, String>> runs = segmentedStructure.get(engineID);
				runs.put(runNumber, new HashMap<String, String>());
				String searchResults = value.get(runNumber).get("searchResults");
				int beginIndex = 0;
				for(int i =0; i< numberOfSegments; i++) {
					HashMap<String, String> segments = runs.get(runNumber);	
					int endIndex = beginIndex + averageSize;
					if(i == numberOfSegments-1) {
						endIndex = searchResults.length();
					}
					String segment = searchResults.substring(beginIndex, endIndex);
					beginIndex = endIndex;
					
					segments.put(Integer.toString(i), Float.toString(segment.chars().filter(ch-> ch=='R').count()/(float)segment.length()));
				}
			}
		}
		
		//preparing probfuse training model
		HashMap<String, HashMap<String, String>> kDataStructure = new HashMap<String, HashMap<String, String>>();
		for(String engineID : segmentedStructure.keySet()) {
			HashMap<String, HashMap<String, String>> values = segmentedStructure.get(engineID);
			kDataStructure.put(engineID, new HashMap<String, String>());
			int totalRunNumbers = values.keySet().size();
			int count = 1;
			for(String runNumber : values.keySet()) {
				HashMap<String, String> kValues = kDataStructure.get(engineID);
				HashMap<String, String> individualKs = values.get(runNumber);
				for(String k : individualKs.keySet()) {
					float kValue = kValues.get(k) == null ? 0: Float.parseFloat(kValues.get(k));
					 //summing up data
					kValues.put(k, Float.toString(kValue+Float.parseFloat(individualKs.get(k))));
					if(count == totalRunNumbers) {
						//averaging at the end(i.e dividing by total search operations in training data)
						kValues.put(k, Float.toString((Float.parseFloat(kValues.get(k))/totalRunNumbers)));
					}
				}
				
				count++;
			}
		}
		
		return kDataStructure;
		
	}

	public static void createDataStructure(Map<String, HashMap<String, HashMap<String, String>>> dataStructure, List<String> lines) {
		for (String line : lines) {
			
			String[] lineFragments = (line.split(";"));
			
			String runNumber = lineFragments[0];
			String engineLetter = lineFragments[1];
			String searchResults = lineFragments[2];
			String totalRels = lineFragments[3];
			HashMap<String, HashMap<String, String>> engineRuns;
			HashMap<String, String> runResults;
			if(dataStructure.containsKey(engineLetter)) {
				//append here
				engineRuns = dataStructure.get(engineLetter);
				
				if(engineRuns.containsKey(runNumber)) {
					//append here
					runResults = engineRuns.get(runNumber);
					runResults.put("searchResults", searchResults);
					runResults.put("REL", totalRels);
				
				}else {
					// create key for this run
					runResults = new HashMap<String, String>();
					runResults.put("searchResults", searchResults);
					runResults.put("REL", totalRels);
					// and append	
				}
				
			}else {
				//create a key for this engineLetter
				engineRuns = new HashMap<String, HashMap<String,String>>();
				//and append
				runResults = new HashMap<String, String>();
				runResults.put("searchResults", searchResults);
				runResults.put("REL", totalRels);				
			}
			engineRuns.put(runNumber, runResults);
			dataStructure.put(engineLetter, engineRuns);
			
		}
	}
	
}
