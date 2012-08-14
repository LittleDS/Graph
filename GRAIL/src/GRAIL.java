import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
/**
 * Copyright reserved by Lei Yang @ Case Western Reserve University
 * August 13th, 2012
 */
public class GRAIL {

	public static void main(String[] args) throws FileNotFoundException {		
		GRAIL tGrail = new GRAIL();
		tGrail.Encoding("graphexample.txt", 2);
		for (Integer i: tGrail.intervalLabel.keySet()) {
			System.out.print(i);
			for (Integer j: tGrail.intervalLabel.get(i))
				System.out.print(" " + j);
			System.out.println();
		}
		
	}

	//Since the attributes of each vertex don't have any contribution to the labeling, we can split them into a separate structure
	HashMap<Integer, List<String>> attributes = new HashMap<Integer, List<String>>();

	//The adjacency list of each vertex
	HashMap<Integer, List<Integer>> neighbors = new HashMap<Integer, List<Integer>>();
	
	//The final labeling result
	HashMap<Integer, List<Integer>> intervalLabel = new HashMap<Integer, List<Integer>>();
	
	/**
	 * Given a graph file, this method build the interval labeling index
	 * using the post-order traversal 
	 * @param D is the number of different interval labelings  
	 * @throws FileNotFoundException 
	 */
	public void Encoding(String fileName, int D) throws FileNotFoundException {
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
			
			//Process the neighbors
			strings = neighborLine.split(",");
			neighbors.put(ID, new LinkedList<Integer>());
			for (int i = 0; i < strings.length; i++) {
				int tN = Integer.parseInt(strings[i]);
				//If the neighbor ID is equal to -1, it means the current vertex doesn't have a neighbor
				if (tN != -1)
					neighbors.get(ID).add(tN);
			}			
		}
		tScanner.close();
		
		//Find all the roots
		int totalVertices = attributes.keySet().size();
		int[] inDegree = new int[totalVertices];
		for (int i = 0; i < totalVertices; i++) {
			for (Integer j : neighbors.get(i))
				inDegree[j]++;
		}
		//The roots are those vertices with 0 in-degree
		LinkedList<Integer> roots = new LinkedList<Integer>();
		for (int i = 0; i < totalVertices; i++)
			if (inDegree[i] == 0)
				roots.add(i);

		//Initialize the visited array
		visited = new boolean[totalVertices];
		
		//Initialize the final result
		for (int i = 0; i < totalVertices; i++)
			intervalLabel.put(i, new LinkedList<Integer>());
				
		//Use DFS to label the graph
		for (int i = 0; i < D; i++) {
			//Reset r for each different interval labeling
			r = 1;
			
			//Reset the visited array
			for (int j = 0; j < totalVertices; j++) visited[j] = false;

			//randomly shuffle the roots
			java.util.Collections.shuffle(roots);
			
			tempLabel = new int[totalVertices][2];
			for (int j = 0; j < totalVertices; j++) {
				tempLabel[j][0] = -1;
				tempLabel[j][1] = -1;
			}
			for (Integer j : roots) {
				DFSLabel(j);
				for (int k = 0; k < totalVertices; k++) {
					intervalLabel.get(k).add(tempLabel[k][0]);
					intervalLabel.get(k).add(tempLabel[k][1]);
				}
			}			
		}
	}

	
	//The array to indicate whether a vertex has been visited before
	boolean[] visited;
	
	//A temp array used to store one label set
	int[][] tempLabel;
	
	//The initial rank of vertex	
	int r = 1;
	/**
	 * The depth first search traversal method which is responsible for the labeling
	 * @param N
	 */
	public void DFSLabel(int x) {
		if (visited[x]) return;
		visited[x] = true;
		
		for (Integer i : neighbors.get(x))
			DFSLabel(i);
		
		int rc = Integer.MAX_VALUE;		
		for (Integer i : neighbors.get(x))
			if (tempLabel[i][0] != -1) {
				if (tempLabel[i][0] < rc)
					rc = tempLabel[i][0];
			}
				
		tempLabel[x][0] = min(rc, r);
		tempLabel[x][1] = r;
		
		//Increase the current rank
		r = r + 1;
	}

	/**
	 * Return the minimum of the two parameters
	 * @param a
	 * @param b
	 */
	public int min(int a, int b) {
		if (a <= b)
			return a;
		else
			return b;
	}
}
