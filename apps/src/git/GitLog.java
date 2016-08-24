package git;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	static int X0=10, DX=20, DY=20;
	private void drawTree() {
		int limit=80;

		List<Column> pcols=new ArrayList<Column>();
		List<Column> cols=new ArrayList<Column>();

		int cy=10-DY;
		Commit pcmt=null;
		for (Commit cmt : commits) {
			cy += DY;
			if (--limit <= 0) break;

			int cf;
			if (pcmt != null) {
				if (pcmt.parentHash.length==0) {
					cols.remove(pcmt);
				}
				else {
					cf=findCol(cols, pcmt);
					cols.get(cf).c = hash.get(pcmt.parentHash[0]);
					cols.get(cf).c.points.add(pcmt.cp);
					for (int i=1; i < pcmt.parentHash.length; ++i) {
						cols.add(cf+i, new Column(getColor(),hash.get(pcmt.parentHash[i])));
						cols.get(cf+i).c.points.add(pcmt.cp);
					}
					for (int i=cf+1; i < cols.size(); ++i) {
						cols.get(i).c.points.add(new Point(X0+(i-1)*DX,cy-DY));
					}
				}
			}

			Point cp;
			cf=findCol(cols, cmt);
			if (cf<0) {
				cols.add(new Column(getColor(), cmt));
				cf = cols.size()-1;
				cp = new Point(X0+cf*DX, cy);
			}
			else {
				cp = new Point(X0+cf*DX, cy);
				for (int i=cf+1; i<cols.size(); ++i) {
					Column c=cols.get(i);
					if (c.c==cmt) {
						retColor(c.color);
						cols.set(i,null);
					}
				}
			}
			for (int i=0; i<cols.size(); ++i) {
				if (cols.get(i) == null) {
					cols.remove(i);
					--i;
				};
			}


			cmt.cp=cp;
			cmt.points.add(cp);
			cmt.color = cols.get(cf).color;
			cmt.cols = cols.size();

			pcmt=cmt;
			pcols.clear();
			for (Column c:cols) pcols.add(new Column(c));
		}

		Svg svg = new Svg();
		svg.strokeWidth(2);
		for (Commit cmt : commits) {
			if (cmt.cp == null) break;

			Point p0=cmt.points.get(0);
			SvgPath path = svg.path();
			path.fill("none").stroke(cmt.color);
			path.moveTo(p0.x, p0.y);
			for (int i = 1; i < cmt.points.size(); ++i) {
				Point p=cmt.points.get(i);
				path.lineTo(p.x, p.y);
			}

			svg.circle(cmt.cp.x, cmt.cp.y, 4).fill("blue");
			svg.text(X0+cmt.cols*DX, cmt.cp.y+6).print(cmt.hash + " | " +
					Text.join(cmt.parentHash," ") + " | " + cmt.message);
		}
		try {
			svg.write(new FileOutputStream("git.html"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Log.debug("svg done");
	}

	void genlog() {
		//String branch = "origin/master";
		//String branch = "origin/devel/k.debski/openssl-20160801";
		String branch = "origin/devel/anchit/gatekeeper";
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

	static String getColor() {
		if (colorAvail.isEmpty()) {
			return colors[0];
		}
		String c = colorAvail.remove(0);
		return c;
	}
	static void retColor(String c) {
		if (!colorAvail.contains(c))
			colorAvail.add(c);
	}
	static final String[] colors = {
		"#6963FF", "#47E8D4", "#6BDB52", "#E84BA5", "#FFA657",
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
	static List<String> colorAvail = new ArrayList<String>();
	static {
		Collections.addAll(colorAvail, colors);
	}
}
