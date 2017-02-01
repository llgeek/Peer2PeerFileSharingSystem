package Utilities;

import java.io.Serializable;

/**
 * Serialize the response message sent from server side
 * @author Linlin Chen
 *		  lchen96@hawk.iit.edu
 * Jan 20, 2017 7:45:54 PM
 */
public class ResponseMessage implements Serializable {
	private ResponseMessageType responseType;
	private Object responseData;
	
	public ResponseMessageType getResponseType () {
		return this.responseType;
	}
	public Object getResponseData () {
		return this.responseData;
	}
	public void setResponseType (ResponseMessageType responseType) {
		this.responseType = responseType;
	}
	public void setResponseData (Object responseData) {
		this.responseData = responseData;
	}
}
