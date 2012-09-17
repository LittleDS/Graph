import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
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
		IndexEdges(fileName + "Edges");
		IndexJoints(fileName + "Joints");
		
	}
	
	
	//The inverted index for vertices
	//Here I implement a two levels version
	HashMap<String, HashMap<String, List<Integer>>> verticesIndex = new HashMap<String, HashMap<String,List<Integer>>>();
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException 
	 */
	public void IndexVertices(String fileName) throws IOException {
		for (Integer i : attributes.keySet()) {
			
			//All the attributes that each vertex have
			List<String> strings = attributes.get(i);
			
			//The first level 
			for (String s: strings) {
				//Current attribute
				if (!verticesIndex.containsKey(s)) {
					verticesIndex.put(s, new HashMap<String, List<Integer>>());
				}
				
				//The second level
				for (String s1 : strings) {
					//Only index those attributes larger than the current attribute
					if (s1.compareTo(s) > 0) {
						if (!verticesIndex.get(s).containsKey(s1))
							verticesIndex.get(s).put(s1, new LinkedList<Integer>());
						
						//Insert into the index
						verticesIndex.get(s).get(s1).add(i);
					}
				}
			}
		}
		
		
		//Open the file
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);
		
		//Write the index into file
		for (String s : verticesIndex.keySet()) {
			
			//The second level of the index
			HashMap<String, List<Integer>> temp = verticesIndex.get(s);
			
			//For each attribute at the second level
			for (String s1 : temp.keySet()) {

				//The first two elements are the indexed attributes
				String in = s + "," + s1;

				//All the vertices that contains the two attributes
				for (Integer i : temp.get(s1)) {
					in += "," + i;
				}
				
				//Write into the file
				out.write(in + "\r\n");
				
			}
		}
		
		//dont' forget to close the stream
		out.close();			
	}
	

	HashMap<String, HashMap<String, List<Integer>>> edgesIndex = new HashMap<String, HashMap<String,List<Integer>>>();
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException 
	 */
	public void IndexEdges(String fileName) throws IOException {
	
		//For each vertex
		for (Integer i : children.keySet()) {
			
			//Get all the attributes for that vertex
			List<String> sAttributes = attributes.get(i);
			
			//In case that attribute is not in the index, we initialize it first
			for (String a : sAttributes)
				if (!edgesIndex.containsKey(a))
					edgesIndex.put(a, new HashMap<String, List<Integer>>());
			
			//All the edges starting from the same source
			List<Integer> childrenList = children.get(i);
			
			//For each edge with different target
			for (Integer j : childrenList) {
				
				//The attributes for the target vertex
				List<String> tAttributes = attributes.get(j);
				
				//Insert them into the index
				for (String k : sAttributes)
					for (String l : tAttributes) {
						HashMap<String, List<Integer>> temp = edgesIndex.get(k);
						if (!temp.containsKey(l))
							temp.put(l, new LinkedList<Integer>());
						
						temp.get(l).add(i);
						temp.get(l).add(j);
						
					}
			}
		}
		
		//Open the file
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);
		
		//Write the index into file
		for (String s : edgesIndex.keySet()) {
			
			HashMap<String, List<Integer>> temp = edgesIndex.get(s);
			
			for (String s1 : temp.keySet()) {

				String in = s + "," + s1;

				//All the vertices that contains the two attributes
				for (Integer i : temp.get(s1)) {
					in += "," + i;
				}
				
				//Write into the file
				out.write(in + "\r\n");
				
			}
		}
		
		//dont' forget to close the stream
		out.close();			
		
	}
	
	
	/**
	 * 
	 * @param fileName
	 */
	public void IndexJoints(String fileName) {
		
	}
}
