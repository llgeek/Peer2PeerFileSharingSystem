package Utilities;

import java.io.Serializable;

/**
 * Serialize the request message sent from client side
 * @author Linlin Chen
 *		  lchen96@hawk.iit.edu
 * Jan 20, 2017 7:45:26 PM
 */
public class RequestMessage implements Serializable {
	private RequestMessageType requestType;
	private Object requestData;
	
	public RequestMessageType getRequestType () {
		return this.requestType;
	}
	public Object getRequestData () {
		return this.requestData;
	}
	public void setRequestType (RequestMessageType requestType) {
		this.requestType = requestType;
	}
	public void setRequestData (Object requestData) {
		this.requestData = requestData;
	}
}
