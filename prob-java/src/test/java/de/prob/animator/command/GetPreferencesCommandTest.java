package de.prob.animator.command;

import java.util.Collections;

import de.prob.animator.domainobjects.ProBPreference;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ResultParserException;
import de.prob.parser.SimplifiedROMap;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class GetPreferencesCommandTest {
	@Test
	void getPreferencesNull() {
		GetDefaultPreferencesCommand gpc = new GetDefaultPreferencesCommand();
		Assertions.assertNull(gpc.getPreferences());
	}

	@Test
	void writeCommand() {
		StructuredPrologOutput pto = new StructuredPrologOutput();
		GetDefaultPreferencesCommand command = new GetDefaultPreferencesCommand();
		command.writeCommand(pto);
		pto.fullstop();

		Assertions.assertEquals(1, pto.getSentences().size());
		CompoundPrologTerm cmdTerm = BindingGenerator.getCompoundTerm(pto.getSentences().get(0), "list_all_eclipse_preferences", 1);
		BindingGenerator.getVariable(cmdTerm.getArgument(1));
	}

	@Test
	void processResultValid() {
		GetDefaultPreferencesCommand command = new GetDefaultPreferencesCommand();
		PrologTerm resultTerm = new ListPrologTerm(
			new CompoundPrologTerm("preference",
				new CompoundPrologTerm("tinker"),
				new CompoundPrologTerm("tailor"),
				new CompoundPrologTerm("soldier"),
				new CompoundPrologTerm("sailor"),
				new CompoundPrologTerm("foo")
			),
			new CompoundPrologTerm("preference",
				new CompoundPrologTerm("richman"),
				new CompoundPrologTerm("poorman"),
				new CompoundPrologTerm("beggarman"),
				new CompoundPrologTerm("thief"),
				new CompoundPrologTerm("bar")
			)
		);
		command.processResult(new SimplifiedROMap<>(Collections.singletonMap("Prefs", resultTerm)));

		Assertions.assertEquals(2, command.getPreferences().size());

		ProBPreference pref1 = command.getPreferences().get(0);
		Assertions.assertEquals("tinker", pref1.name);
		Assertions.assertEquals("tailor", pref1.type.atomToString());
		Assertions.assertEquals("soldier", pref1.description);
		Assertions.assertEquals("sailor", pref1.category);
		Assertions.assertEquals("foo", pref1.defaultValue);

		ProBPreference pref2 = command.getPreferences().get(1);
		Assertions.assertEquals("richman", pref2.name);
		Assertions.assertEquals("poorman", pref2.type.atomToString());
		Assertions.assertEquals("beggarman", pref2.description);
		Assertions.assertEquals("thief", pref2.category);
		Assertions.assertEquals("bar", pref2.defaultValue);
	}

	@Test
	void processResultInvalidList() {
		GetDefaultPreferencesCommand command = new GetDefaultPreferencesCommand();
		PrologTerm resultTerm = new ListPrologTerm(new CompoundPrologTerm("blah blah blah"));

		Assertions.assertThrows(ResultParserException.class, () ->
			command.processResult(new SimplifiedROMap<>(Collections.singletonMap("Prefs", resultTerm)))
		);
	}

	@Test
	void processResultInvalid() {
		GetDefaultPreferencesCommand command = new GetDefaultPreferencesCommand();
		PrologTerm resultTerm = new CompoundPrologTerm("blah blah blah");

		Assertions.assertThrows(ResultParserException.class, () ->
			command.processResult(new SimplifiedROMap<>(Collections.singletonMap("Prefs", resultTerm)))
		);
	}
}
