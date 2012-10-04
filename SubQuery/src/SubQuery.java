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
	public LinkedList<ArrayList<Integer>> Query(List<String> components, Graph subGraph) {
		//The result to be returned
		LinkedList<ArrayList<Integer>> result = new LinkedList<ArrayList<Integer>>();
		
		ArrayList<Integer> x = new ArrayList<Integer>(subGraph.attributes.size());
		for (int i = 0; i < x.size(); i++) 
			x.set(i, -1);
				
		for (String s : components) {
			String[] ids = s.split(",");			

			Integer[] idsInteger = new Integer[ids.length];
			String[] attributeString = new String[ids.length];
			Integer[] correspondingPosition = new Integer[ids.length];
			
			for (int i = 0; i < ids.length; i++) {
				idsInteger[i] = Integer.parseInt(ids[i]);
				attributeString[i] = subGraph.primaryAttribute.get(idsInteger[i]);

				//Get the corresponding position in the result list
				for (int j = 0; j < x.size(); j++) {
					if (x.get(j).equals(idsInteger[i])) 
						correspondingPosition[i] = j;
				}
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
			
		
			Iterator<ArrayList<Integer>> li = lists.iterator();
			while (li.hasNext()) {
				ArrayList<Integer> currentList = li.next();
				
			}

		}
	}
}	
