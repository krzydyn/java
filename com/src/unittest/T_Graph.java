package unittest;

import sys.Log;
import sys.UnitTest;
import graphs.Graph;

public class T_Graph extends UnitTest {
	static Graph g = new Graph();

	static {
	g.addEdge(0, 1, 5);
	g.addEdge(0, 2, 3);
	g.addEdge(1, 3, 6);
	g.addEdge(1, 2, 2);
	g.addEdge(2, 4, 4);
	g.addEdge(2, 5, 2);
	g.addEdge(2, 3, 7);
	g.addEdge(3, 5, 1);
	g.addEdge(3, 4, -1);
	g.addEdge(4, 5, -2);
	}

	static void graph_bfs() {
		Log.raw("BFS");
		g.bfs(0, new Graph.Processor() {
			@Override
			public void process(int i) {
				Log.raw("Node %d",i);
			}
		});
	}
	static void graph_dfs() {
		Log.raw("DFS");
		g.dfs(0, new Graph.Processor() {
			@Override
			public void process(int i) {
				Log.raw("Node %d",i);
			}
		});

	}
	static void graph_dijkstra() {
		g.shortestPathDijkstra(0, -1);
	}
}
