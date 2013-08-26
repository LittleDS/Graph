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
		File in = new File("result10.txt");
		Scanner sc = new Scanner(in);

		while (sc.hasNext()) {
			String currentLine = sc.nextLine();
			if (currentLine.contains("nanosecond")) {
				String[] s = currentLine.split(" ");
				System.out.println(s[0]);
			}

//			if (currentLine.contains("Total Component Query time:")) {
//				String[] s = currentLine.split(" ");
//				System.out.print(s[4]);
//			}
//			else if (currentLine.contains("Total Path Building time:")) {
//				String[] s = currentLine.split(" ");
//				System.out.print("\t" + Long.parseLong(s[4]));				
//			}
//			else if (currentLine.contains("Total running time:")) {
//				String[] s = currentLine.split(" ");
//				System.out.println("\t" + Long.parseLong(s[3]));				
//			}
		}		
		sc.close();		
	}

}
