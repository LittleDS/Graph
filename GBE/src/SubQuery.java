import java.io.IOException;
import java.util.*;

/**
 * This is the class to solve the subgraph query problem
 * Each subgraph query is a common graph matching problem
 * Especially that each subgraph should be quite small
 * @author Administrator
 *
 */
public class SubQuery {
	Graph dataGraph;
	List<Graph> subGraphs;
	Joints index;
	NeighborHood nhindex;
	
	public static void main(String[] args) throws IOException {		

		Joints j = new Joints();
		j.loadEdgeIndexFromFile("datagraph.txtEdges");
		j.loadJointIndexFromFile("datagraph.txtJoints");
				
		System.out.println("Finish Loading Index....");
		
		Graph d = new Graph();
		d.loadGraphFromFile("datagraph.txt");

		System.out.println("Finish Loading Data Graph....");
		
		Graph t = new Graph();
		t.loadGraphFromFile("q1.txt");
		
		System.out.println("Finish Loading Query Pattern....");
		
		NeighborHood n = new NeighborHood();
		n.Encode(d);
		
		System.out.println("Finish Building NeighborHood Index");
		
		List<Graph> listGraph = new LinkedList<Graph>();
		listGraph.add(t);
						
		
		SubQuery sq = new SubQuery(d, listGraph, j, n);		
		
		System.out.println("Start Query Matching...");
		
		long startTime = System.nanoTime();
		HashMap<Graph, LinkedList<MatchedCandidates>> result = sq.Execute();
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
				
		System.out.println(duration + " nanosecond");
		for (Graph i : result.keySet()) {
			i.print();
			System.out.println("Total Matches: " + result.get(i).size());
			
			for (MatchedCandidates c : result.get(i)) {
				c.Print();
				System.out.println("~~~~~~~~~~~~~~~");
			}
			
			System.out.println("<--------------->");
		}
	}
	
	/**
	 * The constructor class
	 * @param d the data graph
	 * @param l the subgraphs
	 * @param i the joints index for data graph
	 */
	public SubQuery(Graph d, List<Graph> l, Joints i, NeighborHood nh) {
		dataGraph = d;
		subGraphs = l;
		index = i;
		nhindex = nh;
	}
	
	/**
	 * Execute the query to return all the matchings
	 * @return
	 */
	public HashMap<Graph, LinkedList<MatchedCandidates>> Execute() {
		HashMap<Graph, LinkedList<MatchedCandidates>> r = new HashMap<Graph, LinkedList<MatchedCandidates>>();
		for (Graph g : subGraphs) {
			LinkedList<MatchedCandidates> gr = Query(SortComponents(Divide(g)), g);
			//If any component doesn't have match, the algorithm is terminated
			if (gr.size() > 0)				
				r.put(g, gr);
			else {
				r.clear();
				break;
			}
		}
		return r;
	}
	
	/**
	 * Divide the graph into joints and edges
	 * @param g
	 * @return
	 */
	public List<String> Divide(Graph input) {
		List<String> result = new LinkedList<String>();
				
		//First determine those vertices with both indegree and outdegree
		HashSet<Integer> cand = new HashSet<Integer>();		
		for (Integer i : input.attributes.keySet()) {
			if (input.outdegree.containsKey(i) && input.indegree.containsKey(i) 
			 && input.outdegree.get(i) > 0 && input.indegree.get(i) > 0) {
				cand.add(i);
			}
		}
		
		//Make a copy
		Graph g = new Graph(input);

		while (true) {
			boolean updated = false;
			int min = Integer.MAX_VALUE;			
			Integer jointParent = -1, jointChild = -1, jointCore = -1;
			
			for (Integer i : cand) {
				List<Integer> parentsList = g.parents.get(i);
				List<Integer> childrenList = g.children.get(i);
				
				for (Integer p : parentsList) {
					for (Integer c : childrenList) {
						
						//Get the attributes
						String j1 = g.primaryAttribute.get(p);
						String j2 = g.primaryAttribute.get(i);
						String j3 = g.primaryAttribute.get(c);
						
						List<Integer> triples = index.jointsIndex.get(j1).get(j2).get(j3);
												
						if (triples != null && triples.size() < min) {
							min = triples.size();
							jointParent = p;
							jointChild = c;
							jointCore = i;							
						} 
						else if (triples == null) {
							result.clear();
							return result;
						}
					}
				}
			}			
			
			if (min != Integer.MAX_VALUE && jointParent != -1) {
				updated = true;
				result.add(jointParent + "," + jointCore + "," + jointChild);
				g.children.get(jointCore).remove(jointChild);
				g.children.get(jointParent).remove(jointCore);
				g.parents.get(jointCore).remove(jointParent);
				g.parents.get(jointChild).remove(jointCore);				
				
			}
			if (!updated)
				break;
		}
		
		for (Integer i : g.children.keySet()) {
			List<Integer> childrenList = g.children.get(i);
			if (childrenList.size() > 0) {
				for (Integer j : childrenList) {
					result.add(i + "," + j);
				}
			}
		}
		return result;		
	}

	
	/**
	 * Sort the components
	 * @param components
	 * @return
	 */
	public List<String> SortComponents(List<String> components) {
		//We need to determine an order of those components first
		//Because we have to make sure the next component has at least one intersection with the previous components
		List<String> componentsInOrder = new LinkedList<String>();		
		
		if (components.size() == 0)
			return componentsInOrder;
		
		//First insert an initial component
		componentsInOrder.add(components.get(0));
		components.remove(0);

		//Make sure we don't leave any component
		while (components.size() > 0) {
			boolean match = false;
			
			for (int i = 0; i < componentsInOrder.size(); i++) {
				String[] ids1 = componentsInOrder.get(i).split(",");
				
				for (int j = 0; j < components.size(); j++) {
					String[] ids2 = components.get(j).split(",");
				
					for (int k = 0; k < ids1.length; k++)
						for (int l = 0; l < ids2.length; l++) {
							if (ids1[k].equals(ids2[l])) {
								match = true;								
								break;
							}
						}
					
					if (match) {			
						componentsInOrder.add(components.get(j));
						components.remove(j);
						break;
					}
				}
				
				if (match) {
					break;
				}
				
			}
		}
		return componentsInOrder;
	}
	
