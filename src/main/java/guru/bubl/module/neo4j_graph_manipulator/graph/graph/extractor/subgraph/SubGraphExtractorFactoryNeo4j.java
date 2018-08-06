/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph;

import com.google.inject.assistedinject.Assisted;
import guru.bubl.module.model.graph.ShareLevel;

import java.net.URI;
import java.util.Set;

public interface SubGraphExtractorFactoryNeo4j {
    SubGraphExtractorNeo4j withCenterVertexAndDepth(
            URI centerVertexUri,
            @Assisted("depth") Integer depth
    );

    SubGraphExtractorNeo4j withCenterVertexInShareLevels(
            URI centerVertexUri,
            Set<ShareLevel> shareLevels
    );

    SubGraphExtractorNeo4j withCenterVertexInShareLevelsAndDepth(
            URI centerVertexUri,
            Set<ShareLevel> shareLevels,
            Integer depth
    );

    SubGraphExtractorNeo4j withCenterVertexDepthAndResultsLimit(
            URI centerVertexUri,
            @Assisted("depth") Integer depth,
            @Assisted("resultsLimit") Integer resultsLimit
    );
}