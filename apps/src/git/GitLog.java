package git;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import svg.Svg;
import svg.SvgPath;
import sys.Log;
import text.Text;
import ui.MainPanel;

@SuppressWarnings("serial")
public class GitLog extends MainPanel {
	static final String[] ArrayOfString0 = new String[0];
	static GitRepo repo = new GitRepo("~/sec-os/secos");
	//static GitRepo repo = new GitRepo("~/tmp/nuclear-js");

	static class Commit {
		String hash;
		String[] parentHash;
		String refs;
		String author;
		String date;
		String message;
		Point cp;
		List<Point> points=new ArrayList<Point>();
		int cols;
		String color;
	}

	static class Column {
		Column(String color,Commit c) {
			this.color=color;
			this.c=c;
		}
		Column(Column o) {
			color = o.color;
			c = o.c;
		}
		@Override
		public String toString() {
			return c.hash;
		}

		final String color;
		Commit c;
	}

	private final List<Commit> commits = new ArrayList<Commit>();
	private final Map<String,Commit> hash = new HashMap<String, Commit>();

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
			Commit c=parseRecord(log, i, rl);
			commits.add(c);
			hash.put(c.hash, c);
		}
		Log.debug("commits %d", commits.size());
	}
	private int findCol(List<Column>cols, Commit cmt) {
		for (int i=0; i < cols.size(); ++i) {
			Column c=cols.get(i);
			if (c!=null && c.c == cmt) return i;
		}
		return -1;
	}
	void addPoint(Commit c, Point p) {
		//Log.info(1,"addPoint %s %d, %d", c.hash, p.x,p.y);
		c.points.add(p);
	}
	static int X0=10, DX=20, DY=20;

	private void genGitGraph() {
		int limit=100;

		List<Column> pcols=new ArrayList<Column>();
		List<Column> cols=new ArrayList<Column>();

		int cy=10-DY;
		for (Commit cmt : commits) {
			cy += DY;
			//if (--limit <= 0) break;

			//Log.raw("commit: %s | %s", cmt.hash, Text.join(cmt.parentHash, " "));

			Point cp;
			int cf=findCol(cols, cmt);
			if (cf<0) {
				Log.debug("** Start of branch '%s'", cmt.hash);
				cols.add(new Column(getColor(), cmt));
				cf = cols.size()-1;
				cp = new Point(X0+cf*DX, cy);
			}
			else {
				cp = new Point(X0+cf*DX, cy);
				for (int i=cf+1; i<cols.size(); ++i) {
					Column c=cols.get(i);
					if (c==null) continue;
					if (c.c==cmt) {
						pcols.get(i).c.points.add(cp);
						retColor(pcols.get(i).c.color);
						cols.set(i,null);
					}
				}
			}

			for (int i=cols.size(); i>cf; ) {
				--i;
				if (cols.get(i) == null) cols.remove(i);
			}
			//Log.raw("cols: %s", Text.join(cols, " "));
			for (int i=0; i<cols.size(); ++i) {
				if (cols.get(i)!=null)
					addPoint(cols.get(i).c, new Point(X0+i*DX,cy));
			}

			cmt.cp=cp;
			cmt.color = cols.get(cf).color;
			cmt.cols = cols.size();

			pcols.clear();
			for (Column c:cols) {
				if (c==null) pcols.add(null);
				else pcols.add(new Column(c));
			}

			if (cmt.parentHash.length==0) {
				retColor(cmt.color);
				cols.set(cf, null);
				Log.debug("** End of branch %s",cmt.hash);
			}
			else {
				Commit c = hash.get(cmt.parentHash[0]);
				if (c.points.isEmpty()){
					addPoint(c, cp);
					cols.get(cf).c = c;
				}
				else {
					cmt.points.add(c.points.get(c.points.size()-1));
					retColor(cmt.color);
					cols.set(cf, null);
				}
				for (int i=1; i < cmt.parentHash.length; ++i) {
					c=hash.get(cmt.parentHash[i]);
					if (c.points.isEmpty()){
						cols.add(cf+i, new Column(getColor(),c));
						pcols.add(cf+i, null);
						addPoint(c, cp);
					}
					else {
						cmt.points.add(c.points.get(c.points.size()-1));
					}
				}
				for (int i=cols.size(); i>0; ) {
					--i;
					if (cols.get(i) == null) cols.remove(i);
				}
			}
		}

		Svg svg = new Svg();
		svg.strokeWidth(2);
		for (Commit cmt : commits) {
			if (cmt.cp == null) {
				Log.error("commit not located %s",cmt.hash);
				break;
			}

			if (cmt.points.size() > 0) {
				Point p0=cmt.points.get(0);
				SvgPath path = svg.path();
				path.fill("none").stroke(cmt.color);
				path.moveTo(p0.x, p0.y);
				for (int i = 1; i < cmt.points.size(); ++i) {
					Point p=cmt.points.get(i);
					path.lineTo(p.x, p.y);
				}
			}

			if (cmt.parentHash.length == 0) {
				Log.debug("EOB: %d,%d %s | %s", cmt.cp.x, cmt.cp.y, cmt.hash, cmt.message);
			}

			svg.circle(cmt.cp.x, cmt.cp.y, 4).fill("blue");
			svg.text(X0+cmt.cols*DX, cmt.cp.y+6).print(cmt.hash + " | " +
					Text.join(cmt.parentHash," ") + " | " + cmt.message);
		}
		try (OutputStream os=new FileOutputStream("git.html")) {
			svg.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.debug("svg done");
	}

	void genlog() {
		//String branch = "e9973a9";
		//String branch = "origin/devel/k.debski/openssl-20160801";
		String branch = "origin/devel/anchit/gatekeeper";
		try {
			readBranch(branch);
			genGitGraph();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//start(GitLog.class);
		new GitLog().genlog();
	}

	static String getColor() {
		if (colorAvail.isEmpty()) {
			return colors[0];
		}
		String c = colorAvail.remove(0);
		//Log.raw("use col[%d] %s, avail %d",colorAll.indexOf(c),c,colorAvail.size());
		return c;
	}
	static void retColor(String c) {
		if (!colorAvail.contains(c)) {
			colorAvail.add(0,c);
			//Log.raw("free col[%d] %s, avail %d",colorAll.indexOf(c),c,colorAvail.size());
		}
	}
	static final String[] colors = {
		//"#6963FF", "#47E8D4", "#6BDB52", "#E84BA5", "#FFA657",
		"#6963FF", //blue
		"#e11d21", //red
		"#FFA657", //yellow
		"#eb6420",
		"#fbca04",
		"#009800",
		"#006b75",
		"#207de5",
		"#0052cc",
		"#5319e7",
		"#f7c6c7",
		"#fad8c7",
		//"#fef2c0",
		"#bfe5bf",
		"#c7def8",
		"#bfdadc",
		"#bfd4f2",
		"#d4c5f9",
		"#cccccc",
		"#84b6eb",
		"#e6e6e6",
		"#cc317c"
	};
	static List<String> colorAll = new ArrayList<String>();
	static List<String> colorAvail = new ArrayList<String>();
	static {
		Collections.addAll(colorAvail, colors);
		Collections.addAll(colorAll, colors);
	}
}
