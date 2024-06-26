package de.prob.animator.command;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.OperationInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GetMachineOperationInfosWithTypes extends AbstractCommand {
	private static final String PROLOG_COMMAND_NAME = "get_machine_operation_infos_typed";
	private static final String RESULT_VARIABLE = "MachineOperationInfosWithTypes";

	private final List<OperationInfo> operationInfos = new ArrayList<>();

	public GetMachineOperationInfosWithTypes() {
		super();
	}

	private static List<String> convertPossiblyUnknownAtomicStringList(final PrologTerm list) {
		if (list instanceof ListPrologTerm) {
			return PrologTerm.atomsToStrings((ListPrologTerm) list);
		} else if (list.hasFunctor("unknown")) {
			return Collections.emptyList();
		} else {
			throw new AssertionError("Not a list or 'unknown': " + list);
		}
	}

	private static OperationInfo operationInfoFromPrologTerm(final PrologTerm prologTerm) {
		final CompoundPrologTerm cpt = BindingGenerator.getCompoundTerm(prologTerm, "operation_info", 9);
		final String opName = cpt.getArgument(1).atomToString();
		final List<String> outputParameterNames = PrologTerm.atomsToStrings(BindingGenerator.getList(cpt.getArgument(2)));
		final List<String> parameterNames = PrologTerm.atomsToStrings(BindingGenerator.getList(cpt.getArgument(3)));
		final boolean topLevel = Boolean.parseBoolean(cpt.getArgument(4).atomToString());
		final OperationInfo.Type type = OperationInfo.Type.fromProlog(cpt.getArgument(5).atomToString());
		final List<String> readVariables = convertPossiblyUnknownAtomicStringList(cpt.getArgument(6));
		final List<String> writtenVariables = convertPossiblyUnknownAtomicStringList(cpt.getArgument(7));
		final List<String> nonDetWrittenVariables = convertPossiblyUnknownAtomicStringList(cpt.getArgument(8));
		final Map<String, String> identifierTypes = convertPrologTermIntoMap(cpt.getArgument(9));
		return new OperationInfo(opName, parameterNames, outputParameterNames, topLevel, type, readVariables, writtenVariables, nonDetWrittenVariables, identifierTypes);
	}

	private static Map<String, String> convertPrologTermIntoMap(PrologTerm argument) {
		return BindingGenerator.getList(argument).stream()
				.map(entry -> BindingGenerator.getCompoundTerm(entry, "-", 2))
				.collect(Collectors.toMap(
						entry -> entry.getArgument(1).toString(),
						entry -> entry.getArgument(2).toString(),
						(oldVal, newVal) -> {
							if (!Objects.equals(oldVal, newVal)) {
								throw new AssertionError(oldVal + " != " + newVal);
							}
							return newVal;
						}
				));
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		BindingGenerator.getList(bindings, RESULT_VARIABLE).stream()
				.map(GetMachineOperationInfosWithTypes::operationInfoFromPrologTerm)
				.collect(Collectors.toCollection(() -> this.operationInfos));
	}

	public List<OperationInfo> getOperationInfos() {
		return new ArrayList<>(this.operationInfos);
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME).printVariable(RESULT_VARIABLE).closeTerm();
	}
}
