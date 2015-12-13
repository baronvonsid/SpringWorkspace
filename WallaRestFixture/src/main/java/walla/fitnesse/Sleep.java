package walla.fitnesse;


public class Sleep {
	
	public Sleep() {
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {}
	}
	
	public Sleep(int duration) {
		try {
			Thread.sleep(duration * 1000);
		} catch (InterruptedException e) {}
	}
	
	
}