package eu.sqoooss.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import eu.sqooss.service.util.Graph;
import eu.sqooss.service.util.TopologicalGraphSorter;

public class TopologicalSortedGraphTest {

	@Test
	public void testSorting() {
		Graph<String> graph = new Graph<String>(2);
		TopologicalGraphSorter<String> sorter = new TopologicalGraphSorter<String>(graph);
		
		// Add 2 vertices
		int a = graph.addVertex("a");
		int b = graph.addVertex("b");
		
		// Add edge from a to b
		graph.addEdge(a, b);
		
		// Get sorted list
		List<String> sorted = sorter.sort();
		
		// Assert sort
		assertEquals(sorted.get(0), "b");
		assertEquals(sorted.get(1), "a");
	}
	
	@Test
	public void testCycles(){
		Graph<String> graph = new Graph<String>(2);
		TopologicalGraphSorter<String> sorter = new TopologicalGraphSorter<String>(graph);
		
		// Add 2 vertices
		int a = graph.addVertex("a");
		int b = graph.addVertex("b");
		
		// Add edge from a to b
		graph.addEdge(a, b);
		// Add another edge from b to a
		graph.addEdge(b, a);
		
		// Get sorted list
		List<String> sorted = sorter.sort();
		
		// As there is a cycle, the sorted list should be null
		assertNull(sorted);
	}

}
