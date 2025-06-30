package de.prob.animator.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompletionTest {

	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testCompletions() throws IOException, BCompoundException {
		Start ast = new BParser("M").parseMachine("MACHINE M\n" +
				"CONSTANTS a, b, c\n" +
				"PROPERTIES a = TRUE & b : 1..3 & c = \"hello\"\n" +
				"END");
		StateSpace ss = api.b_load(ast);

		CompleteIdentifierCommand cmd1 = new CompleteIdentifierCommand("");
		ss.execute(cmd1);

		List<String> completions = cmd1.getCompletions().stream().map(CompleteIdentifierCommand.Completion::getCompletion).sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("a", "b", "c", "from_string"), completions);
		// from_string is the machine name

		ss.kill();
	}

	@Test
	public void testCompletionsString() throws IOException, BCompoundException {
		Start ast = new BParser("M").parseMachine("MACHINE M\n" +
				"CONSTANTS a, b, c\n" +
				"PROPERTIES a = TRUE & b : 1..3 & c = \"hello\"\n" +
				"END");
		StateSpace ss = api.b_load(ast);

		CompleteIdentifierCommand cmd1 = new CompleteIdentifierCommand("");
		cmd1.addKeywordContext(CompleteIdentifierCommand.KeywordContext.STRING);
		ss.execute(cmd1);

		List<String> completions = cmd1.getCompletions().stream().map(CompleteIdentifierCommand.Completion::getCompletion).sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("a", "b", "c", "from_string", "hello"), completions);

		ss.kill();
	}

	@Test
	public void testCompletionsStringOnly() throws IOException, BCompoundException {
		Start ast = new BParser("M").parseMachine("MACHINE M\n" +
				"CONSTANTS a, b, c\n" +
				"PROPERTIES a = TRUE & b : 1..3 & c = \"hello\"\n" +
				"END");
		StateSpace ss = api.b_load(ast);

		CompleteIdentifierCommand cmd1 = new CompleteIdentifierCommand("");
		cmd1.setStringsOnly(true);
		ss.execute(cmd1);

		List<String> completions = cmd1.getCompletions().stream().map(CompleteIdentifierCommand.Completion::getCompletion).sorted().collect(Collectors.toList());
		assertEquals(Collections.singletonList("hello"), completions);

		ss.kill();
	}
}
