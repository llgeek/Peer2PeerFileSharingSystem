package Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.FileUtils;



/**
 * This class is built for all kinds of file operations, like delete/add/checksum/...
 * @author Linlin Chen
 *		  lchen96@hawk.iit.edu
 * Jan 19, 2017 5:01:07 PM
 */

public class FileUtility {
	private static final String WRITINGCONTENTS = "Hello world. I love coding. ";
	private static String dir;
	private File folder;
	
	
	/**
	 * constructor for class
	 * @param dir: sharing file directory
	 * @throws Exception 
	 */
	public FileUtility(String dir) throws Exception {
		this.dir = dir;
		this.folder = new File(dir);
		if (!folder.exists()) {
			throw new RuntimeException("Directory " + dir + " not found");
		}
	}
	
	
	/**
	 * This method is used for MD5 checksum, to check the integrity of a file.
	 * @param file
	 * @return
	 */
	public String CheckMD5ForFile (String filename) {
		String md5value = null;
		FileInputStream is = null;
		try {
			is = new FileInputStream(dir + filename);
			md5value = DigestUtils.md5Hex(IOUtils.toByteArray(is));
		} catch (IOException e) {
			System.out.println("Occuring error: " + e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		return md5value;
	}
	
	
	/**
	 * This method is used to create file
	 * @param filename: the name for the created file
	 * @param times: file size (number of writing sentences)
	 * @return true if created successfully
	 */
	public boolean CreatFile (String filename, int times) {
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
	
	
	/**
	 * This method used for deleting file
	 * @param filename: the name of file to be deleted
	 * @return true if deleted successfully
	 */
	public boolean DeleteFile (String filename) {
		File deletefile = new File (dir + filename);
		try {
			FileUtils.forceDelete(deletefile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	
	/**
	 * List all files in the sharing directory
	 * @return ArrayList<String>, contains all file names
	 */
	public ArrayList<String> ListandGetAllFiles (boolean silent) {
		String [] listoffiles = folder.list(HiddenFileFilter.VISIBLE);;
		ArrayList<String> allfiles = new ArrayList<String> (Arrays.asList(listoffiles));
		int i = 0;
		if (silent) {
			for (i = 0; i < listoffiles.length; i++) {
				System.out.println(i + ". " + listoffiles[i]);
//				allfiles.add(listoffiles[i]);
			}
		}
		return allfiles;
	}
	 
	
	//public boolean 

}
