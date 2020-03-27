/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.graph.edge;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import guru.bubl.module.model.Image;
import guru.bubl.module.model.UserUris;
import guru.bubl.module.model.graph.FriendlyResourcePojo;
import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.edge.Edge;
import guru.bubl.module.model.graph.edge.EdgeOperator;
import guru.bubl.module.model.graph.edge.EdgePojo;
import guru.bubl.module.model.graph.tag.Tag;
import guru.bubl.module.model.graph.tag.TagPojo;
import guru.bubl.module.model.graph.vertex.Vertex;
import guru.bubl.module.model.graph.vertex.VertexInSubGraphPojo;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.neo4j_graph_manipulator.graph.FriendlyResourceNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.OperatorNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.Relationships;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.GraphElementFactoryNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.GraphElementOperatorNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.vertex.VertexFactoryNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.vertex.VertexInSubGraphOperatorNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.vertex.VertexTypeOperatorNeo4j;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.graphdb.Node;

import javax.inject.Inject;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static guru.bubl.module.neo4j_graph_manipulator.graph.RestApiUtilsNeo4j.map;
import static guru.bubl.module.neo4j_graph_manipulator.graph.graph.GraphElementOperatorNeo4j.decrementNbNeighborsQueryPart;
import static guru.bubl.module.neo4j_graph_manipulator.graph.graph.GraphElementOperatorNeo4j.incrementNbNeighborsQueryPart;
import static org.neo4j.driver.v1.Values.parameters;

public class EdgeOperatorNeo4j implements EdgeOperator, OperatorNeo4j {

    public enum props {
        source_vertex_uri,
        destination_vertex_uri
    }

    protected Node node;
    protected GraphElementOperatorNeo4j graphElementOperator;
    protected VertexFactoryNeo4j vertexFactory;
    protected EdgeFactoryNeo4j edgeFactory;

    protected Vertex sourceVertex;
    protected Vertex destinationVertex;

    private static final Map<String, Object> edgeCreateProperties = map(
            VertexInSubGraphOperatorNeo4j.props.shareLevel.name(), ShareLevel.PRIVATE.getIndex()
    );

    @Inject
    protected
    Driver driver;

    @AssistedInject
    protected EdgeOperatorNeo4j(
            VertexFactoryNeo4j vertexFactory,
            EdgeFactoryNeo4j edgeFactory,
            GraphElementFactoryNeo4j graphElementFactory,
            @Assisted URI uri
    ) {
        this.vertexFactory = vertexFactory;
        this.edgeFactory = edgeFactory;
        this.graphElementOperator = graphElementFactory.withUri(
                uri
        );
    }

    @AssistedInject
    protected EdgeOperatorNeo4j(
            VertexFactoryNeo4j vertexFactory,
            EdgeFactoryNeo4j edgeFactory,
            GraphElementFactoryNeo4j graphElementFactory,
            @Assisted("source") Vertex sourceVertex,
            @Assisted("destination") Vertex destinationVertex
    ) {
        this.vertexFactory = vertexFactory;
        this.edgeFactory = edgeFactory;
        UserUris userUris = new UserUris(
                UserUris.ownerUserNameFromUri(sourceVertex.uri())
        );
        URI newEdgeUri = userUris.generateEdgeUri();
        this.graphElementOperator = graphElementFactory.withUri(
                newEdgeUri
        );
        this.sourceVertex = sourceVertex;
        this.destinationVertex = destinationVertex;
    }

    @AssistedInject
    protected EdgeOperatorNeo4j(
            VertexFactoryNeo4j vertexFactory,
            EdgeFactoryNeo4j edgeFactory,
            GraphElementFactoryNeo4j graphElementFactory,
            @Assisted URI uri,
            @Assisted("source") Vertex sourceVertex,
            @Assisted("destination") Vertex destinationVertex
    ) {
        this.vertexFactory = vertexFactory;
        this.edgeFactory = edgeFactory;
        this.graphElementOperator = graphElementFactory.withUri(
                uri
        );
        this.sourceVertex = sourceVertex;
        this.destinationVertex = destinationVertex;
    }

