package de.prob.check.tracereplay.check.refinement;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.inject.Injector;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.json.TraceManager;
import de.prob.cli.CliTestCommon;
import de.prob.model.eventb.EventBModel;
import de.prob.scripting.EventBFactory;
import de.prob.scripting.ExtractedModel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TraceConnectorTest {

	@Test
	public void traceConnectorTest1() throws IOException {
		Injector injector = CliTestCommon.getInjector();
		TraceManager traceManager = injector.getInstance(TraceManager.class);
		List<PersistentTransition> traceOld = traceManager.load(new File("src/test/resources/de/prob/testmachines/eventB/trafficLight/trace1.prob2trace").toPath()).getTransitionList();
		List<PersistentTransition> traceNew = traceManager.load(new File("src/test/resources/de/prob/testmachines/eventB/trafficLight/trace2.prob2trace").toPath()).getTransitionList();

		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		ExtractedModel<EventBModel> extractedModel = eventBFactory.extract(new File("src/test/resources/de/prob/testmachines/eventB/trafficLight/mac1.bum").toPath().toString());
		extractedModel.load();
		EventBModel eventBModel = extractedModel.getModel();

		TraceConnector traceConnector = new TraceConnector(traceOld, traceNew, eventBModel.introducedBySkip());

		List<TraceConnector.Pair<PersistentTransition, PersistentTransition>> result = traceConnector.connect();

		Assertions.assertEquals(new TraceConnector.SkipTransition(), result.get(1).getFirst());
		Assertions.assertEquals(10, result.size());
	}

	@Test
	public void traceConnectorTest2() throws IOException {
		Injector injector = CliTestCommon.getInjector();
		TraceManager traceManager = injector.getInstance(TraceManager.class);
		List<PersistentTransition> traceOld = traceManager.load(new File("src/test/resources/de/prob/testmachines/eventB/trafficLight/trace3.prob2trace").toPath()).getTransitionList();
		List<PersistentTransition> traceNew = traceManager.load(new File("src/test/resources/de/prob/testmachines/eventB/trafficLight/trace4.prob2trace").toPath()).getTransitionList();

		EventBFactory eventBFactory = injector.getInstance(EventBFactory.class);
		ExtractedModel<EventBModel> extractedModel = eventBFactory.extract(new File("src/test/resources/de/prob/testmachines/eventB/trafficLight/mac1.bum").toPath().toString());
		extractedModel.load();
		EventBModel eventBModel = extractedModel.getModel();

		TraceConnector traceConnector = new TraceConnector(traceOld, traceNew, eventBModel.introducedBySkip());

		List<TraceConnector.Pair<PersistentTransition, PersistentTransition>> result = traceConnector.connect();

		Assertions.assertEquals(5, result.size());
	}

}
