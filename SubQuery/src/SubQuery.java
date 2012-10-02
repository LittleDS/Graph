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
		
		//First find all the joints
		HashSet<Integer> cand = new HashSet<Integer>();
		for (Integer i : g.indegree.keySet()) {
			if (g.outdegree.containsKey(i)) {
				cand.add(i);
			}
		}

		for (Integer i : cand) {			
			int t = g.parents.get(i).size() < g.children.get(i).size()? g.parents.get(i).size() : g.children.get(i).size();
			List<Integer> parentsList = g.parents.get(i);
			List<Integer> childrenList = g.children.get(i);
			
			for (int a = 0; a < t; a++) {
				Integer p = parentsList.get(a);
				Integer c = childrenList.get(a);
				result.add(p + "," + i + "," + c);
				g.parents.get(i).remove(p);
				g.children.get(i).remove(c);
			}					
		}
		
		//Get all the rest edges
		
		for (Integer i : g.children.keySet()) {
			List<Integer> childrenList = g.children.get(i);
			for (Integer j : childrenList) {
				result.add(i + "," + j);
			}
		}
		return result;		
	}
	
	public HashMap<Integer, List<Integer>> Query(List<String> components, Graph subGraph) {
		HashMap<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
		
		List<String> componentsInOrder = new LinkedList<String>();		
		
		//First insert an initial component
		componentsInOrder.add(components.get(0));
		components.remove(0);
		
		while (components.size() > 0) {
			boolean match = false;
			
			for (int i = 0; i < componentsInOrder.size(); i++) {
				String[] ids1 = componentsInOrder.get(i).split(",");
				
				for (int j = 0; j < components.size(); j++) {
					String[] ids2 = components.get(j).split(",");
				
					for (int k = 0; k < ids1.length; j++)
						for (int l = 0; k < ids2.length; k++) {
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
		
		for (String s : componentsInOrder) {
			String[] ids = s.split(",");			

			Integer[] idsInteger = new Integer[ids.length];
			String[] attributeString = new String[ids.length];

			for (int i = 0; i < ids.length; i++) {
				idsInteger[i] = Integer.parseInt(ids[i]);
				attributeString[i] = subGraph.primaryAttribute.get(idsInteger[i]);
			}
			LinkedList<LinkedList<Integer>> lists = new LinkedList<LinkedList<Integer>>();
			if (ids.length == 3) {//Joint				
				//All the triples that match the attributes
				List<Integer> triples = index.jointsIndex.get(attributeString[0]).get(attributeString[1]).get(attributeString[2]);
				
				
			} else {//Edge			
				List<Integer> pairs = index.edgesIndex.get(attributeString[0]).get(attributeString[1]);				
			}
			
			for (int i = 0; i < ids.length; i++) {
				if (result.containsKey(idsInteger[i])) {
					
				}
				else {
					
				}
			}
		}
	}
}
