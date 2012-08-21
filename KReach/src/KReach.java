import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * Copyright reserved by Lei Yang @ Case Western Reserve University
 * August 15th, 2012
 */

public class KReach {
	//Since the attributes of each vertex don't have any contribution to the labeling, we can split them into a separate structure
	HashMap<Integer, List<String>> attributes = new HashMap<Integer, List<String>>();

	//The adjacency list of each vertex
	HashMap<Integer, List<Integer>> children = new HashMap<Integer, List<Integer>>();
	HashMap<Integer, List<Integer>> parents = new HashMap<Integer, List<Integer>>();
	int totalEdges = 0;
	
	HashMap<Integer, List<Integer>> childrenCopy = new HashMap<Integer, List<Integer>>();
	
	public static void main(String[] args) throws FileNotFoundException {
		KReach kr = new KReach();
		kr.Encode("graphexample.txt", 3);
	}

	/**
	 * 
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void Encode(String fileName, int K) throws FileNotFoundException {
		//The input file
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
		
		for (Integer i : children.keySet()) {
			int i1 = i;
			childrenCopy.put(i1, new LinkedList<Integer>());
			for (Integer j : children.get(i)) {
				int j1 = j;
				childrenCopy.get(i1).add(j1);
			}
		}
		
		//Calculat the vertex cover using the simplest method
		CalculateVertexCoverGreedy();
		
		//Using a K-hop breadth first search to build the index
		BFS(K);
		
		for (Integer i : FinalIndex.keySet()) {
			System.out.print(i + ":");
			for (IndexNode j : FinalIndex.get(i)) {
				System.out.print(" " + j.vertex + " " + j.distance);
			}
			System.out.println();
		}
	}

	//The vertex cover
	Set<Integer> VC = new HashSet<Integer>();
	
	/**
	 * Obtain the vertex cover using the simplest method
	 */
	public void CalculateVertexCoverSimple() {
		VC.clear();
		
		Random randomGenerator = new Random();
		//Get all the vertices
		LinkedList<Integer> vertices = new LinkedList<Integer>(children.keySet());
		
		//Mark if one vertex is used
		boolean[] used = new boolean[vertices.size()];
		
		//Randomly shuffle the vertices and it's equal to randomly pick up one vertex from the set
		Collections.shuffle(vertices);
		
		//The algorithm is straightforward.  We first choose a vertex and from the children of this vertex we choose the other one
		for (Integer i : vertices) {
			//If this vertex is used before, we can skip it
			if (used[i]) continue;
			
			//Mark it as used
			used[i] = true;
			
			//Get the children of the first vertex
			List<Integer> edges = children.get(i);
			
			//In case the vertex doesn't have any child, we skip it
			if (edges.size() == 0) continue;
			
			//Randomly choose one of its children
			Integer j = edges.get(randomGenerator.nextInt(edges.size()));
			used[j] = true;
			
			//Add the two vertices into the set
			VC.add(i);
			VC.add(j);
			
			//Remove the corresponding edges
			//First remove the children of these two vertices
			totalEdges -= children.get(i).size();
			children.remove(i);
			
			totalEdges -= children.get(j).size();			
			children.remove(j);
			
			//For every parent of the vertex, we remove the vertex from their list one by one
			if (parents.containsKey(i)) {				
				for (Integer k : parents.get(i)) {
					if (children.containsKey(k)) {
						children.get(k).remove(i);					
						totalEdges--;
						if (children.get(k).size() == 0)
							used[k] = true;
					}
				}
			}
			if (parents.containsKey(j)) {
				for (Integer k : parents.get(j)) {
					if (children.containsKey(k)) {
						children.get(k).remove(j);
						totalEdges--;
						if (children.get(k).size() == 0)
							used[k]= true;
					}
				}
			}
			
			//If the total number of edges left is zero, we're done
			if (totalEdges == 0)
				break;
		}		
	}
	
