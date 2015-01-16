package eu.sqooss.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Representation of a Graph
 */
public class Graph<T> {
	protected List<Vertex<T>> vertices;
	protected Map<Vertex<T>, Set<Vertex<T>>> edges;
	
	public Graph(){
		this.vertices = new ArrayList<Vertex<T>>();
		this.edges = new HashMap<Vertex<T>, Set<Vertex<T>>>();
	}
	
	public Graph(int size){
		this.vertices = new ArrayList<Vertex<T>>(size);
		this.edges = new HashMap<Vertex<T>, Set<Vertex<T>>>(size);
	}
	
	public List<Vertex<T>> getVertices(){
		return this.vertices;
	}
	
	public Map<Vertex<T>, Set<Vertex<T>>> getEdges() {
		return edges;
	}
	
	/**
	 * Add a vertex to the graph
	 * @param lab The label of the vertex
	 * @return the postition of the vertex
	 */
	public int addVertex(T lab) {
		vertices.add(new Vertex<T>(lab));
		return vertices.size()-1;
	}
	
	/**
	 * Get the index of a vertex, or -1 if it is not in the graph
	 * @param vertex
	 */
	public int index(T label){
		int index = -1;
		int i = 0;
		while(i < vertices.size() && index == -1){
			if(vertices.get(i).label.equals(label)) index = i;
			i++;
		}
		return index;
	}
	
	/**
	 * Get the vertex with the given label
	 * @param label
	 */
	public Vertex<T> getVertex(T label){
		return this.vertices.get(this.index(label));
	}
	
	/**
	 * Remove the vertex on the given index, along with edges from/to this vertex
	 * @param index
	 */
	public void removeVertex(int index){
		Vertex<T> vertex = vertices.remove(index);
		this.removeEdges(vertex);
	}
	
	/**
	 * Remove all in- and outgoing edges of the given vertex
	 */
	public void removeEdges(Vertex<T> vertex){
		// Remove outgoing edges
		edges.remove(vertex);
		
		// Remove ingoing edges
		for(Entry<Vertex<T>, Set<Vertex<T>>> entry : edges.entrySet()){
			entry.getValue().remove(vertex);
		}
	}

	/**
	 * Add an edge
	 * @param start The postition of the start vertex
	 * @param end The postition of the end vertex
	 */
	public void addEdge(int start, int end) {
		if(!edges.containsKey(start)){
			edges.put(vertices.get(start), new HashSet<Vertex<T>>());
		}
		edges.get(vertices.get(start)).add(vertices.get(end));
	}

	public void displayVertex(int v) {
		System.out.print(vertices.get(v).label);
	}
	
	/**
	 * Return the size (number of vertices) of this graph
	 */
	public int size(){
		return vertices.size();
	}
	
	/**
	 * Create a deep copy of this graph
	 */
	public Graph<T> deepCopy(){
		Graph<T> clone = new Graph<T>();
		// Copy vertices
		for(Vertex<T> v : vertices){
			clone.addVertex(v.label);
		}
		// Copy edges
		for(Entry<Vertex<T>, Set<Vertex<T>>> entry : edges.entrySet()){
			for(Vertex<T> end : entry.getValue()){
				clone.addEdge(this.index(entry.getKey().label), clone.index(end.label));
			}
		}
		return clone;
	}
	
	/**
	 * Vertex/Node class of a Graph
	 */
	static class Vertex<T> {
	    public T label;

	    public Vertex(T label) {
	        this.label = label;
	    }
	    
	    @Override
	    public String toString() {
	    	return "Vertex<" + this.label.getClass().getName() + ">(" + label + ")";
	    }
	    
	    @Override
	    public boolean equals(Object obj) {
	    	return (obj instanceof Vertex) && label.getClass().equals(((Vertex)obj).label.getClass()) && label.equals(((Vertex)obj).label);
	    }
	}
}
