package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class OperationFinderTest {

	@Test
	public void test_if_promoted_seen_operations_are_recognized() throws BCompoundException, IOException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "operationFinder", "MachineB.mch");


		BParser parser = new BParser(file.toString());
		Start result = parser.parseFile(file.toFile(), false);
		OperationsFinder operationsFinder = new OperationsFinder("MachineA", result);
		operationsFinder.explore();

		Map<String, Set<String>> expected = new HashMap<>();
		expected.put("Foo", Collections.singleton("Inc2"));


		Map<String, Set<String>> expected2 = new HashMap<>();
		expected2.put("Inc2", Collections.singleton("Foo"));


		Assertions.assertEquals(Collections.singleton("Inc"), operationsFinder.getPromoted());
		Assertions.assertEquals(expected, operationsFinder.getUsed());
		Assertions.assertEquals(expected2, operationsFinder.usedOperationsReversed());


	}

	@Test
	public void extends_are_recognized() throws BCompoundException, IOException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "extends", "M2.mch");


		BParser parser = new BParser(file.toString());
		Start result = parser.parseFile(file.toFile(), false);
		OperationsFinder operationsFinder = new OperationsFinder("M1", result);
		operationsFinder.explore();


		Assertions.assertTrue(operationsFinder.isExtendsSourceMachine());



	}
}
