package de.prob.model.classicalb;

import java.io.File;
import java.nio.file.Paths;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.model.representation.DependencyGraph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class ClassicalBModelTest {
	private ClassicalBModel model;

	@BeforeEach
	void setUp() throws BCompoundException {
		File modelFile = Paths.get("groovyTests", "machines", "references", "Foo.mch").toFile();
		model = new ClassicalBModel(null);
		BParser bparser = new BParser(modelFile.toString());
		Start ast = bparser.parseFile(modelFile);
		RecursiveMachineLoader rml = RecursiveMachineLoader.loadFromAst(bparser, ast, new ParsingBehaviour(), bparser.getContentProvider());
		model = model.create(ast, rml, modelFile, bparser);
	}

	@Test
	void graphMachineNames() {
		Assertions.assertTrue(model.getGraph().containsVertex("A"));
		Assertions.assertTrue(model.getGraph().containsVertex("A"));
		Assertions.assertTrue(model.getGraph().containsVertex("Foo"));
		Assertions.assertTrue(model.getGraph().containsVertex("C"));
		Assertions.assertTrue(model.getGraph().containsVertex("Bar"));
		Assertions.assertFalse(model.getGraph().containsVertex("Baz"));
	}

	@Test
	void refTypes() {
		Assertions.assertEquals(DependencyGraph.ERefType.REFINES, model.getEdge("Foo", "Bar"));
		Assertions.assertEquals(DependencyGraph.ERefType.SEES, model.getEdge("Foo", "A"));
		Assertions.assertEquals(DependencyGraph.ERefType.INCLUDES, model.getEdge("Foo", "C"));
		Assertions.assertEquals(DependencyGraph.ERefType.EXTENDS, model.getEdge("Foo", "D"));
	}

	@Test
	void nonexistantEdgesBetweenMachines() {
		Assertions.assertNull(model.getEdge("A", "C"));
		Assertions.assertNull(model.getEdge("D", "C"));
	}

	@Test
	void edgesBetweenNonexistantMachines() {
		IllegalArgumentException exc1 = Assertions.assertThrows(IllegalArgumentException.class, () -> model.getEdge("Blah", "A"));
		Assertions.assertTrue(exc1.getMessage().contains("is not in graph"));
		
		IllegalArgumentException exc2 = Assertions.assertThrows(IllegalArgumentException.class, () -> model.getEdge("A", "Blah"));
		Assertions.assertTrue(exc2.getMessage().contains("is not in graph"));
	}

	@Test
	void getRelationshipEqualsGetEdge() {
		Assertions.assertEquals(model.getRelationship("Foo", "Bar"), model.getEdge("Foo", "Bar"));
		Assertions.assertEquals(model.getRelationship("Foo", "A"), model.getEdge("Foo", "A"));
		Assertions.assertEquals(model.getRelationship("Foo", "C"), model.getEdge("Foo", "C"));
		Assertions.assertEquals(model.getRelationship("Foo", "D"), model.getEdge("Foo", "D"));
		Assertions.assertEquals(model.getRelationship("A", "C"), model.getEdge("A", "C"));
		Assertions.assertEquals(model.getRelationship("D", "C"), model.getEdge("D", "C"));
	}
}
