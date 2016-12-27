/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package graphs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sys.Log;
import text.Text;
import algebra.Sorting;

/**
 * Operate on N nodes numbered (0...N-1)
 * Note: any graph can be converted to enumerated nodes
 * @author k.dynowski
 *
 */
public class Graph extends AbstractGraph {
	static public interface Processor {
		void process(int i);
	}

	private final Map<Integer,List<Edge>> adj = new HashMap<Integer, List<Edge>>();
	private int[] flag;
	private long weight = 0;
	private static final List<Edge> empty = new ArrayList<Edge>();

	public Graph() {this(0);}
	public Graph(int n) {nodeCnt=n;}
	public void reset() {
		nodeCnt = 0;
		weight = 0;
		adj.clear();
	}
	public long getWeight() {
		return weight;
	}

	private void addEdge(Edge e) {
		weight+=e.w;
		List<Edge> a = adj.get(e.src);
		if (a == null) {
			a = new ArrayList<Edge>();
			adj.put(e.src, a);
		}
		a.add(e);
	}

	public void addEdge(int src, int dst, int w) {
		if (src < dst) {
			if (nodeCnt <= dst) nodeCnt=dst+1;
		}
		else {
			if (nodeCnt <= src) nodeCnt=src+1;
		}
		addEdge(new Edge(src,dst,w));
	}
	public void addEdge(int src, int dst) { addEdge(src,dst,1); }

	public List<Edge> getEdges() {
		List<Edge> edges = new ArrayList<Edge>();
		for (Iterator<Integer> i = adj.keySet().iterator(); i.hasNext(); ) {
			edges.addAll(adj.get(i.next()));
		}
		return edges;
	}

	@Override
	public List<Edge> adj(int src) {
		List<Edge> a = adj.get(src);
		return a != null ? a : empty;
	}

	public void bfs(int from, Processor p) {
		if (flag == null || flag.length < nodeCnt) {flag = new int[nodeCnt];}
		else {for (int i=0; i < nodeCnt; ++i) flag[i]=0;}

		List<Integer> q = new ArrayList<Integer>();
		q.add(from);
		while (!q.isEmpty()) {
			int u = q.remove(0);
			flag[u] = 1;
			p.process(u);
			for (Edge e : adj(u)) {
				if (flag[e.dst]==0) {
					q.add(e.dst);
					flag[e.dst] = 2;
				}
			}
		}
	}

	public void dfs(int from, Processor p) {
		if (flag == null || flag.length < nodeCnt) {flag = new int[nodeCnt];}
		else {for (int i=0; i < nodeCnt; ++i) flag[i]=0;}

		List<Integer> q = new ArrayList<Integer>();
		q.add(from);
		while (!q.isEmpty()) {
			int u = q.remove(q.size()-1);
			flag[u] = 1;
			p.process(u);
			for (Edge e : adj(u)) {
				if (flag[e.dst]==0) {
					q.add(e.dst);
					flag[e.dst] = 2;
				}
			}
		}
	}

	public void shortestPathDijkstra(int src, int dst) {
		if (flag == null || flag.length < nodeCnt) {flag = new int[nodeCnt];}
		else {for (int i=0; i < nodeCnt; ++i) flag[i]=0;}
		int[] dist = new int[nodeCnt];
		int[] prev = new int[nodeCnt];

		for (int i=0; i < nodeCnt; ++i) {
			dist[i] = Integer.MAX_VALUE;
			prev[i] = -1;
		}
		dist[src] = 0;

		List<Integer> q = new ArrayList<Integer>();
		q.add(src);
		while (!q.isEmpty()) {
			int u,im=-1, dm=Integer.MAX_VALUE;
			//find vertex with min dist (TODO PriorityQueue)
			for (int i=0; i < q.size(); ++i) {
				u=dist[q.get(i)];
				if (dm > u) {
					dm = u;
					im = i;
				}
			}
			u = q.remove(im);
			if (flag[u]!=0) continue;
			flag[u] = 1;
			if (u == dst) break;

			for (Edge e : adj(u)) {
				if (e.src == u && flag[e.dst]==0) {
					int v = e.dst;
					q.add(v);
					int a = dist[u] + e.w;
					if (dist[v] > a) {
						dist[v] = a;
						prev[v] = u;
					}
				}
			}
		}
		if (dst>=0) {
			List<Integer> s = new ArrayList<Integer>();
			int u=dst;
			s.add(0, u);
			while ((u=prev[u])>=0) s.add(0, u);
			Log.prn("path: %s", Text.join(",", s));
		}
		else {
			Log.prn("dist: %s", Text.join(",", dist));
		}
		//return dist,prev
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
		Sorting.comboSort(edges, new Comparator<Edge>() {
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
	public List<Graph> getSpanningTrees() {
		Graph[] used = new Graph[nodeCnt]; //track tree to which node belongs
		List<Edge> edges = getEdges();
		Log.debug("edges = %d", edges.size());

		//1. sort edges by weight
		sortByWeight(edges);

		List<Graph> trees = new ArrayList<Graph>();
		while (edges.size() > 0) {
			//2. remove first edge (with lowest weight)
			Edge me = edges.remove(edges.size()-1);
			Log.debug("proc edge %d,%d w=%d",me.src,me.dst,me.w);

			//3. add edge to tree
			if (used[me.src]==null && used[me.dst]==null) { // create new tree
				Log.debug("new tree");
				Graph g = new Graph(nodeCnt);
				used[me.src]=used[me.dst]=g;
				g.addEdge(me);
				trees.add(g);
			}
			else if (used[me.src] == null) { // add to 's' tree
				Graph g = used[me.dst];
				used[me.src]=g;
				g.addEdge(me);
			}
			else if (used[me.dst] == null) { // add to 'e' tree
				Graph g = used[me.src];
				used[me.dst]=g;
				g.addEdge(me);
			}
			else if (used[me.src] != used[me.dst]) { // join trees
				Graph mst = used[me.src];
				Graph g = used[me.dst];
				trees.remove(g);
				mst.addEdge(me);
				used[me.dst]=mst;
				for (Edge e : g.getEdges()) {
					mst.addEdge(e);
					used[e.src]=used[e.dst]=mst;
				}
			}
			else {
				// cycle in the tree
				// or reversed edge in non-directed graph
			}
		}
		return trees;
	}

	public Graph minSpanningTree() {
		List<Graph> trees = getSpanningTrees();
		if (trees.size() != 1) return null;
		return trees.get(0);

	}
}
