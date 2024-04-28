package de.prob.animator.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.prob.animator.domainobjects.DynamicCommandItem;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

public abstract class AbstractDynamicVisualizationCommand<T extends DynamicCommandItem> extends AbstractCommand {

	private final String commandName;
	private final Trace trace;
	private final T item;
	private final File file;
	private final List<IEvalElement> formulas;

	protected AbstractDynamicVisualizationCommand(String commandName, Trace trace, T item, File file, List<IEvalElement> formulas) {
		this.commandName = commandName;
		this.trace = trace;
		this.item = item;
		this.file = file;
		this.formulas = new ArrayList<>(formulas);
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(this.commandName);
		pto.openList();
		// TODO: do we want to ignore the forward history?
		for (Transition t : this.trace.getTransitionList()) {
			pto.printAtomOrNumber(t.getId());
		}
		pto.closeList();
		pto.printAtom(this.item.getCommand());
		pto.openList();
		for (IEvalElement formula : this.formulas) {
			formula.printProlog(pto);
		}
		pto.closeList();
		this.writeExtra(pto);
		pto.closeTerm();
	}

	protected void writeExtra(IPrologTermOutput pto) {
		pto.printAtom(this.file.getAbsolutePath());
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables, the command only creates a file.
	}
}
