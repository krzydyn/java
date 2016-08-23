package git;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import svg.Svg;
import sys.Log;
import text.Text;
import ui.MainPanel;

@SuppressWarnings("serial")
public class GitLog extends MainPanel {
	static final String[] ArrayOfString0 = new String[0];
	//static GitRepo repo = new GitRepo("~/sec-os/secos");
	static GitRepo repo = new GitRepo("~/tmp/nuclear-js");

	static class Commit {
		String hash;
		String[] parentHash;
		String refs;
		String author;
		String date;
		String message;
	}

	static class Column {
		Column(String c) {this.color=c;}
		Commit c;
		final String color;
		@Override
		public String toString() {
			return c.hash;
		}
	}

	private final List<Commit> commits = new ArrayList<Commit>();
	private final Map<String,Commit> hash = new HashMap<String, Commit>();

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
	private int findCol(List<Column>cols, Commit cmt) {
		for (int i=0; i < cols.size(); ++i) {
			if (cols.get(i).c == cmt) return i;
		}
		return -1;
	}
	private void drawTree() {
		int X0=10,DX=20, DY=40;
		int limit=50;

		//List<Color> colors=new ArrayList<Color>();
		List<Column> pcols=new ArrayList<Column>();
		List<Column> cols=new ArrayList<Column>();

		int cy=10-DY;
		Svg svg = new Svg();
		svg.strokeWidth(2);
		for (Commit cmt : commits) {
			cy += DY;
			if (--limit <= 0) break;

			int cf=findCol(cols, cmt);
			if (cf<0) {
				cols.add(newCol(cmt));
				cf = cols.size()-1;
			}
			else {
				for (int i=cf+1; i<cols.size(); ++i) {
					if (cols.get(i).c==cmt) {
						cols.remove(i); --i;
					}
				}
			}
			Log.raw("pcols: %s", Text.join(pcols, " "));
			Log.raw("cols: %s", Text.join(cols, " "));
			Log.raw("--------------------");

			for (int i=0; i < cols.size(); ++i) {
				Column ci = cols.get(i);
				for (int j=0; j < pcols.size(); ++j) {
					Column cj = pcols.get(j);
					if (ci.c.hash.equals(cj.c.hash)) {
						svg.path().moveTo(X0+j*DX, cy-DY).lineRel((i-j)*DX, DY).stroke(cj.color);
					}
				}
			}

			svg.circle(X0+cf*DX, cy, 5);
			svg.text(X0+cols.size()*DX, cy+6).print(cmt.hash + " | " +
					Text.join(cmt.parentHash," ") + " | " + cmt.message);

			while (pcols.size() > 0) {
				Column c=pcols.get(0);
				if (!cols.contains(c)) delCol(c);
				pcols.remove(0);
			}
			pcols.addAll(cols);

			if (cmt.parentHash.length==0) {
				cols.remove(cf);
			}
			else {
				cols.get(cf).c = hash.get(cmt.parentHash[0]);
				for (int i=1; i < cmt.parentHash.length; ++i) {
					cols.add(cf+i, newCol(hash.get(cmt.parentHash[i])));
				}
			}
		}
		try {
			svg.write(new FileOutputStream("git.html"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Log.debug("svg done");
	}

	void genlog() {
		String branch = "origin/master";
		//String branch = "origin/devel/k.debski/openssl-20160801";
		try {
			readBranch(branch);
			drawTree();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//start(GitLog.class);
		new GitLog().genlog();
	}

	static Column newCol(Commit cmt) {
		Column c = columns.remove(columns.size()-1);
		c.c=cmt;
		return c;
	}
	static void delCol(Column c) {
		columns.add(c);
		c.c=null;
	}
	static String[] c1 = { "#6963FF", "#47E8D4", "#6BDB52", "#E84BA5", "#FFA657"};
	static String[] c2 = {
			"#e11d21",
		    //"#eb6420",
		    "#fbca04",
		    "#009800",
		    "#006b75",
		    "#207de5",
		    "#0052cc",
		    "#5319e7",
		    "#f7c6c7",
		    "#fad8c7",
		    "#fef2c0",
		    "#bfe5bf",
		    "#c7def8",
		    "#bfdadc",
		    "#bfd4f2",
		    "#d4c5f9",
		    "#cccccc",
		    "#84b6eb",
		    "#e6e6e6",
		    "#ffffff",
		    "#cc317c"
	};
	static List<Column> columns=new ArrayList<GitLog.Column>();
	static {
		for (int i=0; i < c1.length; ++i)
			columns.add(new Column(c1[i]));
	}

}
