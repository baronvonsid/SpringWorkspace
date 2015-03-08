package walla.datatypes.java;

public enum AccountStatus {

	Init(1), Live(2), Frozen(3), Closing(4), Closed(5);
	
	private int value;
	
	private AccountStatus(int value) 
	{
		this.value = value;
	}
	
	public int getValue() 
	{
		return this.value;
	}
}
