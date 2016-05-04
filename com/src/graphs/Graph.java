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

	private int nodeCnt;
	private final List<Edge> edges=new ArrayList<Graph.Edge>();
	private int[] flag;
	private int weight = 0;


	public Graph() {this(0);}
	public Graph(int n) {nodeCnt=n;}
	public void reset() {
		nodeCnt = 0;
		weight = 0;
		edges.clear();
	}
	public int getWeight() { return weight; }
	public int addNode() {
		return ++nodeCnt;
	}
	public void addEdge(int s, int e, int w) {
		if (s < e) {
			if (e<=nodeCnt) nodeCnt=e+1;
		}
		else {
			if (s<=nodeCnt) nodeCnt=s+1;
		}
		addEdge(new Edge(s,e,w));
	}
	private void addEdge(Edge e) {
		weight+=e.w;
		edges.add(e);
	}

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

	private void sortByWeight(List<Edge> edges) {
		//combo sort
		Edge t;
		int gap = edges.size();
		boolean swapped=false;
		while (gap > 1 || swapped) {
		      gap = gap * 10 / 13; //empiric
		      if (gap==0) gap=1;
		      //else if (gap==9||gap==10) gap=11;
		      swapped = false;
		      for (int i = 0; i + gap < edges.size(); ++i) {
		         if (edges.get(i).w > edges.get(i + gap).w) {
		            t = edges.get(i);
		            edges.set(i, edges.get(i + gap));
		            edges.set(i + gap, t);
		            swapped = true;
		           }
		      }
		   }
	}


	/**
	 * Find minimum spanning tree for a graph (Kruskal)
	 * @return
	 */
	public Graph minSpanningTree() {
		List<Edge> edges = new ArrayList<Graph.Edge>(this.edges);
		List<Graph> trees = new ArrayList<Graph>();
		Graph[] used = new Graph[nodeCnt];

		//1. sort edges by weight
		sortByWeight(edges);

		while (edges.size() > 0) {
			//2. remove first edge (with lowest weight)
			Edge me = edges.remove(0);

			//3. add edge to tree
			if (used[me.s]==null && used[me.e]==null) {
				Graph g = new Graph(nodeCnt);
				used[me.s]=used[me.e]=g;
				g.addEdge(me);
				trees.add(g);
			}
			else if (used[me.s] == null) {
				Graph g = used[me.e];
				used[me.s]=g;
				g.addEdge(me);
			}
			else if (used[me.e] == null) {
				Graph g = used[me.s];
				used[me.e]=g;
				g.addEdge(me);
			}
			else if (used[me.s] != used[me.e]) {
				// join trees
				Graph mst = used[me.s];
				Graph g = used[me.e];
				trees.remove(g);
				mst.addEdge(me);
				used[me.e]=mst;
				for (Edge e : g.edges) {
					mst.addEdge(e);
					used[e.s]=used[e.e]=mst;
				}
			}
			// else edge would create a cycle
		}
		if (trees.size() != 1) return null;
		return trees.get(0);
	}
}
