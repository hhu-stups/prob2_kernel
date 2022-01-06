package de.prob.animator.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.prob.animator.domainobjects.VisBItem;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.State;

public final class GetVisBAttributeValuesCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_visb_attributes_for_state";
	
	private static final String ATTRIBUTES_VARIABLE = "Attributes";
	
	private final State state;
	
	private Map<VisBItem.VisBItemKey, String> values;
	
	public GetVisBAttributeValuesCommand(final State state) {
		this.state = state;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(this.state.getId());
		pto.printVariable(ATTRIBUTES_VARIABLE);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.values = new HashMap<>();
		for (final PrologTerm term : BindingGenerator.getList(bindings, ATTRIBUTES_VARIABLE)) {
			BindingGenerator.getCompoundTerm(term, "set_attr", 3);
			final String id = PrologTerm.atomicString(term.getArgument(1));
			final String attribute = PrologTerm.atomicString(term.getArgument(2));
			final String value = PrologTerm.atomicString(term.getArgument(3));
			this.values.put(new VisBItem.VisBItemKey(id, attribute), value);
		}
	}
	
	public Map<VisBItem.VisBItemKey, String> getValues() {
		return Collections.unmodifiableMap(this.values);
	}
}
