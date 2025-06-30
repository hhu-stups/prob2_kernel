package de.prob.model.eventb;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.cli.CliTestCommon;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.ModelElementList;
import de.prob.model.representation.Named;
import de.prob.scripting.Api;
import de.prob.statespace.StateSpace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EventTest {

	@Test
	public void inheritanceRepresentation() throws IOException {
		StateSpace stateSpace = CliTestCommon.getInjector().getInstance(Api.class).eventb_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "abrialTrain",  "train_1.bum").toString());

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
		List<Event> actual = ((EventBMachine) stateSpace.getMainComponent()).getEvents();

		Assertions.assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			Event expectedEvent = expected.get(i);
			Event actualEvent = actual.get(i);
			Assertions.assertEquals(expectedEvent.getName(), actualEvent.getName());
			Assertions.assertEquals(expectedEvent.getType(), actualEvent.getType());
			Assertions.assertEquals(expectedEvent.getInheritance(), actualEvent.getInheritance());
			if (expectedEvent.getParentEvent() == null) {
				Assertions.assertNull(actualEvent.getParentEvent());
			} else {
				Assertions.assertNotNull(actualEvent.getParentEvent());
				Assertions.assertEquals(expectedEvent.getParentEvent().getName(), actualEvent.getParentEvent().getName());
			}
		}

		stateSpace.kill();
	}

	@Test
	@Disabled("bcm files lose the param order given in the bum")
	public void testParamOrder() throws IOException {
		StateSpace stateSpace = CliTestCommon.getInjector().getInstance(Api.class).eventb_load(Paths.get("src", "test", "resources", "de", "prob", "testmachines", "eventB", "paramOrder",  "M.bum").toString());
		EventBModel m = (EventBModel) stateSpace.getModel();
		Event event = m.getEventList().getElement("E");
		Assertions.assertEquals(Arrays.asList("dist_x", "dist_y", "dist_z"), event.getParameters().stream().map(Named::getName).collect(Collectors.toList()));
		stateSpace.kill();
	}
}
