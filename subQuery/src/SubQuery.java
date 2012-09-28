import java.util.List;


public class SubQuery {
	Graph dataGraph;
	List<Graph> querySubgraphs;
	Joints indices;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public SubQuery(Graph d, List<Graph> q, Joints i) {
		dataGraph = d;
		querySubgraphs= q;
		indices = i;
	}

	
	/**
	 * Divide a graph into joints and edges
	 * @param g
	 */
	public void Divide(Graph g) {
		for (Integer i : g.attributes.keySet()) {
			if (g.indegree.get(i) > 0 && g.outdegree.get(i) > 0) {
				g.children.get(i);
				g.parents.get(i);
			}
		}
	}
}
