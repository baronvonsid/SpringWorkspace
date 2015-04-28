package walla.datatypes.java;

public enum LogMethodType {

	Web(1), Message(2), Internal(3);
	
	private int value;
	
	private LogMethodType(int value) 
	{
		this.value = value;
	}
	
	public int getValue() 
	{
		return this.value;
	}
}
