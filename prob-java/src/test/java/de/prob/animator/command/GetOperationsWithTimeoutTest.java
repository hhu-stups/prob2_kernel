package de.prob.animator.command;

import java.util.Collections;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ResultParserException;
import de.prob.parser.SimplifiedROMap;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class GetOperationsWithTimeoutTest {
	@Test
	void writeCommand() {
		StructuredPrologOutput pto = new StructuredPrologOutput();
		GetOperationsWithTimeout command = new GetOperationsWithTimeout("state");
		command.writeCommand(pto);
		pto.fullstop();

		Assertions.assertEquals(1, pto.getSentences().size());
		CompoundPrologTerm cmdTerm = BindingGenerator.getCompoundTerm(pto.getSentences().get(0), "op_timeout_occurred", 2);
		Assertions.assertEquals("state", cmdTerm.getArgument(1).atomToString());
		BindingGenerator.getVariable(cmdTerm.getArgument(2));
	}

	@Test
	void processResultValid() {
		GetOperationsWithTimeout command = new GetOperationsWithTimeout("state");
		PrologTerm resultTerm = new ListPrologTerm(
			new CompoundPrologTerm("foobar"),
			new CompoundPrologTerm("bliblablub")
		);
		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("TO", resultTerm)));

		Assertions.assertEquals(2, command.getTimeouts().size());
		Assertions.assertEquals("foobar", command.getTimeouts().get(0));
		Assertions.assertEquals("bliblablub", command.getTimeouts().get(1));
	}
	
	@Test
	void processResultInvalid() {
		GetOperationsWithTimeout command = new GetOperationsWithTimeout("state");

		Assertions.assertThrows(ResultParserException.class, () ->
			command.processResult(new SimplifiedROMap<>(Collections.singletonMap("TO", new CompoundPrologTerm("bang!!!"))))
		);
	}
}
