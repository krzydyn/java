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

import java.util.List;

abstract public class AbstractGraph {
	static public class Edge {
		Edge(int s, int d, int w){this.src=s;this.dst=d;this.w=w;}
		int src,dst; // source,destination node
		int w;   // weight
	}

	protected int nodeCnt;

	abstract public List<Edge> adj(int src);
}
