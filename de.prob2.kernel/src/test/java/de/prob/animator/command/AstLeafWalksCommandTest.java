package de.prob.animator.command;

import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.cli.CliTestCommon;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AstLeafWalksCommandTest {
	private static Api api;
	private static StateSpace stateSpace;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@BeforeEach
	void beforeEach() throws IOException, URISyntaxException {
		String example_mch = Paths.get(CbcSolveCommand.class.getClassLoader()
				.getResource("de/prob/testmachines/b/VariablesOnly.mch")
				.toURI()).toString();
		stateSpace = api.b_load(example_mch);
	}

	@Test
	void shouldGetPaths() {
		String predicate = "a:INTEGER & a > 5";
		ClassicalB bast = new ClassicalB(predicate, FormulaExpand.EXPAND);

		AstLeafWalksCommand cmd = new AstLeafWalksCommand(bast);
		stateSpace.execute(cmd);

		Set<List<String>> expected = new HashSet<>();
		expected.add(path(new String[]{"a", "identifier"},
				"greater",
				new String[]{"integer", "5"}));

		Set<List<String>> actual = cmd.getWalks();

		assertEquals(expected, actual);
	}

	@Test
	void shouldGetPathsForExistsExpression() {
		String predicate =
				"#a,b . (a:INTEGER & b:INTEGER & c:INTEGER & d:INTEGER & c > d & e > 1)";

		ClassicalB bast = new ClassicalB(predicate, FormulaExpand.EXPAND);

		AstLeafWalksCommand cmd = new AstLeafWalksCommand(bast);
		stateSpace.execute(cmd);

		Set<List<String>> expected = new HashSet<>();
		expected.add(path(new String[]{"c", "identifier"},
				"greater",
				new String[]{"identifier", "d"}));
		expected.add(path(new String[]{"c", "identifier", "greater"},
				"conjunct",
				new String[]{"greater", "identifier", "e"}));
		expected.add(path(new String[]{"c", "identifier", "greater"},
				"conjunct",
				new String[]{"greater", "integer", "1"}));
		expected.add(path(new String[]{"d", "identifier", "greater"},
				"conjunct",
				new String[]{"greater", "identifier", "e"}));
		expected.add(path(new String[]{"d", "identifier", "greater"},
				"conjunct",
				new String[]{"greater", "integer", "1"}));
		expected.add(path(new String[]{"e", "identifier"},
				"greater",
				new String[]{"integer", "1"}));

		Set<List<String>> actual = cmd.getWalks();

		assertEquals(expected, actual);
	}

	@Test
	void shouldTranslatePath() {
		// a-b-root-(d-e-f)
		// -(-(-(a, b), root), -(-(d, e), f))
		CompoundPrologTerm p =
				m(
						m(
								m(a("a"), a("b")),
								new CompoundPrologTerm("root", a("r"))),
						m(
								m(a("d"), a("e")),
								a("f")));

		List<String> expected = new ArrayList<>();
		String up = AstLeafWalksCommand.AST_UP_SYMB;
		String dn = AstLeafWalksCommand.AST_DOWN_SYMB;
		expected.add("a" + up);
		expected.add("b" + up);
		expected.add("r");
		expected.add(dn + "d");
		expected.add(dn + "e");
		expected.add(dn + "f");

		List<String> actual = AstLeafWalksCommand.translateLeafPath(p);

		assertEquals(expected, actual);
	}

	CompoundPrologTerm m(PrologTerm lhs, PrologTerm rhs) {
		return new CompoundPrologTerm("-", lhs, rhs);
	}

	CompoundPrologTerm a(String atom) {
		return new CompoundPrologTerm(atom);
	}

	List<String> path(String[] ups, String root, String[] downs) {
		List<String> p = new ArrayList<>();
		for (String u : ups) {
			p.add(u + AstLeafWalksCommand.AST_UP_SYMB);
		}
		p.add(root);
		for (String d : downs) {
			p.add(AstLeafWalksCommand.AST_DOWN_SYMB + d);
		}

		return p;
	}
}
