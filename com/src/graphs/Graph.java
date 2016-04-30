package graphs;

import java.util.ArrayList;
import java.util.List;

/**
 * Operate on N nodes numbered (0...N-1)
 * @author k.dynowski
 *
 */
public class Graph {
	static public class Edge {
		Edge(int s, int e, int w){this.s=s;this.e=e;this.w=w;}
		int s,e; // start,end node
		int w;   // weight
	}

	public Graph() {this(0);}
	public Graph(int n) {nodeCnt=n;}
	public void reset() {
		nodeCnt=0;
		edges.clear();
	}
	public int addNode() {
		int n=nodeCnt;
		++nodeCnt;
		return n;
	}
	public void addEdge(int s, int e, int w) {
		edges.add(new Edge(s,e,w));
	}

	private int nodeCnt;
	private final List<Edge> edges=new ArrayList<Graph.Edge>();
	private int[] flag;

	private void do_dfs(int from) {
		flag[from] = 1;
		for (Edge e : edges) {
			if (e.s == from && flag[e.e]==0) {
				do_dfs(e.e);
			}
		}
		flag[from] = 2;
	}
	public void dfs(int from) {
		if (flag == null || flag.length < nodeCnt) {flag = new int[nodeCnt];}
		else {for (int i=0; i < nodeCnt; ++i) flag[i]=0;}
		do_dfs(from);
	}

	private void do_bfs(int from) {
		List<Integer> q = new ArrayList<Integer>();
		q.add(from);
		while (!q.isEmpty()) {
			int u = q.remove(q.size()-1);
			flag[u] = 1;
			for (Edge e : edges) {
				if (e.s == from && flag[e.e]==0) {
					q.add(e.e);
				}
			}
		}
	}
	public void bfs(int from) {
		if (flag == null || flag.length < nodeCnt) {flag = new int[nodeCnt];}
		else {for (int i=0; i < nodeCnt; ++i) flag[i]=0;}
		do_bfs(from);
	}
}
