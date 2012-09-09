package org.triple_brain.module.neo4j_graph_manipulator.graph;

import org.triple_brain.module.model.graph.Edge;
import org.triple_brain.module.model.graph.SubGraph;
import org.triple_brain.module.model.graph.Vertex;

import java.util.HashSet;
import java.util.Set;

/*
* Copyright Mozilla Public License 1.1
*/
public class Neo4JSubGraph implements SubGraph{

    private Set<Vertex> vertices = new HashSet<Vertex>();
    private Set<Edge> edges = new HashSet<Edge>();

    public static Neo4JSubGraph withVerticesAndEdges(Set<Vertex> vertices, Set<Edge> edges){
        return new Neo4JSubGraph(vertices, edges);
    }

    protected Neo4JSubGraph(Set<Vertex> vertices, Set<Edge> edges){
        this.vertices = vertices;
        this.edges = edges;
    }

    @Override
    public Vertex vertexWithIdentifier(String identifier) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Edge edgeWithIdentifier(String identifier) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int numberOfEdgesAndVertices() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int numberOfEdges() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int numberOfVertices() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean containsVertex(Vertex vertex) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<Vertex> vertices() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<Edge> edges() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}