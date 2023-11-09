package de.prob.model.representation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import de.prob.model.representation.DependencyGraph.Node;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyGraphTest {


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

		Assertions.assertEquals(Collections.singletonList(new Node(name)), dependencyGraph.refinementChain());
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

		DependencyGraph.Node node1 = new Node(one);
		DependencyGraph.Node node2 = new Node(two);
		DependencyGraph.Node node3 = new Node(three);

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

		DependencyGraph.Node node1 = new Node(one);
		DependencyGraph.Node node2 = new Node(two);
		DependencyGraph.Node node3 = new Node(three);

		Assertions.assertEquals(Stream.of(node1, node2, node3).collect(Collectors.toList()), dependencyGraph.refinementChain());
	}
}
