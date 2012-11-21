import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class Graph implements Comparable<Graph> {
	//Since the attributes of each vertex don't have any contribution to the labeling, we can split them into a separate structure
	public HashMap<Integer, List<String>> attributes = new HashMap<Integer, List<String>>();
	public HashMap<Integer, String> primaryAttribute = new HashMap<Integer, String>();
	
	//The adjacency list of each vertex
	public HashMap<Integer, List<Integer>> children = new HashMap<Integer, List<Integer>>();
	public HashMap<Integer, List<Integer>> parents = new HashMap<Integer, List<Integer>>();
	
	//The value will be set after loading the graph
	public int totalEdges = 0;

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
	
		if (!attributes.containsKey(s)) {
			attributes.put(s, aS);
			if (primaryPosition > 0)
				primaryAttribute.put(s, aS.get(primaryPosition - 1));
			else
				primaryAttribute.put(s, String.valueOf(s));
		}
		
		if (!attributes.containsKey(t)) {
			attributes.put(t, aT);
			if (primaryPosition > 0)
				primaryAttribute.put(t, aT.get(primaryPosition - 1));
			else
				primaryAttribute.put(t, String.valueOf(t));
		}
		
		if (!children.containsKey(s))
			children.put(s, new LinkedList<Integer>());
		
		if (!children.get(s).contains(t)) {
			children.get(s).add(t);
			totalEdges++;
		}
		
		
		if (!parents.containsKey(t))
			parents.put(t,  new LinkedList<Integer>());
		if (!parents.get(t).contains(s))
			parents.get(t).add(s);
	}
	
	/**
	 * Calculate the indegree and outdegree
	 */
	public void calculateDegree() {
		outdegree.clear();
		for (Integer i : children.keySet()) {
			outdegree.put(i, children.get(i).size());
		}
		indegree.clear();
		for (Integer i : parents.keySet()) {
			indegree.put(i, parents.get(i).size());			
		}		
	}
	
	/**
	 * Print the structure of the graph
	 */
	public void print() {
		for (Integer i : children.keySet()) {
			System.out.print(i + ": ");
			for (Integer j : children.get(i)) {
				System.out.print(j + " ");
			}
			System.out.println();
		}
	}
	
	
	/**
	 * Constructor
	 */
	public Graph() {
		
	}
	
	/**
	 * The copy constructor
	 * @param another
	 */
	public Graph(Graph another) {
		//Attributes
		for (Integer i : another.attributes.keySet()) {
			attributes.put(i, new LinkedList<String>());
			for (String s : another.attributes.get(i)) {
				attributes.get(i).add(s);
			}
		}
		
		//Primary Attribute
		for (Integer i : another.primaryAttribute.keySet()) {
			primaryAttribute.put(i, another.primaryAttribute.get(i));
		}
		
		totalEdges = 0;
		
		//Children		
		for (Integer i : another.children.keySet()) {
			children.put(i, new LinkedList<Integer>());
			for (Integer j : another.children.get(i)) {
				children.get(i).add(j);
				totalEdges++;
			}
		}
		
		//Parents
		for (Integer i : another.parents.keySet()) {
			parents.put(i, new LinkedList<Integer>());
			for (Integer j : another.parents.get(i))
				parents.get(i).add(j);
		}
		
		graphLoaded = true;
		
		calculateDegree();
	}

	@Override
	public int compareTo(Graph arg0) {
		// TODO Auto-generated method stub
		if (this.totalEdges > arg0.totalEdges)
			return -1;
		else if (this.totalEdges < arg0.totalEdges)
			return 1;
		else
			return 0;
			
	
	}
}
