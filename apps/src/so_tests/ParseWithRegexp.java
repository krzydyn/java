package so_tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseWithRegexp {

	static String str = "[amt is E234.98.valid 23/12/2013.Sample text  E134.95.valid 23/12/2015]";
	static String news = "<p><a href=\"https://news.yahoo.com/during-siege-orlando-gunman-told-police-islamic-soldier-034552865.html\">"
			+ "<img src=\"https://s1.yimg.com/bt/api/res/1.2/1aLfwfzLVx7.osxsV87uog--/YXBwaWQ9eW5ld3NfbGVnbztmaT1maWxsO2g9ODY7cT03NTt3PTEzMA--/http://media.zenfs.com/en_us/News/Reuters/2016-06-20T125326Z_1_LYNXNPEC5J0TN_RTROPTP_2_FLORIDA-SHOOTING.JPG\""
					+ "width=\"130\" height=\"86\""
					+ "alt=\"***A woman mourns as she sits on the ground and takes part in a vigil for the Pulse night club victims following last week&#039;s shooting in Orlando**\" align=\"left\""
					+ "title=\"**A woman mourns as she sits on the ground and takes part in a vigil for the Pulse night club victims following last week&#039;s shooting in Orlando***\" border=\"0\" />"
			+ "</a>"
			+ "The Florida nightclub killer called himself an &quot;Islamic soldier&quot; and threatened to strap hostages into explosive vests in calls with police during the three-hour siege, according to transcripts released by the FBI on Monday. In a first call he made to a 911 emergency operator, Mateen said &quot;I pledge allegiance to Abu Bakr al-Baghdadi, may God protect him, on behalf of the Islamic State,&quot; referring to the head of Islamic State. The FBI and U.S. State Department released partial transcripts of the four calls with the emergency operator and crisis negotiators earlier on Monday, omitting the shooter&#039;s references to the leader of Islamic State, saying they did not want to provide a platform for propaganda.</p><br clear=\"all\"/>";

	public static void main(String[] args) throws Exception {
		//Pattern p = Pattern.compile("E([0-9.]+)\\.valid");
		Pattern p = Pattern.compile("E([0-9]+\\.[0-9]+)");
		Matcher m = p.matcher(str);
		while (m.find()) {
			System.out.println("found: "+m.group(1));
		}


		System.out.println("news: "+news.replaceAll("<.*?>", ""));
		p = Pattern.compile("(alt|title).*?\"(.*?)\"");
		m = p.matcher(news);
		while (m.find()) {
			System.out.printf("%s: %s\n",m.group(1), m.group(2));
		}

	}

}
