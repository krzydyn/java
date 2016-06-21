package graphs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import algebra.Combinatory;

/**
 * Operate on N nodes numbered (0...N-1)
 * @author k.dynowski
 *
 */
public class Graph {
	static interface Processor {
		void process(int i);
	}

	static public class Edge {
		Edge(int s, int e, int w){this.s=s;this.e=e;this.w=w;}
		int s,e; // start,end node
		int w;   // weight
	}

	private int nodeCnt;
	private final List<Edge> edges=new ArrayList<Graph.Edge>();
	private int[] flag;
	private long weight = 0;

	public Graph() {this(0);}
	public Graph(int n) {nodeCnt=n;}
	public void reset() {
		nodeCnt = 0;
		weight = 0;
		edges.clear();
	}
	public long getWeight() {
		return weight;
	}
	public int addNode() {
		return ++nodeCnt;
	}

	private void addEdge(Edge e) {
		weight+=e.w;
		edges.add(e);
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

	public void dfs(int from, Processor p) {
		if (flag == null || flag.length < nodeCnt) {flag = new int[nodeCnt];}
		else {for (int i=0; i < nodeCnt; ++i) flag[i]=0;}

		List<Integer> q = new ArrayList<Integer>();
		q.add(from);
		while (!q.isEmpty()) {
			int u = q.remove(0);
			flag[u] = 1;
			p.process(u);
			for (Edge e : edges) {
				if (e.s == u && flag[e.e]==0) {
					q.add(e.e);
				}
			}
		}
}

	public void bfs(int from, Processor p) {
		if (flag == null || flag.length < nodeCnt) {flag = new int[nodeCnt];}
		else {for (int i=0; i < nodeCnt; ++i) flag[i]=0;}

		List<Integer> q = new ArrayList<Integer>();
		q.add(from);
		while (!q.isEmpty()) {
			int u = q.remove(q.size()-1);
			flag[u] = 1;
			p.process(u);
			for (Edge e : edges) {
				if (e.s == u && flag[e.e]==0) {
					q.add(e.e);
				}
			}
		}
	}

	/**
	 * sort edges by weight from upper to lower
	 * @param edges
	 */
	private void sortByWeight(List<Edge> edges) {
		/*java.util.Collections.sort(edges, new Comparator<Edge>() {
			@Override
			public int compare(Edge o1, Edge o2) {
				return o2.w-o1.w;
			}
		});*/
		Combinatory.comboSort(edges, new Comparator<Edge>() {
			@Override
			public int compare(Edge o1, Edge o2) {
				return o2.w-o1.w;
			}
		});
	}


	/**
	 * Find minimum spanning tree for a graph (Kruskal)
	 * @return
	 */
	public Graph minSpanningTree() {
		List<Edge> edges = new ArrayList<Graph.Edge>(this.edges);
		List<Graph> trees = new ArrayList<Graph>();
		Graph[] used = new Graph[nodeCnt]; //track three to which node belongs

		//1. sort edges by weight
		sortByWeight(edges);

		while (edges.size() > 0) {
			//2. remove first edge (with lowest weight)
			Edge me = edges.remove(edges.size()-1);

			//3. add edge to tree
			if (used[me.s]==null && used[me.e]==null) { // create new tree
				Graph g = new Graph(nodeCnt);
				used[me.s]=used[me.e]=g;
				g.addEdge(me);
				trees.add(g);
			}
			else if (used[me.s] == null) { // add to 's' tree
				Graph g = used[me.e];
				used[me.s]=g;
				g.addEdge(me);
			}
			else if (used[me.e] == null) { // add to 'e' tree
				Graph g = used[me.s];
				used[me.e]=g;
				g.addEdge(me);
			}
			else if (used[me.s] != used[me.e]) { // join trees
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
			else {  // cycle in the tree found
				throw new RuntimeException("Cycle in the tree");
			}
		}
		if (trees.size() != 1) return null;
		return trees.get(0);
	}
}
