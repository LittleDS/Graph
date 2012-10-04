import java.util.ArrayList;
import java.util.HashMap;

/**
 * Actually this class is used to describe the structure of a temporary result during subgraph querying
 * @author Administrator
 *
 */
public class MatchedCandidates {
	
	HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();
	
	/**
	 * Check whether the a component can join with the current candidates 
	 * @param list
	 * @param ids
	 * @return
	 */
	public boolean CanJoin(ArrayList<Integer> list, Integer[] ids) {
		for (int i = 0; i < ids.length; i++) {
			if (mapping.containsKey(ids[i])) {
				if (!list.get(i).equals(mapping.get(ids[i])))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Join a component with the graph piece
	 * @param list
	 * @param ids
	 */
	public void Join(ArrayList<Integer> list, Integer[] ids) {
		for (int i = 0; i < ids.length; i++) {
			if (!mapping.containsKey(ids[i])) {
				mapping.put(ids[i], list.get(i));
			}
		}
	}
	
	/**
	 * The constructor
	 * @param list
	 * @param ids
	 */
	public MatchedCandidates(ArrayList<Integer> list, Integer[] ids) {
		for (int i = 0; i < ids.length; i++) {
			mapping.put(ids[i], list.get(i));
		}
	}

	/**
	 * Copy constructor
	 * @param another
	 */
	public MatchedCandidates(MatchedCandidates another) {
		for (Integer i : another.mapping.keySet()) {
			mapping.put(i, another.mapping.get(i));
		}
	}
}
