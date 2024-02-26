package de.prob.model.eventb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.prob.animator.domainobjects.EventB;

import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.FreeIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class FormulaUtilTest {
	private static final Map<String, EventB> TEST_IDENTIFIER_MAPPING;
	static {
		Map<String, EventB> mapping = new HashMap<>();
		mapping.put("product", new EventB("p"));
		mapping.put("x", new EventB("x0"));
		mapping.put("y", new EventB("y0"));
		TEST_IDENTIFIER_MAPPING = Collections.unmodifiableMap(mapping);
	}

	private static final List<EventB> TEST_FORMULAS = Collections.unmodifiableList(Arrays.asList(
		new EventB("x := x + 1"),
		new EventB("x < 10"),
		new EventB("y > 10"),
		new EventB("z + 1 + x")
	));

	private FormulaUtil fuu;

	@BeforeEach
	void setUp() {
		fuu = new FormulaUtil();
	}

	@Test
	void substitutePredicate() {
		EventB e = fuu.substitute(new EventB("product = x * y"), TEST_IDENTIFIER_MAPPING);
		Assertions.assertEquals("p=x0*y0", e.getCode());
	}

	@Test
	void substituteExpression() {
		EventB e = fuu.substitute(new EventB("x * f(y)"), TEST_IDENTIFIER_MAPPING);
		Assertions.assertEquals("x0*f(y0)", e.getCode());
	}

	@Test
	void substituteSimpleAssignment() {
		EventB e = fuu.substitute(new EventB("product := x * y"), TEST_IDENTIFIER_MAPPING);
		Assertions.assertEquals("p:=x0*y0", e.getCode());
	}

	@Test
	void substituteMultipleAssignments() {
		EventB e = fuu.substitute(new EventB("product,x,y := x * y,y,x"), TEST_IDENTIFIER_MAPPING);
		Assertions.assertEquals("p,x0,y0:=x0*y0,y0,x0", e.getCode());
	}

	@Test
	void substituteBecomesSuchThat() {
		EventB e = fuu.substitute(new EventB("product :| product' = x * y"), TEST_IDENTIFIER_MAPPING);
		Assertions.assertEquals("p:|p'=x0*y0", e.getCode());
	}

	@Test
	void abstractAssignment() {
		Assertions.assertInstanceOf(Assignment.class, fuu.getRodinFormula(new EventB("x := 1")));
	}

	@Test
	void substituteBecomesElementOf() {
		EventB e = fuu.substitute(new EventB("x :: 1..x"), TEST_IDENTIFIER_MAPPING);
		Assertions.assertEquals("x0::1 .. x0", e.getCode());
	}

	@Test
	void substitutionMustBeExpression() {
		Map<String, EventB> mapping = new HashMap<>(TEST_IDENTIFIER_MAPPING);
		mapping.put("product", new EventB("p<1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> fuu.substitute(new EventB("x :: 1..x"), mapping));
	}

	@Test
	void substituteRequiresAssignment() {
		Map<String, EventB> mapping = new HashMap<>(TEST_IDENTIFIER_MAPPING);
		mapping.put("product", new EventB("p<1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> fuu.substitute(new EventB("x+1"), mapping));
	}

	@Test
	void substituteRequiresDeterministicAssignment() {
		Map<String, EventB> mapping = new HashMap<>(TEST_IDENTIFIER_MAPPING);
		mapping.put("product", new EventB("p<1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> fuu.substitute(new EventB("x::NAT"), mapping));
	}

	@Test
	void getIdentifier() {
		Assertions.assertInstanceOf(FreeIdentifier.class, fuu.getIdentifier(new EventB("x")));
	}

	@Test
	void getIdentifierChecksType() {
		Assertions.assertThrows(AssertionError.class, () -> fuu.getIdentifier(new EventB("x + 1")));
	}

	@Test
	void getIdentifierChecksType2() {
		Assertions.assertThrows(AssertionError.class, () -> fuu.getIdentifier(new EventB("x < 1")));
	}

	@Test
	void formulasWithX() {
		List<EventB> formulas = fuu.formulasWith(TEST_FORMULAS, new EventB("x"));
		Assertions.assertEquals(3, formulas.size());
	}

	@Test
	void formulasWithY() {
		List<EventB> formulas = fuu.formulasWith(TEST_FORMULAS, new EventB("y"));
		Assertions.assertEquals(1, formulas.size());
	}

	@Test
	void formulasWithZ() {
		List<EventB> formulas = fuu.formulasWith(TEST_FORMULAS, new EventB("z"));
		Assertions.assertEquals(1, formulas.size());
	}

	@Test
	void formulasWithM() {
		List<EventB> formulas = fuu.formulasWith(TEST_FORMULAS, new EventB("M"));
		Assertions.assertTrue(formulas.isEmpty());
	}

	@Test
	void conjunctToAssignments() {
		EventB f = new EventB("res = x / y & rem = x mod y");
		List<EventB> formulas = fuu.conjunctToAssignments(f, new HashSet<>(Arrays.asList("x", "y")), new HashSet<>(Arrays.asList("res", "rem")));
		Assertions.assertEquals(2, formulas.size());
		Assertions.assertEquals("res := x / y", formulas.get(0).getCode());
		Assertions.assertEquals("rem := x mod y", formulas.get(1).getCode());
	}

	@Test
	void predicateToBecomeSuchThat() {
		EventB f = new EventB("res = x / y & rem = x mod y");
		EventB formula = fuu.predicateToBecomeSuchThat(f, new HashSet<>(Arrays.asList("res", "rem")));
		Assertions.assertEquals("res,rem :| res'=x / y&rem'=x mod y", formula.getCode());
	}

	@Test
	void applyAssignment() {
		EventB f1 = fuu.applyAssignment(new EventB("p + (x0*y0) = x*y"),  new EventB("y0 := y0*2"));
		Assertions.assertEquals("p+x0*(y0*2)=x*y", f1.getCode());

		EventB f2 = fuu.applyAssignment(f1, new EventB("x0 := x0 / 2"));
		Assertions.assertEquals("p+x0 / 2*(y0*2)=x*y", f2.getCode());

		EventB f3 = fuu.applyAssignment(f2, new EventB("p := p + y0"));
		Assertions.assertEquals("(p+y0)+x0 / 2*(y0*2)=x*y", f3.getCode());
	}
}
