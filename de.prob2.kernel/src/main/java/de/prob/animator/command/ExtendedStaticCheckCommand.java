package de.prob.animator.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.prob.animator.domainobjects.ErrorItem;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public final class ExtendedStaticCheckCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_extended_static_check";
	
	private static final String PROBLEMS_VAR = "Problems";
	
	private List<ErrorItem> problems;
	
	public ExtendedStaticCheckCommand() {}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(PROBLEMS_VAR);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.problems = BindingGenerator.getList(bindings, PROBLEMS_VAR).stream()
			.map(ErrorItem::fromProlog)
			.collect(Collectors.toList());
	}
	
	public List<ErrorItem> getProblems() {
		return Collections.unmodifiableList(this.problems);
	}
}
