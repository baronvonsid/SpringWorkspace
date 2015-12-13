package walla.fitnesse;

public class StopTestException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public StopTestException(String message) {
		super(message);
	}
}