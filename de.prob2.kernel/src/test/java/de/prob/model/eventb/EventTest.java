package de.prob.model.eventb;

import de.prob.ProBEventBKernelStub;
import de.prob.cli.CliTestCommon;
import de.prob.statespace.StateSpace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventTest {

	@Test
	public void inheritanceRepresentation() throws IOException {
		ProBEventBKernelStub proBEventBKernelStub =  CliTestCommon.getInjector().getInstance(ProBEventBKernelStub.class);

		StateSpace stateSpace = proBEventBKernelStub.createStateSpace(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "abrialTrain",  "train_1.bum"));

		Event ini = new Event("INITIALISATION", Event.EventType.ORDINARY, Event.Inheritance.REFINES).withParentEvent(new Event("INITIALISATION", Event.EventType.ORDINARY, Event.Inheritance.NONE));

		Event res = new Event("route_reservation", Event.EventType.ORDINARY, Event.Inheritance.EXTENDS).withParentEvent(new Event("route_reservation", Event.EventType.ORDINARY, Event.Inheritance.NONE));

		Event free = new Event("route_freeing", Event.EventType.ORDINARY, Event.Inheritance.EXTENDS).withParentEvent(new Event("route_freeing", Event.EventType.ORDINARY, Event.Inheritance.NONE));

		Event f1 = new Event("FRONT_MOVE_1", Event.EventType.ORDINARY, Event.Inheritance.REFINES).withParentEvent(new Event("FRONT_MOVE_1", Event.EventType.ORDINARY, Event.Inheritance.NONE));

		Event f2 = new Event("FRONT_MOVE_2", Event.EventType.ORDINARY, Event.Inheritance.REFINES).withParentEvent(new Event("FRONT_MOVE_2", Event.EventType.ORDINARY, Event.Inheritance.NONE));

		Event b1 = new Event("BACK_MOVE_1", Event.EventType.ORDINARY, Event.Inheritance.REFINES).withParentEvent(new Event("BACK_MOVE", Event.EventType.ORDINARY, Event.Inheritance.NONE));

		Event b2 = new Event("BACK_MOVE_2", Event.EventType.ORDINARY, Event.Inheritance.REFINES).withParentEvent(new Event("BACK_MOVE", Event.EventType.ORDINARY, Event.Inheritance.NONE));

		Event pp = new Event("point_positionning", Event.EventType.ORDINARY, Event.Inheritance.NONE);

		Event rf = new Event("route_formation", Event.EventType.ORDINARY, Event.Inheritance.NONE);


		List<Event> expected = new ArrayList<>(Arrays.asList(ini, res,free,f1,f2,b1,b2,pp,rf));

		//Otherwise the events have substitutions which will lead to failing comparisons
		List<Event> prepedResults = ((EventBMachine) stateSpace.getMainComponent()).getEvents().stream().map(Event::stripBody).collect(Collectors.toList());

		Assertions.assertEquals(expected, prepedResults);


	}

}
