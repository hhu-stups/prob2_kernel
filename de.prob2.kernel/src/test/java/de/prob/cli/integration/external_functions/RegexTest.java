package de.prob.cli.integration.external_functions;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.cli.CliTestCommon;
import de.prob.scripting.Api;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Trace;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RegexTest {

	private Api api;

	@Before
	public void setupClass() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testRegex() throws IOException, ModelTranslationError {
		StateSpace s = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "external_functions", "Regex.mch").toString());
		assertNotNull(s);
		Trace t = new Trace(s);
		assertEquals(1, t.getNextTransitions().size());
		s.kill();
	}


}
