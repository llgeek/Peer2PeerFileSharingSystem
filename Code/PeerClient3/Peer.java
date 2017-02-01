package PeerClient3;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.monitor.*;

import IndexServer.*;
import Utilities.*;

/**
 * This class is built for peer object.
 * @author Linlin Chen
 *		  lchen96@hawk.iit.edu
 * Jan 19, 2017 5:03:39 PM
 */

public class Peer {
	
	private static final String CONFIG_FILE = "./config.properties"; //config file
	private static final int KILOBYTES = 1024;	//kilo-bytes
	private static final boolean DIR_AUTO_WATCHER = false;
	
	private static int BASE_PORT;		//base port for client acts as a server
	private int PEER_AS_SERVER_PORT;		//port number when peer acts as a server
	// equals BASE_PORT + ID
	
	private boolean AUTO_DIR_WATCHER;		//whether set a director watcher to monitor all changes
	
	private static String SERVER_IPADDR;		//index server ip address
	private static int SERVER_PORT;
//	private static int SERVER_REGISTER_PORT;	//server register port
//	private static int SERVER_LOOKUP_PORT;		//server lookup port
	private String shareFolder;			//folder to share files with others
	private int ID;						//peer id
	private int bandwidth;				//bandwidth
	
	private Scanner reader = new Scanner (System.in);
	
	private FileUtility FUtil;

