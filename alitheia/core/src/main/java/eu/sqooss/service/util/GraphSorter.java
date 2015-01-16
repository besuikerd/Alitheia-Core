package eu.sqooss.service.util;

import java.util.List;

public abstract class GraphSorter<T> {
	protected Graph<T> graph;
	
	public GraphSorter(Graph<T> graph){
		this.graph = graph;
	}
	
	public abstract List<T> sort();
	
}
