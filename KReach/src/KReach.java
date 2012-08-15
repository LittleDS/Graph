import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Copyright reserved by Lei Yang @ Case Western Reserve University
 * August 15th, 2012
 */
public class KReach {
	//Since the attributes of each vertex don't have any contribution to the labeling, we can split them into a separate structure
	HashMap<Integer, List<String>> attributes = new HashMap<Integer, List<String>>();

	//The adjacency list of each vertex
	HashMap<Integer, List<Integer>> neighbors = new HashMap<Integer, List<Integer>>();

	public static void main(String[] args) throws FileNotFoundException {
		KReach kr = new KReach();
		kr.Encode("graphexample.txt");
	}
	
	public void Encode(String fileName) throws FileNotFoundException {
		File tFile = new File(fileName);
		Scanner tScanner = new Scanner(tFile);
		//Read the original graph file and build the graph in the main memory
		while (tScanner.hasNext()) {
			//Read the graph file
			//Get the ID and attributes line
			String idLine = tScanner.nextLine();

			//Get the neighbors line
			String neighborLine = tScanner.nextLine();
			
			//Build the graph in the main memory
			//Process the ID and the attributes
			String[] strings = idLine.split(",");
			int ID = Integer.parseInt(strings[0]);
			attributes.put(ID, new LinkedList<String>());

			//The first element is the vertex ID, we start from the second one
			for (int i = 1; i < strings.length; i++) {
				attributes.get(ID).add(strings[i]);
			}
			
			//Process the neighbors and also transform the graph into a undirected one
			strings = neighborLine.split(",");
			if (!neighbors.containsKey(ID))
				neighbors.put(ID, new LinkedList<Integer>());
			for (int i = 0; i < strings.length; i++) {
				int tN = Integer.parseInt(strings[i]);
				//If the neighbor ID is equal to -1, it means the current vertex doesn't have a neighbor
				if (tN != -1) {
					neighbors.get(ID).add(tN);
					if (!neighbors.containsKey(tN))
						neighbors.put(tN, new LinkedList<Integer>());
					neighbors.get(tN).add(ID);						
				}				
			}			
		}
		tScanner.close();		
		
		GreedyVertexCover();
	}

	

	/**
	 * Obtain the vertex cover using the greedy method
	 */
	public void GreedyVertexCover() {
		//Calculate the degree of each vertex
		int totalVertices = attributes.keySet().size();
		
		int numberofEdge = 0;
		
		int[] degree = new int[totalVertices];
		
		Integer[] index = new Integer[totalVertices];
		
		for (int i = 0; i < totalVertices; i++) {
			index[i] = i;
			//out-degree
			degree[i] += neighbors.get(i).size();
			
			numberofEdge += degree[i];
			
			for (Integer j : neighbors.get(i))
				//in-degree
				degree[j]++;
		}

	}
}