    @Override
    public VertexOperator sourceVertex() {
        try (Session session = driver.session()) {
            Record record = session.run(
                    queryPrefix() +
                            "MATCH (n)-[:SOURCE_VERTEX]->(v) " +
                            "RETURN v.uri as uri",
                    parameters(
                            "uri",
                            uri().toString()
                    )
            ).single();
            return vertexFactory.withUri(
                    URI.create(record.get("uri").asString())
            );
        }
    }

    @Override
    public VertexOperator destinationVertex() {
        try (Session session = driver.session()) {
            Record record = session.run(
                    queryPrefix() +
                            "MATCH (n)-[:DESTINATION_VERTEX]->(v) " +
                            "RETURN v.uri as uri",
                    parameters(
                            "uri",
                            uri().toString()
                    )
            ).single();
            return vertexFactory.withUri(
                    URI.create(record.get("uri").asString())
            );
        }
    }

    private VertexOperator vertexUsingProperty(Enum prop) {
        try (Session session = driver.session()) {
            StatementResult rs = session.run(
                    String.format(
                            "%sRETURN n.%s",
                            queryPrefix(),
                            prop.name()
                    ),
                    parameters(
                            "uri",
                            uri().toString()
                    )
            );
            Record record = rs.next();
            return vertexFactory.withUri(URI.create(
                    record.get(
                            "n." + prop.name()
                    ).asString()
            ));
        }
    }

    @Override
    public VertexOperator otherVertex(Vertex vertex) {
        return sourceVertex().equals(vertex) ?
                destinationVertex() :
                sourceVertex();
    }

    @Override
    public void changeSourceVertex(
            Vertex newSourceVertex
    ) {
        changeEndVertex(
                newSourceVertex,
                Relationships.SOURCE_VERTEX
        );
    }

    @Override
    public void changeDestinationVertex(
            Vertex newDestinationVertex
    ) {
        changeEndVertex(
                newDestinationVertex,
                Relationships.DESTINATION_VERTEX
        );
    }

    private void changeEndVertex(
            Vertex newEndVertex,
            Relationships relationshipToChange
    ) {
        Relationships relationshipToKeep = Relationships.SOURCE_VERTEX == relationshipToChange ?
                Relationships.DESTINATION_VERTEX : Relationships.SOURCE_VERTEX;
        ShareLevel newEndVertexShareLevel = vertexFactory.withUri(
                newEndVertex.uri()
        ).getShareLevel();
        ShareLevel oldEndVertexShareLevel;
        ShareLevel keptEndVertexShareLevel;
        VertexOperator sourceVertex = sourceVertex();
        VertexOperator destinationVertex = destinationVertex();
        if (relationshipToChange == Relationships.SOURCE_VERTEX) {
            oldEndVertexShareLevel = sourceVertex.getShareLevel();
            keptEndVertexShareLevel = destinationVertex.getShareLevel();
        } else {
            oldEndVertexShareLevel = destinationVertex.getShareLevel();
            keptEndVertexShareLevel = sourceVertex.getShareLevel();
        }
        String decrementPreviousVertexQueryPart = decrementNbNeighborsQueryPart(
                keptEndVertexShareLevel,
                "prev_v",
                "SET "
        );
        String incrementKeptVertexQueryPart = "";
        String decrementKeptVertexQueryPart = "";
        if (oldEndVertexShareLevel.isSame(keptEndVertexShareLevel)) {
            if (!newEndVertexShareLevel.isSame(keptEndVertexShareLevel)) {
                incrementKeptVertexQueryPart = incrementNbNeighborsQueryPart(
                        newEndVertexShareLevel,
                        "kept_v",
                        ", "
                );
            }
        } else {
            decrementKeptVertexQueryPart = decrementNbNeighborsQueryPart(
                    oldEndVertexShareLevel,
                    "kept_v",
                    ", "
            );
        }

        String incrementNewEndVertexQueryPart = incrementNbNeighborsQueryPart(
                keptEndVertexShareLevel,
                "new_v",
                ", "
        );
        String query = String.format(
                "%s, (new_v:Resource{uri:$endVertexUri}), " +
                        "(n)-[prev_rel:%s]->(prev_v), " +
                        "(n)-[:%s]->(kept_v) " +
                        "CREATE (n)-[:%s]->(new_v) " +
                        "DELETE prev_rel " +
                        decrementPreviousVertexQueryPart +
                        decrementKeptVertexQueryPart +
                        incrementKeptVertexQueryPart +
                        incrementNewEndVertexQueryPart + ",%s",
                queryPrefix(),
                relationshipToChange,
                relationshipToKeep,
                relationshipToChange,
                FriendlyResourceNeo4j.LAST_MODIFICATION_QUERY_PART
        );
        try (Session session = driver.session()) {
            session.run(
                    query,
                    parameters(
                            "uri",
                            this.uri().toString(),
                            "endVertexUri",
                            newEndVertex.uri().toString(),
                            "last_modification_date",
                            new Date().getTime()
                    )
            );
        }
    }

