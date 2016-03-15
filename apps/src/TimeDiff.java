
public class TimeDiff {

	public static void main(String[] args) {
		long t1 = System.currentTimeMillis()/1000;
		long t2 = t1 + 65*60; //add 65 minutes
		
		long diffSeconds = t2 - t1;
		long hours_l = diffSeconds/3600;
		float hours_f = diffSeconds/3600f;
		System.out.printf("hours: %d %f\n", hours_l, hours_f);
	}
}
