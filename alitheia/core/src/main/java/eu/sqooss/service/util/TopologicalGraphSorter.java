/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2010 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.service.util;

import java.util.ArrayList;
import java.util.List;

import eu.sqooss.service.util.Graph.Vertex;

/**
 * Topological sorting for Alitheia Core plugin invocations. Based on code
 * distributed in the public domain by http://www.algorithm-code.com
 * 
 * @author Georgios Gousios <gousiosg@aueb.gr>
 * 
 */
public class TopologicalGraphSorter<T> extends GraphSorter<T>{
	public TopologicalGraphSorter(Graph<T> graph) {
		super(graph);
	}

	/**
	 * Topological sort the graph
	 */
	@Override
	public List<T> sort() {
		Graph<T> clone = this.graph.deepCopy();
		
		List<T> result = new ArrayList<T>(clone.getVertices().size());
		
		while(clone.size() > 0){
			// get a vertex with no successors, or -1
			Vertex<T> currentVertex = noSuccessors(clone);
			
			if (currentVertex == null) // must be a cycle
			{
				System.out.println("ERROR: Graph has cycles");
				return null;
			}
			
			// insert vertex label in sorted array (start at end)
			result.add(currentVertex.label);
			
			// delete vertex
			clone.removeVertex(clone.index(currentVertex.label));
		}
		
		return result;
	}

	/**
	 * Return the first Vertex with no successors or null if it does not exist
	 * @return
	 */
	public Vertex<T> noSuccessors(Graph<T> graph) 
	{
		Vertex<T> result = null;
		int i = 0;
		while(i < graph.size() && result == null){
			if(graph.getEdges().get(graph.getVertices().get(i)) == null || graph.getEdges().get(graph.getVertices().get(i)).isEmpty()){
				result = graph.getVertices().get(i);
			}
			i++;
		}
		return result;
	}
}


