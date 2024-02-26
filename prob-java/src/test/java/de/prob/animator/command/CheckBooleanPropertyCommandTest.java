package de.prob.animator.command;

import java.util.Collections;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ResultParserException;
import de.prob.parser.SimplifiedROMap;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.CompoundPrologTerm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class CheckBooleanPropertyCommandTest {
	@Test
	void writeCommand() {
		StructuredPrologOutput pto = new StructuredPrologOutput();
		CheckBooleanPropertyCommand cmd = new CheckBooleanPropertyCommand("A_DINGO_ATE_MY_BABY", "root");
		cmd.writeCommand(pto);
		pto.fullstop();

		Assertions.assertEquals(1, pto.getSentences().size());
		CompoundPrologTerm cmdTerm = BindingGenerator.getCompoundTerm(pto.getSentences().get(0), "state_property", 3);
		Assertions.assertEquals("A_DINGO_ATE_MY_BABY", cmdTerm.getArgument(1).atomToString());
		Assertions.assertEquals("root", cmdTerm.getArgument(2).atomToString());
		BindingGenerator.getVariable(cmdTerm.getArgument(3));
	}
	
	@Test
	void processResultValid() {
		CheckBooleanPropertyCommand cmd = new CheckBooleanPropertyCommand("BLAH_BLAH", "root");

		cmd.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("true"))));
		Assertions.assertTrue(cmd.getResult());

		cmd.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("false"))));
		Assertions.assertFalse(cmd.getResult());
	}

	@Test
	void processResultInvalid() {
		CheckBooleanPropertyCommand cmd = new CheckBooleanPropertyCommand("BLAH_BLAH", "root");

		Assertions.assertThrows(ResultParserException.class, () ->
			cmd.processResult(new SimplifiedROMap<>(Collections.singletonMap("PropResult", new CompoundPrologTerm("fff"))))
		);
	}

	@Test
	void processResultNull() {
		CheckBooleanPropertyCommand cmd = new CheckBooleanPropertyCommand("BLAH_BLAH", "root");

		Assertions.assertThrows(IllegalStateException.class, cmd::getResult);
	}
}
