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

	public void explore(Start node){
		node.apply(this);
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







	public Set<String> getPromoted() {
		return promoted;
	}

	public Map<String, HashSet<String>> getUsed() {
		return used;
	}

}
