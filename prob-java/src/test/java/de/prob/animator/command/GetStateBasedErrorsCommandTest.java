package de.prob.animator.command;

import java.util.Collections;

import de.prob.animator.domainobjects.StateError;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ResultParserException;
import de.prob.parser.SimplifiedROMap;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class GetStateBasedErrorsCommandTest {
	@Test
	void writeCommand() {
		StructuredPrologOutput pto = new StructuredPrologOutput();
		GetStateBasedErrorsCommand command = new GetStateBasedErrorsCommand("42");
		command.writeCommand(pto);
		pto.fullstop();

		Assertions.assertEquals(1, pto.getSentences().size());
		CompoundPrologTerm cmdTerm = BindingGenerator.getCompoundTerm(pto.getSentences().get(0), "prob2_get_state_errors", 2);
		Assertions.assertEquals(42, BindingGenerator.getAInteger(cmdTerm.getArgument(1)).intValueExact());
		BindingGenerator.getVariable(cmdTerm.getArgument(2));
	}

	@Test
	void processResultNonempty() {
		GetStateBasedErrorsCommand command = new GetStateBasedErrorsCommand("state");
		PrologTerm resultTerm = new ListPrologTerm(
			new CompoundPrologTerm("error", new ListPrologTerm(
				new CompoundPrologTerm("event", new CompoundPrologTerm("foo")),
				new CompoundPrologTerm("description", new CompoundPrologTerm("bar")),
				new CompoundPrologTerm("long_description", new CompoundPrologTerm("baz"))
			))
		);
		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("Errors", resultTerm)));

		Assertions.assertEquals(1, command.getResult().size());
		StateError se = command.getResult().iterator().next();
		Assertions.assertEquals("foo", se.getEvent());
		Assertions.assertEquals("bar", se.getShortDescription());
		Assertions.assertEquals("baz", se.getLongDescription());
	}

	@Test
	void processResultEmpty() {
		GetStateBasedErrorsCommand command = new GetStateBasedErrorsCommand("state");
		PrologTerm resultTerm = new ListPrologTerm();
		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("Errors", resultTerm)));

		Assertions.assertTrue(command.getResult().isEmpty());
	}

	@Test
	void processResultInvalidList() {
		GetStateBasedErrorsCommand command = new GetStateBasedErrorsCommand("state");
		PrologTerm resultTerm = new ListPrologTerm(new CompoundPrologTerm("foobar"));

		Assertions.assertThrows(ResultParserException.class, () ->
			command.processResult(new SimplifiedROMap<>(Collections.singletonMap("Errors", resultTerm)))
		);
	}

	@Test
	void processResultInvalid() {
		GetStateBasedErrorsCommand command = new GetStateBasedErrorsCommand("state");
		PrologTerm resultTerm = new CompoundPrologTerm("foobar");

		Assertions.assertThrows(ResultParserException.class, () ->
			command.processResult(new SimplifiedROMap<>(Collections.singletonMap("Errors", resultTerm)))
		);
	}
}