	public Peer(String shareFolder, int ID, int bandwidth) {
		this.shareFolder = shareFolder;
		this.ID = ID;
		this.bandwidth = bandwidth;
		try {
			this.FUtil = new FileUtility (shareFolder);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			FileInputStream configfile = new FileInputStream(CONFIG_FILE);
			Properties config = new Properties();
			config.load(configfile);
			this.SERVER_IPADDR = config.getProperty("SERVER_IPADDR");
			this.SERVER_PORT = Integer.parseInt(config.getProperty("SERVER_PORT"));
//			this.SERVER_REGISTER_PORT = Integer.parseInt(config.getProperty("SERVER_REGISTER_PORT"));
//			this.SERVER_LOOKUP_PORT = Integer.parseInt(config.getProperty("SERVER_LOOKUP_PORT"));
			this.BASE_PORT = Integer.parseInt(config.getProperty("BASE_PORT"));
			this.AUTO_DIR_WATCHER = Boolean.valueOf(config.getProperty("AUTO_DIR_WATCHER"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.PEER_AS_SERVER_PORT = this.BASE_PORT + this.ID;
		
		System.out.println("||============================================================||");
		System.out.println("||                                                            ||");
		System.out.println("||        		P2P File Sharing System                       ||");
		System.out.println("||              ***********************                       ||");
		System.out.println("||                         Client                             ||");
		System.out.println("||============================================================||");
		
		System.out.println("**************** Client " + this.ID + " Starts ****************");
		System.out.println("**************** Sharing Folder lies in " + this.shareFolder + "****************");
		

	}
	
	public Peer(String shareFolder, int ID, int bandwidth, int SERVER_REGISTER_PORT, int SERVER_LOOKUP_PORT, int BASE_PORT, boolean AUTO_DIR_WATCHER) throws Exception {
		
		try {
			FileInputStream configfile = new FileInputStream(CONFIG_FILE);
			Properties config = new Properties();
			config.load(configfile);
			this.SERVER_IPADDR = config.getProperty("SERVER_IPADDR");
			this.SERVER_PORT = Integer.parseInt(config.getProperty("SERVER_PORT"));
//			this.SERVER_REGISTER_PORT = Integer.parseInt(config.getProperty("SERVER_REGISTER_PORT"));
//			this.SERVER_LOOKUP_PORT = Integer.parseInt(config.getProperty("SERVER_LOOKUP_PORT"));
			this.BASE_PORT = Integer.parseInt(config.getProperty("BASE_PORT"));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.shareFolder = shareFolder;
		this.ID = ID;
		this.bandwidth = bandwidth;
		this.FUtil = new FileUtility (shareFolder);
		this.PEER_AS_SERVER_PORT = this.BASE_PORT + this.ID;
		this.AUTO_DIR_WATCHER = AUTO_DIR_WATCHER;
		
		
		System.out.println("||============================================================||");
		System.out.println("||                                                            ||");
		System.out.println("||        		P2P File Sharing System                       ||");
		System.out.println("||              ***********************                       ||");
		System.out.println("||                         Client                             ||");
		System.out.println("||============================================================||");
		
		System.out.println("**************** Client " + this.ID + " Starts ****************");
		System.out.println("**************** Sharing Folder lies in " + this.shareFolder + "****************");		


	}
	
	public static void main (String [] args) throws IOException {	
		Peer peer = new Peer ("./PeerClient3/ShareFiles/", 3, 500);
		if (peer.AUTO_DIR_WATCHER) {
			new Thread (peer.new CheckFolder()).start();
		}
		
		new Thread (peer.new UIThread ()).start(); 
	
		System.out.println("\n\n**************** Client Server Now Starts at port" + peer.PEER_AS_SERVER_PORT + "****************\n\n");
		try {
			ServerSocket serverlistner = new ServerSocket (peer.PEER_AS_SERVER_PORT);
			try {
				while (true) {
					Thread sthread = new Thread (peer.new PeerServer(serverlistner.accept()));
					sthread.start();
				}
			} finally {
				serverlistner.close();
			}
		} catch (IOException e) {
			System.out.println("Server start unsucessfully");
			e.printStackTrace();
		}
	}
	
	
	
	private class UIThread implements Runnable {
		public void run () {
			UIDesign();
		}
	}
	
	
	/**
	 * User interface
	 */
	private void UIDesign () {
		int choice = 0;
		System.out.println("Do you want to index all files in sharing folder now?\t Y(yes)\tN(no)");
		if (reader.nextLine().equalsIgnoreCase("Y")) {
			ArrayList<String> allfilenow = FUtil.ListandGetAllFiles(false);
			for (String file : allfilenow) {
				registerFile (file);
			}
		}
		while (true) {
			System.out.println("\n\nFunctionality choice:");
			System.out.println("1. Register a file");
			System.out.println("2. Delete a file");
			System.out.println("3. Lookup for a file");
			System.out.println("4. Download for a file");
			System.out.println("5. Exit");
			System.out.println("\nInput your choice: \n");
			choice = Integer.valueOf(reader.nextLine());
			switch (choice) {
				case 1:		//register file
					ArrayList<String> allfiles1 = FUtil.ListandGetAllFiles(true);
					choice = Integer.valueOf(reader.nextLine());
					if (choice >= 0 && choice < allfiles1.size()) {
						registerFile (allfiles1.get(choice));
					} else {
						System.out.println("Wrong selection for the file");
					}
					break;
					
				case 2:		//unregister file
					ArrayList<String> allfiles2 = FUtil.ListandGetAllFiles(true);
					choice = Integer.valueOf(reader.nextLine());
					if (choice >= 0 && choice < allfiles2.size()) {
						if (!FUtil.DeleteFile(allfiles2.get(choice))){
							System.out.println("File " + allfiles2.get(choice) + " deletion fails");
						}
						if (!AUTO_DIR_WATCHER) {	//only when director watcher is closed, deletion need manually unregister
							unregisterFile (allfiles2.get(choice));
						}
					} else {
						System.out.println("Wrong selection for the file");
					}
					break;
					
				case 3: //look up for a file
					String filename1 = null;
					ArrayList<String> allindexedfiles = getAllIndexedFiles ();
					for (int i = 0; i < allindexedfiles.size(); i++) {
						System.out.println(i + ". " + allindexedfiles.get(i));
					}
					System.out.format("%d. Input another filename\n", allindexedfiles.size());
					choice = Integer.valueOf(reader.nextLine());
					if (choice >= 0 && choice <= allindexedfiles.size()) {
						if (choice == allindexedfiles.size()) {
							System.out.println("Input file name");
							filename1 = reader.nextLine();
						} else {
							filename1 = allindexedfiles.get(choice);
						}
						ArrayList<PeerDescriptor> peerswithfile = lookupFile(filename1);
						if (peerswithfile.size() > 0) {
							System.out.println("Following peers have the file you are looking for:");
							System.out.println("PeerID:\t\tIPAddress:\t\tBandwidth:");
							int i = 0;
							for (PeerDescriptor pd : peerswithfile) {
								System.out.format("%d. %d\t\t%s\t\t%d\n", i, pd.getID(), pd.getIP(), pd.getBandwidth());
								i++;
							}
							System.out.println("Do you want to download this file?\t Y(yes) \t N(no)");
							
							if (reader.nextLine().equalsIgnoreCase("Y")) {
								System.out.println("Input which peer you want to download file from");
								choice = Integer.valueOf(reader.nextLine());
								if (choice >= 0 && choice < peerswithfile.size()) {
									Socket downloadpeersocket = null;
									try {
										downloadpeersocket = new Socket (peerswithfile.get(choice).getIP(), peerswithfile.get(choice).getID() + BASE_PORT);
										System.out.format("Connected with peer: %d...\n", peerswithfile.get(choice).getID());
										if (downloadFile(filename1, downloadpeersocket)) {
											System.out.println("File " + filename1 + " downloaded from peer " + peerswithfile.get(choice).getID() + " finished");
										} //else { //already declared in the method
										//	System.out.println("ERROR: File " + filename1 + "dowloaded from peer " + peerswithfile.get(choice).getID() + " falis");
										//}
									} catch (IOException e) {
										e.printStackTrace();
									} finally {
										try {
											downloadpeersocket.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								} else {
									System.out.println("Wrong choice");
								}

							}
							
						} else {
							System.out.println("No such file indexed in Server");
						}
					} else {
						System.out.println("Wrong choice");
					}
					break;
					
				case 4:
					/**
					 * Download file
					 */
					ArrayList<PeerDescriptor> peerswithfile = new ArrayList<PeerDescriptor> ();
					String filename2 = null;
					ArrayList<String> allindexedfiles2 = getAllIndexedFiles ();
					System.out.println("Indexed Server has following files");
					for (int i = 0; i < allindexedfiles2.size(); i++) {
						System.out.format("%d. %s\n", i, allindexedfiles2.get(i));
					}
					System.out.format("%d. Input another filename\n", allindexedfiles2.size());
					choice = Integer.valueOf(reader.nextLine());
					
					if (choice >= 0 && choice <= allindexedfiles2.size()) {
						if (choice == allindexedfiles2.size()) {
							System.out.println("Input file name");
							filename2 = reader.nextLine();
						} else {
							filename2 = allindexedfiles2.get(choice);
						}
						peerswithfile = lookupFile (filename2);
						if (peerswithfile.size() > 0) {
							System.out.println("Following peers have the file you are looking for:");
							System.out.println("PeerID:\t\tIPAddress:\t\tBandwidth:");
							int i = 0;
							for (PeerDescriptor pd : peerswithfile) {
								System.out.format("%d. %d\t\t%s\t\t%d\n", i, pd.getID(), pd.getIP(), pd.getBandwidth());
								i++;
							}
						} else {
							System.out.println("No such file indexed in server");
						}
						
					} else {
						System.out.println("Wrong choice");
					}
					
					if (peerswithfile.size() != 0) {
						System.out.println("Input which peer you want to download file from");
						choice = Integer.valueOf(reader.nextLine());
						if (choice >= 0 && choice < peerswithfile.size()) {
							Socket downloadpeersocket = null;
							try {
								downloadpeersocket = new Socket (peerswithfile.get(choice).getIP(), peerswithfile.get(choice).getID() + BASE_PORT);
								System.out.format("Connected with peer: %d...\n", peerswithfile.get(choice).getID());
								if (downloadFile(filename2, downloadpeersocket)) {
									System.out.println("File " + filename2 + " downloaded from peer " + peerswithfile.get(choice).getID() + " finished");
								}// else { //alreadly declared in the method.
								//	System.out.println("ERROR: File " + filename2 + "dowloaded from peer " + peerswithfile.get(choice).getID() + " falis");
								//}
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								try {
									downloadpeersocket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} else {
							System.out.println("Wrong choice");
						}
						
					}
					break;
					
				case 5:
					System.out.println("Client exist");
					reader.close();
					System.out.println("Thanks for using this system.");
					System.exit(0);
				default:
					System.out.println("Wrong input instruction. (choose from 1 - 5)");
					break;
			}
			
		}
	}

	
	private class PeerClient implements Runnable {
		
		public void run () {
			UIDesign();
		}
	}
	
	/**
	 * Server thread in client, handling download request
	 * @author Linlin Chen
	 *		  lchen96@hawk.iit.edu
	 * Jan 22, 2017 10:50:51 PM
	 */
	private class PeerServer implements Runnable {
		private Socket socket;
		
		public PeerServer (Socket socket) {
			System.out.format("Building connections with %s...\n", socket.getInetAddress().getHostAddress());
			System.out.println("************Starting sending file service**************");
			this.socket = socket;
		}
		public void run() {
			RequestMessage request = new RequestMessage ();
			ResponseMessage response = new ResponseMessage ();
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			BufferedInputStream fileInput = null;
			try {
				in = new ObjectInputStream(socket.getInputStream());
				out = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				request = (RequestMessage) in.readObject();
				if (request.getRequestType() == RequestMessageType.DOWNLOAD) {
					String filename = (String) request.getRequestData();
					System.out.println("Sending files to " + socket.getInetAddress().getHostAddress());
					String str="";

					BufferedReader br = new BufferedReader(new FileReader(shareFolder + filename));
					StringBuilder sb = new StringBuilder ();
					String ls = System.getProperty("line.separator");
					try {
						while ((str = br.readLine() ) != null) {
							sb.append(str);
							sb.append(ls);
						}
					} finally {
						br.close();
					}
					out.writeObject(sb.toString());
					out.flush();
					
					
					//check MD5 value
					request = (RequestMessage) in.readObject();
					if (request.getRequestType() == RequestMessageType.VERIFY) {
						ResponseMessage response3 = new ResponseMessage ();
						if (request.getRequestData().equals(FUtil.CheckMD5ForFile(filename)) == true) {
							response3.setResponseType(ResponseMessageType.SUCCESS);
						} else {
							response3.setResponseType(ResponseMessageType.FAILURE);
						}
						out.writeObject(response3);
						out.flush();
					}
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
					try {
						if (out != null)
							out.close();
						if (in != null)
							in.close();
						if (fileInput != null)
							fileInput.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}	
			Thread.currentThread().interrupt();	
			System.out.println("Disconnect with " + socket.getInetAddress().getHostAddress() + "...\n\n");
		}
	}
	
	/**
	 * File auto watcher
	 * When file deleted, created or modified, will notify the server
	 * @author Linlin Chen
	 *		  lchen96@hawk.iit.edu
	 * Jan 22, 2017 10:51:35 PM
	 */
	private class CheckFolder implements Runnable {
		private static final long pollingInterval = 5 * 1000;	//check the file-system changes every 5s
		private String dir;
		private File folder;
		
		
		public CheckFolder() {
			this.dir = shareFolder;
			this.folder = new File(this.dir);
			if (!folder.exists()) {
				throw new RuntimeException("Directory " + dir + " not found");
			}
			System.out.println("File monitor starts in client " + ID);
		}
		
		public void run () {
			FileAlterationObserver observer = new FileAlterationObserver(folder, HiddenFileFilter.VISIBLE);
			FileAlterationMonitor monitor = new FileAlterationMonitor(pollingInterval);
			
			FileAlterationListener listener = new FileAlterationListenerAdaptor() {
				
				//Triggered when a file is created in the monitored folder
				@Override
				public void onFileCreate (File file) {
					registerFile (file.getName());
				}
				
				//Triggered when a file is deleted in the monitored folder
				@Override
				public void onFileDelete (File file) {
					unregisterFile (file.getName());
				}
				
				//Triggered when a file is deleted in the monitored folder
				@Override
				public void onFileChange (File file) {
					updateFile (file.getName());
					
				}
			};
			observer.addListener(listener);
			monitor.addObserver(observer);
			try {
				monitor.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		
	}
	
	
	/**
	 * Download file method
	 * @param filename
	 * @param socket
	 * @return
	 */
	private boolean downloadFile (String filename, Socket socket) {
		
		RequestMessage request = null;
		ResponseMessage response = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		
		boolean issucess = false;
		
		try {
			request = new RequestMessage ();
			response = new ResponseMessage ();
			out = new ObjectOutputStream (socket.getOutputStream());
			out.flush();
			System.out.println("Downloading file...");
			request.setRequestType(RequestMessageType.DOWNLOAD);
			request.setRequestData(filename);
			out.writeObject(request);
			out.flush();
			
			in = new ObjectInputStream(socket.getInputStream());
			String filestr = "";
			filestr = (String) in.readObject();
			
			FileWriter fw = null;
			try
			{  
				//To Append String s to Existing File
				fw = new FileWriter(new File(shareFolder + filename));
				fw.write(filestr);                                      //Write to file

			} catch(Exception e){
				e.printStackTrace();
				//	System.out.println("Cannot Open File");     // To Mask Print on Console
			} finally {
				fw.close();
			}
			if (new File(shareFolder + filename).length() == 0) {
				RequestMessage request2 = new RequestMessage();
				request2.setRequestType(RequestMessageType.DISCONNECT);
				out.writeObject(request2);
				out.flush();
				issucess = false;
				System.out.println("File downloading failed");
			} else {
				//If file is downloaded, then compare with original MD5 value to check the integrity of the file
				RequestMessage request1 = new RequestMessage();
				request1.setRequestType(RequestMessageType.VERIFY);
				request1.setRequestData(FUtil.CheckMD5ForFile(filename));
				out.writeObject(request1);
				try {
					response = (ResponseMessage) in.readObject();
					if (response.getResponseType() == ResponseMessageType.FAILURE){ 
						System.out.println("MD5 does not match the requested file");
						issucess = false;
					} else {
						System.out.println("MD5 mathches the requested file");
						issucess = true;
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return issucess;
	}
	
	
	/**
	 * register file with filename
	 * @param filename
	 * @return
	 */
	private boolean registerFile (String filename) {
		boolean issucess = false;
		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		try {
			socket = new Socket (SERVER_IPADDR, SERVER_PORT);
			
			out = new ObjectOutputStream (socket.getOutputStream());
			out.flush();
			
			in = new ObjectInputStream (socket.getInputStream());
			
			RequestMessage request = new RequestMessage();
			request.setRequestType(RequestMessageType.REGISTER);
			FileDescriptor fd = new FileDescriptor();
			fd.setBandwidth(bandwidth);
			fd.setfilename(filename);
			fd.setfilesize((double) new File(shareFolder + filename).length() / KILOBYTES);
			fd.setMD5value(FUtil.CheckMD5ForFile(filename));
			fd.setPeerid(ID);
			request.setRequestData(fd);
			out.writeObject(request);
			
			
			ResponseMessage response = (ResponseMessage) in.readObject();
			if (response.getResponseType() == ResponseMessageType.FAILURE) {
				System.out.println("File " + filename + " register failed\n");
				issucess = false;
			} else {
				System.out.print("File " + filename + " register sucess\n");
				issucess = true;
			}
			
		} catch (UnknownHostException unh) {
			System.out.println("cannot connect to an unknown host");
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (ClassNotFoundException ce) {
			ce.printStackTrace();
		} finally {
			try {
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return issucess;
	}
	
	/**
	 * unregister the file with filename
	 * @param filename
	 * @return
	 */
	private boolean unregisterFile (String filename) {
		boolean issucess = false;
		Socket socket = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		
		try {
			socket = new Socket (SERVER_IPADDR, SERVER_PORT);
			in = new ObjectInputStream (socket.getInputStream());
			
			out = new ObjectOutputStream (socket.getOutputStream());
			out.flush();
			
			RequestMessage request = new RequestMessage ();
			request.setRequestType(RequestMessageType.UNREGISTER);
			FileDescriptor fd = new FileDescriptor ();
			fd.setfilename(filename);
			fd.setPeerid(ID);
			request.setRequestData(fd);
			
			out.writeObject(request);
			out.flush();
			
			ResponseMessage response = new ResponseMessage();
			response = (ResponseMessage) in.readObject();
			
			if (response.getResponseType() == ResponseMessageType.FAILURE) {
				System.out.println("File " + filename + " unregister failed\n");
				issucess = false;
			} else {
				System.out.print("File " + filename + " unregister sucess\n");
				issucess = true;
			}
			
		} catch (UnknownHostException unh) {
			System.out.println("cannot connect to an unknown host");
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (ClassNotFoundException ce) {
			ce.printStackTrace();
		} finally {
			try {
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return issucess;
	}
	
	
	/**
	 * Once file is modified, will unregister older version, and register a new version to server
	 * @param filename
	 * @return
	 */
	private boolean updateFile (String filename) {
		if (unregisterFile(filename)) 
		{
			if (registerFile(filename))
				return true;
			else
				return false;
		}else {
			System.out.println("File " + filename + " update failed");
			return false;
		}
	}
	
	
	/**
	 * get all file names indexed in server side
	 * @return
	 * @throws IOException 
	 */
	private ArrayList<String> getAllIndexedFiles () {
		
		ArrayList<String> filelist = new ArrayList<String> ();
		Socket socket = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		
		try {
			socket = new Socket (SERVER_IPADDR, SERVER_PORT);
			in = new ObjectInputStream (socket.getInputStream());
			
			out = new ObjectOutputStream (socket.getOutputStream());
			out.flush();			
			
			RequestMessage request = new RequestMessage ();
			request.setRequestType(RequestMessageType.LISTALLFILES);
			out.writeObject(request);
			out.flush();
			
			ResponseMessage response = new ResponseMessage ();
			response = (ResponseMessage) in.readObject();
			
			filelist = (ArrayList<String>) response.getResponseData();
			
		} catch (UnknownHostException unh) {
			System.out.println("cannot connect to an unknown host");
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (ClassNotFoundException ce) {
			ce.printStackTrace();
		} finally {
			try {
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return filelist;
	}
	
	
	/**
	 * Look up file method
	 * @param filename
	 * @return
	 */
	private ArrayList<PeerDescriptor> lookupFile (String filename) {

		Socket socket = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		ArrayList<PeerDescriptor>peerswithfile = new ArrayList<PeerDescriptor> ();
		
		try {
			socket = new Socket (SERVER_IPADDR, SERVER_PORT);
			in = new ObjectInputStream (socket.getInputStream());
			
			out = new ObjectOutputStream (socket.getOutputStream());
			out.flush();

			
			RequestMessage request = new RequestMessage ();
			request.setRequestType(RequestMessageType.LOOKUP);
			request.setRequestData(filename);
			out.writeObject(request);
			out.flush();
			
			ResponseMessage response = new ResponseMessage ();
			response = (ResponseMessage) in.readObject();
			
			
			/**
			 * Multiple kinds of files with same file name
			 */
			if (response.getResponseType() == ResponseMessageType.INTERACTION) {		//multiple files with same filename exist in server
				ArrayList<Double> filesizelist = (ArrayList<Double>) response.getResponseData();
				System.out.println("There exist different files with filename: " + filename);
				for (int i = 0; i < filesizelist.size(); i++) {
					System.out.println(i + ". " + filesizelist.get(i) + " KB");
				}
//				Scanner reader = new Scanner(System.in);
				System.out.println("Choose which size you want to retrive");
				int choice = Integer.valueOf(reader.nextLine());
				System.out.print(choice);
				
				if (choice >= 0 && choice < filesizelist.size()) {
					RequestMessage request1 = new RequestMessage ();
					request1.setRequestType(RequestMessageType.VERIFY);
					request1.setRequestData(Integer.toString(choice));
					out.writeObject(request1);
					out.flush();
					
					response = (ResponseMessage) in.readObject();
					peerswithfile = (ArrayList<PeerDescriptor>) response.getResponseData();
					for (PeerDescriptor pd : peerswithfile) {
						System.out.println(pd.getBandwidth());
						System.out.println(pd.getID());
						System.out.println(pd.getIP());
					}
					
				} else {
					request.setRequestType(RequestMessageType.DISCONNECT);
					out.writeObject(request);
					out.flush();
					System.out.println("Wrong choice for file size");
				}
			} else if (response.getResponseType() == ResponseMessageType.SUCCESS) {		//only one kind of file with this filename
				peerswithfile = (ArrayList<PeerDescriptor>) response.getResponseData();
			} else {
				System.out.println("File lookup failed");
			}
			
		} catch (UnknownHostException unh) {
			System.out.println("cannot connect to an unknown host");
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (ClassNotFoundException ce) {
			ce.printStackTrace();
		} finally {
			try {
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return peerswithfile;
	}
	
	
}
