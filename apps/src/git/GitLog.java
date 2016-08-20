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

	private Commit parseRecord(String log,int from, int to) {
		Commit c=new Commit();
		int f=0,r;
		for (int j = from; j < to; j=r+1,++f) {
			r=log.indexOf("|", j);
			if (r==-1 || f==5) r = to;
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
		return c;
	}

	private void readBranch(String branch) throws Exception {
		commits.clear();
		hash.clear();

		String log=repo.log(branch + " --topo-order --format=%h|%p|%d|%an|%ai|%s");
		int n=log.length();
		int rl=0;
		for (int i = 0; i < n; i=rl+1) {
			rl=log.indexOf("\n", i);
			if (rl < 0) rl = n;
			//Log.debug("record %d: %s", commits.size(), log.substring(i, rl));

			Commit c=parseRecord(log, i, rl);

			//Log.raw("%s: parents[%d]=(%s)", c.hash, c.parentHash.length, Text.join(c.parentHash, ","));
			commits.add(c);
			hash.put(c.hash, c);
		}
		Log.debug("commits %d", commits.size());
	}
	private void drawTree() {
		List<Commit> cols=new ArrayList<Commit>();
		StringBuilder buf = new StringBuilder();
		//int limit=80;
		for (Commit c : commits) {
			//if (--limit <= 0) break;
			buf.setLength(0);
			if (cols.size()==0) {
				cols.add(c);
			}

			//remove forks, leave one
			boolean cont=false;
			for (int i=0; i < cols.size(); ++i) {
				Commit ci = cols.get(i);
				if (ci==c) {
					if (cont){cols.remove(i);--i;}
					else cont=true;
				}
			}

			for (Commit ci : cols) {
				if (ci==c) buf.append("* ");
				else buf.append("| ");
			}
			buf.append(String.format("%s (%s) %s", c.hash, Text.join(c.parentHash, ","), c.message));
			Log.raw("%s", buf.toString());

			// add parents (create forks)
			if (c.parentHash.length > 0) {
				int i = cols.indexOf(c);
				if (i < 0) continue;
				cols.remove(i);
				for (String ph : c.parentHash) {
					cols.add(i, hash.get(ph));
					++i;
				}
			}
			else cols.remove(c);
		}
	}

	@Override
	protected void windowOpened() {
		//String branch = "origin/devel/j.bialecki/openssl_2016.08.16";
		String branch = "origin/devel/k.debski/openssl-20160801";
		try {
			readBranch(branch);
			drawTree();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		start(GitLog.class);
	}
}
