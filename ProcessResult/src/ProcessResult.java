import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class ProcessResult {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		File in = new File("results.txt");
		Scanner sc = new Scanner(in);
		int i = 0;
		while (sc.hasNext()) {
			String currentLine = sc.nextLine();
			if (currentLine.contains("ns")) {
				System.out.println(i++ + " " + currentLine);
			}
		}
		sc.close();
	}

}