	public void CalculateVertexCoverGreedy() {
		VC.clear();

		//The purpose of this structure is to reduce the size of the candidate vertices set
		//Since each time we remove at least one vertex from this set
		//The performance should be increased by at least a factor of 2
		HashSet<Integer> candidateVertices = new HashSet<Integer>(children.keySet());
		
		//First calculate the degree of each vertex
		int[] degree = new int[children.keySet().size()];	
		for (Integer i: children.keySet()) {
			if (children.containsKey(i))
				degree[i] += children.get(i).size();
			if (parents.containsKey(i))			
				degree[i] += parents.get(i).size();
		}
		
		//Iteratively choose the vertex cover
		while (totalEdges > 0) {
		
			//Choose the one with maximum degree
			int maximumDegree = 0;
			Integer chosenVertex = null;
			for (Integer i : candidateVertices) {
				if (degree[i] > maximumDegree) {
					maximumDegree = degree[i];
					chosenVertex = i; 
				}
			}
			
			//Add the chosen vertex into the vertex cover set
			VC.add(chosenVertex);
			
			//Modify the degree of that vertex to 0, because we will remove all the edges attaching to it
			degree[chosenVertex] = 0;			
			
			//Also remove that vertex from the candidate sets
			candidateVertices.remove(chosenVertex);
			
			//Remove all the children edges and also update the degree of those children
			if (children.containsKey(chosenVertex)) {
				//Update the total number of edges first
				totalEdges -= children.get(chosenVertex).size();
				
				//Update the degree of those children
				for (Integer j : children.get(chosenVertex)) {
					degree[j]--;
					if (degree[j] == 0)
						candidateVertices.remove(j);
				}
				
				//Remove all those children edges
				children.remove(chosenVertex);				
			}
			
			//Remove all the parents edges and also update the degree
			if (parents.containsKey(chosenVertex)) {
				
				//For each parent
				for (Integer j : parents.get(chosenVertex))
				
					//Check whether this parent has been processed before
					if (children.containsKey(j) ) {
					
						//From the children list, remove that chosen vertex
						children.get(j).remove(chosenVertex);
						
						//Also update the degree of that parent and the total number of edges0
						degree[j]--; 						
						totalEdges--;
						
						//If the total degree of that parent becomes 0, then we can remove it from the candidate set
						if (degree[j] == 0)
							candidateVertices.remove(j);							
					}
			}			
		}
	}

	//This class is used specifically for the node in the breadth first search queue
	class QueueNode {
		Integer vertex;
		int depth;
		public QueueNode(Integer v, int d) {
			vertex = v;
			depth = d;
		}
	}	
	
	class IndexNode implements Comparable<IndexNode>{
		Integer vertex;
		int distance;
		public IndexNode(Integer v, int d) {
			vertex = v;
			distance = d;
		}
		
		@Override
		public int compareTo(IndexNode o) {
			return this.vertex.compareTo(o.vertex);
		}
	}
	
	HashMap<Integer, List<IndexNode>> FinalIndex = new HashMap<Integer, List<IndexNode>>();
	
	public void BFS(int K) {
		//For each vertex in the vertex cover, we build a list to record the vertices that can be reached in K hops
	
		for (Integer i: VC) {
			//Initialize the index storage
			FinalIndex.put(i, new LinkedList<IndexNode>());
			
			//Initialize the breadth first search queue
			Queue<QueueNode> q = new LinkedList<QueueNode>();
		
			//We start from depth 0
			QueueNode startNode = new QueueNode(i, 0);
			
			q.add(startNode);
			
			//The total number of vertices is equal to the size of the attributes map
			//Because each vertex must have at least one attribute entry
			boolean[] firstVisit = new boolean[attributes.keySet().size()];
			firstVisit[startNode.vertex] = true;				
			
			while (q.size() > 0) {
				QueueNode current = q.poll();
				int currentDepth = current.depth;
				
				FinalIndex.get(i).add(new IndexNode(current.vertex, currentDepth));
				
				if (currentDepth < K && 
						childrenCopy.containsKey(current.vertex)) {
					for (Integer j: childrenCopy.get(current.vertex)) {
						if (!firstVisit[j])
							q.add(new QueueNode(j,currentDepth + 1));
							firstVisit[j] = true;
					}				
				}
			}
		
			Collections.sort(FinalIndex.get(i));
		}
	}
}

