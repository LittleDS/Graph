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
public class NeighborHood2 {

	/**
	 * @param args
	 * @throws IOException 
	 */
//	public static void main(String[] args) throws IOException {
//		// TODO Auto-generated method stub
//		NeighborHood nh = new NeighborHood();
//		nh.Encode("datagraph.txt");
//	}

	Graph g;
	HashMap<Integer, HashMap<String, Integer>> ChildHood;
	HashMap<Integer, HashMap<String, Integer>> GrandchildHood;
	
	HashMap<Integer, HashMap<String, Integer>> ParentHood; 
	HashMap<Integer, HashMap<String, Integer>> GrandparentHood;
	
	/**
	 * 
	 * @param g
	 */
	public void Encode(Graph g) {
		ChildHood = new HashMap<Integer, HashMap<String, Integer>>();
		GrandchildHood = new HashMap<Integer, HashMap<String, Integer>>(); 
		ParentHood = new HashMap<Integer, HashMap<String, Integer>>();
		GrandparentHood = new HashMap<Integer, HashMap<String, Integer>>();
				
		//Build the index for all the vertices
		for (Integer i : g.primaryAttribute.keySet()) {
			//If the vertex has child
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
				
				//For the grand children
				for (Integer ci : c) {
					//If the vertex has grandchild
					if (g.children.containsKey(ci)) {
						if (!GrandchildHood.containsKey(i))
							GrandchildHood.put(i, new HashMap<String, Integer>());
						
						HashMap<String, Integer> grandlocalInfo = GrandchildHood.get(i);
						
						List<Integer> gc = g.children.get(ci);
						
						for (Integer gci : gc) {
							String theAttribute = g.primaryAttribute.get(gci);
							if (!grandlocalInfo.containsKey(theAttribute))
								grandlocalInfo.put(theAttribute, 1);
							else {
								int value = grandlocalInfo.get(theAttribute);
								grandlocalInfo.remove(theAttribute);
								grandlocalInfo.put(theAttribute,  value + 1);
							}
						}
						
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
				
				//For the grand parents
				for (Integer pi : p) {
					if (g.parents.containsKey(pi)) {
						if (GrandparentHood.containsKey(i)) 
							GrandparentHood.put(i, new HashMap<String, Integer>());
						
						HashMap<String, Integer> grandlocalInfo = GrandparentHood.get(i);
						
						List<Integer> gp = g.children.get(pi);
						
						for (Integer gpi : gp) {
							String theAttribute = g.primaryAttribute.get(gpi);
							if (!grandlocalInfo.containsKey(theAttribute))
								grandlocalInfo.put(theAttribute, 1);
							else {
								int value = grandlocalInfo.get(theAttribute);
								grandlocalInfo.remove(theAttribute);
								grandlocalInfo.put(theAttribute, value + 1);
							}
						}
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
		
		fstream = new FileWriter(fileName + "Grandchild");
		out = new BufferedWriter(fstream);
		
		for (Integer gc : GrandchildHood.keySet()) {
			out.write(gc + "\r\n");
		
			HashMap<String, Integer> temp = GrandchildHood.get(gc);
			
			for (String s: temp.keySet())
				out.write(s + " " + temp.get(s) + " ");
			
			out.write("\r\n");
		}
		
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
		
		fstream = new FileWriter(fileName + "Grandparent");
		out = new BufferedWriter(fstream);
		
		for (Integer gp : GrandparentHood.keySet()) {
			out.write(gp + "\r\n");
			
			HashMap<String, Integer> temp = GrandparentHood.get(gp);
			
			for (String s: temp.keySet())
				out.write(s + " " + temp.get(s) + " ");
			
			out.write("\r\n");
		}
		
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
		
		File gcFile = new File(fileName + "Grandchild");
		
		Scanner gcScanner = new Scanner(gcFile);
		while (gcScanner.hasNext()) {
			String IDString = gcScanner.nextLine();
			Integer ID = Integer.parseInt(IDString);
			GrandchildHood.put(ID,  new HashMap<String, Integer>());
			
			String neigh = gcScanner.nextLine();
			String[] slist = neigh.split(" ");
			HashMap<String, Integer> localInfo = GrandchildHood.get(ID);
			for (int i = 0; i < slist.length; i += 2)
				localInfo.put(slist[i], Integer.parseInt(slist[i + 1]));			
		}
		gcScanner.close();
		
		File gpFile = new File(fileName + "Grandparent");
		
		Scanner gpScanner = new Scanner(gpFile);
		while (gpScanner.hasNext()) {
			String IDString = gpScanner.nextLine();
			Integer ID = Integer.parseInt(IDString);
			GrandparentHood.put(ID, new HashMap<String, Integer>());
			
			String neigh = gpScanner.nextLine();
			String[] slist = neigh.split(" ");
			HashMap<String, Integer> localInfo = GrandparentHood.get(ID);
			for (int i = 0; i < slist.length; i += 2)
				localInfo.put(slist[i], Integer.parseInt(slist[i + 1]));
		}
		
		gpScanner.close();
	}	
}
