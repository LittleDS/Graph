import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class Joints {
	Graph graph;
	
	public static void main(String[] args) throws IOException {
		Joints test = new Joints();
		test.Encode("datagraph.txt");
		System.out.println("Done.");
	}
	
	public void Encode(String fileName) throws IOException {
		graph = new Graph();
		graph.loadGraphFromFile(fileName);
		
		IndexEdges(fileName + "Edges");
		System.out.println("Finished Edges");
		
		IndexJoints(fileName + "Joints");
		System.out.println("Finsihed Joints");
	}
		
	HashMap<String, HashMap<String, List<Integer>>> edgesIndex = new HashMap<String, HashMap<String,List<Integer>>>();
	
	/**
	 * Build the edges index
	 * @param fileName
	 * @throws IOException 
	 */
	public void IndexEdges(String fileName) throws IOException {

		//For each vertex
		for (Integer i : graph.children.keySet()) {

			//Get all the attributes for that vertex
			String sAttribute = graph.primaryAttribute.get(i);

			if (!edgesIndex.containsKey(sAttribute))
				edgesIndex.put(sAttribute, new HashMap<String, List<Integer>>());

			//All the edges starting from the same source
			List<Integer> childrenList = graph.children.get(i);

			//For each edge with different target
			for (Integer j : childrenList) {

				//The attributes for the target vertex
				String tAttribute = graph.primaryAttribute.get(j);
	
	
				if (!edgesIndex.get(sAttribute).containsKey(tAttribute))
					edgesIndex.get(sAttribute).put(tAttribute, new LinkedList<Integer>());
	
				edgesIndex.get(sAttribute).get(tAttribute).add(i);
				edgesIndex.get(sAttribute).get(tAttribute).add(j);

			}
		}

		//Open the file
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		//Write the index into file
		for (String s : edgesIndex.keySet()) {

			HashMap<String, List<Integer>> temp = edgesIndex.get(s);

			for (String s1 : temp.keySet()) {

				out.write(s + "," + s1);

				//All the vertices that contains the two attributes
				for (Integer i : temp.get(s1)) {
					out.write("," + i);
				}

				//Write into the file
				out.write("\r\n");

			}
		}

		//don't forget to close the stream
		out.close();	
	}
	
	HashMap<String, HashMap<String, HashMap<String, List<Integer>>>> jointsIndex = new HashMap<String, HashMap<String, HashMap<String, List<Integer>>>>();
	
	/**
	 * Build the joints index
	 * @param fileName
	 * @throws IOException 
	 */
	public void IndexJoints(String fileName) throws IOException {
		int totalJonts = 0;
		
		//Choose the triples into the index
		for (Integer i : graph.children.keySet()) {

			List<Integer> childrenList = graph.children.get(i);
			
			String attributeA = graph.primaryAttribute.get(i);

			if (!jointsIndex.containsKey(attributeA))
				jointsIndex.put(attributeA, new HashMap<String, HashMap<String, List<Integer>>>());
			
			for (Integer j : childrenList) {

				List<Integer> grandchildren = graph.children.get(j);
							
				String attributeB = graph.primaryAttribute.get(j);
			
				if (!jointsIndex.get(attributeA).containsKey(attributeB))
					jointsIndex.get(attributeA).put(attributeB, new HashMap<String, List<Integer>>());
				
				for (Integer k : grandchildren) {
					
					String attributeC = graph.primaryAttribute.get(k);
					
					if (!jointsIndex.get(attributeA).get(attributeB).containsKey(attributeC))
						jointsIndex.get(attributeA).get(attributeB).put(attributeC, new LinkedList<Integer>());
					
					List<Integer> toInsert = jointsIndex.get(attributeA).get(attributeB).get(attributeC); 				
					toInsert.add(i);
					toInsert.add(j);							
					toInsert.add(k);
					totalJonts++;
				}
								
			}
		}
		
		
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);

		//Write the index into file
		for (String s : jointsIndex.keySet()) {

			HashMap<String, HashMap<String, List<Integer>>> temp = jointsIndex.get(s);

			for (String s1 : temp.keySet()) {

				HashMap<String, List<Integer>> temp1 = temp.get(s1);

				for (String s2: temp1.keySet()) {

					List<Integer> temp2 = temp1.get(s2);

					out.write(s + "," + s1 + "," + s2);

					//All the vertices that contains the two attributes
					for (Integer i : temp2) {
						out.write("," + i);
					}

					//Write into the file
					out.write("\r\n");	
				}
			}
		}

		//dont' forget to close the stream
		out.close();				
	}

	/**
	 * Load the edge index from file
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void loadEdgeIndexFromFile(String fileName) throws FileNotFoundException {
		
		edgesIndex.clear();
		
		File tFile = new File(fileName);
		
		Scanner tScanner = new Scanner(tFile);
		
		//Read the original graph file and build the graph in the main memory
		while (tScanner.hasNext()) {
			String idLine = tScanner.nextLine();			
			String[] strings = idLine.split(",");
			
			String attributeA = strings[0];
			if (!edgesIndex.containsKey(attributeA))
				edgesIndex.put(attributeA, new HashMap<String, List<Integer>>());
			
			String attributeB = strings[1];
			if (!edgesIndex.get(attributeA).containsKey(attributeB))
				edgesIndex.get(attributeA).put(attributeB, new LinkedList<Integer>());
			
			List<Integer> toInsert = edgesIndex.get(attributeA).get(attributeB);
			for (int i = 2; i < strings.length; i++) {
				toInsert.add(Integer.parseInt(strings[i]));
			}
			
		}
		
		tScanner.close();			
	}
	
	/**
	 * Load the joint index from file
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void loadJointIndexFromFile(String fileName) throws FileNotFoundException {
		
		jointsIndex.clear();
		
		File tFile = new File(fileName);
		
		Scanner tScanner = new Scanner(tFile);
		
		//Read the original graph file and build the graph in the main memory
		while (tScanner.hasNext()) {
			String idLine = tScanner.nextLine();			
			String[] strings = idLine.split(",");						
			
			String attributeA = strings[0];
			if (!jointsIndex.containsKey(attributeA))
				jointsIndex.put(attributeA, new HashMap<String, HashMap<String, List<Integer>>>());
			
			String attributeB = strings[1];
			if (!jointsIndex.get(attributeA).containsKey(attributeB))
				jointsIndex.get(attributeA).put(attributeB, new HashMap<String, List<Integer>>());
			
			String attributeC = strings[2];			
			if (!jointsIndex.get(attributeA).get(attributeB).containsKey(attributeC))
				jointsIndex.get(attributeA).get(attributeB).put(attributeC, new LinkedList<Integer>());
			
			List<Integer> toInsert = jointsIndex.get(attributeA).get(attributeB).get(attributeC); 
			for (int i = 3; i < strings.length; i++) {			
				toInsert.add(Integer.parseInt(strings[i]));
			}
			
			strings = null;
		}
		
		tScanner.close();			
		
	}
}