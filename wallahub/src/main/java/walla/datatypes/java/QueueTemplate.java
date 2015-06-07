package walla.datatypes.java;

public enum QueueTemplate {

	Log(1), Agg(2), NoAgg(3), Email(4), NewImage(5);
	
	private int value;
	
	private QueueTemplate(int value) 
	{
		this.value = value;
	}
	
	public int getValue() 
	{
		return this.value;
	}
}