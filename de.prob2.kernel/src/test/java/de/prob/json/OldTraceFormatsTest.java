package de.prob.json;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.prob.check.tracereplay.json.TraceManager;
import de.prob.check.tracereplay.json.storage.TraceJsonFile;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class OldTraceFormatsTest {
	private static StateSpace stateSpace;
	private static TraceManager traceManager;
	
	@BeforeAll
	public static void beforeAll() throws IOException {
		stateSpace = CliTestCommon.getInjector().getInstance(Api.class).b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "Lift.mch").toString());
		traceManager = CliTestCommon.getInjector().getInstance(TraceManager.class);
	}
	
	@AfterAll
	public static void afterAll() {
		if (stateSpace != null) {
			stateSpace.kill();
		}
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
		"v0-prob2-ui-prerelease.prob2trace",
		"v0-prob2-ui.prob2trace",
		"v1-prob2-ui.prob2trace",
	})
	public void loadOldTraceFileTest(final String traceFileName) throws IOException {
		final Path path = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "Lift", "oldFormats", traceFileName);
		final TraceJsonFile jsonFile = traceManager.load(path);
		Assertions.assertNotNull(jsonFile);
		Assertions.assertFalse(jsonFile.getTransitionList().isEmpty());
		// TODO Check that it replays correctly
	}
}
