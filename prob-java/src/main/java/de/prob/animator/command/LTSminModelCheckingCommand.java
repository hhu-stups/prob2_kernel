/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 */

package de.prob.animator.command;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.prob.check.IModelCheckingResult;
import de.prob.check.ModelCheckErrorUncovered;
import de.prob.check.ModelCheckOk;
import de.prob.check.LTSminModelCheckingOptions;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Transition;

public class LTSminModelCheckingCommand extends AbstractCommand implements IStateSpaceModifier {

	private static final String PROLOG_COMMAND_NAME = "do_ltsmin_modelchecking";
	private static final String RESULT_VARIABLE = "Result";

	private final LTSminModelCheckingOptions options;
	private IModelCheckingResult result;

	public LTSminModelCheckingCommand(LTSminModelCheckingOptions options) {
		this.options = Objects.requireNonNull(options);
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		this.result = this.extractResult(bindings.get(RESULT_VARIABLE));
	}

	private IModelCheckingResult extractResult(PrologTerm t) {
		CompoundPrologTerm result = BindingGenerator.getCompoundTerm(t, t.getArity());
		switch (result.getFunctor()) {
			case "counter_example_found":
				return new ModelCheckErrorUncovered("counter example found", result.getArgument(1).atomicToString());
			case "no_counter_example_found":
				return new ModelCheckOk("no counter example found");
			default:
				throw new IllegalArgumentException("model checking result unknown: " + result);
		}
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.options.backend().getPrologName());
		pto.openList();
		for (LTSminModelCheckingOptions.Option o : this.options.getPrologOptions()) {
			pto.printAtom(o.getPrologName());
		}
		pto.closeList();
		pto.printVariable(RESULT_VARIABLE);
		pto.closeTerm();
	}

	@Override
	public List<Transition> getNewTransitions() {
		// This method has to be implemented to indicate that the state space may have expanded by executing this command.
		// However, we don't know which transitions have been added exactly, so we can only return an empty list here.
		// TODO Is there a better way to handle this than just returning an empty list?
		return Collections.emptyList();
	}

	public IModelCheckingResult getResult() {
		return this.result;
	}
}
