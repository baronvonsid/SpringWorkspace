package walla.fitnesse;



import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Date {

	private int dateDiff = 0;
	private boolean weekdaysOnly = true;

	public Date() { }
	
	public Date(int dateDiff) {
		this.dateDiff = dateDiff;
	}
	
	public Date(int dateDiff, boolean weekdaysOnly) {
		this.dateDiff = dateDiff;
		this.weekdaysOnly = weekdaysOnly;
	}
	
	public String get(int dateDiff) {
		this.dateDiff = dateDiff;
		return get();
	}
	
	public String get() {
		int sign = dateDiff > 0 ? 1 : -1;
		dateDiff *= sign;
		Calendar c = Calendar.getInstance();
		for (int i = 0; i < dateDiff; i++) {
			c.add(Calendar.DATE, sign);
			if (weekdaysOnly) {
				while (c.get(Calendar.DAY_OF_WEEK) == 1 || c.get(Calendar.DAY_OF_WEEK) == 7) {
					c.add(Calendar.DATE, sign);
				}
			}
		}
		
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}
}