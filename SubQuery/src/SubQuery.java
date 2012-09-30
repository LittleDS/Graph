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
	
	public void Query(List<String> components) {
		for (String s : components) {
			String[] ids = s.split(" ");
			//It's a joint
			if (ids.length == 3) {
				
			} else { //It's an edge
				
			}
		}
	}
	
}
