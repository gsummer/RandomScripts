import java.util.List;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPathBFS;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPathDijkstra;
import org.neo4j.graphalgo.impl.util.IntegerAdder;
import org.neo4j.graphalgo.impl.util.IntegerComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;

public class ShortestPathTests {

	public static void main(String[] args) {

		String tiny1 = "/Users/gsu/random/shortestpathtest/tiny1/data/graph.db";
		String tiny2 = "/Users/gsu/random/shortestpathtest/tiny2/data/graph.db";

		System.out.println("\nUsing DB: " + tiny1);
		
		GraphDatabaseService g1 = connectToGraph(tiny1);
		runTest(g1,"Node 7","Node 10");
		g1.shutdown();
		
		System.out.println("\nUsing DB: " + tiny2);
		
		GraphDatabaseService g2 = connectToGraph(tiny2);
		runTest(g2,"Node 7","Node 10");
		g2.shutdown();
	}
	
	public static void runTest(GraphDatabaseService g,String startName, String endName){
		try(Transaction tx = g.beginTx()){
			RelationshipType[] types = Iterables.toArray(RelationshipType.class,GlobalGraphOperations.at(g).getAllRelationshipTypes());

			SingleSourceShortestPath<Integer> bfs = new SingleSourceShortestPathBFS(null, Direction.BOTH, types);
			SingleSourceShortestPath<Integer> dijkstra = new SingleSourceShortestPathDijkstra<Integer>(0, null, new CostEvaluator<Integer>(){
				@Override
				public Integer getCost(Relationship relationship, Direction direction) {

					return new Integer(1);
				}
			}, new IntegerAdder(), new IntegerComparator(), Direction.BOTH, types);

			Node start 	= findNodeByName(startName,g);
			Node end 	= findNodeByName(endName,g);
			
			bfs.reset();
			bfs.setStartNode(start);
			List<List<Node>> shortestPathsBFS = bfs.getPathsAsNodes(end);
			System.out.println("BFS paths:");
			printPaths(shortestPathsBFS);
			
			dijkstra.reset();
			dijkstra.setStartNode(start);
			List<List<Node>> shortestPathsDijstra = dijkstra.getPathsAsNodes(end);
			System.out.println("Dijkstra paths:");
			printPaths(shortestPathsDijstra);
		}
	}

	private static Node findNodeByName(String name,GraphDatabaseService g) {
		for(Node n : GlobalGraphOperations.at(g).getAllNodes()){
			if(n.hasProperty("name") && n.getProperty("name").equals(name)){
				return n;
			}
		}

		return null;
	}
	
	private static void printPaths(List<List<Node>> paths){
		for(List<Node> path : paths){
			System.out.print("\t");
			for(Node n : path){
				System.out.print(n.getProperty("name") + "\t");
			}
			System.out.print("\n");
		}
	}

	public static GraphDatabaseService connectToGraph(String location){
		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(location);

		try (Transaction tx = g.beginTx()){
			long numNodes = Iterables.count(GlobalGraphOperations.at(g).getAllNodes());
			long numEdges = Iterables.count(GlobalGraphOperations.at(g).getAllRelationships());
			RelationshipType[] types = Iterables.toArray(RelationshipType.class,GlobalGraphOperations.at(g).getAllRelationshipTypes());
			long numTypes = types.length;

			System.out.println("numNodes: " + numNodes);
			System.out.println("numEdges: " + numEdges);
			System.out.println("numTypes: " + numTypes);

			tx.success();

		}

		return g;
	}

}
