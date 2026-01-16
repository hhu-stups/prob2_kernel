package de.prob.animator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

public class GetRightClickOptionsForStateVisualizationCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "get_react_to_item_right_click_options_for_state";
	private static final String OPTIONS = "Options";
	private final String stateId;
	private final int row;
	private final int column;

	private List<Option> options = new ArrayList<>();

	public GetRightClickOptionsForStateVisualizationCommand(String stateId, int row, int column) {
		this.stateId = stateId;
		this.row = row;
		this.column = column;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtomOrNumber(stateId);
		pto.printNumber(row);
		pto.printNumber(column);
		pto.printVariable(OPTIONS);
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		ListPrologTerm optionsWithDesc = BindingGenerator.getList(bindings, OPTIONS);
		this.options = optionsWithDesc.stream()
				.map(Option::fromPrologTerm)
				.collect(Collectors.toList());
	}

	public List<String> getOptions() {
		return Collections.unmodifiableList(options.stream().map(Option::getTransitionTerm).collect(Collectors.toList()));
	}

	public List<Option> getOptionsWithDescription() {
		return Collections.unmodifiableList(options);
	}

	public static class Option {
		private final String transitionTerm, description;

		public Option(final String transitionTerm, final String description) {
			this.transitionTerm = transitionTerm;
			this.description = description;
		}

		public static Option fromPrologTerm(PrologTerm term) {
			CompoundPrologTerm compound = BindingGenerator.getCompoundTerm(term, "option", 2);
			return new Option(compound.getArgument(1).atomToString(), compound.getArgument(2).atomToString());
		}

		public String getTransitionTerm() {
			return this.transitionTerm;
		}

		public String getDescription() {
			return this.description;
		}
	}

}
