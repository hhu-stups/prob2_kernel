package de.prob.animator.domainobjects;

import de.prob.parser.BindingGenerator;
import de.prob.prolog.term.PrologTerm;

/**
 * An instance of this class represents a state based error. Such errors
 * happened in an event starting in a state.
 * 
 * The current implementation is very limited, in future, the error should be
 * easy to examine by the user, including visualization of predicates or
 * expressions.
 * 
 * @author plagge
 */
public class StateError {
	private final String event;
	private final String shortDescription;
	private final String longDescription;
	private final String spanDescription;

	public StateError(String event, String shortDescription, String longDescription, String spanDescription) {
		this.event = event;
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		this.spanDescription = spanDescription;
	}

	public StateError(final String event, final String shortDescription,
			final String longDescription) {
		this(event, shortDescription, longDescription, null);
	}

	public static StateError fromPrologTerm(PrologTerm term) {
		String event = null;
		String shortDescription = null;
		String longDescription = null;
		String spanDescription = null;
		
		BindingGenerator.getCompoundTerm(term, "error", 1);
		for (PrologTerm entry : BindingGenerator.getList(term.getArgument(1))) {
			BindingGenerator.getCompoundTerm(entry, 1);
			final PrologTerm arg = entry.getArgument(1);
			switch (entry.getFunctor()) {
				case "event":
					event = arg.atomToString();
					break;
				
				case "description":
					shortDescription = arg.atomToString();
					break;
				
				case "long_description":
					longDescription = arg.atomToString();
					break;
				
				case "span_description":
					spanDescription = arg.atomToString();
					break;
				
				default:
					// Ignore unknown entries to allow adding more information in the future.
					break;
			}
		}
		
		return new StateError(event, shortDescription, longDescription, spanDescription);
	}

	public String getEvent() {
		// For backwards compatibility, return a special string instead of null...
		return this.event == null ? "*unknown*" : this.event;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public String getSpanDescription() {
		return this.spanDescription;
	}
}
