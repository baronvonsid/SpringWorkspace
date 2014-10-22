package walla.datatypes.java;

public class CustomResponse {

	private int responseCode;
	private String message = null;
	
	public CustomResponse() {
	}
	
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}
	
	public int getResponseCode()
	{
		return this.responseCode;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
}
