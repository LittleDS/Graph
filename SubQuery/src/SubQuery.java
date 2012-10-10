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
	
	/**
	 * The constructor class
	 * @param d the data graph
	 * @param l the subgraphs
	 * @param i the joints index for data graph
	 */
	public SubQuery(Graph d, List<Graph> l, Joints i) {
		dataGraph = d;
		subGraphs = l;
		index = i;
	}
	
	/**
	 * Execute the query to return all the matchings
	 * @return
	 */
	public HashMap<Graph, LinkedList<MatchedCandidates>> Execute() {
		HashMap<Graph, LinkedList<MatchedCandidates>> r = new HashMap<Graph, LinkedList<MatchedCandidates>>();
		for (Graph g : subGraphs) {
			r.put(g, Query(SortComponents(Divide(g)), g));
		}
		return r;
	}
	
	/**
	 * Divide the graph into joints and edges
	 * @param g
	 * @return
	 */
	public List<String> Divide(Graph g) {
		List<String> result = new LinkedList<String>();
		
		//In the first step, we want to find all the joints
		//First determine those vertices with both indegree and outdegree
		HashSet<Integer> cand = new HashSet<Integer>();		
		for (Integer i : g.indegree.keySet()) {
			if (g.outdegree.containsKey(i)) {
				cand.add(i);
			}
		}
		
		
		//For each of those vertices, choose one child and one parent to build a joint
		for (Integer i : cand) {			
			List<Integer> parentsList = g.parents.get(i);
			List<Integer> childrenList = g.children.get(i);
			
			Iterator<Integer> pi = parentsList.iterator();
			Iterator<Integer> ci = childrenList.iterator();
			while (pi.hasNext() && ci.hasNext()) {
				Integer p = pi.next();
				Integer c = ci.next();
				result.add(p + "," + i + "," + c);
				g.parents.get(i).remove(p);
				g.children.get(i).remove(c);				
			}
		}
		
		//Get all the rest edges
		
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
	 * @param components Already sorted
	 * @param subGraph
	 * @return
	 */
	public LinkedList<MatchedCandidates> Query(List<String> components, Graph subGraph) {
		//The result to be returned
		LinkedList<MatchedCandidates> result = new LinkedList<MatchedCandidates>();
					
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
				
				Iterator<Integer> li = triples.iterator();
				while (li.hasNext()) {
					ArrayList<Integer> tArrayList = new ArrayList<Integer>(3);
					tArrayList.add(0, li.next());
					tArrayList.add(1, li.next());
					tArrayList.add(2, li.next());
					lists.add(tArrayList);
				}
				
			} else {//Edge			
				List<Integer> pairs = index.edgesIndex.get(attributeString[0]).get(attributeString[1]);
				
				Iterator<Integer> li = pairs.iterator();
				while (li.hasNext()) {
					ArrayList<Integer> tArrayList = new ArrayList<Integer>(2);
					tArrayList.add(0, li.next());
					tArrayList.add(1, li.next());
					lists.add(tArrayList);
				}
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
				
				//Clear the old result set forcibly
				result.clear();
				//Update the result set to the new one
				result = newResult;
			}
		}
		return result;
	}
}	
