package git;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import svg.Svg;
import svg.SvgPath;
import sys.Log;

public class GitGraph {
	static final String[] ArrayOfString0 = new String[0];
	private final GitRepo repo;

	private final List<Commit> commits = new ArrayList<Commit>();
	private final Map<String,Commit> hash = new HashMap<String, Commit>();

	public GitGraph(GitRepo repo) {
		this.repo = repo;
	}

	public Svg buildSvg(String branch, int limit) {
		try {
			readBranch(branch,limit);
			return genGitGraph();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	private void readBranch(String branch, int limit) throws Exception {
		commits.clear();
		hash.clear();
		String args;
		if (limit < 0) args = branch;
		else args = String.format("%s -n %d", branch, limit);
		String log=repo.log(args + " --topo-order --format=%h|%p|%d|%an|%ai|%s");
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

	private Svg genGitGraph() {
		List<Column> pcols=new ArrayList<Column>();
		List<Column> cols=new ArrayList<Column>();

		Log.notice("Building graph");
		long tm = System.currentTimeMillis() + 5*1000;
		int cy=10-DY;
		int cn=0;
		for (Commit cmt : commits) {
			cy += DY;
			++cn;

			//Log.raw("commit: %s | %s", cmt.hash, Text.join(cmt.parentHash, " "));
			long t=System.currentTimeMillis();
			if (tm < t) {
				Log.raw("Processed %d of %d",cn, commits.size());
				tm=t+10*1000;
			}

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
				cmt.flag = 1;
				retColor(cmt.color);
				cols.set(cf, null);
				Log.debug("** End of branch %s",cmt.hash);
			}
			else {
				Commit c = hash.get(cmt.parentHash[0]);
				if (c==null) {
					cmt.flag = 2;
					retColor(cmt.color);
					cols.set(cf, null);
				}
				else if (c.points.isEmpty()){
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
					if (c==null) {
						cmt.flag = 2;
						cols.add(cf+i, null);
						pcols.add(cf+i, null);
					}
					else if (c.points.isEmpty()){
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

		Log.notice("Generating SVG");
		Svg svg = new Svg();
		svg.strokeWidth(2);
		for (Commit cmt : commits) {
			if (cmt.cp == null) {
				//Log.error("commit not located %s",cmt.hash);
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

			if (cmt.flag==1) svg.circle(cmt.cp.x, cmt.cp.y, 6).fill("red");
			else if (cmt.flag==2) svg.circle(cmt.cp.x, cmt.cp.y, 4).stroke("red").fill("none");
			else svg.circle(cmt.cp.x, cmt.cp.y, 4).fill("blue");
			svg.text(X0+cmt.cols*DX, cmt.cp.y+6).print(cmt.hash + " | " +
					cmt.message);
		}
		Log.notice("SVG done");
		return svg;
	}

	private int findCol(List<Column>cols, Commit cmt) {
		for (int i=0; i < cols.size(); ++i) {
			Column c=cols.get(i);
			if (c!=null && c.c == cmt) return i;
		}
		return -1;
	}
	private void addPoint(Commit c, Point p) {
		c.points.add(p);
	}
	static int X0=10, DX=20, DY=20;

	static class Commit {
		String hash;
		String[] parentHash;
		String refs;
		String author;
		String date;
		String message;
		Point cp;
		int flag;
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
