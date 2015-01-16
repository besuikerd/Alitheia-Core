package eu.sqooss.service.util;

import java.util.List;

/**
 * Abstract class for sorting (and flattening) a Graph
 * @param <T>
 */
public abstract class GraphSorter<T> {
	protected Graph<T> graph;
	
	public GraphSorter(Graph<T> graph){
		this.graph = graph;
	}
	
	/**
	 * Sort and flatten the Graph into a List
	 */
	public abstract List<T> sort();
	
}
