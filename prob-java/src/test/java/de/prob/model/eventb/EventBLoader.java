package de.prob.model.eventb;

import com.google.inject.Injector;
import de.prob.cli.CliTestCommon;
import de.prob.scripting.EventBFactory;
import de.prob.scripting.ExtractedModel;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class EventBLoader {

	public static EventBModel loadResource(String path) throws IOException {
		Path file;
		try {
			URL resource = EventBLoader.class.getResource("/de/prob/testmachines/eventB/" + path);
			file = Paths.get(Objects.requireNonNull(resource, "resource").toURI());
		} catch (Exception e) {
			throw new IOException("could not locate resource", e);
		}

		return loadPath(file);
	}

	public static EventBModel loadPath(Path file) throws IOException {
		Injector injector = CliTestCommon.getInjector();
		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		ExtractedModel<EventBModel> extractedModel = eventBFactory.extract(file.toString());
		extractedModel.load();
		return extractedModel.getModel();
	}
}
