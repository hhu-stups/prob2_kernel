package de.prob.cli.integration.rules;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.prob.model.brules.ComputationStatus;
import de.prob.model.brules.OperationStatuses;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleResult.CounterExample;
import de.prob.model.brules.RuleResults;
import de.prob.model.brules.RuleResults.ResultSummary;
import de.prob.model.brules.RuleStatus;
import de.prob.model.brules.RulesMachineRun;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.State;

import org.junit.jupiter.api.Test;

import static de.prob.cli.integration.rules.RulesTestUtil.createRulesMachineFile;
import static de.prob.cli.integration.rules.RulesTestUtil.startRulesMachineRun;
import static de.prob.cli.integration.rules.RulesTestUtil.startRulesMachineRunWithOperations;
import static org.junit.jupiter.api.Assertions.*;

public class RulesMachineTest {

	private static final Path DIR = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "brules");

	@Test
	public void testSimpleRulesMachine() {
		RulesMachineRun rulesMachineRun = startRulesMachineRun(DIR.resolve("SimpleRulesMachine.rmch").toFile());
		assertFalse(rulesMachineRun.hasError());
		assertTrue(rulesMachineRun.getErrorList().isEmpty());
		assertNull(rulesMachineRun.getFirstError());

		RuleResults ruleResults = rulesMachineRun.getRuleResults();
		ResultSummary summary = ruleResults.getSummary();
		// summary is created only once
		assertEquals(summary, ruleResults.getSummary());

		assertEquals(4, ruleResults.getRuleResultList().size());

		RuleResult rule1Result = ruleResults.getRuleResult("Rule1");
		assertEquals(RuleStatus.SUCCESS, rule1Result.getRuleState());
		RuleOperation rule1Operation = rule1Result.getRuleOperation();
		assertEquals("Rule1", rule1Operation.getName());
		assertTrue(rule1Result.getNotCheckedDependencies().isEmpty(), "Should be empty");

		RuleResult result2 = ruleResults.getRuleResult("Rule2");
		assertEquals(RuleStatus.FAIL, result2.getRuleState());
		String message = result2.getCounterExamples().get(0).getMessage();
		assertEquals("ERROR2", message);

		assertEquals(RuleStatus.NOT_CHECKED, ruleResults.getRuleResult("Rule3").getRuleState());
		assertEquals("Rule2", ruleResults.getRuleResult("Rule3").getFailedDependencies().get(0));
	}

	@Test
	public void testRulesMachineExample() {
		RulesMachineRun rulesMachineRun = startRulesMachineRun(DIR.resolve("RulesMachineExample.rmch").toFile());
		assertFalse(rulesMachineRun.hasError());
		State finalState = rulesMachineRun.getExecuteRun().getExecuteModelCommand().getFinalState();
		RulesModel model = (RulesModel) rulesMachineRun.getAnimator().getCurrentStateSpace().getModel();
		AbstractOperation operation = rulesMachineRun.getRulesProject().getOperationsMap().get("COMP_comp1");
		assertEquals(ComputationStatus.EXECUTED, OperationStatuses.getStatus(model, operation, finalState));
	}

	@Test
	public void testCounterExample() {
		RulesMachineRun rulesMachineRun = startRulesMachineRunWithOperations(
				"RULE foo BODY RULE_FAIL ERROR_TYPE 1 COUNTEREXAMPLE \"error\"END END");
		assertNull(rulesMachineRun.getFirstError());
		RuleResult ruleResult = rulesMachineRun.getRuleResults().getRuleResult("foo");
		List<CounterExample> counterExamples = ruleResult.getCounterExamples();
		assertEquals(1, counterExamples.size());
		CounterExample counterExample = counterExamples.get(0);
		assertEquals(1, counterExample.getErrorType());
	}

	@Test
	public void testReuseStateSpace() {
		String ruleWithWDError = "RULE Rule1 BODY VAR xx IN xx := {1|->2}(3) END;RULE_FAIL WHEN 1=2 COUNTEREXAMPLE \"fail\" END END";
		RulesMachineRun rulesMachineRun = startRulesMachineRunWithOperations(ruleWithWDError);
		BigInteger numberAfterFirstRun = rulesMachineRun.getTotalNumberOfProBCliErrors();

		RulesMachineRun rulesMachineRun2 = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(),
				RulesTestUtil.createRulesMachineFileContainingOperations(ruleWithWDError).getAbsoluteFile());
		rulesMachineRun2.setAnimator(rulesMachineRun.getAnimator());
		rulesMachineRun2.start();
		BigInteger numberAfterSecondRun = rulesMachineRun2.getTotalNumberOfProBCliErrors();

		assertTrue(numberAfterSecondRun.intValue() > numberAfterFirstRun.intValue());
	}

	@Test
	public void testNumberOfReportedCounterExamples() {
		RulesMachineRun rulesMachineRun = startRulesMachineRunWithOperations(
				"RULE Rule1 BODY RULE_FAIL x WHEN x : 1..1000 COUNTEREXAMPLE STRING_FORMAT(\"~w\", x) END END");
		// default value is 50
		assertEquals(50, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getCounterExamples().size());
	}

	@Test
	public void testNumberOfReportedSuccessMessages() {
		RulesMachineRun rulesMachineRun = startRulesMachineRunWithOperations(
			"RULE Rule1 BODY RULE_FORALL x WHERE x : 1..1000 EXPECT x : 1..200 ON_SUCCESS ```${x}``` COUNTEREXAMPLE STRING_FORMAT(\"~w\", x) END END");
		assertEquals(50, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getSuccessMessages().size());
	}

	@Test
	public void testChangeNumberOfReportedCounterExamples() {
		File file = createRulesMachineFile(
				"OPERATIONS RULE Rule1 BODY RULE_FAIL x WHEN x : 1..1000 COUNTEREXAMPLE STRING_FORMAT(\"~w\", x) END END");
		RulesMachineRun rulesMachineRun = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(), file);
		rulesMachineRun.setMaxNumberOfReportedCounterExamples(20);
		rulesMachineRun.start();
		assertEquals(20, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getCounterExamples().size());
	}

	@Test
	public void testChangeNumberOfReportedSuccessMessages() {
		File file = createRulesMachineFile(
			"OPERATIONS RULE Rule1 BODY RULE_FORALL x WHERE x : 1..1000 EXPECT x : 1..200 ON_SUCCESS ```${x}``` COUNTEREXAMPLE STRING_FORMAT(\"~w\", x) END END");
		RulesMachineRun rulesMachineRun = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(), file);
		rulesMachineRun.setMaxNumberOfReportedSuccessMessages(20);
		rulesMachineRun.start();
		assertEquals(20, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getSuccessMessages().size());
	}

	@Test
	public void testExtractingAllCounterExample() {
		File file = createRulesMachineFile(
				"OPERATIONS RULE Rule1 BODY RULE_FAIL x WHEN x : 1..1000 COUNTEREXAMPLE STRING_FORMAT(\"This is a long counter example message including a unique number to test that the extracted B value will not be truncated: ~w\", x) END END");
		RulesMachineRun rulesMachineRun = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(), file);
		rulesMachineRun.setMaxNumberOfReportedCounterExamples(-1);
		rulesMachineRun.start();
		assertEquals(1000, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getCounterExamples().size());
	}

	@Test
	public void testExtractingAllCounterExamplesAndSuccessMessages() {
		File file = createRulesMachineFile(
			"OPERATIONS RULE Rule1 BODY RULE_FORALL x WHERE x : 1..1000 EXPECT x : 1..200 ON_SUCCESS ```${x}``` COUNTEREXAMPLE STRING_FORMAT(\"This is a long counter example message including a unique number to test that the extracted B value will not be truncated: ~w\", x) END END");
		RulesMachineRun rulesMachineRun = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(), file);
		rulesMachineRun.setMaxNumberOfReportedCounterExamples(-1);
		rulesMachineRun.setMaxNumberOfReportedSuccessMessages(-1);
		rulesMachineRun.start();
		assertEquals(800, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getCounterExamples().size());
		assertEquals(200, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getSuccessMessages().size());
	}

	@Test
	public void testExtractingAllCounterExamplesAndSuccessMessagesAndUncheckedMessages() {
		File file = createRulesMachineFile(
				"OPERATIONS RULE Rule1 BODY FOR x IN 1..1000 DO RULE_FORALL y WHERE x > 500 & x=y EXPECT x : 1..600 ON_SUCCESS ```${x|->y}``` UNCHECKED ```${x}``` COUNTEREXAMPLE ```${x} ce $ {y}``` END END END");
		RulesMachineRun rulesMachineRun = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(), file);
		rulesMachineRun.setMaxNumberOfReportedCounterExamples(-1);
		rulesMachineRun.setMaxNumberOfReportedSuccessMessages(-1);
		rulesMachineRun.setMaxNumberOfReportedUncheckedMessages(-1);
		rulesMachineRun.start();
		assertEquals(400, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getCounterExamples().size());
		assertEquals(100, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getSuccessMessages().size());
		assertEquals(500, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getUncheckedMessages().size());
	}

	@Test
	public void testInjectConstants() {
		File file = createRulesMachineFile(
				"CONSTANTS k PROPERTIES k : STRING OPERATIONS RULE Rule1 BODY IF k = \"abc\" THEN RULE_FAIL COUNTEREXAMPLE \"fail\" END END END");
		Map<String, String> constantValuesToBeInjected = new HashMap<>();
		constantValuesToBeInjected.put("k", "abc");
		RulesMachineRun rulesMachineRun = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(), file, null, constantValuesToBeInjected);
		rulesMachineRun.start();
		assertTrue(rulesMachineRun.getRuleResults().getRuleResult("Rule1").hasFailed());
	}

	@Test
	public void testPreferences() {
		File file = createRulesMachineFile(
				"OPERATIONS RULE Rule1 BODY RULE_FAIL x WHEN x : 1..MAXINT COUNTEREXAMPLE STRING_FORMAT(\"~w\", x) END END");
		Map<String, String> prefs = new HashMap<>();
		prefs.put("MAXINT", "12");
		RulesMachineRun rulesMachineRun = new RulesMachineRun(RulesTestUtil.getRulesMachineRunner(), file, prefs, Collections.emptyMap());
		rulesMachineRun.start();
		assertEquals(12, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getCounterExamples().size());
	}

	@Test
	public void testMachineWithFailingRule() {
		// @formatter:off
		RulesMachineRun rulesMachineRun = startRulesMachineRunWithOperations(
				"RULE Rule1 RULEID id1 BODY RULE_FAIL COUNTEREXAMPLE \"foo\" END END",
				"RULE Rule2 DEPENDS_ON_RULE Rule1 BODY RULE_FAIL WHEN 1=2 COUNTEREXAMPLE \"fail\" END END");
		// @formatter:on
		assertFalse(rulesMachineRun.hasError());
		assertTrue(rulesMachineRun.getRuleResults().getRuleResult("Rule1").hasFailed());
		assertEquals(RuleStatus.FAIL, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getRuleState());
		assertEquals(RuleStatus.NOT_CHECKED, rulesMachineRun.getRuleResults().getRuleResult("Rule2").getRuleState());
		assertEquals("Rule1", rulesMachineRun.getRuleResults().getRuleResult("Rule2").getFailedDependencies().get(0));
	}

	@Test
	public void testMachineWithFailingRuleSequence() {
		// @formatter:off
		RulesMachineRun rulesMachineRun = startRulesMachineRunWithOperations("RULE Rule1 ERROR_TYPES 3 BODY "
				+ " RULE_FAIL COUNTEREXAMPLE \"foo1\" END ;" + " RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"foo2\" END ;"
				+ " RULE_FAIL ERROR_TYPE 3 COUNTEREXAMPLE \"foo3\" END " + " END");
		// @formatter:on
		assertTrue(rulesMachineRun.getRuleResults().getRuleResult("Rule1").hasFailed());
		assertEquals(3, rulesMachineRun.getRuleResults().getRuleResult("Rule1").getCounterExamples().size());
	}

	@Test
	public void testReplaces1() {
		RulesMachineRun rulesMachineRun = startRulesMachineRun(DIR.resolve("Replaces.rmch").toFile());
		assertFalse(rulesMachineRun.hasError());
		assertTrue(rulesMachineRun.getErrorList().isEmpty());
		assertNull(rulesMachineRun.getFirstError());

		RuleResults ruleResults = rulesMachineRun.getRuleResults();
		ResultSummary summary = ruleResults.getSummary();
		// summary is created only once
		assertEquals(summary, ruleResults.getSummary());
		assertEquals(1, ruleResults.getRuleResultList().size());

		RuleResult rule1Result = ruleResults.getRuleResult("RULE_Rule1");
		assertEquals(RuleStatus.FAIL, rule1Result.getRuleState());
	}

	@Test
	public void testReplaces2() {
		RulesMachineRun rulesMachineRun = startRulesMachineRunWithOperations(
				" COMPUTATION COMP_comp1 BODY DEFINE V_Value1 TYPE INTEGER DUMMY_VALUE 0 VALUE 10 END END",
				" RULE RULE_BasedOnValue1 BODY RULE_FORALL x WHERE x : 1..V_Value1 EXPECT x <= 10 COUNTEREXAMPLE \"fail\" END END",
				" COMPUTATION COMP_NewComp1 REPLACES COMP_comp1 BODY DEFINE V_Value1 TYPE INTEGER DUMMY_VALUE 0 VALUE 12 END END");
		RuleResult ruleResult = rulesMachineRun.getRuleResults().getRuleResult("RULE_BasedOnValue1");
		assertTrue(ruleResult.hasFailed());
	}

}
