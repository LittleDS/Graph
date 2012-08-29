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
	//Since the attributes of each vertex don't have any contribution to the labeling, we can split them into a separate structure
	HashMap<Integer, List<String>> attributes = new HashMap<Integer, List<String>>();

	//The adjacency list of each vertex
	HashMap<Integer, List<Integer>> children = new HashMap<Integer, List<Integer>>();
	HashMap<Integer, List<Integer>> parents = new HashMap<Integer, List<Integer>>();
	int totalEdges = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	/**
	 * Load the graph from a file in the disk
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void loadGraphFromFile(String fileName) throws FileNotFoundException {
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
	}
	
	public void Encode(String fileName) throws IOException {
		IndexVertices(fileName + "Vertices");
	}
	
	
	HashMap<String, List<Integer>> vertices = new HashMap<String, List<Integer>>();
	/**
	 * 
	 * @param fileName
	 * @throws IOException 
	 */
	public void IndexVertices(String fileName) throws IOException {
		//Inverted Index
		for (Integer i : attributes.keySet()) {
			
			//All the attributes that each vertex have
			List<String> strings = attributes.get(i);
			
			for (String s: strings) {
				if (!vertices.containsKey(s)) {
					vertices.put(s, new LinkedList<Integer>());
				}
				vertices.get(s).add(i);
			}
		}
		
		//Open the file
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);
		
		//Write the index into file
		for (String s : vertices.keySet()) {
			String in = s;
			for (Integer i : vertices.get(s)) {
				in += "," + i;
			}
			out.write(in + "\r\n");
		}
		//Write the index line by line
		out.close();			
	}
}
