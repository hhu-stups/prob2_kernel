package de.prob.animator.command;

import de.prob.check.tracereplay.interactive.InteractiveReplayStep;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public final class InteractiveTraceReplayInitCommand extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "prob2_interactive_replay_json_trace_file";

	private static final String STEP_LIST = "StepList";

	private final File file;

	private List<InteractiveReplayStep> replaySteps;

	public InteractiveTraceReplayInitCommand(final File file) {
		this.file = file;
	}
	
	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(this.file.getAbsolutePath());
		pto.printVariable(STEP_LIST);
		pto.closeTerm();
	}
	
	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		this.replaySteps = BindingGenerator.getList(bindings.get(STEP_LIST)).stream()
				.map(term -> BindingGenerator.getCompoundTerm(term, "ireplay_step", 3))
				.map(compoundTerm -> new InteractiveReplayStep(
						((AIntegerPrologTerm) compoundTerm.getArgument(1)).intValueExact(),
						compoundTerm.getArgument(2).getFunctor(),
						PrologTerm.atomicsToStrings(BindingGenerator.getList(compoundTerm.getArgument(3)))
				))
				.collect(Collectors.toList());
	}

	public List<InteractiveReplayStep> getReplaySteps() {
		return replaySteps;
	}

}
