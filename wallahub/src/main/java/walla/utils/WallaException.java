package walla.utils;

import org.apache.log4j.Logger;

import walla.business.TagService;
import org.springframework.http.HttpStatus;

public class WallaException extends Exception {

	private int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
	
	public WallaException() {
		
	}

	/*
	public WallaException(String theClass, String theMethod, String custom, String theError, int customHttpStatus) {
		super("Class:" + theClass + " Method:" + theMethod + " generated a Custom exception. Error message:" + theError);

		this.statusCode = (customHttpStatus == 0) ? HttpStatus.INTERNAL_SERVER_ERROR.value() : customHttpStatus;
	}
	*/
	
	public WallaException(String theClass, String theMethod, String theError, int customHttpStatus) {
		super("Class:" + theClass + " Method:" + theMethod + " generated a Custom exception. Error message:" + theError);

		this.statusCode = (customHttpStatus == 0) ? HttpStatus.INTERNAL_SERVER_ERROR.value() : customHttpStatus;
	}

	public WallaException(Exception ex, int customHttpStatus) {
		super(ex);
		
		this.statusCode = (customHttpStatus == 0) ? HttpStatus.INTERNAL_SERVER_ERROR.value() : customHttpStatus;
	}
	
	
	public WallaException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public WallaException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public WallaException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	public void setCustomStatus(int httpReturnStatus)
	{
		this.statusCode = httpReturnStatus;
	}
	
	public int getCustomStatus()
	{
		return this.statusCode;
	}
}
