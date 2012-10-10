import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class Graph {
	//Since the attributes of each vertex don't have any contribution to the labeling, we can split them into a separate structure
	public HashMap<Integer, List<String>> attributes = new HashMap<Integer, List<String>>();
	public HashMap<Integer, String> primaryAttribute = new HashMap<Integer, String>();
	
	//The adjacency list of each vertex
	public HashMap<Integer, List<Integer>> children = new HashMap<Integer, List<Integer>>();
	public HashMap<Integer, List<Integer>> parents = new HashMap<Integer, List<Integer>>();
	
	//The value will be set after loading the graph
	public int totalEdges;

	//The position of the primary attribute, by default it's set to 1
	public int primaryPosition = 1;
	
	public boolean graphLoaded = false;

	public HashMap<Integer, Integer> indegree = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> outdegree = new HashMap<Integer, Integer>();
	
	/**
	 * Load the graph from a file in the disk
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void loadGraphFromFile(String fileName) throws FileNotFoundException {
		//Initialize the value
		totalEdges = 0;
		
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

			if (strings.length > 1)
				primaryAttribute.put(ID, strings[primaryPosition]);
			else
				primaryAttribute.put(ID, strings[0]);
			
			//The first element is the vertex ID, we start from the second one
			for (int i = 1; i < strings.length; i++) {
				attributes.get(ID).add(strings[i]);
			}
			
			//Process the neighbors
			strings = neighborLine.split(",");
			if (!children.containsKey(ID))
				children.put(ID, new LinkedList<Integer>());
			for (int i = 0; i < strings.length; i++) {
				int tN = Integer.parseInt(strings[i]);
				//If the neighbor ID is equal to -1, it means the current vertex doesn't have a neighbor
				if (tN != -1) {
					children.get(ID).add(tN);
					//Calculate the total number of edges
					totalEdges++;
						
					//The parents of each node is also record
					if (!parents.containsKey(tN))
						parents.put(tN, new LinkedList<Integer>());
					parents.get(tN).add(ID);						
				}				
			}
		}
		
		tScanner.close();			
		
		calculateDegree();
		 
		graphLoaded = true;
	}
	
	/**
	 * Add an edge to the graph
	 * 
	 */
	public void addEdge(Integer s, List<String> aS, Integer t, List<String> aT) {
		if (!attributes.containsKey(s))
			attributes.put(s, aS);
		if (!attributes.containsKey(t))
			attributes.put(t, aT);
		if (!children.containsKey(s))
			children.put(s, new LinkedList<Integer>());
		children.get(s).add(t);
	}
	
	/**
	 * Calculate the indegree and outdegree
	 */
	public void calculateDegree() {
		for (Integer i : children.keySet()) {
			outdegree.put(i, children.get(i).size());
		}
		for (Integer i : parents.keySet()) {
			indegree.put(i, parents.get(i).size());			
		}		
	}
}