    @Override
    public EdgeOperator forkUsingSourceAndDestinationVertexAndCache(
            Vertex sourceVertex,
            Vertex destinationVertex,
            Edge cache
    ) {
        EdgeOperator clone = edgeFactory.withSourceAndDestinationVertex(
                sourceVertex,
                destinationVertex
        );
        graphElementOperator.forkUsingCreationPropertiesAndCache(
                clone,
                map(),
                cache
        );
        return clone;
    }


    @Override
    public void inverse() {
        try (Session session = driver.session()) {
            session.run(
                    String.format(
                            "%sMATCH (n)-[source_rel:%s]->(source_vertex), " +
                                    "(n)-[destination_rel:%s]->(destination_vertex) " +
                                    "CREATE (n)-[:%s]->(source_vertex) " +
                                    "CREATE (n)-[:%s]->(destination_vertex) " +
                                    "DELETE source_rel, destination_rel " +
                                    "SET %s",
                            queryPrefix(),
                            Relationships.SOURCE_VERTEX,
                            Relationships.DESTINATION_VERTEX,
                            Relationships.DESTINATION_VERTEX,
                            Relationships.SOURCE_VERTEX,
                            FriendlyResourceNeo4j.LAST_MODIFICATION_QUERY_PART
                    ),
                    parameters(
                            "uri",
                            this.uri().toString(),
                            "last_modification_date",
                            new Date().getTime()
                    )
            );
        }
    }

    @Override
    public void remove() {
        this.graphElementOperator.remove();
    }

    @Override
    public Date creationDate() {
        return graphElementOperator.creationDate();
    }

    @Override
    public Date lastModificationDate() {
        return graphElementOperator.lastModificationDate();
    }

    @Override
    public URI uri() {
        return graphElementOperator.uri();
    }

    @Override
    public String label() {
        return graphElementOperator.label();
    }

    @Override
    public void label(String label) {
        graphElementOperator.label(label);
    }

    @Override
    public Set<Image> images() {
        return graphElementOperator.images();
    }

    @Override
    public Boolean gotImages() {
        return graphElementOperator.gotImages();
    }

    @Override
    public String comment() {
        return graphElementOperator.comment();
    }

    @Override
    public void comment(String comment) {
        graphElementOperator.comment(
                comment
        );
    }

    @Override
    public Boolean gotComments() {
        return graphElementOperator.gotComments();
    }

    @Override
    public void addImages(Set<Image> images) {
        graphElementOperator.addImages(images);
    }

    @Override
    public boolean hasLabel() {
        return graphElementOperator.hasLabel();
    }

    @Override
    public String getColors() {
        return graphElementOperator.getColors();
    }

    @Override
    public String getFont() {
        return graphElementOperator.getFont();
    }

    @Override
    public void setColors(String colors) {
        graphElementOperator.setColors(colors);
    }

    @Override
    public void setFont(String font) {
        graphElementOperator.setFont(font);
    }

