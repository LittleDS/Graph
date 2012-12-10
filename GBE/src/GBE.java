import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import javax.naming.spi.DirStateFactory.Result;

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
	NeighborHood nh = new NeighborHood();
	
	//Mark the visited vertices
	HashSet<Integer> visited = new HashSet<Integer>();

	//Store all the super edges
	LinkedList<SuperEdge> superEdges = new LinkedList<SuperEdge>();
	
	public static void main(String[] args) throws Exception {
		GBE test = new GBE();
		test.dataGraph.loadGraphFromFile("datagraph.txt");
		test.jointsIndex.Encode("datagraph.txt");
		test.Query("querypattern.txt");
	}
	
	public void Query(String fileName) throws Exception {
		//Divide the query pattern into subgraphs and super edges
		List<Graph> sG =  DivideGraph(fileName);
		
		//Sort the graph list such that larger graph is going to be tested earlier
		Collections.sort(sG);
		
//		for (SuperEdge se : superEdges)
//			System.out.println(se.source + " " + se.target + " " + se.length);
//		
//		for (Graph i : sG) {
//			i.print();
//			System.out.println();
//		}

		//Get the matching candidates for each subgraph
		SubQuery sQ = new SubQuery(dataGraph, sG, jointsIndex, nh);
		HashMap<Graph, LinkedList<MatchedCandidates>> subResult = sQ.Execute();
		
//		for (Graph i : subResult.keySet()) {
//			System.out.println("The components:");
//			i.print();
//			System.out.println("The matchings:");
//			
//			for (MatchedCandidates c : subResult.get(i)) {
//				c.Print();
//				System.out.println("~~~~~~~~~~~~~~~");
//			}
//		
//			System.out.println("<--------------->");
//		}		

		jointsIndex = null;
		
		//Determine the components on each super edge
		for (SuperEdge se : superEdges) {
			//Currently use this two marker to check errors
			boolean sourceComponentAssigned = false;
			boolean targetComponentAssigned = false;
			
			for (Graph g : sG) {
				if (g.attributes.containsKey(se.source)) {
					if (!sourceComponentAssigned) {
						sourceComponentAssigned = true;
						se.sourceComponent = g;
					}
					else
						System.out.println("Error!");
				}
				
				if (g.attributes.containsKey(se.target)) {
					if (!targetComponentAssigned) {
						targetComponentAssigned = true;
						se.targetComponent = g;
					}
					else {
						System.out.println("Error!");
					}
				}
			}
		}
				
		while (superEdges.size() > 0) {
			//First check whether there is any super edge connecting the same component now
			for (SuperEdge se : superEdges) {
				if (se.sourceComponent.equals(se.targetComponent)) {
					LinkedList<MatchedCandidates> mc = subResult.get(se.sourceComponent);
					ProcessFirstCategorySE(se, mc);			
				}				
			}
			
			Iterator<SuperEdge> it = superEdges.iterator();
			while (it.hasNext()) {
				SuperEdge t = it.next();
				if (t.processed)
					it.remove();
			}
			
			
			
			
		}
	}
	
	/**
	 * This method is used to process the first category super edges
	 * The main function of this method is to prune the false positive matching candidate using the paths info
	 * @param se
	 * @param mc
	 * @throws Exception
	 */
	public void ProcessFirstCategorySE(SuperEdge se, LinkedList<MatchedCandidates> mc) throws Exception {
		Iterator<MatchedCandidates> it = mc.iterator();
		if (it != null) {
			while (it.hasNext()) {
				MatchedCandidates ca = it.next();
				Integer sourceInData = ca.mapping.get(se.source);
				Integer targetInData = ca.mapping.get(se.target);
				List<String> pathReturned = BFSBuildShortestPath(sourceInData, targetInData, se.length);
				if (pathReturned.size() > 0) {
					ca.paths.put(sourceInData + " " + targetInData, pathReturned);
				}
				else {
					it.remove();
				}
			}					
		}
		se.processed = true;		
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
			g.parents.put(ID, new LinkedList<Integer>());
			
			if (s.length > 1) {//If the current vertex has at least one child, the length should be at least 2
				for (int i = 0; i < s.length; i += 2) {
					int l = Integer.parseInt(s[i + 1]);
					
					if (l > 1) {  //If current edge is a super edge
						Integer t = Integer.parseInt(s[i]);
						superEdges.add(new SuperEdge(ID, t, l));
					} 
					else { //normal edge
						int tN = Integer.parseInt(s[i]);
						
						g.children.get(ID).add(tN);

						//Also for the parents
						if (!g.parents.containsKey(tN))
							g.parents.put(tN, new LinkedList<Integer>());
						g.parents.get(tN).add(ID);												
					}
				}
			}
		}
		
		sc.close();
		
		//The return result
		List<Graph> re = new LinkedList<Graph>();
		
		//Start from each vertex to floodfill
		for (Integer i : g.children.keySet()) {
			if (!visited.contains(i) && (g.children.get(i).size() > 0 || g.parents.get(i).size() > 0)) {				
				Graph in = new Graph();

				//DFS
				FloodFill(i, in, g);
				
				//Calculate the degree first
				in.calculateDegree();
				
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
		visited.add(source);
		
		//Visit every child
		for (Integer i : g.children.get(source)) {
			re.addEdge(source, g.attributes.get(source), i, g.attributes.get(i));
			if (!visited.contains(i)) {				
				//DFS
				FloodFill(i, re, g);
			}			
		}

		//Visit every parent
		for (Integer i : g.parents.get(source)) {
			re.addEdge(i, g.attributes.get(i), source, g.attributes.get(source));
			if (!visited.contains(i)){				
				FloodFill(i, re, g);				
			}			
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
		
		QueueNode pathParent;
		QueueNode pathChild;
		
		QueueNode(Integer id, int d) {
			VertexID = id;
			depth = d;
		}		
	}
	
	public List<String> BFSBuildShortestPath(Integer source, Integer target, int length) throws Exception {
		List<String> re = new LinkedList<String>();
		
//		//If the source vertex cannot reach the target vertex, halt immediately
//		if (!grailIndex.CheckContainment(source, target))
//			return;
//		
//		//Use KReach Index to determine the distance between the two vertices
//		if (!kreachIndex.query(source, target)) 
//			return;

		//If the two vertices pass the first two exams, we can start to BFS
		//In order to improve the performance, we use bidirectional BFS
		Queue<QueueNode> forward = new LinkedList<QueueNode>();
		Queue<QueueNode> backward = new LinkedList<QueueNode>();
		
		//Determine the forward steps and backward steps
		int forwardSteps = length / 2;
		int backwardSteps = length - forwardSteps;

		//Initialize the two queues
		QueueNode s = new QueueNode(source, 0);
		QueueNode t = new QueueNode(target, 0);
		
		forward.add(s);
		backward.add(t);
		
		HashMap<Integer, List<QueueNode>> forwardVisited = new HashMap<Integer, List<QueueNode>>();
		HashMap<Integer, List<QueueNode>> backwardVisited = new HashMap<Integer, List<QueueNode>>();
		
		forwardVisited.put(s.VertexID, new LinkedList<QueueNode>());
		forwardVisited.get(s.VertexID).add(s);
		
		backwardVisited.put(t.VertexID, new LinkedList<QueueNode>());
		backwardVisited.get(t.VertexID).add(t);
		
		while (forward.size() > 0 || backward.size() > 0) {
			
			//Each time we pop a node from the forward queue and backward queue
			if (forward.size() > 0) {
				QueueNode head = forward.poll();										
				
				if (head.depth + 1 <= forwardSteps && !backwardVisited.containsKey(head.VertexID)) {
				
					for (Integer i : dataGraph.children.get(head.VertexID)) {
						boolean flag = false;
						QueueNode temp = new QueueNode(i, head.depth + 1);
						temp.pathParent = head;
						
						//Indicate whether we have found a new path
						boolean fVFlag = false;
						
						if (!forwardVisited.containsKey(i)) {
							forwardVisited.put(i, new LinkedList<QueueNode>());
							forwardVisited.get(i).add(temp);
							fVFlag = true;
						} 
						else {
							//Get the first element of the list
							//Since the depth of all the elements in the list should be the same
							//We can use the first element as the representative
							Iterator<QueueNode> it = forwardVisited.get(i).iterator();
							QueueNode first = it.next();
							
							//The depth of the current element should be equal to head.depth + 1
							//If the depth is equal to the shortest one, we add this node to the list
							//However, since this node is already visited before, we don't add it to the BFS queue
							//Otherwise, we will expand the same vertex multiple times
							if (head.depth + 1 == first.depth) {
								forwardVisited.get(i).add(temp);
								fVFlag = true;
							}
						}
						
						if (backwardVisited.containsKey(i) && fVFlag) {
							flag = true;
							
							//We have found a intersection vertex
							List<QueueNode> tempList = backwardVisited.get(i);
							
							//A temp node used to recursively get the path from source to the current vertex
							QueueNode tempHead = head;
							
							Stack<Integer> verticesOnPath = new Stack<Integer>();
							
							while (tempHead.pathParent != null) {
								verticesOnPath.push(tempHead.VertexID);						
								tempHead = tempHead.pathParent;						
							}
							verticesOnPath.push(tempHead.VertexID);
							
							StringBuffer path = new StringBuffer();

							//Have to reverse the path
							while (!verticesOnPath.empty()) {
								path.append(verticesOnPath.pop());
								path.append(',');
							}
							
							for (QueueNode q : tempList) 
								if (head.depth + 1 + q.depth <= length)
								{
									StringBuffer emptyString = new StringBuffer();
									QueueNode tempTail = q;
									while (tempTail.pathChild != null) {
										emptyString.append(tempTail.VertexID);
										emptyString.append(',');
										tempTail = tempTail.pathChild;
									}
									emptyString.append(tempTail.VertexID);
									
									//System.out.println(path.append(emptyString).toString());
									re.add(path.append(emptyString).toString());
								}
						}				
						
						if (!flag)
							forward.add(temp);
						
						
					}
				}
				
			}
			
			//The backward search
			if (backward.size() > 0) {
				QueueNode tail = backward.poll();		
				
				if (tail.depth + 1 <= backwardSteps && !forwardVisited.containsKey(tail.VertexID)) {
					for (Integer i : dataGraph.parents.get(tail.VertexID)) {
						boolean flag = false;
						//Find a path
						
						QueueNode temp = new QueueNode(i, tail.depth + 1);
						temp.pathChild = tail;
						
						//Indicate whether we have found a new path
						boolean fVFlag = false;
						
						if (!backwardVisited.containsKey(i)) {
							backwardVisited.put(i, new LinkedList<QueueNode>());
							backwardVisited.get(i).add(temp);
							fVFlag = true;
						}
						else {
							Iterator<QueueNode> it = backwardVisited.get(i).iterator();
							QueueNode first = it.next();
							
							if (tail.depth + 1 == first.depth) {
								backwardVisited.get(i).add(temp);
								fVFlag = true;
							}
						}

						if (forwardVisited.containsKey(i) && fVFlag) {
							flag = true;
							List<QueueNode> tempList = forwardVisited.get(i);

							QueueNode tempTail = tail;
							
							StringBuffer path = new StringBuffer();
							
							while (tempTail.pathChild != null) {
								path.append(tempTail.VertexID);
								path.append(',');
								tempTail = tempTail.pathChild;
							}
							path.append(tempTail.VertexID);
							
							for (QueueNode q : tempList)
								if (q.depth + tail.depth + 1 <= length) {
									
									QueueNode tempHead = q;
									
									Stack<Integer> backPath = new Stack<Integer>();
									
									while (tempHead.pathParent != null) {
										backPath.push(tempHead.VertexID);
										tempHead = tempHead.pathParent;
									}
									
									backPath.push(tempHead.VertexID);
									
									StringBuffer emptyString = new StringBuffer();
									
									while (!backPath.empty()) {
										emptyString.append(backPath.pop());
										emptyString.append(',');
									}
									
									//System.out.println(emptyString.append(path).toString());
									re.add(emptyString.append(path).toString());
								}					
						}
						
						if (!flag)
							backward.add(temp);
					}
				}					
			}				
		}
		return re;
	}

	public  ArrayList<LinkedList<QueueNode>> ForwardBFS(Integer source, int depth) {
		ArrayList<LinkedList<QueueNode>> result = new ArrayList<LinkedList<QueueNode>>();
		
	}
	
	public BackwardBFS(Integer target, int depth) {
		
	}
}
