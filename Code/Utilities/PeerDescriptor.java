package Utilities;

import java.io.Serializable;

/**
 * Peer descriptor, to descript a peer
 * used in lookup file stage
 * return peer id and ip address
 * @author Linlin Chen
 *		  lchen96@hawk.iit.edu
 * Jan 26, 2017 5:31:10 PM
 */
public class PeerDescriptor implements Serializable {
	private int peer_id;
	private String ip_addr;
	private int bandwidth;
	
	public PeerDescriptor (int id, String ip) {
		this.peer_id = id;
		this.ip_addr = ip;
	}
	public PeerDescriptor (int id, String ip, int bandwidth) {
		this.peer_id = id;
		this.ip_addr = ip;
		this.bandwidth = bandwidth;
	}
	public PeerDescriptor () {
		
	}
	public void setID (int id) {
		this.peer_id = id;
	}
	public void setIP (String ip) {
		this.ip_addr = ip;
	}
	public void setBandwidth (int bandwidth) {
		this.bandwidth = bandwidth;
	}
	public int getID () {
		return this.peer_id;
	}
	public String getIP () {
		return this.ip_addr;
	}
	public int getBandwidth () {
		return this.bandwidth;
	}
}
