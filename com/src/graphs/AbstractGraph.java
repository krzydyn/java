package graphs;

import java.util.List;

abstract public class AbstractGraph {
	static public class Edge {
		Edge(int s, int d, int w){this.src=s;this.dst=d;this.w=w;}
		int src,dst; // source,destination node
		int w;   // weight
	}

	abstract public List<Edge> adj(int src);
}
