package de.prob.check.tracereplay.check.refinement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OperationFinderTest {

	@Test
	public void test_if_promoted_seen_operations_are_recognized() throws BCompoundException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "operationFinder", "MachineB.mch");


		BParser parser = new BParser(file.toString());
		Start result = parser.parseFile(file.toFile());
		OperationsFinder operationsFinder = new OperationsFinder("MachineA", result);
		operationsFinder.explore();

		Map<String, Set<String>> expected = new HashMap<>();
		expected.put("Foo", Collections.singleton("Inc2"));


		Map<String, Set<String>> expected2 = new HashMap<>();
		expected2.put("Inc2", Collections.singleton("Foo"));


		Assertions.assertEquals(Collections.singleton("Inc"), operationsFinder.getPromoted().stream().map(OperationsFinder.RenamingContainer::toString).collect(Collectors.toSet()));
		Assertions.assertEquals(expected, operationsFinder.getUsed());
		Assertions.assertEquals(expected2, operationsFinder.usedOperationsReversed());


	}

	@Test
	public void extends_are_recognized() throws BCompoundException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "extends", "M2.mch");


		BParser parser = new BParser(file.toString());
		Start result = parser.parseFile(file.toFile());
		OperationsFinder operationsFinder = new OperationsFinder("M1", result);
		operationsFinder.explore();


		Assertions.assertTrue(operationsFinder.isExtendsSourceMachine());

	}


	@Test
	public void promoted_operations_are_recognized_correctly() throws BCompoundException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "promotes", "M4.mch");


		BParser parser = new BParser(file.toString());
		Start result = parser.parseFile(file.toFile());
		OperationsFinder operationsFinder = new OperationsFinder("M3", result);
		operationsFinder.explore();

		Set<String> operationResult = operationsFinder.getPromoted().stream().map(OperationsFinder.RenamingContainer::toString).collect(Collectors.toSet());

		Set<String> expected = Stream.of("gna.Inc", "Pow").collect(Collectors.toSet());

		Assertions.assertEquals(expected, operationResult);

	}

	@Test
	public void included_operations_are_recognized_correctly() throws BCompoundException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "promotes", "M4.mch");


		BParser parser = new BParser(file.toString());
		Start result = parser.parseFile(file.toFile());
		OperationsFinder operationsFinder = new OperationsFinder("M3", result);
		operationsFinder.explore();

		Set<String> operationResult = operationsFinder.getIncludedImportedMachines().stream().map(OperationsFinder.RenamingContainer::toString).collect(Collectors.toSet());

		Set<String> expected = Stream.of("gna.M1", "M3").collect(Collectors.toSet());

		Assertions.assertEquals(expected, operationResult);

	}
}