    @Override
    public void setChildrenIndex(String childrenIndex) {
        graphElementOperator.setChildrenIndex(
                childrenIndex
        );
    }

    @Override
    public Boolean isUnderPattern() {
        return graphElementOperator.isUnderPattern();
    }

    @Override
    public Boolean isPatternOrUnderPattern() {
        return graphElementOperator.isUnderPattern();
    }

    @Override
    public String getPrivateContext() {
        return graphElementOperator.getPrivateContext();
    }

    @Override
    public String getChildrenIndex() {
        return graphElementOperator.getChildrenIndex();
    }

    @Override
    public URI getPatternUri() {
        return graphElementOperator.getPatternUri();
    }


    @Override
    public ShareLevel getShareLevel() {
        return graphElementOperator.getShareLevel();
    }

    @Override
    public Boolean isPublic() {
        return graphElementOperator.isPublic();
    }

    @Override
    public void create() {
        createEdge();
    }

    @Override
    public EdgePojo createEdge() {
        return createEdgeUsingInitialValues(
                edgeCreateProperties
        );
    }

    @Override
    public EdgePojo createEdgeWithAdditionalProperties(Map<String, Object> props) {
        props.putAll(
                edgeCreateProperties
        );
        return createEdgeUsingInitialValues(
                props
        );
    }

    @Override
    public EdgePojo createWithShareLevel(ShareLevel shareLevel) {
        return createEdgeUsingInitialValues(
                map(
                        VertexInSubGraphOperatorNeo4j.props.shareLevel.name(),
                        shareLevel.getIndex()
                )
        );
    }

    @Override
    public EdgePojo createEdgeUsingInitialValues(Map<String, Object> values) {
        Map<String, Object> creationProperties = addCreationProperties(values);
        try (Session session = driver.session()) {
            session.run(
                    "MATCH (source_node:Resource{uri:$sourceUri}), " +
                            "(destination_node:Resource{uri:$destinationUri}) " +
                            "CREATE (n:Resource:GraphElement:Edge $edge), (n)-[:SOURCE_VERTEX]->(source_node), (n)-[:DESTINATION_VERTEX]->(destination_node)",
                    parameters(
                            "sourceUri",
                            sourceVertex.uri().toString(),
                            "destinationUri",
                            destinationVertex.uri().toString(),
                            "edge",
                            creationProperties
                    )
            );
            EdgePojo edge = new EdgePojo(
                    graphElementOperator.pojoFromCreationProperties(
                            creationProperties
                    )
            );
            edge.setSourceVertex(new VertexInSubGraphPojo(
                    sourceVertex.uri()
            ));
            edge.setDestinationVertex(new VertexInSubGraphPojo(
                    destinationVertex.uri()
            ));
            return edge;
        }
    }

    @Override
    public void createUsingInitialValues(Map<String, Object> values) {
        createEdgeUsingInitialValues(values);
    }

    @Override
    public void removeTag(Tag tag, ShareLevel sourceShareLevel) {
        graphElementOperator.removeTag(tag, sourceShareLevel);
    }

    @Override
    public Map<URI, TagPojo> addTag(Tag friendlyResource, ShareLevel sourceShareLevel) {
        return graphElementOperator.addTag(friendlyResource, sourceShareLevel);
    }

    @Override
    public Map<URI, TagPojo> getTags() {
        return graphElementOperator.getTags();
    }

    @Override
    public boolean equals(Object edgeToCompareAsObject) {
        return graphElementOperator.equals(edgeToCompareAsObject);
    }

    @Override
    public int hashCode() {
        return graphElementOperator.hashCode();
    }

    @Override
    public String queryPrefix() {
        return graphElementOperator.queryPrefix();
    }

    @Override
    public Map<String, Object> addCreationProperties(Map<String, Object> map) {
        Map<String, Object> newMap = map(
                VertexInSubGraphOperatorNeo4j.props.shareLevel.name(), ShareLevel.PRIVATE.getIndex()
        );
        newMap.putAll(
                map
        );
        newMap = graphElementOperator.addCreationProperties(
                newMap
        );
        return newMap;
    }
}
