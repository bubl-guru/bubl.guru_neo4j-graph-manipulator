package org.triple_brain.module.neo4j_graph_manipulator.graph;

import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.logging.BufferingLogger;
import org.triple_brain.module.model.TripleBrainUris;
import org.triple_brain.module.model.WholeGraph;
import org.triple_brain.module.model.graph.Vertex;

import javax.inject.Inject;
import java.util.Iterator;

/*
* Copyright Mozilla Public License 1.1
*/
public class Neo4JWholeGraph implements WholeGraph {

    @Inject
    protected GraphDatabaseService graphDb;

    @Inject
    protected Neo4JVertexFactory neo4JVertexFactory;

    @Override
    public Iterator<Vertex> getAllVertices() {
        return new Iterator<Vertex>() {
            ExecutionEngine engine = new ExecutionEngine(graphDb, new BufferingLogger());
            ExecutionResult result = engine.execute(
                    "START n = node(*) " +
                            "MATCH n-[:" +
                            Relationships.TYPE +
                            "]-type " +
                            "WHERE type." + Neo4JUserGraph.URI_PROPERTY_NAME + " " +
                            "= '" + TripleBrainUris.TRIPLE_BRAIN_VERTEX + "' " +
                            "RETURN n"
            );
            @Override
            public boolean hasNext() {
                return result.hasNext();
            }

            @Override
            public Vertex next() {
                return neo4JVertexFactory.createOrLoadUsingNode(
                        (Node) result.next().get("n").get()
                );
            }

            @Override
            public void remove() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }
}