package de.prob.model.classicalb;

import java.io.IOException;
import java.nio.file.Paths;

import de.prob.cli.CliTestCommon;
import de.prob.exception.ProBError;
import de.prob.scripting.Api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ParseErrorTest {

	private Api api;

	@BeforeEach
	public void setup() {
		api = CliTestCommon.getInjector().getInstance(Api.class);
	}

	@Test
	public void testLoadBMachineWithParseError() {
		Assertions.assertThrows(ProBError.class,
			() -> api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "ParseError.mch").toString()));
	}

	@Test
	public void testLoadBMachineButFileDoesNotExists() {
		Assertions.assertThrows(IOException.class,
			() -> api.b_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "b", "FileDoesNotExists.mch").toString()));
	}
}
