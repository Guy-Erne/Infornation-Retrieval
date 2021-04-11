import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class task1 {
	
	public static void main(String[] args) {
		try {
			String filename = args[0];
			
			// importing source file (runNumber;engineLetter;results;totalRelDocs \n+)	
			List<String> lines = Files.readAllLines(Paths.get(".\\" + filename));
			Map<String, HashMap<String, HashMap<String, String>>> dataStructure = new HashMap<String, HashMap<String, HashMap<String, String>>>();
			createDataStructure(dataStructure, lines);
			doRetrievalEngineEvaluation(dataStructure);
			
			
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter("EvaluationsTask1.txt"));
				String resultString = "";
				
				for(String engineID: dataStructure.keySet()) {
					resultString += "-----------------------------------------------\n";
					resultString += engineID +"\n";
					resultString += "-----------------------------------------------\n";
					
					HashMap<String, HashMap<String, String>> runers = dataStructure.get(engineID);
					
					for(String runID : runers.keySet()) {
						resultString += "\t" + runID +"\n";
						
						HashMap<String, String> evaluations = runers.get(runID);
						
						for(String eval: evaluations.keySet()) {
							
							resultString += "\t\t\t\t" + eval +"\t" + evaluations.get(eval) + "\n";
							
						}
					}
					
				}
				writer.write(resultString);
				writer.close();
				System.out.println(resultString);
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			
			
		} catch (IOException e) {
			System.out.println("file not found");
		}
	}
	
	private static void doRetrievalEngineEvaluation(Map<String, HashMap<String, HashMap<String, String>>> dataStructure) {
		
		for (HashMap<String, HashMap<String, String>> engine : dataStructure.values()) {
			float meanAveragePrecision = 0;
			float totalAveragePrecision = 0;
			float totalRuns = 0;
			for (HashMap<String, String> runs : engine.values()) {
				totalAveragePrecision += Float.parseFloat(runs.get("averagePrecision"));
				totalRuns++;
			}
			meanAveragePrecision = totalAveragePrecision/totalRuns;
			
			HashMap<String, String> evaluationResults = new HashMap<String, String>();
			evaluationResults.put("MAP",Float.toString(meanAveragePrecision));
			engine.put("evaluationResults", evaluationResults);
		}
		
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
					insertRunResults(runResults,searchResults, totalRels);
				
				}else {
					// create key for this run
					runResults = new HashMap<String, String>();
					insertRunResults(runResults,searchResults, totalRels);
					// and append	
				}
				
			}else {
				//create a key for this engineLetter
				engineRuns = new HashMap<String, HashMap<String,String>>();
				//and append
				runResults = new HashMap<String, String>();
				insertRunResults(runResults,searchResults, totalRels);
				
			}
			engineRuns.put(runNumber, runResults);
			dataStructure.put(engineLetter, engineRuns);
			
		}
	}
	
	public static void insertRunResults( HashMap<String, String> runResults, String searchResults, String rel) {
		runResults.put("searchResults", searchResults);
		runResults.put("REL", rel);
				
		float precision = calculatePrecision(searchResults);
		float recall = calculateRecall(searchResults, Float.parseFloat(rel));
		
		String precisionString = Float.toString(precision);
		String recallString = Float.toString(recall);
		
		runResults.put("RELRET", Long.toString(calculateRelRet(searchResults)));
		runResults.put("RET", Integer.toString(calculateRet(searchResults)));
		
		runResults.put("Precision", precisionString);
		runResults.put("Recall", recallString);
		
		long relsAtFive = searchResults.substring(0, 5).chars().filter(ch->ch == 'R').count();
		float precisionAtFive = relsAtFive / (float) 5;
		
		String precisionAtFiveString = Float.toString(precisionAtFive);
		
		runResults.put("P@5", precisionAtFiveString);
		
		// calculating precision at recall = 0.5
		int relret_new = Math.round(Float.parseFloat(rel) *(float) 0.5);
		
		String searchResults_new = substringToNthIndexOf('R', searchResults, relret_new);
		String precisionAtRecall0_5String = Float.toString(calculatePrecision(searchResults_new));
		runResults.put("P@R=0.5", precisionAtRecall0_5String);
		
		// computing index of all R's
		float averagePrecision = 0;
		float sumPrecisions = 0;
		int n=1;
		for(int i=0; i<searchResults.length(); i++) {
			if(searchResults.charAt(i) == 'R') {
				sumPrecisions += calculatePrecision(substringToNthIndexOf('R', searchResults, n));
				n++;
			}
		}
		
		averagePrecision = sumPrecisions/calculateRelRet(searchResults);

		runResults.put("averagePrecision", Float.toString(averagePrecision));
	}
	
	public static String substringToNthIndexOf(char character, String string, int indexNumber) {
		String s = "";
		for(int i=0; i < indexNumber; i++ ) {
			int index = string.indexOf(character,0);
			s += string.substring(0, index+1);
			string = string.substring(index+1, string.length());
		}
		return s;
	}
	
	public static float calculatePrecision(String searchResults) {
		float precision = calculateRelRet(searchResults)/(float)calculateRet(searchResults);
		return precision;	
	}
	
	public static float calculateRecall(String searchResults, float rel) {
		float recall = calculateRelRet(searchResults)/rel;
		return recall;	
	}
	
	public static long calculateRelRet(String searchResults) {
		long relret = searchResults.chars().filter(ch -> ch == 'R').count();
		return relret;
	}
	
	public static int calculateRet(String searchResults) {
		int ret = searchResults.trim().length();
		return ret;
	}
	
	
	

}