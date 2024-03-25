package de.prob.model.eventb.translate;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import de.prob.model.eventb.EventBLoader;
import de.prob.model.eventb.EventBModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelToXMLTest {

	Path dir;

	@BeforeEach
	void setup() throws IOException {
		dir = Files.createTempDirectory(this.getClass().getSimpleName()).toRealPath();
	}

	@AfterEach
	void cleanup() throws IOException {
		MoreFiles.deleteRecursively(dir, RecursiveDeleteOption.ALLOW_INSECURE);
	}

	@Test
	void testThatItWorks() throws IOException {
		EventBModel model = EventBLoader.loadResource("trafficLight/mac.bum");
		new ModelToXML().writeToRodin(model, "trafficLight", dir.toString());
		assertTrue(Files.isRegularFile(dir.resolve("trafficLight/mac.bum")));
	}
}
