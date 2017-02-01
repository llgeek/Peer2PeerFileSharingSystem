package Utilities;

import java.io.Serializable;

/**
 * This class is for socket transmission
 * transmit the file name, file md5 value and file size
 * @author Linlin Chen
 *		  lchen96@hawk.iit.edu
 * Jan 24, 2017 4:24:16 PM
 */
public class FileDescriptor implements Serializable{
	private String filename;
	private String MD5value;
	private double filesize;
	private int bandwidth;
	private int peer_id;
	
	public void setfilename (String filename) {
		this.filename = filename;
	}
	public void setMD5value (String MD5value) {
		this.MD5value = MD5value;
	}
	public void setfilesize (double filesize) {
		this.filesize = filesize;
	}
	public void setBandwidth (int bandwidth) {
		this.bandwidth = bandwidth;
	}
	public void setPeerid (int peer_id) {
		this.peer_id = peer_id;
	}
	public String getFilename () {
		return this.filename;
	}
	public String getMD5Value () {
		return this.MD5value;
	}
	public double getFileize () {
		return this.filesize;
	}
	public int getBandwidth () {
		return this.bandwidth;
	}
	public int getPeerid () {
		return this.peer_id;
	}
}
