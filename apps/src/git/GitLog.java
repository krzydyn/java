package git;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sys.Log;
import text.Text;
import ui.MainPanel;

@SuppressWarnings("serial")
public class GitLog extends MainPanel {
	static final String[] ArrayOfString0 = new String[0];
	static GitRepo repo = new GitRepo("~/sec-os/secos");

	static class Commit {
		String hash;
		String[] parentHash;
		String refs;
		String author;
		String date;
		String message;
	}

	private List<Commit> commits = new ArrayList<Commit>();
	private Map<String,Commit> hash = new HashMap<String, Commit>();

	public GitLog() {

	}

	private void parseLine(int from, int to) {

	}

	private void readBranch(String branch) throws Exception {
		commits.clear();
		hash.clear();

		String log=repo.log(branch + " --format=%h|%p|%d|%an|%ai|%s");
		int n=log.length();
		int rl=0;
		for (int i = 0; i < n; i=rl+1) {
			rl=log.indexOf("\n", i);
			if (rl < 0) rl = n;
			//Log.debug("record %d: %s", commits.size(), log.substring(i, rl));
			int r=0;
			Commit c=new Commit();
			int f=0;
			for (int j = i; j < rl; j=r+1,++f) {
				r=log.indexOf("|", j);
				if (r==-1 || f==5) r = rl;
				String fld = log.substring(j, r);

				if (f == 0) c.hash = fld;
				else if (f == 1) {
					if (fld.length() > 0) {
						String[] hl = fld.split(" ");
						c.parentHash = new String[hl.length];
						for (int ii=0; ii<hl.length; ++ii)
							c.parentHash[ii] = hl[ii];
					}
					else c.parentHash = ArrayOfString0;
				}
				else if (f == 2) c.refs = fld;
				else if (f == 3) c.author = fld;
				else if (f == 4) c.date = fld;
				else if (f == 5) c.message = fld;
			}

			Log.raw("%s: parents[%d]=(%s)", c.hash, c.parentHash.length, Text.join(c.parentHash, ","));
			commits.add(c);
			hash.put(c.hash, c);
		}
		Log.debug("commits %d", commits.size());
	}

	@Override
	protected void windowOpened() {
		String branch = "origin/devel/j.bialecki/openssl_2016.08.16";
		try {
			readBranch(branch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		start(GitLog.class);
	}
}
