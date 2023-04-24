package de.prob.animator.domainobjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The VisBEvent is designed for the JSON / VisB file
 */
public class VisBEvent {
	private final String id;
	private final String event;
	private final List<String> predicates;
	private final List<VisBHover> hovers; // TO DO: maybe provide multiple values and allow value to depend on B state


	/**
	 * These two parameters will be mapped to the id of the corresponding svg element
	 * @param id the id of the svg element, that should be clickable
	 * @param event the event has to be an executable operation of the corresponding B machine
	 * @param predicates the predicates have to be the predicates, which are used for the event above
	 */
	public VisBEvent(String id, String event, List<String> predicates, List<VisBHover> hovers){
		this.id = id;
		this.event = event;
		this.predicates = predicates;
		this.hovers = hovers;
	}

	public String getEvent() {
		return event;
	}

	public List<String> getPredicates() {
		return predicates;
	}

	public String getId() {
		return id;
	}

	public List<VisBHover> getHovers() {
		return hovers;
	}

	public static VisBEvent fromPrologTerm(final PrologTerm term, final Map<String, List<VisBHover>> hoverMap) {
		BindingGenerator.getCompoundTerm(term, "execute_event", 3);
		final String id = term.getArgument(1).atomicToString();
		final String event = term.getArgument(2).atomToString();
		final List<String> predicates = PrologTerm.atomsToStrings(BindingGenerator.getList(term.getArgument(3)));
		final List<VisBHover> hovers = hoverMap.get(id) == null ? new ArrayList<>() : hoverMap.get(id);
		return new VisBEvent(id, event, predicates, hovers);
	}

	@Override
	public String toString(){
		return "ID: "+id+"\nEVENT: "+event+"\nPREDICATES: "+predicates+"\n";
	}
}
