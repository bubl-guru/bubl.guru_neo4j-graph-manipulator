package org.triple_brain.module.neo4j_graph_manipulator.graph;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.joda.time.DateTime;
import org.neo4j.graphdb.Node;
import org.triple_brain.module.model.FriendlyResource;
import org.triple_brain.module.model.Image;
import org.triple_brain.module.model.suggestion.SuggestionOrigin;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

/*
* Copyright Mozilla Public License 1.1
*/
public class Neo4JSuggestionOrigin implements SuggestionOrigin{

    public static final String ORIGIN_PROPERTY = "origin";

    protected Neo4JFriendlyResource friendlyResource;

    @AssistedInject
    protected Neo4JSuggestionOrigin(
            Neo4JFriendlyResourceFactory neo4JFriendlyResourceFactory,
            @Assisted Node node
    ){
        this.friendlyResource = neo4JFriendlyResourceFactory.createOrLoadFromNode(
                node
        );
    }

    @AssistedInject
    protected Neo4JSuggestionOrigin(
            Neo4JFriendlyResourceFactory neo4JFriendlyResourceFactory,
            @Assisted String origin,
            @Assisted Neo4JSuggestion suggestion
    ){
        this.friendlyResource = neo4JFriendlyResourceFactory.createOrLoadFromUri(
                URI.create(
                        suggestion.uri() + "/origin/" +
                                UUID.randomUUID().toString()
                )
        );
        suggestion.node.createRelationshipTo(
                friendlyResource.getNode(),
                Relationships.SUGGESTION_ORIGIN
        );
        setOrigin(origin);
    }

    @Override
    public Boolean isRelatedToFriendlyResource(FriendlyResource friendlyResource) {
        return getOrigin().contains(
                friendlyResource.uri().toString()
        );
    }

    @Override
    public void remove() {
        friendlyResource.getNode().delete();
    }

    @Override
    public String toString(){
        return getOrigin();
    }

    @Override
    public boolean equals(Object originToCompareAsObject) {
        SuggestionOrigin originToCompare = (SuggestionOrigin) originToCompareAsObject;
        return getOrigin().equals(originToCompare.toString());
    }

    @Override
    public int hashCode() {
        return getOrigin().hashCode();
    }

    private void setOrigin(String origin){
        friendlyResource.getNode().setProperty(
                ORIGIN_PROPERTY,
                origin
        );
    }

    public String getOrigin(){
        return friendlyResource.getNode().getProperty(
                ORIGIN_PROPERTY
        ).toString();
    }

    @Override
    public URI uri() {
        return friendlyResource.uri();
    }

    @Override
    public String label() {
        return friendlyResource.label();
    }

    @Override
    public void label(String label) {
        friendlyResource.label(
                label
        );
    }

    @Override
    public Set<Image> images() {
        return friendlyResource.images();
    }

    @Override
    public Boolean gotTheImages() {
        return friendlyResource.gotTheImages();
    }

    @Override
    public String comment() {
        return friendlyResource.comment();
    }

    @Override
    public void comment(String comment) {
        friendlyResource.comment(
                comment
        );
    }

    @Override
    public Boolean gotComments() {
        return friendlyResource.gotComments();
    }

    @Override
    public void addImages(Set<Image> images) {
        friendlyResource.addImages(images);
    }

    @Override
    public DateTime creationDate() {
        return friendlyResource.creationDate();
    }

    @Override
    public DateTime lastModificationDate() {
        return friendlyResource.lastModificationDate();
    }
}