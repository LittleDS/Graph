import java.util.List;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * NeighborHood Information Index
 * @author Administrator
 *
 */
public class NeighborHood {

	/**
	 * @param args
	 * @throws IOException 
	 */
//	public static void main(String[] args) throws IOException {
//		// TODO Auto-generated method stub
//		NeighborHood nh = new NeighborHood();
//		nh.Encode("datagraph.txt");
//	}

	public static void main(String[] args) throws IOException {
		NeighborHood2 nh2 = new NeighborHood2();
		nh2.Encode("P2P");
		nh2.OutputToFile("P2PNH1");
	}
	
	Graph g;
	HashMap<Integer, HashMap<String, Integer>> ChildHood;
	HashMap<Integer, HashMap<String, Integer>> ParentHood; 

	/**
	 * 
	 * @param g
	 */
	public void Encode(Graph g) {
		ChildHood = new HashMap<Integer, HashMap<String, Integer>>();
		ParentHood = new HashMap<Integer, HashMap<String, Integer>>();
		
		//Build the index for all the vertices
		for (Integer i : g.primaryAttribute.keySet()) {
			if (g.children.containsKey(i)) {
				if (!ChildHood.containsKey(i))
					ChildHood.put(i, new HashMap<String, Integer>());
				
				HashMap<String, Integer> localInfo = ChildHood.get(i);
				
				List<Integer> c = g.children.get(i);
				for (Integer ci : c) {
					String theAttribute = g.primaryAttribute.get(ci);
					if (!localInfo.containsKey(theAttribute))
						localInfo.put(theAttribute, 1);
					else {
						int value = localInfo.get(theAttribute);
						localInfo.remove(theAttribute);
						localInfo.put(theAttribute, value + 1);
					}
					
				}				
			}
			
			if (g.parents.containsKey(i)) {
				if (!ParentHood.containsKey(i)) 
					ParentHood.put(i,  new HashMap<String, Integer>());
				
				HashMap<String, Integer> localInfo = ParentHood.get(i);
				
				List<Integer> p = g.parents.get(i);
				
				for (Integer pi : p) {
					String theAttribute = g.primaryAttribute.get(pi);
					if (!localInfo.containsKey(theAttribute))
						localInfo.put(theAttribute, 1);
					else {
						int value = localInfo.get(theAttribute);
						localInfo.remove(theAttribute);
						localInfo.put(theAttribute, value + 1);
					}
				}
			}
		}			
	}
	public void Encode(String fileName) throws IOException {
		//Only initialize the graph when we start to use it
		g = new Graph();
		g.loadGraphFromFile(fileName);
		Encode(g);
	}
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException 
	 */
	public void OutputToFile(String fileName) throws IOException {
		//Output to file		
		FileWriter fstream = new FileWriter(fileName + "Child");
		BufferedWriter out = new BufferedWriter(fstream);
		
		//Write the index into file
		for (Integer c : ChildHood.keySet()) {
			out.write(c + "\r\n");
			
			HashMap<String, Integer> temp = ChildHood.get(c);
			
			for (String s : temp.keySet())
				out.write(s + " " + temp.get(s) + " ");
				
			//Write into the file
			out.write("\r\n");
		}
		
		//don't forget to close the stream
		out.close();
		
		fstream = new FileWriter(fileName + "Parent");
		out = new BufferedWriter(fstream);
		
		//Write the index into file
		for (Integer p : ParentHood.keySet()) {
			out.write(p + "\r\n");
			
			HashMap<String, Integer> temp = ChildHood.get(p);
			
			for (String s : temp.keySet())
				out.write(s + " " + temp.get(s) + " ");
				
			//Write into the file
			out.write("\r\n");
		}
		
		//don't forget to close the stream
		out.close();				
	}
	
	/**
	 * 
	 * @param fileName
	 * @throws FileNotFoundException 
	 */
	public void loadIndexFromFile(String fileName) throws FileNotFoundException {
		ChildHood = new HashMap<Integer, HashMap<String, Integer>>();
		ParentHood = new HashMap<Integer, HashMap<String, Integer>>();
		
		//Child Part
		File cFile = new File(fileName + "Child");
		
		Scanner cScanner = new Scanner(cFile);		
		while (cScanner.hasNext()) {
			String IDString = cScanner.nextLine();
			Integer ID = Integer.parseInt(IDString);			
			ChildHood.put(ID, new HashMap<String, Integer>());
			
			String neigh = cScanner.nextLine();
			String[] slist = neigh.split(" ");
			HashMap<String,  Integer> localInfo = ChildHood.get(ID);			
			for (int i = 0; i < slist.length; i += 2)
				localInfo.put(slist[i], Integer.parseInt(slist[i + 1]));			
		}
		cScanner.close();
		
		File pFile = new File(fileName + "Parent");
		
		Scanner pScanner = new Scanner(pFile);
		while (pScanner.hasNext()) {
			String IDString = pScanner.nextLine();
			Integer ID = Integer.parseInt(IDString);
			ParentHood.put(ID, new HashMap<String, Integer>());
			
			String neigh = pScanner.nextLine();
			String[] slist = neigh.split(" ");
			HashMap<String, Integer> localInfo = ParentHood.get(ID);
			for (int i = 0; i < slist.length; i += 2)
				localInfo.put(slist[i], Integer.parseInt(slist[i + 1]));
		}
		pScanner.close();
	}

	/**
	 * Check the containment of neighborhood index
	 * @param query is the neighborhood of the query pattern
	 * @param s is the vertex in the query pattern
	 * @param t is the corresponding matching vertex in the data graph
	 * @return
	 */
	public boolean Check(NeighborHood query, Integer s, Integer t) {
		
		if (query.ChildHood.containsKey(s)) {
			HashMap<String, Integer> nhQ = query.ChildHood.get(s);
			
			if (!ChildHood.containsKey(t))
				return false;
			
			HashMap<String, Integer> nhD = ChildHood.get(t);
			
			//First check the childhood
			for (String skey : nhQ.keySet())
				if (!nhD.containsKey(skey))
					return false;
				else if (nhD.get(skey) < nhQ.get(skey)) {
					return false;
				}
		}
		
		//Second check the parenthood		
		if (query.ParentHood.containsKey(s)) {
			HashMap<String, Integer> nhQ = query.ParentHood.get(s);
			
			if (!ParentHood.containsKey(t))
				return false;

			HashMap<String, Integer> nhD = ParentHood.get(t);
			
			for (String skey : nhQ.keySet())
				if (!nhD.containsKey(skey))
					return false;
				else if (nhD.get(skey) < nhQ.get(skey)) {
					return false;
				}
		}
		return true;
	}
}
