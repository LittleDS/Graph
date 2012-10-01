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
	
	public void Query(List<String> components, Graph subGraph) {
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

			if (ids.length == 3) {//Joint
				
				String a1 = subGraph.primaryAttribute.get(Integer.parseInt(ids[0]));
				String a2 = subGraph.primaryAttribute.get(Integer.parseInt(ids[1]));
				String a3 = subGraph.primaryAttribute.get(Integer.parseInt(ids[2]));
				
				//All the triples that match the attributes
				List<Integer> triples = index.jointsIndex.get(a1).get(a2).get(a3);
				
			} else {//Edge

				String a1 = subGraph.primaryAttribute.get(Integer.parseInt(ids[0]));
				String a2 = subGraph.primaryAttribute.get(Integer.parseInt(ids[1]));
				
				List<Integer> pairs = index.edgesIndex.get(a1).get(a2);
				
			}
		}
	}
}
