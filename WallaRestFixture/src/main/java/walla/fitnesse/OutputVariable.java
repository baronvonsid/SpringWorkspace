package walla.fitnesse;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OutputVariable {

	private String output;
	private int exitCode;
	
	public OutputVariable(String variable) throws StopTestException {
		output = variable;
	}

	public String output() {
		return output;
	}
	
	public int result() {
		return exitCode;
	}
}