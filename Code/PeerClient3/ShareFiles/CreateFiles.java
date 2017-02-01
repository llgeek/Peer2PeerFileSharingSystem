import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class CreateFiles {
	private static final String WRITINGCONTENTS = "Hello world. I love coding. ";
	private static final String dir = "./";
	
	public static void main (String [] args) {
		System.out.println("Generate how many files (Increasing size by 1K sentences each)");
		Scanner reader = new Scanner(System.in);
		int num = Integer.valueOf(reader.nextLine());
		for (int i = 1; i <= num; i++) {
			CreateFile ("files" + i + "KB", i * 1024);
		}
		reader.close();
	}
	
	private static boolean CreateFile (String filename, int times) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream(dir + filename), "utf-8"));
			bw.write(new String(new char[times]).replace("\0", WRITINGCONTENTS));	//repeat for size times
		} catch (IOException e) {
			System.out.println("Cannot create file " + filename);
			return false;
		} finally {
			try {bw.close();} catch (Exception ex) {ex.printStackTrace();}
		}
		
		return true;
	}
}
