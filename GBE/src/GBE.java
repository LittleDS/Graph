import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * The headquarter class
 * @author Administrator
 *
 */
public class GBE {

	Joints jointsIndex = new Joints();
	GRAIL grailIndex = new GRAIL();
	KReach kreachIndex = new KReach();
	Graph dataGraph = new Graph();
	
	public void Query(String fileName) throws FileNotFoundException {
		List<Graph> sG =  DivideGraph(fileName);
		SubQuery sQ = new SubQuery(dataGraph, sG, jointsIndex);
		sQ.Execute();
	}
	
	LinkedList<String> superEdges = new LinkedList<String>();
	/**
	 * This method is used to remove the super edge from the pattern
	 * Store the necessary information for rebuilding the graph
	 * @throws FileNotFoundException 
	 */
	public List<Graph> DivideGraph(String fileName) throws FileNotFoundException {
		Graph g = new Graph();
		
		//Since the format of graph pattern is different from data graph
		//We still need to load it from file first
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		while (sc.hasNext()) {
			//Get the ID and attributes line
			String IdLine = sc.nextLine();

			//Decode it
			String[] s = IdLine.split(",");
			
			//Parse the ID first
			Integer ID = Integer.parseInt(s[0]);
			
			//Initialize the attributes list
			g.attributes.put(ID, new LinkedList<String>());
			
			//Insert all the attributes
			for (int i = 1; i < s.length; i++)
				g.attributes.get(ID).add(s[i]);
			
			//Set the primary attribute
			if (s.length > 1) {
				g.primaryAttribute.put(ID, s[1]);
			}
			else
				g.primaryAttribute.put(ID, s[0]);
			
			//Process the neighbors
			String neighborLine = sc.nextLine();
			
			//Decode it
			s = neighborLine.split(",");
			
			//Initialize the children adjacency list
			g.children.put(ID, new LinkedList<Integer>());
			
			if (s.length > 1) {//If the current vertex has at least one child, the length should be at least 2
				for (int i = 0; i < s.length; i += 2) {
					int l = Integer.parseInt(s[i + 1]);
					
					if (l > 1) {  //If current edge is a super edge
						Integer t = Integer.parseInt(s[i]);
						superEdges.add(ID + "," + t + "," + l);
					} 
					else { //normal edge
						g.children.get(ID).add(Integer.parseInt(s[i]));
					}
				}
			}
		}
		
		//The return result
		List<Graph> re = new LinkedList<Graph>();
		
		//Start from each vertex to floodfill
		for (Integer i : g.children.keySet()) {
			if (!visited.contains(i) && g.children.get(i).size() > 0) {				
				Graph in = new Graph();

				//DFS
				FloodFill(i, in, g);
				
				//Push into the return list
				re.add(in);
			}
		}
		
		return re;
	}
	
	//Mark the visited vertices
	HashSet<Integer> visited = new HashSet<Integer>();
	
	/**
	 * Using flood fill to get all the connected components
	 * @param g
	 * @return
	 */
	public void FloodFill(Integer source, Graph re, Graph g) {
		visited.add(source);
		for (Integer i : g.children.get(source)) 
			if (!visited.contains(i))
			{
				re.addEdge(source, g.attributes.get(source), i, g.attributes.get(i));
				FloodFill(i, re, g);
			}
	}
	
}
