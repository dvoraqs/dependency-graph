package com.gbtec.dependency.graph;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

public class DependencyGraphTest {

    static GraphDatabaseService graphDb;
    Transaction tx;
    List<Dependency> dependencies;
    GraphWriter w;

    @BeforeClass
    public static void prepareTestDatabase() {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @AfterClass
    public static void destroyTestDatabase() {
        graphDb.shutdown();
    }

    @Before
    public void setUp() throws Exception {
        w = new Neo4JWriter(graphDb);
        dependencies = new ArrayList<Dependency>();
        tx = graphDb.beginTx();
    }

    @After
    public void tearDown() throws Exception {
        tx.close();
    }

    private int countNodes() {
        Iterable<Node> allNodes = GlobalGraphOperations.at(graphDb)
                .getAllNodes();
        return (int) Iterables.count(allNodes);
    }

    @Test
    public void oneDependencyMustBeAddedAsTwoNodes() throws Exception {
        dependencies.add(new Dependency("1", "2"));

        w.write(dependencies);

        assertThat(countNodes(), equalTo(2));
    }

    @Test
    public void twoDependenciesHavingSameParentMustBeAddedAsThreeNodes() throws Exception {
        dependencies.add(new Dependency("1", "2"));
        dependencies.add(new Dependency("1", "3"));

        w.write(dependencies);

        assertThat(countNodes(), equalTo(3));
    }

    @Test
    public void nodesMustHaveReleationship() throws Exception {
        dependencies.add(new Dependency("1", "2"));
        
        w.write(dependencies);

        ResourceIterable<Node> nodes = graphDb.findNodesByLabelAndProperty(
               Neo4JWriter.Label, "name", "1");
        Node from = nodes.iterator().next();
        Relationship relationship = from.getRelationships().iterator().next();
           
        assertEquals(relationship.getStartNode().getProperty("name"), "1");
        assertEquals(relationship.getEndNode().getProperty("name"), "2");
    }

}
