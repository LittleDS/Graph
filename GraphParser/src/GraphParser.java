import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class GraphParser {

	String[] attributePool;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	HashSet<Integer> vertices = new HashSet<Integer>();
	
	HashMap<Integer, List<Integer>> adjacencyList = new HashMap<Integer, List<Integer>>();
	
	HashMap<Integer, List<String>> attributeList = new HashMap<Integer, List<String>>();
	
	public void Parser(String fileName) throws FileNotFoundException {
		File inputFile = new File(fileName);
		Scanner sc = new Scanner(inputFile);
		while (sc.hasNext()) {
			String current = sc.nextLine();
			String[] strings = current.split(" ");
			Integer source = Integer.parseInt(strings[0]);
			Integer target = Integer.parseInt(strings[1]);
			
			vertices.add(source);
			vertices.add(target);
			
			if (!adjacencyList.containsKey(source))
				adjacencyList.put(source, new LinkedList<Integer>());
			adjacencyList.get(source).add(target);			
		}
		sc.close();
		
		
		//Randomly give the vertices some attributes
		attributePool = new String[52];		
		for (int i = 0; i < 26; i++) {
			attributePool[i] = String.valueOf(i + 65);
			attributePool[i + 26] = String.valueOf(i + 97);
		}
		
		Random r = new Random();
		for (Integer i : vertices) {
			attributeList.put(i, new LinkedList<String>());
			attributeList.get(i).add(String.valueOf(r.nextInt(52)));
		}
	}
	
	public void output(String fileName) throws IOException {		
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);
		for (Integer i : vertices) {
			String temp = "";
			temp += i;
			List<String> as = attributeList.get(i);
			for (String s: as)
				temp += "," + s;
			out.write(temp + "\r\n");
			
			temp = "";
			if (adjacencyList.containsKey(i)) {
				for (Integer j : adjacencyList.get(i)) {
					
				}					
			}
		}
		out.close();	
	}
}
