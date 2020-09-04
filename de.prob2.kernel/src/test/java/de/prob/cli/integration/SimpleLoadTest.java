package de.prob.cli.integration;

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

public class SimpleLoadTest {

	private Api api;

	@Before
	public void setupClass() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testLoadTLAFile() throws IOException, ModelTranslationError {
		StateSpace s = api.tla_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "tla", "Foo.tla").toString());
		assertNotNull(s);
		s.kill();
	}

	@Test
	public void testLoadTLAFile2() throws IOException, ModelTranslationError {
		StateSpace s = api.tla_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "tla", "Definitions.tla").toString());
		assertNotNull(s);
		Trace t = new Trace(s);
		assertEquals(1, t.getNextTransitions().size());
		s.kill();
	}

	@Test
	public void testClub() throws IOException, ModelTranslationError {
		StateSpace s = api.tla_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "tla", "ForDistribution", "Club.tla").toString());
		assertNotNull(s);
		Trace t = new Trace(s);
		assertEquals(1, t.getNextTransitions().size());
		s.kill();
	}

	@Test
	public void testLoadBFile() throws IOException, ModelTranslationError {
		StateSpace s = api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "tla", "Foo.mch").toString());
		assertNotNull(s);
		s.kill();
	}

	@Test
	public void testLoadTLAFileChoose() throws IOException, ModelTranslationError {
		StateSpace s = api.tla_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "tla", "Choose.tla").toString());
		assertNotNull(s);
		s.kill();
	}

	@Test
	public void testLoadBRulesFile() throws IOException {
		StateSpace s = api.brules_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "brules", "SimpleRulesMachine.rmch").toString());
		assertNotNull(s);
		s.kill();
	}

}
