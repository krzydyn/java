package SOtests;

import java.text.DecimalFormat;
import java.util.Locale;

public class FormatNum {
	public static void main(String[] args)
	{
	    Locale.setDefault(Locale.ENGLISH);
	    DecimalFormat df = new DecimalFormat("##0.0E0");

	    String realOutput = df.format(12344);
	    String expected = "12.34E3";

	    System.out.println(realOutput);
	    if (realOutput.equals(expected))
	    {
	        System.out.println("OK");
	    }
	    else
	    {
	    	System.out.flush();
	        System.err.println("Formatted output " + realOutput + " differs from documented output " + expected);
	    }
	}
}
