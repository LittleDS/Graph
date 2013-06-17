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
		j.loadEdgeIndexFromFile("LinkedINEdges");
		j.loadJointIndexFromFile("LinkedINJoints");
				
		System.out.println("Finish Loading Index....");
		
		Graph d = new Graph();
		d.loadGraphFromFile("LinkedIN");

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
	public List<ArrayList<Integer>> Divide(Graph input) {
		List<ArrayList<Integer>> result = new LinkedList<ArrayList<Integer>>();
				
		//First determine those vertices with both indegree and outdegree
		HashSet<Integer> cand = new HashSet<Integer>();		
		for (Integer i : input.primaryAttribute.keySet()) {
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
				ArrayList<Integer> ti = new ArrayList<Integer>(3);
				ti.add(jointParent);
				ti.add(jointCore);
				ti.add(jointChild);
				result.add(ti);
				
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
					ArrayList<Integer> ti = new ArrayList<Integer>(2);
					ti.add(i);
					ti.add(j);			
					result.add(ti);
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
	public List<ArrayList<Integer>> SortComponents(List<ArrayList<Integer>> components) {
		//We need to determine an order of those components first
		//Because we have to make sure the next component has at least one intersection with the previous components
		List<ArrayList<Integer>> componentsInOrder = new LinkedList<ArrayList<Integer>>();		
		
		if (components.size() == 0)
			return componentsInOrder;
		
		//First insert an initial component
		componentsInOrder.add(components.get(0));
		components.remove(0);

		//Make sure we don't leave any component
		while (components.size() > 0) {
			boolean match = false;
			
			for (int i = 0; i < componentsInOrder.size(); i++) {
				ArrayList<Integer> ids1 = componentsInOrder.get(i);
				
				for (int j = 0; j < components.size(); j++) {
					ArrayList<Integer> ids2 = components.get(j);
				
					for (int k = 0; k < ids1.size(); k++)
						for (int l = 0; l < ids2.size(); l++) {
							if (ids1.get(k).equals(ids2.get(l))) {
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
	 * If the joint is a cycle, the corresponding matching must be also a cycle
	 * @param list
	 * @param ids
	 * @return
	 */
	public boolean isValid(ArrayList<Integer> list, ArrayList<Integer> ids) {
		for (int i = 0; i < ids.size() - 1; i++) {
			for (int j = i + 1; j < ids.size(); j++) {
				if (ids.get(i).equals(ids.get(i)) && !list.get(i).equals(list.get(j)))
						return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Determine whether a joint is a cycle
	 * @param ids
	 * @return
	 */
	public boolean isCycle(ArrayList<Integer> ids) {
		for (int i = 0; i < ids.size() - 1; i++) {
			for (int j = i + 1; j < ids.size(); j++) {
				if (ids.get(i).equals(ids.get(j)))
						return true;
			}
		}		
		return false;
	}
		
	/**
	 * Using the index to query the data graph
	 * @param components already sorted
	 * @param subGraph
	 * @return
	 */
	public LinkedList<MatchedCandidates> Query(List<ArrayList<Integer>> components, Graph subGraph) {
		//The result to be returned
		LinkedList<MatchedCandidates> result = new LinkedList<MatchedCandidates>();
		
		//The neighborhood index
		NeighborHood localnh = new NeighborHood();
		localnh.Encode(subGraph);
		
		for (ArrayList<Integer> s : components) {			

			String[] attributeString = new String[s.size()];

			for (int i = 0; i < s.size(); i++)
				attributeString[i] = subGraph.primaryAttribute.get(s.get(i));
							
			LinkedList<ArrayList<Integer>> lists = new LinkedList<ArrayList<Integer>>();
			
			if (s.size() == 3) {//Joint				
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
					
					for (int i = 0; i < s.size(); i++) {
						//The neighborhood of query pattern
						if (!nhindex.Check(localnh, s.get(i), tArrayList.get(i))) {
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

					for (int i = 0; i < s.size(); i++) {
						if (!nhindex.Check(localnh, s.get(i), tArrayList.get(i))) {
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
				
				boolean cycleFlag = false;
				if (isCycle(s))  
					cycleFlag = true;
				
				while (li.hasNext()) {
					ArrayList<Integer> tempAList = li.next();
					if (cycleFlag && isValid(tempAList, s)) {
						MatchedCandidates mT = new MatchedCandidates(tempAList, s);
						result.add(mT);
					}
					else {
						MatchedCandidates mT = new MatchedCandidates(tempAList, s);
						result.add(mT);						
					}
				}				
			}
			else {
				//The temp result
				LinkedList<MatchedCandidates> newResult = new LinkedList<MatchedCandidates>();

				boolean cycleFlag = false;
				if (isCycle(s))  
					cycleFlag = true;
				
				//Iterate over all the graph pieces in the result set
				for (MatchedCandidates m : result) {
					Iterator<ArrayList<Integer>> li = lists.iterator();

					//Iterate over all the components 
					while (li.hasNext()) {
						ArrayList<Integer> currentList = li.next();
						if (cycleFlag && isValid(currentList, s)) {
							if (m.CanJoin(currentList, s)) {
								//Make a copy of the current graph piece
								MatchedCandidates mT = new MatchedCandidates(m);
								mT.Join(currentList, s);
								newResult.add(mT);
							}
						}
						else {
							if (m.CanJoin(currentList, s)) {
								//Make a copy of the current graph piece
								MatchedCandidates mT = new MatchedCandidates(m);
								mT.Join(currentList, s);
								newResult.add(mT);
							}								
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
