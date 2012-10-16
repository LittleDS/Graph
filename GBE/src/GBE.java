import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

/**
 * The headquarters class
 * @author Administrator
 *
 */
public class GBE {

	//The input for this class
	Joints jointsIndex = new Joints();
	GRAIL grailIndex = new GRAIL();
	KReach kreachIndex = new KReach();
	Graph dataGraph = new Graph();
		
	//Mark the visited vertices
	HashSet<Integer> visited = new HashSet<Integer>();

	//Store all the super edges
	LinkedList<String> superEdges = new LinkedList<String>();

	public void Query(String fileName) throws FileNotFoundException {
		List<Graph> sG =  DivideGraph(fileName);
		SubQuery sQ = new SubQuery(dataGraph, sG, jointsIndex);
		HashMap<Graph, LinkedList<MatchedCandidates>> subResult = sQ.Execute();
		
	}
	
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
		
		sc.close();
		
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

	
	/**
	 * Using flood fill to get all the connected components
	 * @param g
	 * @return
	 */
	public void FloodFill(Integer source, Graph re, Graph g) {
		//Mark the vertex as visited
		visited.add(source);

		//Visit every child
		for (Integer i : g.children.get(source)) 
			if (!visited.contains(i))
			{
				re.addEdge(source, g.attributes.get(source), i, g.attributes.get(i));
				
				//DFS
				FloodFill(i, re, g);
			}
	}
	
	/**
	 * The node in the queue for BFS 
	 * We need to record the depth of each vertex in the queue
	 * Therefore, we have to make this class
	 */
	class QueueNode {
		Integer VertexID;
		int depth;
		
		QueueNode parent;
		QueueNode child;
		
		QueueNode(Integer id, int d) {
			VertexID = id;
			depth = d;
		}		
	}
	
	/**
	 * Given two vertices in the data graph, this method build all the paths between these
	 * two vertices which satisfy the condition
	 * @throws Exception 
	 */
	public void BFSBuildPath(Integer source, Integer target, int length) throws Exception {
		//If the source vertex cannot reach the target vertex, halt immediately
		if (!grailIndex.CheckContainment(source, target))
			return;
		
		//Use KReach Index to determine the distance between the two vertices
		if (!kreachIndex.query(source, target)) 
			return;

		//If the two vertices pass the first two exams, we can start to BFS
		//In order to improve the performance, we use bidirectional BFS
		Queue<QueueNode> forward = new LinkedList<QueueNode>();
		Queue<QueueNode> backward = new LinkedList<QueueNode>();
		
		int forwardSteps = length / 2;
		int backwardSteps = length - forwardSteps;

		//Initialize the two queues
		QueueNode s = new QueueNode(source, 0);
		QueueNode t = new QueueNode(target, 0);
		forward.add(s);
		backward.add(t);
		
		HashSet<Integer> visited = new HashSet<Integer>();
		
		while (forward.size() > 0 || backward.size() > 0) {
			//Each time we pop a node from the forward queue and backward queue
			QueueNode head = forward.poll();
			visited.add(head.VertexID);
			if (head.depth + 1 <= forwardSteps) {
				for (Integer i : dataGraph.children.get(head.VertexID)) {
					if (!visited.contains(i)) {
						QueueNode temp = new QueueNode(i, head.depth + 1);
						temp.parent = head;
						forward.add(temp);
					}
				}
			} 
			else {  //The end vertex
				
			}
			
			QueueNode tail = backward.poll();
			visited.add(tail.VertexID);
			if (tail.depth + 1 <= backwardSteps) {
				for (Integer i : dataGraph.parents.get(tail.VertexID)) {
					if (!visited.contains(i)) {
						QueueNode temp = new QueueNode(i, tail.depth + 1);
						temp.child = tail;								
						backward.add(temp);
					}
				}
			}
			else {  //The end vertex
				
			}
		}
		
	}
	
}
