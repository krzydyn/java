package activity;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import sys.Env;
import sys.Log;
import sys.XThread;
import text.Text;

/*
 * http://stackoverflow.com/questions/5206633/find-out-what-application-window-is-in-focus-in-java
 * http://stackoverflow.com/questions/6391439/getting-active-window-information-in-java
 */
public class ActivityMon {
	static interface ActivityMonitor {
		void run() throws Exception;
		void help();
	}
	static class LinuxMon implements ActivityMonitor {
		@Override
		public void help() {
		}
/*
LOG=$HOME/Documents/activity.log
PAID=""
while true; do
    sleep 2
    WID=`xprop -root _NET_ACTIVE_WINDOW | cut -d ' ' -f 5`
    AID=`xprop -id $WID WM_CLASS WM_NAME `
    if [ -z "$PAID" ]; then
        PAID=$AID
    fi
    if [ "$PAID" != "$AID" ]; then
        echo "$PAID" > /tmp/paid.txt
        echo "$AID" > /tmp/aid.txt
        PAID=$AID
    else
        continue;
    fi

    echo -n `date '+%Y%m%d%H%M%S'`": " >> $LOG
    if grep "not found" /tmp/aid.txt &> /dev/null ; then
        echo "Locked($WID)" >> $LOG
    else
        APP=`grep 'WM_CLASS' /tmp/aid.txt|sed 's/[^=]* = //'`
        TAB=`grep 'WM_NAME' /tmp/aid.txt|sed 's/[^=]* = //'`
        echo "Unlocked($WID)|$APP|$TAB" >> $LOG
    fi
done
*/

		String prev;
		@Override
		public void run() throws Exception {
			String s = Env.exec("xprop", "-root", "_NET_ACTIVE_WINDOW");
			int wid = parseWinId(s);
			if (wid < 0) throw new RuntimeException("can't ind wid in " + s);
			s = Env.exec("xprop", "-id", String.format("0x%x", wid));
			if (prev!=null) {
				if (!prev.equals(s)) {
					Log.notice("diff detected");
					Log.notice("%s", Text.diff(prev,s));
					Log.notice("diff done");
				}
				else Log.info("no diff");
			}
			prev=s;
		}
	}
	static class WindowsMon implements ActivityMonitor {
		@Override
		public void help() {
		}
		/*
		public interface XLib extends StdCallLibrary {
		    XLib INSTANCE = (XLib) Native.loadLibrary("XLib", Psapi.class);

		    int XGetInputFocus(X11.Display display, X11.Window focus_return, Pointer revert_to_return);
		}*/
		@Override
		public void run() throws Exception {
		}
	}
	static class MacosMon implements ActivityMonitor {
		@Override
		public void help() {
			System.out.println("To read window title the privilidge need to be unblocked in\n"
					+ "Mac system settings");
		}

		final static String script_appname="tell application \"System Events\"\n" +
                "name of application processes whose frontmost is true\n" +
                "end\n";
		/*
		 * need system config to allow access to assistive devices
		 */
		final static String script_title="global appProc,winTitle\n"
			+"tell application \"System Events\"\n"
		        + "set appProc to first application process whose frontmost is true\n"
		    + "end\n"
		    +"tell appProc\n"
		    +"if count of windows > 0 then\n"
		    	+"name of front window\n"
		    +"end if\n"
		    +"end tell\n";

		final static String script2="global frontApp, frontAppName, windowTitle\n"
			+ "set windowTitle to \"\"\n"
			+ "tell application \"System Events\"\n"
				+ "set frontApp to first application process whose frontmost is true\n"
				+ "set frontAppName name of frontApp\n"
				+ "tell process frontApp\n"
                	+ "tell (first window whose value of attribute \"AXMain\" is true)\n"
                	+ "set windowTitle to value of attribute \"AXTitle\"\n"
                	+ "end tell\n"
                + "end tell\n"
            + "end\n"
            + "return {frontAppName, windowTitle}\n";
		final static String script3 = "global appProc,appName\n"
			+ "tell application \"System Events\"\n"
		        + "set appProc to first application process whose frontmost is true\n"
		        + "set appName to name of appProc\n"
		    + "end\n"
		    +"tell appProc\n"
		    +"if count of windows > 0 then\n"
		    	+"set window_name to name of front window\n"
		    +"end if\n"
		    +"end tell\n"
		    +"return {appName,window_name}\n";
		final static String script4 = "tell application \"System Events\"\n"
			+ "set window_name to name of first window of (first application process whose frontmost is true)\n"
			+"end tell\n";
		@Override
		public void run() throws Exception {
			ScriptEngine se = new ScriptEngineManager().getEngineByName("AppleScript");
			System.out.println("script1");
			try {
				String result=Text.join("\n",se.eval(script_appname));
		        System.out.println(result);
			}
			catch (Exception e) {
				Log.error(e.getMessage());
			}
			System.out.println("script2");
			try {
				String result=Text.join("\n",se.eval(script_title));
		        System.out.println(result);
			}
			catch (Exception e) {
				Log.error(e.getMessage());
			}
		}
	}

	private static int parseWinId(String s) {
		Pattern p = Pattern.compile("0x([0-9a-f]+)");
		Matcher m = p.matcher(s);
		return m.find() ? Integer.parseInt(m.group(1),16) : -1;
	}


	static ActivityMonitor monitor = null;
	public static void main(String[] args) {
		String osname = Env.osName();
		if ("Linux".equalsIgnoreCase(osname)) {
			monitor = new LinuxMon();
		}
		else if ("Mac OS X".equals(osname)) {
			monitor = new MacosMon();
		}
		else if ("Windows".equalsIgnoreCase(osname)) {
			monitor = new WindowsMon();
		}
		else {
			Log.error("not supported os: '%s' %s", osname, Text.vis(osname));
			return ;
		}
		try {
			Calendar cal = Calendar.getInstance();
			cal.set(2015, 10, 10);
			//cal.
			System.out.println("dow:"+cal.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG,Locale.US));
			while (true) {
				try {
					monitor.run();
				}
				catch (Exception e) {
					Log.error(e);
				}
				XThread.sleep(5000);
			}
		}
		catch (Throwable e) {
			Log.error(e);
		}
	}
}
