package de.prob.check;

import de.prob.animator.command.ReplayStateTraceFileCommand;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TLCModelCheckerTest {
	private static class TLCModelCheckTestListener implements IModelCheckListener {
		StateSpaceStats stats;

		@Override
		public void updateStats(String jobId, long timeElapsed, IModelCheckingResult result, StateSpaceStats stats) {
			this.stats = stats;
		}

		@Override
		public void isFinished(String jobId, long timeElapsed, IModelCheckingResult result, StateSpaceStats stats) {
			this.stats = stats;
		}
	}

	private static final String MACHINE_DIR = "src/test/resources/de/prob/testmachines/b/tlc/";
	private static TLCModelCheckingOptions defaultOptions() {
		return new TLCModelCheckingOptions().saveGeneratedFiles(false);
	}

	private static final String ASSERTION_MSG = "Assertion violation found.";
	private static final String DEADLOCK_MSG = "Deadlock found.";
	private static final String INVARIANT_MSG = "Invariant violation found.";
	private static final String OK_MSG = "Model Checking complete. No error nodes found.";
	private static final String PARTIAL_MSG = " Not all nodes were considered.";
	private static final Map<String, String> PREFERENCES = new HashMap<>();
	static {
		PREFERENCES.put("MAX_INITIALISATIONS", "20");
	}

	private static Api api;
	private static TLCModelCheckTestListener messageListener;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
		messageListener = new TLCModelCheckTestListener();
	}

	private static Stream<Arguments> simpleMachines() {
		return Stream.of(
				// depends on MAX_INITIALISATIONS:
				arguments("useProBConstantsSimpleCompleteSimple",
						MACHINE_DIR + "InvariantViolation", defaultOptions().setupConstantsUsingProB(true).checkInvariantViolations(false),
						ModelCheckOk.class, OK_MSG, 64, 64, 96),
				arguments("useProBConstantsLargeIncomplete",
						MACHINE_DIR + "LargeProperties", defaultOptions().setupConstantsUsingProB(true),
						ModelCheckOk.class, OK_MSG + PARTIAL_MSG, 60, 60, 100), // 20 * 3

				arguments("simpleAssertionViolation",
						MACHINE_DIR + "AssertionViolation", defaultOptions(),
						ModelCheckErrorUncovered.class, ASSERTION_MSG, 7, 7, 9),
				arguments("simpleAssertionViolationWithoutAssertionCheck",
						MACHINE_DIR + "AssertionViolation", defaultOptions().checkAssertions(false),
						ModelCheckOk.class, OK_MSG, 10, 10, 13),

				arguments("simpleDeadlock",
						MACHINE_DIR + "Deadlock", defaultOptions(),
						ModelCheckErrorUncovered.class, DEADLOCK_MSG, 16, 16, 25),
				arguments("simpleDeadlockWithoutDeadlockCheck",
						MACHINE_DIR + "Deadlock", defaultOptions().checkDeadlocks(false),
						ModelCheckOk.class, OK_MSG, 16, 16, 25),

				arguments("simpleInvariantViolation",
						MACHINE_DIR + "InvariantViolation", defaultOptions(),
						ModelCheckErrorUncovered.class, INVARIANT_MSG, 49, 49, 49),
				arguments("simpleInvariantViolationWithoutInvariantCheck",
						MACHINE_DIR + "InvariantViolation", defaultOptions().checkInvariantViolations(false),
						ModelCheckOk.class, OK_MSG, 64, 64, 96) // 4 * 4 * 4
		);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("simpleMachines")
	void modelCheckSimpleMachines(
			String name, String machineName, TLCModelCheckingOptions options,
			Class<? extends IModelCheckingResult> expectedResult, String expectedMessage,
			int expectedTotalNodes, int expectedProcessedNodes, int expectedTotalTransitions
	) throws IOException {
		String machinePath = machineName + ".mch";
		StateSpace stateSpace = api.b_load(machinePath, PREFERENCES);
		TLCModelChecker modelChecker = new TLCModelChecker(machinePath, stateSpace, messageListener, options);
		modelChecker.execute();

		assertInstanceOf(expectedResult, modelChecker.getResult());
		assertEquals(expectedMessage, modelChecker.getResult().getMessage());
		assertEquals(expectedTotalNodes, messageListener.stats.getNrTotalNodes());
		assertEquals(expectedProcessedNodes, messageListener.stats.getNrProcessedNodes());
		assertEquals(expectedTotalTransitions, messageListener.stats.getNrTotalTransitions());

		if (modelChecker.getResults().hasTrace()) {
			ReplayStateTraceFileCommand cmd = new ReplayStateTraceFileCommand(modelChecker.getResults().getTraceFilePath(),
					stateSpace.getRoot().getId());
			stateSpace.execute(cmd);  // counter example replay works
			stateSpace.getState(cmd.getDestStateId()); // output is valid state ID
		}
		stateSpace.kill();
	}

	// TODO: Event-B
}
