package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

public class NodeCollectorTest {

	@Test
	public void test_node_traversal_collected_all_nodes() throws IOException, BCompoundException {
		Path file = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "check", "ContainsClauses.mch");
		BParser bParser = new BParser(file.toString());

		Start start = bParser.parseFile(file.toFile(), false);

		NodeCollector nodeCollector = new NodeCollector(start);



		Assertions.assertEquals("g ", nodeCollector.getConstants().stream().findFirst().get().toString());
		Assertions.assertEquals("x ",  nodeCollector.getVariables().stream().findFirst().get().toString());
		Assertions.assertEquals("DIRECTIONS left_blink right_blink neutral_blink ", nodeCollector.getSets().stream().findFirst().get().toString());
		Assertions.assertEquals("x ", nodeCollector.getInvariant().toString());
		Assertions.assertEquals("g ", nodeCollector.getProperties().toString());
		Assertions.assertEquals(new HashSet<>(Arrays.asList(Transition.INITIALISE_MACHINE_NAME, "[op ]")), nodeCollector.getOperationMap().keySet());

	}
}
