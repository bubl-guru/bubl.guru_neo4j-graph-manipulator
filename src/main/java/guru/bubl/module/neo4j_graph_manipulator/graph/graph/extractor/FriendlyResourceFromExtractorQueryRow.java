/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor;

import guru.bubl.module.model.Image;
import guru.bubl.module.model.graph.FriendlyResourcePojo;
import guru.bubl.module.model.json.ImageJson;
import guru.bubl.module.neo4j_graph_manipulator.graph.Neo4jFriendlyResource;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.Neo4jUserGraph;
import guru.bubl.module.neo4j_graph_manipulator.graph.image.Neo4jImages;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FriendlyResourceFromExtractorQueryRow {

    private Map<String, Object> row;
    private String nodeKey;

    public static FriendlyResourceFromExtractorQueryRow usingRowAndNodeKey(
            Map<String, Object> row,
            String nodeKey
    ) {
        return new FriendlyResourceFromExtractorQueryRow(
                row,
                nodeKey
        );
    }

    public static FriendlyResourceFromExtractorQueryRow usingRowAndPrefix(
            Map<String, Object> row,
            String nodeKey
    ) {
        return new FriendlyResourceFromExtractorQueryRow(
                row,
                nodeKey
        );
    }

    protected FriendlyResourceFromExtractorQueryRow(Map<String, Object> row, String nodeKey) {
        this.row = row;
        this.nodeKey = nodeKey;
    }

    public FriendlyResourcePojo build() {
        return new FriendlyResourcePojo(
                getUri(),
                getLabel(),
                getImages(),
                getComment(),
                getCreationDate(),
                getLastModificationDate()
        );
    }

    private Set<Image> getImages() {
        Object imagesValue = row.get(
                nodeKey + "." + Neo4jImages.props.images
        );
        if (imagesValue == null) {
            return new HashSet<>();
        }
        return ImageJson.fromJson(
                imagesValue.toString()
        );
    }

    public String getLabel() {
        String labelKey = nodeKey + "." + Neo4jFriendlyResource.props.label + "";
        return row.get(
                labelKey
        ) != null ? row.get(labelKey).toString() : "";
    }

    private String getComment() {
        String commmentKey = nodeKey + "." + Neo4jFriendlyResource.props.comment + "";
        return row.get(
                commmentKey
        ) != null ? row.get(commmentKey).toString() : "";
    }

    private Date getLastModificationDate() {
        String key = nodeKey + "." + Neo4jFriendlyResource.props.last_modification_date.name();
        if (row.get(key) == null) {
            return new Date();
        }
        return new Date((Long) row.get(
                key
        ));
    }

    private Date getCreationDate() {
        String key = nodeKey + "." + Neo4jFriendlyResource.props.creation_date.name();
        if (row.get(key) == null) {
            return new Date();
        }
        return new Date((Long) row.get(
                key
        ));
    }

    public URI getUri() {
        return URI.create(
                row.get(nodeKey + "." + Neo4jUserGraph.URI_PROPERTY_NAME).toString()
        );
    }

}