package de.prob.animator.command;

import com.google.common.io.MoreFiles;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.Trace;
import de.prob.statespace.Transition;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Export a trace created by the ProB sequent prover.
 * <p>
 * Valid file extensions: HTML, BPR, all supported Dot extensions
 */
public class ExportSequentProverProofCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "export_proof";

	private final Path path;
	private final List<String> stateIds;
	private final List<String> transIds;

	public ExportSequentProverProofCommand(final Path path, final List<String> stateIds, final List<String> transIds) {
		this.path = path;
		this.stateIds = stateIds;
		this.transIds = transIds;
	}

	public ExportSequentProverProofCommand(final Path path, final Trace trace) {
		this(path, trace.getTransitionList());
	}

	public ExportSequentProverProofCommand(final Path path, final List<Transition> transitions) {
		this(path, getStateIdList(transitions),
				transitions.stream().map(Transition::getId).collect(Collectors.toList()));
	}

	private static List<String> getStateIdList(List<Transition> transitions) {
		List<String> stateIds = transitions.stream()
				.map(e -> e.getSource().getId())
				.collect(Collectors.toList());
		stateIds.add(transitions.get(transitions.size() - 1).getDestination().getId());
		return stateIds;
	}

	@Override
	public void writeCommand(IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printAtom(MoreFiles.getFileExtension(path).toLowerCase());
		pto.printAtom(path.toAbsolutePath().toString());
		pto.openList();
		this.stateIds.forEach(pto::printAtomOrNumber);
		pto.closeList();
		pto.openList();
		this.transIds.forEach(pto::printAtomOrNumber);
		pto.closeList();
		pto.closeTerm();
	}

	@Override
	public void processResult(ISimplifiedROMap<String, PrologTerm> bindings) {
		// There are no output variables.
	}
}
