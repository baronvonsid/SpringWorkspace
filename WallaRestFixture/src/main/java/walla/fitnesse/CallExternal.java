package walla.fitnesse;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CallExternal {

	private String output;
	private int exitCode;
	
	public CallExternal(String cmd) throws StopTestException {

		StringBuffer cmdOut = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			exitCode = p.exitValue();
			String line = "";
			while ((line = reader.readLine()) != null) {
				cmdOut.append(line + "\n");
			}
		} catch (Exception e) {
			throw new StopTestException("Failed to call command with error: " + e.getMessage());
		}

		output = cmdOut.toString();
	}

	public String output() {
		return output;
	}
	
	public int result() {
		return exitCode;
	}
}