	/**
	 * Using the index to query the data graph
	 * @param components already sorted
	 * @param subGraph
	 * @return
	 */
	public LinkedList<MatchedCandidates> Query(List<String> components, Graph subGraph) {
		//The result to be returned
		LinkedList<MatchedCandidates> result = new LinkedList<MatchedCandidates>();
		
		//The neighborhood index
		NeighborHood localnh = new NeighborHood();
		localnh.Encode(subGraph);
		
		for (String s : components) {
			String[] ids = s.split(",");			

			Integer[] idsInteger = new Integer[ids.length];
			String[] attributeString = new String[ids.length];
			
			for (int i = 0; i < ids.length; i++) {
				idsInteger[i] = Integer.parseInt(ids[i]);
				attributeString[i] = subGraph.primaryAttribute.get(idsInteger[i]);
			}
			
			
			LinkedList<ArrayList<Integer>> lists = new LinkedList<ArrayList<Integer>>();
			
			if (ids.length == 3) {//Joint				
				//All the triples that match the attributes
				List<Integer> triples = index.jointsIndex.get(attributeString[0]).get(attributeString[1]).get(attributeString[2]);
				
				if (triples == null || triples.size() == 0)
					return result;
				
				Iterator<Integer> li = triples.iterator();
				
				while (li.hasNext()) {					
					ArrayList<Integer> tArrayList = new ArrayList<Integer>(3);					
					tArrayList.add(0, li.next());
					tArrayList.add(1, li.next());
					tArrayList.add(2, li.next());
					
					boolean flag = true;
					
					for (int i = 0; i < ids.length; i++) {
						//The neighborhood of query pattern
						if (!nhindex.Check(localnh, idsInteger[i], tArrayList.get(i))) {
							flag = false;
							break;						
						}					
					}
					
					if (flag) 
						lists.add(tArrayList);
				}
				
			} else {//Edge			
				List<Integer> pairs = index.edgesIndex.get(attributeString[0]).get(attributeString[1]);
				
				if (pairs == null || pairs.size() == 0)
					return result;
					
				Iterator<Integer> li = pairs.iterator();
				while (li.hasNext()) {
					ArrayList<Integer> tArrayList = new ArrayList<Integer>(2);
					tArrayList.add(0, li.next());
					tArrayList.add(1, li.next());

					boolean flag = true;

					for (int i = 0; i < ids.length; i++) {
						if (!nhindex.Check(localnh, idsInteger[i], tArrayList.get(i))) {
							flag = false;
							break;						
						}											
					}
					
					if (flag) 
						lists.add(tArrayList);
				}
					
			}
			
			if (lists.size() == 0) {
				result.clear();
				return result;
			}
			
			//First time, we insert all the candidates into the result set
			if (result.size() == 0) {
				Iterator<ArrayList<Integer>> li = lists.iterator();
				while (li.hasNext()) {
					MatchedCandidates mT = new MatchedCandidates(li.next(), idsInteger);
					result.add(mT);
				}				
			}
			else {
				//The temp result
				LinkedList<MatchedCandidates> newResult = new LinkedList<MatchedCandidates>();

				//Iterate over all the graph pieces in the result set
				for (MatchedCandidates m : result) {
					Iterator<ArrayList<Integer>> li = lists.iterator();

					//Iterate over all the components 
					while (li.hasNext()) {
						ArrayList<Integer> currentList = li.next();
						if (m.CanJoin(currentList, idsInteger)) {
							//Make a copy of the current graph piece
							MatchedCandidates mT = new MatchedCandidates(m);
							mT.Join(currentList,  idsInteger);
							newResult.add(mT);
						}
					}
					
				}
				
				if (newResult.size() == 0) {
					result.clear();
					break;				
				}			
				
				//Clear the old result set forcibly
				result.clear();
				//Update the result set to the new one
				result = newResult;
			}
		}
		
		return result;
	}
}	
