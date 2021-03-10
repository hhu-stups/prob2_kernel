package de.prob.animator.command;

import de.prob.animator.domainobjects.VisBEvent;
import de.prob.animator.domainobjects.VisBHover;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadVisBEventsHoversCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_click_events_and_hovers";
	private static final String EVENTS = "Events";
	private static final String HOVERS = "Hovers";
	private List<VisBEvent> events;

	public ReadVisBEventsHoversCommand() {
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(EVENTS);
		pto.printVariable(HOVERS);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		Map<String, VisBHover> hoverMap = new HashMap<>();
		List<VisBHover> hovers = BindingGenerator.getList(bindings, HOVERS).stream()
				.map(VisBHover::fromPrologTerm)
				.collect(Collectors.toList());
		for(VisBHover hover : hovers) {
			hoverMap.put(hover.getHoverId(), hover);
		}

		this.events = BindingGenerator.getList(bindings, EVENTS).stream()
				.map(term -> VisBEvent.fromPrologTerm(term, hoverMap))
				.collect(Collectors.toList());
	}

	public List<VisBEvent> getEvents() {
		return events;
	}
}
