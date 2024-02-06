package de.prob.model.representation;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DependencyGraphTest {
	@Test
	void basicGraphApi() {
		DependencyGraph g = new DependencyGraph()
			.addEdge("M0", "ctx0", DependencyGraph.ERefType.SEES)
			.addEdge("ctx1", "ctx0", DependencyGraph.ERefType.EXTENDS)
			.addEdge("ctx2", "ctx1", DependencyGraph.ERefType.EXTENDS)
			.addEdge("M1", "ctx1", DependencyGraph.ERefType.SEES)
			.addEdge("M1", "ctx2", DependencyGraph.ERefType.SEES)
			.addEdge("M1", "M0", DependencyGraph.ERefType.REFINES)
			.addEdge("M2", "M0", DependencyGraph.ERefType.REFINES)
			.addEdge("M2", "ctx2", DependencyGraph.ERefType.REFINES);

		Assertions.assertEquals(8, g.getEdges().size());
		Assertions.assertEquals(6, g.getVertices().size());

		java.util.Set<DependencyGraph.Edge> outEdges = g.getOutEdges("M1");

		for (DependencyGraph.Edge e : outEdges) {
			Assertions.assertEquals("M1", e.getFrom().getElementName());
		}

		Assertions.assertIterableEquals(Arrays.asList("M0", "ctx1", "ctx2"), outEdges.stream()
			.map(DependencyGraph.Edge::getTo)
			.map(DependencyGraph.Node::getElementName)
			.sorted()
			.collect(Collectors.toList()));

		Assertions.assertIterableEquals(Collections.singletonList("M0"), outEdges.stream()
			.filter(e -> e.getRelationship() == DependencyGraph.ERefType.REFINES)
			.map(DependencyGraph.Edge::getTo)
			.map(DependencyGraph.Node::getElementName)
			.sorted()
			.collect(Collectors.toList()));

		Assertions.assertIterableEquals(Arrays.asList("ctx1", "ctx2"), outEdges.stream()
			.filter(e -> e.getRelationship() == DependencyGraph.ERefType.SEES)
			.map(DependencyGraph.Edge::getTo)
			.map(DependencyGraph.Node::getElementName)
			.sorted()
			.collect(Collectors.toList()));

		java.util.Set<DependencyGraph.Edge> inEdges = g.getIncomingEdges("M0");
		Assertions.assertEquals(2, inEdges.size());

		for (DependencyGraph.Edge e : inEdges) {
			Assertions.assertEquals(e.getRelationship(), DependencyGraph.ERefType.REFINES);
			Assertions.assertEquals("M0", e.getTo().getElementName());
		}

		Assertions.assertIterableEquals(Arrays.asList("M1", "M2"), inEdges.stream()
			.map(DependencyGraph.Edge::getFrom)
			.map(DependencyGraph.Node::getElementName)
			.sorted()
			.collect(Collectors.toList()));
	}

	@Test
	public void refinement_relationship_empty(){
		DependencyGraph dependencyGraph = new DependencyGraph();

		Assertions.assertTrue(dependencyGraph.refinementChain().isEmpty());
	}

	@Test
	public void refinement_relationship_one_entry(){
		DependencyGraph dependencyGraph = new DependencyGraph();

		String name = "NumberOne";
		dependencyGraph = dependencyGraph.addVertex(name);

		Assertions.assertEquals(Collections.singletonList(new DependencyGraph.Node(name)), dependencyGraph.refinementChain());
	}

	@Test
	public void refinement_relationship_three_chained_entries(){
		DependencyGraph dependencyGraph = new DependencyGraph();

		String one = "NumberOne";
		String two = "NumberTwo";
		String three = "NumberThree";

		dependencyGraph = dependencyGraph.addVertex(one);
		dependencyGraph = dependencyGraph.addVertex(two);
		dependencyGraph = dependencyGraph.addVertex(three);

		dependencyGraph = dependencyGraph.addEdge(one, two, DependencyGraph.ERefType.REFINES);
		dependencyGraph = dependencyGraph.addEdge(two, three, DependencyGraph.ERefType.REFINES);

		DependencyGraph.Node node1 = new DependencyGraph.Node(one);
		DependencyGraph.Node node2 = new DependencyGraph.Node(two);
		DependencyGraph.Node node3 = new DependencyGraph.Node(three);

		Assertions.assertEquals(Stream.of(node1, node2, node3).collect(Collectors.toList()), dependencyGraph.refinementChain());
	}

	@Test
	public void refinement_relationship_three_chained_entries_one_unused(){
		DependencyGraph dependencyGraph = new DependencyGraph();

		String one = "NumberOne";
		String two = "NumberTwo";
		String three = "NumberThree";
		String four = "four";

		dependencyGraph = dependencyGraph.addVertex(one);
		dependencyGraph = dependencyGraph.addVertex(two);
		dependencyGraph = dependencyGraph.addVertex(three);
		dependencyGraph = dependencyGraph.addVertex(four);

		dependencyGraph = dependencyGraph.addEdge(one, two, DependencyGraph.ERefType.REFINES);
		dependencyGraph = dependencyGraph.addEdge(two, three, DependencyGraph.ERefType.REFINES);
		dependencyGraph = dependencyGraph.addEdge(two, four, DependencyGraph.ERefType.SEES);

		DependencyGraph.Node node1 = new DependencyGraph.Node(one);
		DependencyGraph.Node node2 = new DependencyGraph.Node(two);
		DependencyGraph.Node node3 = new DependencyGraph.Node(three);

		Assertions.assertEquals(Stream.of(node1, node2, node3).collect(Collectors.toList()), dependencyGraph.refinementChain());
	}
}
