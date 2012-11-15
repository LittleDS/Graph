
public class SuperEdge {
	public Integer source;
	public Integer target;
	public int length;
	public Graph sourceComponent;
	public Graph targetComponent;
	
	public boolean processed;
	/**
	 * Constructor
	 * @param s
	 * @param t
	 * @param l
	 */
	public SuperEdge(Integer s, Integer t, int l) {
		source = s;
		target = t;
		length = l;
		processed = false;
	}
}
