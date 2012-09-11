package org.triple_brain.module.neo4j_graph_manipulator.graph;

import com.google.inject.assistedinject.Assisted;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.ReadableIndex;
import org.triple_brain.module.model.User;
import org.triple_brain.module.model.graph.SubGraph;
import org.triple_brain.module.model.graph.UserGraph;
import org.triple_brain.module.model.graph.Vertex;
import org.triple_brain.module.model.graph.exceptions.InvalidDepthOfSubVerticesException;
import org.triple_brain.module.model.graph.exceptions.NonExistingResourceException;

import javax.inject.Inject;

/*
* Copyright Mozilla Public License 1.1
*/
public class Neo4JUserGraph implements UserGraph {

    public static final String URI_PROPERTY_NAME = "uri";

    private User user;

    private GraphDatabaseService graphDb;
    private ReadableIndex<Node> nodeIndex;
    private ReadableIndex<Relationship> relationshipIndex;

    @Inject
    protected Neo4JUserGraph(
            GraphDatabaseService graphDb,
            ReadableIndex<Node> nodeIndex,
            ReadableIndex<Relationship> relationshipIndex,
            @Assisted User user
    ) {
        this.graphDb = graphDb;
        this.nodeIndex = nodeIndex;
        this.relationshipIndex = relationshipIndex;
        this.user = user;
    }

    @Override
    public Vertex defaultVertex() {
        Node node = nodeIndex.get(
                URI_PROPERTY_NAME,
                user.defaultVertexUri()
        ).getSingle();
        return Neo4JVertex.loadUsingNodeOfOwner(
                node,
                user
        );
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public boolean haveElementWithId(String id) {
        return nodeIndex.get(
                URI_PROPERTY_NAME,
                id
        ).hasNext() ||
                relationshipIndex.get(
                        URI_PROPERTY_NAME,
                        id
                ).hasNext();
    }

    @Override
    public SubGraph graphWithDepthAndCenterVertexId(Integer depthOfSubVertices, String centerVertexURI) throws NonExistingResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SubGraph graphWithDefaultVertexAndDepth(Integer depth) throws InvalidDepthOfSubVerticesException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toRdfXml() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
