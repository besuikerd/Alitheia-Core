package eu.sqoooss.service.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import eu.sqooss.service.util.TopologicalSortedGraph;

public class TopologicalSortedGraphTest {

	@Test
	public void testSorting() {
		TopologicalSortedGraph<String> graph = new TopologicalSortedGraph<String>(2);
		
		// Add 2 vertices
		graph.addVertex("a");
		graph.addVertex("b");
		
		// Add edge from a to b
		graph.addEdge(1, 2);
		
		// Get sorted list
		List<String> sorted = graph.topo();
		
		// Assert sort
		assertEquals(sorted.get(0), "b");
		assertEquals(sorted.get(1), "a");
	}
	
	@Test
	public void testCycles(){
		TopologicalSortedGraph<String> graph = new TopologicalSortedGraph<String>(2);
		
		// Add 2 vertices
		graph.addVertex("a");
		graph.addVertex("b");
		
		// Add edge from a to b
		graph.addEdge(1, 2);
		// Add another edge from b to a
		graph.addEdge(2, 1);
		
		// Get sorted list
		List<String> sorted = graph.topo();
		
		// As there is a cycle, the sorted list should be null
		assertNull(sorted);
	}

}
