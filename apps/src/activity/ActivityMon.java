package activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sys.Env;
import sys.Log;
import sys.XThread;
import text.Text;

public class ActivityMon {
	static interface ActivityMonitor {
		void run() throws Exception;
	}
	static class LinuxMon implements ActivityMonitor {
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
		public void run() throws Exception {
		}

	}

	static ActivityMonitor monitor = null;
	public static void main(String[] args) {
		if ("Linux".equalsIgnoreCase(Env.osName())) {
			monitor = new LinuxMon();
		}
		else if ("Windows".equalsIgnoreCase(Env.osName())) {
			monitor = new WindowsMon();
		}
		else {
			Log.error("not supported os: %s", Env.osName());
			return ;
		}
		try {
			while (true) {
				monitor.run();
				XThread.sleep(1000);
			}
		}
		catch (Throwable e) {
			Log.error(e);
		}
	}

	private static int parseWinId(String s) {
		Pattern p = Pattern.compile("0x([0-9a-f]+)");
		Matcher m = p.matcher(s);
		return m.find() ? Integer.parseInt(m.group(1),16) : -1;
	}
}
