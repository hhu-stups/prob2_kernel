package de.prob.scripting;

import de.prob.cli.CliTestCommon;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class LoadEventBTest {

	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void checkLoadIfProofFilesAreAbsent() throws IOException {
		api.eventb_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "trafficLight",
						"trafficLightWithoutProofs", "mac.bum").toString())
			.kill();
	}

}
