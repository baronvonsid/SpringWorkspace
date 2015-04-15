package walla.datatypes.java;

public enum ActionType {

	Account(1), UserApp(2), Gallery(3);
	
	private int value;
	
	private ActionType(int value) 
	{
		this.value = value;
	}
	
	public int getValue() 
	{
		return this.value;
	}
}
