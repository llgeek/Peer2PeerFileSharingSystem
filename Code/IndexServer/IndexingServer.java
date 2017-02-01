package IndexServer;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.net.Socket;
import java.net.ServerSocket;

import Utilities.*;



/**
 * This class is built for central index server
 * 
 * @author Linlin Chen
 *		  lchen96@hawk.iit.edu
 * Jan 18, 2017 3:53:04 PM
 */

/*
 * class for index server
 */
public class IndexingServer {
	/*
	 * define the value for indexed contents
	 */
	private static class indexValue{
		int peer_id;	//peer id
		String ip_addr;	//peer address
		int bandwidth;	//peer network bandwidth
		double filesize;	//size of the file
		
		public indexValue(int peer_id, String ip_addr, int bandwidth){
			this.peer_id = peer_id;
			this.ip_addr = ip_addr;
			this.bandwidth = bandwidth;
			//this.filesize = filesize;
		}
		public boolean containsPeer (int peer_id, String ip_addr) {
			if (this.peer_id == peer_id && this.ip_addr.equalsIgnoreCase(ip_addr)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/*
	 * MD5 stored as key, index value as value
	 */
	private static class MD52indexvalue {
		 private ArrayList< indexValue> md5_info;		//md5 as key, peer info as value
		 private String MD5value;
		 private double filesize;
		 
		 private static int PEER_OWNING_NUM = 0;		//number of peers having this file
		 
		 public MD52indexvalue (String md5value, int peer_id, String ip_addr, int bandwidth, double filesize) {
			 this.MD5value = md5value;
			 this.filesize = filesize;
			 md5_info = new ArrayList<indexValue> ();
			 indexValue iv = new indexValue (peer_id, ip_addr, bandwidth);
			 md5_info.add(iv);	
			 PEER_OWNING_NUM ++;
			 //md5_info.put(this.MD5value, al);
		 }
		 
		 public double getFilesize () {
			 return this.filesize;
		 }
		 public int getPeerOwningNum () {
			 return this.PEER_OWNING_NUM;
		 }
		 public String getMD5value () {
			 return this.MD5value;
		 }
		 
		 public void addIndexValue (int peer_id, String ip_addr, int bandwidth) {
			 md5_info.add(new indexValue(peer_id, ip_addr, bandwidth));
			 PEER_OWNING_NUM ++;
		 }
		 public boolean deleteIndexValue (int peer_id, String ip_addr) {
			 boolean isdelete = false;
			 Iterator<indexValue> iter = md5_info.iterator();
			 while (iter.hasNext()) {
				 if (iter.next().containsPeer(peer_id, ip_addr)){
					 iter.remove();
					 PEER_OWNING_NUM --;
					 isdelete = true;
				 }
			 }			
			 return isdelete;
		 }
		 
		 public boolean alreadyIndexed (int peer_id, String ip_addr) {
			 boolean isindexed = false;
			 for (indexValue iv : md5_info) {
				 if (iv.containsPeer(peer_id, ip_addr)) {
					 isindexed = true;
					 break;
				 }
			 }
			 return isindexed;
		 }
		 
		 public ArrayList<PeerDescriptor> getallpeers() {
			 ArrayList<PeerDescriptor> pdlist = new ArrayList<PeerDescriptor>();
			 for (indexValue iv : md5_info) {
				 PeerDescriptor pd = new PeerDescriptor();
				 pd.setID(iv.peer_id);
				 pd.setBandwidth(iv.bandwidth);
				 pd.setIP(iv.ip_addr);
				 pdlist.add(pd);
			 }
			 return pdlist;
		 }
	}
	
	
	private static ConcurrentHashMap <String, HashMap<String, MD52indexvalue> > indexedDatabase = new ConcurrentHashMap<String, HashMap<String, MD52indexvalue> > ();
	private static int connectedpeers = 0;	//count the number of currently connected peers
	
	private static int SERVER_PORT;		//port for server to listen
//	private static int SERVER_REGISTER_PORT;		//register port for server to listen, load from config file
//	private static int SERVER_LOOKUP_PORT;			//lookup port for server to listen, load from config file
	private static String SERVER_IPADDR;		//server ip address
	private static final String CONFIG_FILE = "./config.properties";
	private static int BASE_PORT;		//base port for client acts as a server
	
	
	/**
	 * default constructor, automatically load the configurations from the config file
	 */
	public IndexingServer() {	
		try {
			FileInputStream configfile = new FileInputStream(CONFIG_FILE);
			Properties config = new Properties();
			config.load(configfile);
			this.SERVER_IPADDR = config.getProperty("SERVERIPADDR");
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

		serverListener();
//		registerListener();
//		lookupListener();
	}
	
	
	/**
	 * constructor, with assigned ip_addr and port, for server.
	 * @param ipaddr: ip address
	 * @param port: port
	 */
	public IndexingServer(String ipaddr, int port, int base_port) {
		this.SERVER_IPADDR = ipaddr;
		this.SERVER_PORT = port;
//		this.SERVER_REGISTER_PORT = register_port;
//		this.SERVER_LOOKUP_PORT = lookup_port;
		this.BASE_PORT = base_port;

		serverListener();
//		registerListener();
//		lookupListener();
	}
	
	
	
	public static void main (String[] args) {
		
		
		System.out.println("*********************************************************************");
		System.out.println("*			Napster Style Peer to Peer File Sharing System			*");
		System.out.println("*********************************************************************");
		
		IndexingServer server = new IndexingServer ();
		
		
	}
	
	/**
	 * register method for a file
	 * @param filename: file name
	 * @param md5value: md5value of the file
	 * @param peer_id: peer id owning this file
	 * @param ip_addr: peer ip address
	 * @param bandwidth: the bandwidth of the peer
	 * @param filesize: file size
	 * @return
	 */
	
	private boolean registerFile (String filename, String md5value, int peer_id, String ip_addr, int bandwidth, double filesize) {
		if (indexedDatabase.containsKey(filename)) {
			if (indexedDatabase.get(filename).containsKey(md5value)) {
				if (!indexedDatabase.get(filename).get(md5value).alreadyIndexed(peer_id, ip_addr))
					indexedDatabase.get(filename).get(md5value).addIndexValue(peer_id, ip_addr, bandwidth);
			} else {
				indexedDatabase.get(filename).put(md5value, new MD52indexvalue(md5value, peer_id, ip_addr, bandwidth, filesize));
			}
		} else {
			HashMap<String, MD52indexvalue> hm = new HashMap<String, MD52indexvalue> ();
			hm.put(md5value, new MD52indexvalue(md5value, peer_id, ip_addr, bandwidth, filesize));
			indexedDatabase.put(filename,hm);
		}
		return true;
	}
	
	
	/**
	 * unregister method for unregister file requested from peer side
	 * @param filename: file name to be unregister
	 * @param peer_id: peer id
	 * @param ip_addr: peer ip address
	 * @return
	 */
	
	private boolean unregisterFile (String filename, int peer_id, String ip_addr) {
		boolean issucess = false;
		if (!indexedDatabase.containsKey(filename)) {	//whether database contains this filename
			System.out.println("Index database have no file " + filename);
			issucess = false;
		} else {
			Iterator <Map.Entry<String, MD52indexvalue>> it = indexedDatabase.get(filename).entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, MD52indexvalue> entry = it.next();
				if (entry.getValue().deleteIndexValue(peer_id, ip_addr)) {	//true, when one file is deleted; false if no file is deleted
					if (entry.getValue().getPeerOwningNum() <= 0) {		//if no peer owns this file, accoding to MD5, delete this record in database
						it.remove();
					}
					issucess = true;
				} else {
					issucess = false;
				}
			}
		}
		//System.out.println("Index database have no file " + filename + " for peer " + peer_id);
		return issucess;
	}
	
	
	
	/***
	 * The private class for implement register file thread
	 * @author Linlin Chen
	 *		  lchen96@hawk.iit.edu
	 * Jan 20, 2017 4:43:11 PM
	 */
	private class serverThread implements Runnable {
		private Socket socket;
		
		public serverThread (Socket socket) {
			this.socket = socket;
			//this.clientID = clientID;
			System.out.println("\n\nNew socket connection with client: "  + "at " + socket.getInetAddress().getHostAddress());
			connectedpeers ++;
			
		}
		
		public void run() {
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
				
				String clientIP = socket.getInetAddress().getHostAddress();
				
				RequestMessage peerRequestMessage = new RequestMessage();
				RequestMessageType peerRequestMessageType = null;
				ResponseMessage peerResponseMessage = new ResponseMessage();
				
				in = new ObjectInputStream(socket.getInputStream());
				peerRequestMessage = (RequestMessage) in.readObject();
				peerRequestMessageType = peerRequestMessage.getRequestType();
					
				switch (peerRequestMessageType) {
				/**
				 * register service
				 */
				case REGISTER:
					System.out.println("**********  File registering service	*********");
					FileDescriptor rfd = (FileDescriptor) peerRequestMessage.getRequestData();
					if (registerFile(rfd.getFilename(), rfd.getMD5Value(), rfd.getPeerid(), clientIP, rfd.getBandwidth(), rfd.getFileize())) {
						System.out.println("File " + rfd.getFilename() + " register sucess");
						peerResponseMessage.setResponseType(ResponseMessageType.SUCCESS);
						out.writeObject(peerResponseMessage);
						out.flush();
					} else {
						System.out.println("File " + rfd.getFilename() + " register failed");
						peerResponseMessage.setResponseType(ResponseMessageType.FAILURE);
						out.writeObject(peerResponseMessage);
						out.flush();
					}
					break;
					/**
					 * unregister service 
					 */
				case UNREGISTER:
					System.out.println("**********  File unregistering service	*********");
					FileDescriptor urfd = (FileDescriptor) peerRequestMessage.getRequestData();
					if (unregisterFile(urfd.getFilename(), urfd.getPeerid(), clientIP)) {
						System.out.println("File " + urfd.getFilename() + " unregister sucess");
						peerResponseMessage.setResponseType(ResponseMessageType.SUCCESS);
						out.writeObject(peerResponseMessage);
						out.flush();
					} else {
						System.out.println("File " + urfd.getFilename() + " unregister failed");
						peerResponseMessage.setResponseType(ResponseMessageType.FAILURE);
						out.writeObject(peerResponseMessage);
						out.flush();
					}
					break;
					/**
					 * Look up file request
					 * will return multiple choices if server indexed different kinds of files with same file name
					 */
				case LOOKUP:
					System.out.println("**********  File lookup service	*********");
					String filename = peerRequestMessage.getRequestData().toString();
					if (indexedDatabase.containsKey(filename)) {
						int fileclassnum = indexedDatabase.get(filename).size();		//the number of files having the same file name
						if (fileclassnum > 1) {	//more than one kind of files having same file name
							ArrayList<Double> filesizelist = new ArrayList<Double> ();
							ArrayList<String> filehash = new ArrayList<String> ();
							Iterator <Map.Entry<String, MD52indexvalue>> it = indexedDatabase.get(filename).entrySet().iterator();
							while (it.hasNext()) {
								Map.Entry<String, MD52indexvalue> entry = it.next();
								filesizelist.add(entry.getValue().getFilesize());
								filehash.add(entry.getKey());
							}
							peerResponseMessage.setResponseType(ResponseMessageType.INTERACTION);
							peerResponseMessage.setResponseData(filesizelist);
							out.writeObject(peerResponseMessage);
							out.flush();
							
							peerRequestMessage = (RequestMessage) in.readObject();
							if (peerRequestMessage.getRequestType() == RequestMessageType.DISCONNECT) {
								System.out.println("disconnect");
								break;
							} else {
								ResponseMessage peerResponseMessage1 = new ResponseMessage (); 
								int hashindex = Integer.valueOf((String)peerRequestMessage.getRequestData());
								ArrayList<PeerDescriptor> pdlist = indexedDatabase.get(filename).get(filehash.get(hashindex)).getallpeers();
								peerResponseMessage1.setResponseType(ResponseMessageType.SUCCESS);
								peerResponseMessage1.setResponseData(pdlist);
								out.writeObject(peerResponseMessage1);
								out.flush();	
								for (PeerDescriptor pd : pdlist) {
									System.out.println(pd.getBandwidth());
									System.out.println(pd.getID());
									System.out.println(pd.getIP());
								}
							}
							
						} else{		//only one kind of file with file name, return directly
							String hash = (String) indexedDatabase.get(filename).keySet().toArray()[0];
							ArrayList<PeerDescriptor> pdlist = indexedDatabase.get(filename).get(hash).getallpeers();
							peerResponseMessage.setResponseType(ResponseMessageType.SUCCESS);
							peerResponseMessage.setResponseData(pdlist);
							out.writeObject(peerResponseMessage);
							out.flush();	
						}
					} else {
						peerResponseMessage.setResponseType(ResponseMessageType.FAILURE);
						peerResponseMessage.setResponseData("No such file indexed");
						out.writeObject(peerResponseMessage);
						out.flush();
						System.out.println("No such file "+ filename + " indexed");
					}
					break;
					
					/**
					 * List all indexed files in server
					 */
				case LISTALLFILES:
					ArrayList<String> allindexedfiles =  Collections.list(indexedDatabase.keys());
					peerResponseMessage.setResponseType(ResponseMessageType.SUCCESS);
					peerResponseMessage.setResponseData(allindexedfiles);
					out.writeObject(peerResponseMessage);
					out.flush();
					break;
					
				default:
					System.out.println("Incorrect Request From Client " + clientIP);
					peerResponseMessage.setResponseType(ResponseMessageType.WRONGREQUEST);
					out.writeObject(peerResponseMessage);
					out.flush();
					break;
							
				}
			Thread.currentThread().interrupt();	
			System.out.println("Disconnect with " + socket.getInetAddress().getHostAddress() + "...\n\n");
			} catch (IOException ie) {
				System.out.println("IOException");
				ie.printStackTrace();
			} catch (ClassNotFoundException e){
				e.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				} catch (IOException ie) {
					ie.printStackTrace();
				}
					
			}
		}
	}
	

	
	/**
	 * Listen for any request from client
	 */
	
	private void serverListener () {
		ServerSocket serversocket = null;
		try {
			serversocket = new ServerSocket (this.SERVER_PORT);
			while (true) {
				new Thread (new serverThread (serversocket.accept())).start();
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			try {
				serversocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Socket closed unsucessfully");
			}
		}
	}
	


}

