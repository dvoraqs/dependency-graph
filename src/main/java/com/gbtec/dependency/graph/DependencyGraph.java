package com.gbtec.dependency.graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.gbtec.dependency.graph.impl.Neo4JWriter;
import com.gbtec.dependency.parser.Parser;

public class DependencyGraph {

	public static void main(String[] args) {
		String userHome = System.getProperty("user.home");
		String defaultDbPath = userHome + "/Documents/Neo4j/default.graphdb";
		String defaultInputPath = "src/main/resources/tree.dot.txt";
		
		String inputPath = args.length > 0 ? args[0] : defaultInputPath;
		String dbPath = args.length > 1 ? args[1] : defaultDbPath;

		writeGraph(inputPath, dbPath);
	}

	private static void writeGraph(String inputPath, String dbPath) {
		Logger LOG = LogManager.getLogger();
		
		deleteFileOrDirectory(new File(dbPath));

		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(dbPath);
		
		registerShutdownHook(graphDb);

		try {
			Parser parser = new Parser();

			List<String> lines = FileUtils.readLines(new File(inputPath));

			GraphWriter writer = new Neo4JWriter(graphDb);
			writer.createRelatedNodes(parser.parse(lines));

		} catch (IOException e) {
			LOG.error(e);
		}

		graphDb.shutdown();
		LOG.info("Shut down finished");
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	private static void deleteFileOrDirectory(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					deleteFileOrDirectory(child);
				}
			}
			file.delete();
		}
	}
}
