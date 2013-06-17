import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to describe the structure of a temporary result during subgraph querying
 * @author Administrator
 *
 */
public class MatchedCandidates {
	
	HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();
	HashMap<String, List<String>> paths = new HashMap<String, List<String>>();
	/**
	 * Check whether the a component can join with the current candidates 
	 * @param list
	 * @param ids
	 * @return
	 */
	public boolean CanJoin(ArrayList<Integer> list, ArrayList<Integer> ids) {
		for (int i = 0; i < ids.size(); i++) {
			if (mapping.containsKey(ids.get(i))) {
				if (!list.get(i).equals(mapping.get(ids.get(i))))
					return false;
			}
			else {
				if (mapping.containsValue(list.get(i)))
					return false;
			}
		}				
		return true;
	}
	
	/**
	 * Join a component with the graph piece
	 * @param list
	 * @param s
	 */
	public void Join(ArrayList<Integer> list, ArrayList<Integer> s) {
		for (int i = 0; i < s.size(); i++) {
			if (!mapping.containsKey(s.get(i))) {
				mapping.put(s.get(i), list.get(i));
			}
		}
	}
	
	/**
	 * The default constructor
	 * @param list
	 * @param s
	 */
	public MatchedCandidates(ArrayList<Integer> list, ArrayList<Integer> s) {
		for (int i = 0; i < s.size(); i++) {
			mapping.put(s.get(i), list.get(i));
		}
	}

	/**
	 * Copy constructor
	 * @param another
	 */
	public MatchedCandidates(MatchedCandidates another) {
		mapping.putAll(another.mapping);
	}
	

	/**
	 * Combine the two matching candidates
	 * @param another
	 * @throws Exception 
	 */
	public void Combine(MatchedCandidates another) throws Exception {
		for (Integer k : another.mapping.keySet()) {
			if (!this.mapping.containsKey(k))
				this.mapping.put(k, another.mapping.get(k));
			else
				throw new Exception("Duplicate Keys");
		}
		
		for (String k : another.paths.keySet()) {
			this.paths.put(k, another.paths.get(k));
		}
	}
	
	/**
	 * Print the matching result
	 */
	public void Print() {
		for (Integer i: mapping.keySet()) {
			System.out.println(i + " " + mapping.get(i));
		}
		
		if (paths.keySet().size() > 0) {
			System.out.println("Paths between ");
			for (String k : paths.keySet()) {
				System.out.println(k);
				for (String l : paths.get(k))
					System.out.print(l + " ");
				System.out.println();
			}			
		}			
		
		System.out.println("~~~~~~~~~~~~~~~");
	}
	
}
