package de.prob.model.classicalb;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.cli.CliTestCommon;
import de.prob.exception.ProBError;
import de.prob.scripting.Api;

import org.junit.Before;
import org.junit.Test;

public class ParseErrorTest {

	private Api api;

	@Before
	public void setup() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test(expected = ProBError.class)
	public void testLoadBMachineWithParseError() throws IOException {
		api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ParseError.mch").toString());
	}

	@Test(expected = IOException.class)
	public void testLoadBMachineButFileDoesNotExists() throws IOException {
		api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "FileDoesNotExists.mch").toString());
	}
}
