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
	public static void main(String[] args) throws IOException {
		GRAIL t = new GRAIL();
		t.Encoding("Data.txt", 1);
		t.outputToFile("index.txt");
		System.out.println("Done.");
	}
	
	Graph graph = new Graph();

	//The final labeling result
	HashMap<Integer, List<Integer>> intervalLabel = new HashMap<Integer, List<Integer>>();

	//Initialize the visited array
	HashSet<Integer> visited = new HashSet<Integer>();

	/**
	 * Given a graph file, this method build the interval labeling index
	 * using the post-order traversal 
	 * @param D is the number of different interval labelings  
	 * @throws IOException 
	 */
	public void Encoding(String fileName, int D) throws IOException {
	
		graph.loadGraphFromFile(fileName);
			
		//The roots are those vertices with 0 in-degree
		LinkedList<Integer> roots = new LinkedList<Integer>();

		for (Integer i : graph.indegree.keySet()) {
			if (graph.indegree.get(i) == 0)
				roots.add(i);
		}
		
		//There is a chance that every vertex has incoming edge, so there is no root
		if (roots.size() == 0) {
			Integer[] l = new Integer[graph.attributes.keySet().size()];
			graph.attributes.keySet().toArray(l);
			
			//Add the first element as the root
			roots.add(l[0]);
		}
				
		//Initialize the final result
		for (Integer j : graph.attributes.keySet())
			intervalLabel.put(j, new LinkedList<Integer>());
				
		//Use DFS to label the graph
		for (int i = 0; i < D; i++) {
			HashMap<Integer, Integer> postOrder = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> lowestOrder = new HashMap<Integer, Integer>();
			
			//Reset r for each different interval labeling
			int r = 1;
			
			//Reset the visited array
			visited.clear();
			
			//Randomly shuffle the children first
			for (Integer c : graph.children.keySet())
				Collections.shuffle(graph.children.get(c));
				
			//randomly shuffle the roots
			Collections.shuffle(roots);
				
			//Since there could be multiple roots in the graph
			//we need to label them all
			for (Integer j : roots) {
				//Initialize the stack for the depth first search
				Stack<Integer> s = new Stack<Integer>();
				//Push the root into the stack
				s.push(j);
				
				while (!s.empty()) {
					//Check the top element without picking it out
					Integer current = s.peek();
					
					//If this vertex has been visited before
					//It means we come back to it
					if (visited.contains(current)) {						
						//It means that all the children of the current vertex are visited
						postOrder.put(current, r);

						//Update the rank
						r++;
						
						//Get the lowest rank from the descdents
						int min = postOrder.get(current);
						
						for (Integer ch : graph.children.get(current)) {
							//There might be cycle in the graph
							//That's why we need to check whether the lowest rank of a child already exists
							if (lowestOrder.get(ch) != null && lowestOrder.get(ch) < min)
									min = lowestOrder.get(ch);							
						}
						
						//Add the lowest rank
						lowestOrder.put(current, min);
				
						//Pop this vertex out from the stack, it's useless now
						s.pop();
					}
					else 
						{ 
							//If it's not visited, we mark it
							visited.add(current);
								
							//Sometimes the current vertex indeed has children, but the children are already visited
							//So we need to set up a contribute marker to indicate whether the current vertex has any useful children
							boolean contribute = false;
							
							if (graph.children.containsKey(current) && graph.children.get(current).size() > 0) {
								for (Integer ch : graph.children.get(current))
									if (!visited.contains(ch)) {
										//If some of its children are pushed into the stack, then it's useful
										s.push(ch);
										contribute = true;
									}
							}

							//If it doesn't contribute, then we update the order and pop it out
							if (!contribute) {
								postOrder.put(current, r);
								lowestOrder.put(current, r);
								r++;						
								s.pop();
							}
						}
				}
			}
			
			for (Integer j : graph.attributes.keySet()) {
				intervalLabel.get(j).add(lowestOrder.get(j));
				intervalLabel.get(j).add(postOrder.get(j));
			}
		}		
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
		tScanner.close();
	}

	/**
	 * This method check whether the interval label of y is contained in x
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean CheckContainment(Integer x, Integer y) {
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
