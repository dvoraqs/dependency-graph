package com.gbtec.dependency.graph;

import java.util.List;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

public class Neo4JWriter implements GraphWriter {

    private GraphDatabaseService graphDb = null;

    static Label Label = DynamicLabel.label("Label_Name");
    
    public Neo4JWriter(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    @Override
    public void write(List<Dependency> dependencies) {

        Transaction tx = graphDb.beginTx();

        for (Dependency d : dependencies) {

            ResourceIterable<Node> nodes = graphDb.findNodesByLabelAndProperty(
                    Label, "name", d.from());

            Node from;
            if (nodes.iterator().hasNext()) {
                from = nodes.iterator().next();
                
                nodes.iterator().close();
            } else {
                from = graphDb.createNode(Label);
                from.setProperty("name", d.from());
            }

            nodes = graphDb.findNodesByLabelAndProperty(Label, "name", d.to());
            Node to;
            if (nodes.iterator().hasNext()) {
                to = nodes.iterator().next();
                nodes.iterator().close();
            } else {
                to = graphDb.createNode(Label);
                to.setProperty("name", d.to());
            }

            from.createRelationshipTo(to, DynamicRelationshipType.withName("dependsOn"));
        }
        tx.success();

    }
}
