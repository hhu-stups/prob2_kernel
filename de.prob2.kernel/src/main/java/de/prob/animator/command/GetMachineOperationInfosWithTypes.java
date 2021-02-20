package de.prob.animator.command;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.TypedOperationInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
			return PrologTerm.atomicStrings((ListPrologTerm) list);
		} else if ("unknown".equals(PrologTerm.atomicString(list))) {
			return Collections.emptyList();
		} else {
			throw new AssertionError("Not a list or 'unknown': " + list);
		}
	}

	private static OperationInfo operationInfoFromPrologTerm(final PrologTerm prologTerm) {
		final CompoundPrologTerm cpt = BindingGenerator.getCompoundTerm(prologTerm, "operation_info", 9);
		final String opName = PrologTerm.atomicString(cpt.getArgument(1));
		final List<String> outputParameterNames = PrologTerm.atomicStrings(BindingGenerator.getList(cpt.getArgument(2)));
		final List<String> parameterNames = PrologTerm.atomicStrings(BindingGenerator.getList(cpt.getArgument(3)));
		final boolean topLevel = Boolean.parseBoolean(PrologTerm.atomicString(cpt.getArgument(4)));
		final OperationInfo.Type type = OperationInfo.Type.fromProlog(PrologTerm.atomicString(cpt.getArgument(5)));
		final List<String> readVariables = convertPossiblyUnknownAtomicStringList(cpt.getArgument(6));
		final List<String> writtenVariables = convertPossiblyUnknownAtomicStringList(cpt.getArgument(7));
		final List<String> nonDetWrittenVariables = convertPossiblyUnknownAtomicStringList(cpt.getArgument(8));
		final Map<String, String> identifierTypes = convertPrologTermIntoMap(cpt.getArgument(9));
		return new OperationInfo(opName, parameterNames, outputParameterNames, topLevel, type, readVariables, writtenVariables, nonDetWrittenVariables, identifierTypes);
	}

	private static Map<String, String> convertPrologTermIntoMap(PrologTerm argument) {
		Set<String> split = BindingGenerator.getList(argument).stream().map(entry -> entry.getArgument(1)+ "-"+entry.getArgument(2)).collect(Collectors.toSet());
		return split.stream().collect(Collectors.toMap(entry -> entry.substring(0, entry.indexOf("-")), entry -> entry.substring(entry.indexOf("-")+1 )));
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
