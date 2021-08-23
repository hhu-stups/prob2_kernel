package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperationsFinder extends DepthFirstAdapter {

	private Set<String> promoted = new HashSet<>();
	private Map<String, HashSet<String>> used = new HashMap<>();
	private AOperation currentOperation;
	private final String sourceMachine;
	private final Start node;
	private boolean extendsSourceMachine = false;

	public OperationsFinder(String sourceMachine, Start node){
		this.sourceMachine = sourceMachine;
		this.node = node;
	}

	public void explore(){
		node.apply(this);
	}


	public Map<String, Set<String>> usedOperationsReversed(){
		return used.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()).stream().collect(Collectors.toMap(entry -> entry, entry ->
			 used.entrySet().stream().filter(innerEntry -> innerEntry.getValue().contains(entry))
					 .map(Map.Entry::getKey).collect(Collectors.toSet())
		));
	}

	@Override
	public void caseAPromotesMachineClause(APromotesMachineClause node)
	{
		promoted = node.getOperationNames().stream().map(Object::toString)
				.map(String::trim)
				.collect(Collectors.toSet());
	}


	@Override
	public void caseAOperation(AOperation node)
	{
		currentOperation = node;
		if(node.getOperationBody() != null)
		{
			node.getOperationBody().apply(this);
		}
	}

	@Override
	public void caseAOpSubstitution(AOpSubstitution node)
	{
		String function = node.getName().toString().trim();
		if(used.containsKey(function)){
			used.get(function).add(currentOperation.getOpName().getFirst().getText());
		}
		else{
			used.put(currentOperation.getOpName().getFirst().getText(), new HashSet<>(Stream.of(function).collect(Collectors.toSet())));
		}
	}

	@Override
	public void caseAExtendsMachineClause(AExtendsMachineClause node)
	{

		List<PMachineReference> copy = new ArrayList<>(node.getMachineReferences());
		for(PMachineReference reference : copy){
			reference.apply(this);
		}

	}

	@Override
	public void caseAMachineReference(AMachineReference node)
	{
		if(node.parent() instanceof AExtendsMachineClause){
			boolean sourceMachineExtended = node.getMachineName().stream().anyMatch(entry -> entry.toString().trim().equals(sourceMachine));
			extendsSourceMachine = sourceMachineExtended || extendsSourceMachine;
		}

	}


	public Set<String> getPromoted() {
		return promoted;
	}

	public Map<String, HashSet<String>> getUsed() {
		return used;
	}

	public boolean isExtendsSourceMachine() {
		return extendsSourceMachine;
	}

}
