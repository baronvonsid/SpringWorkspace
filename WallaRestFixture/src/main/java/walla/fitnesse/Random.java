package walla.fitnesse;

import java.util.UUID;

public class Random {
	
	private int length;
	
	public Random() {
		length = 10;
	}
	
	public Random(int length) {
		this.length = length;
	}
	
	public String get() {
	    StringBuffer buffer = new StringBuffer();
	    while (buffer.length() < length) {
	        buffer.append(UUID.randomUUID().toString().replaceAll("-", ""));
	    }

	    return buffer.substring(0, length);  
	}
}