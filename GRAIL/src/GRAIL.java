import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Copyright reserved by Lei Yang @ Case Western Reserve University
 * August 13th, 2012
 */
public class GRAIL {
	Graph graph = new Graph();
	//The final labeling result
	HashMap<Integer, List<Integer>> intervalLabel = new HashMap<Integer, List<Integer>>();
	
	/**
	 * Given a graph file, this method build the interval labeling index
	 * using the post-order traversal 
	 * @param D is the number of different interval labelings  
	 * @throws IOException 
	 */
	public void Encoding(String fileName, int D) throws IOException {
		graph.loadGraphFromFile(fileName);
		
		//Find all the roots
		int totalVertices = graph.attributes.keySet().size();
		int[] inDegree = new int[totalVertices];
		for (int i = 0; i < totalVertices; i++) {
			for (Integer j : graph.children.get(i))
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
			Collections.shuffle(roots);
			
			//Initialize the array for current labels
			tempLabel = new int[totalVertices][2];			
			for (int j = 0; j < totalVertices; j++) {
				tempLabel[j][0] = -1;
				tempLabel[j][1] = -1;
			}
			
			//Since there could be multiple roots in the graph
			//we need to label them all
			for (Integer j : roots) {
				DFSLabel(j);
				
				//Assign the current label to the final result
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
		
		//Visit the children randomly
		Collections.shuffle(graph.children.get(x));
		for (Integer i : graph.children.get(x))
			DFSLabel(i);
		
		//Get the minimum rank value from the children of current vertex
		int rc = Integer.MAX_VALUE;		
		for (Integer i : graph.children.get(x))
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
	
	/**
	 * Output the index to a file for future use
	 * @param fileName
	 * @throws IOException 
	 */
	public void outputToFile(String fileName) throws IOException {
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);
		for (int i = 0; i < intervalLabel.keySet().size(); i++) {
			String t = i + "";
			for (Integer j : intervalLabel.get(i))
				t += "," + j;
			out.write(t + "\r\n");
		}
		out.close();		
	}
	
	/**
	 * Load the index from the file
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void loadIndexFromFile(String fileName) throws FileNotFoundException {
		File tFile = new File(fileName);
		Scanner tScanner = new Scanner(tFile);
		//Just in case
		intervalLabel.clear();
		
		while (tScanner.hasNext()) {
			//Read the next line
			String currentLine = tScanner.nextLine();

			//Split the integers
			String[] strings = currentLine.split(",");
			
			//Fetch the vertex ID
			int ID = Integer.parseInt(strings[0]);
			
			//Initialize the intervalLabel
			intervalLabel.put(ID, new LinkedList<Integer>());
			
			//Insert the labels into the structure
			for (int i = 1; i < strings.length; i++) {
				intervalLabel.get(ID).add(Integer.parseInt(strings[i]));
			}
			
		}		
	}

	/**
	 * This method check whether the interval label of y is contained in x
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean CheckContainment(int x, int y) {
		List<Integer> xL = intervalLabel.get(x);
		List<Integer> yL = intervalLabel.get(y);
		//Check the intervals one by one
		for (int i = 0; i < xL.size() / 2; i+=2) {
			//  If the only condition doesn't hold, we can return false directly
			if (!(xL.get(i) < yL.get(i) && xL.get(i + 1) > yL.get(i + 1)))
				return false;
		}
		return true;				
	}
	
}
