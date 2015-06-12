/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.graph;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;
import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.model.graph.SubGraphPojo;
import guru.bubl.module.model.graph.UserGraph;
import guru.bubl.module.model.graph.edge.EdgeOperator;
import guru.bubl.module.model.graph.exceptions.InvalidDepthOfSubVerticesException;
import guru.bubl.module.model.graph.exceptions.NonExistingResourceException;
import guru.bubl.module.model.graph.schema.SchemaOperator;
import guru.bubl.module.model.graph.schema.SchemaPojo;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.model.graph.vertex.VertexPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.edge.Neo4jEdgeFactory;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.schema.Neo4jSchemaExtractorFactory;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.Neo4jSubGraphExtractorFactory;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.schema.SchemaFactory;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.vertex.Neo4jVertexFactory;

import java.net.URI;
import java.util.Map;

import static guru.bubl.module.neo4j_graph_manipulator.graph.Neo4jRestApiUtils.map;

public class Neo4jUserGraph implements UserGraph {

    public static final String URI_PROPERTY_NAME = "uri";

    private User user;
    private QueryEngine<Map<String, Object>> queryEngine;
    private Neo4jVertexFactory vertexFactory;
    private SchemaFactory schemaFactory;
    private Neo4jSubGraphExtractorFactory subGraphExtractorFactory;
    private Neo4jEdgeFactory edgeFactory;
    private Neo4jSchemaExtractorFactory schemaExtractorFactory;

    @AssistedInject
    protected Neo4jUserGraph(
            QueryEngine queryEngine,
            Neo4jVertexFactory vertexFactory,
            Neo4jEdgeFactory edgeFactory,
            Neo4jSubGraphExtractorFactory subGraphExtractorFactory,
            Neo4jSchemaExtractorFactory schemaExtractorFactory,
            SchemaFactory schemaFactory,
            @Assisted User user
    ) {
        this.queryEngine = queryEngine;
        this.user = user;
        this.vertexFactory = vertexFactory;
        this.edgeFactory = edgeFactory;
        this.subGraphExtractorFactory = subGraphExtractorFactory;
        this.schemaExtractorFactory = schemaExtractorFactory;
        this.schemaFactory = schemaFactory;
    }

    @Override
    public VertexOperator defaultVertex() {
        return getAnyVertex();
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Boolean haveElementWithId(URI uri) {
        QueryResult<Map<String, Object>> result = queryEngine.query(
                "START n=node:node_auto_index('uri:" + uri + "') "
                        + "return n.uri",
                map()
        );
        return result.iterator().hasNext();
    }

    @Override
    public SubGraphPojo graphWithDepthAndCenterVertexId(Integer depthOfSubVertices, URI centerVertexURI) throws NonExistingResourceException {
        if (depthOfSubVertices < 0) {
            throw new InvalidDepthOfSubVerticesException(
                    depthOfSubVertices,
                    centerVertexURI
            );
        }
        SubGraphPojo subGraph = subGraphExtractorFactory.withCenterVertexAndDepth(
                centerVertexURI,
                depthOfSubVertices
        ).load();
        if (subGraph.vertices().isEmpty()) {
            throw new NonExistingResourceException(
                    centerVertexURI
            );
        }
        return subGraph;
    }

    @Override
    public SubGraphPojo graphWithAnyVertexAndDepth(Integer depth) throws InvalidDepthOfSubVerticesException {
        return graphWithDepthAndCenterVertexId(
                depth,
                getAnyVertex().uri()
        );
    }

    @Override
    public String toRdfXml() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VertexOperator vertexWithUri(URI uri) {
        return vertexFactory.withUri(
                uri
        );
    }

    @Override
    public EdgeOperator edgeWithUri(URI uri) {
        return edgeFactory.withUri(
                uri
        );
    }

    @Override
    public SchemaPojo schemaPojoWithUri(URI uri) {
        return schemaExtractorFactory.havingUri(
                uri
        ).load();
    }

    @Override
    public SchemaOperator schemaOperatorWithUri(URI uri) {
        return schemaFactory.withUri(uri);
    }

    @Override
    public VertexPojo createVertex() {
        VertexOperator operator = vertexFactory.createForOwnerUsername(
                user.username()
        );
        return new VertexPojo(
                operator.uri()
        );
    }

    @Override
    public SchemaPojo createSchema() {
        SchemaOperator schemaOperator = schemaFactory.createForOwnerUsername(
                user.username()
        );
        return new SchemaPojo(
                schemaOperator.uri()
        );
    }


    private VertexOperator getAnyVertex(){
        QueryResult<Map<String, Object>> result = queryEngine.query(
                "START n=node:node_auto_index(" +
                        "'type:" + GraphElementType.vertex+ " AND " +
                        "owner:" + user.username() + 
                        "') "
                        + "return n.uri limit 1",
                map()
        );
        URI uri = URI.create(
                result.iterator().next().get("n.uri").toString()
        );
        return vertexFactory.withUri(uri);
    }
}