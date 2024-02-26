package de.prob.model.classicalb;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.Invariant;
import de.prob.model.representation.Variable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class DomWalkerTest {
	private ClassicalBMachine machine;

	@BeforeEach
	void setUp() throws BCompoundException {
		String testmachine = "MACHINE SimplyStructure\n"
			+ "FREETYPES Option = Some(INTEGER), None\n"
			+ "VARIABLES aa, b, Cc\n"
			+ "INVARIANT aa : NAT\n"
			+ "INITIALISATION aa:=1\n"
			+ "CONSTANTS dd, e, Ff\n"
			+ "PROPERTIES dd : NAT\n"
			+ "SETS GGG; Hhh; JJ = {dada, dudu, TUTUT}; iII; kkk = {LLL}\n"
			+ "END";
		BParser parser = new BParser("testcase");
		Start ast = parser.parseMachine(testmachine);
		machine = new DomBuilder(new File("SimplyStructure.mch"), "SimplyStructure", null).build(ast);
	}

	@Test
	void variables() {
		Assertions.assertIterableEquals(Arrays.asList("aa", "b", "Cc"), machine.getVariables().stream()
			.map(Variable::getName)
			.collect(Collectors.toList()));
	}

	@Test
	void name() {
		Assertions.assertEquals("SimplyStructure", machine.getName());
	}

	@Test
	void constants() {
		Assertions.assertIterableEquals(Arrays.asList("dd", "e", "Ff"), machine.getConstants().stream()
			.map(ClassicalBConstant::getName)
			.collect(Collectors.toList()));
	}

	@Test
	void invariants() {
		Assertions.assertIterableEquals(Collections.singletonList("aa:NAT"), machine.getInvariants().stream()
			.map(Invariant::getPredicate)
			.map(IEvalElement::getCode)
			.collect(Collectors.toList()));
	}

	@Test
	void freetypes() {
		Assertions.assertIterableEquals(Collections.singletonList("Option"), machine.getFreetypes().stream()
			.map(Freetype::getName)
			.collect(Collectors.toList()));
	}
}
