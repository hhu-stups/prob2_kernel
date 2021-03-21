package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.prob.statespace.Transition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class NodeCollector extends DepthFirstAdapter{

	private PPredicate invariant;
	private PPredicate properties;
	private final LinkedList<PExpression> variables;
	private final LinkedList<PExpression> constants;
	private final LinkedList<PSet> sets;
	private final Map<String, PSubstitution> operationMap;



	public NodeCollector(Start start){
		variables = new LinkedList<>();
		constants = new LinkedList<>();
		sets = new LinkedList<>();
		operationMap = new HashMap<>();
		start.apply(this);
	}

	@Override
	public void caseAInitialisationMachineClause(AInitialisationMachineClause node)
	{
		operationMap.put(Transition.INITIALISE_MACHINE_NAME, node.getSubstitutions());
	}

	@Override
	public void caseAInvariantMachineClause(AInvariantMachineClause node)
	{
		invariant = node.getPredicates();
	}

	@Override
	public void caseAPropertiesMachineClause(APropertiesMachineClause node)
	{
		properties = node.getPredicates();
	}

	@Override
	public void caseAOperation(AOperation node) {
		operationMap.put(node.getOpName().toString(), node.getOperationBody());
	}

	@Override
	public void caseAVariablesMachineClause(AVariablesMachineClause node)
	{
		variables.addAll(node.getIdentifiers());
	}

	@Override
	public void caseAConstantsMachineClause(AConstantsMachineClause node)
	{
		constants.addAll(node.getIdentifiers());
	}

	@Override
	public void caseASetsMachineClause(ASetsMachineClause node)
	{
		sets.addAll(node.getSetDefinitions());
	}

	public PPredicate getInvariant() {
		return invariant;
	}

	public PPredicate getProperties() {
		return properties;
	}

	public Map<String, PSubstitution> getOperationMap() {
		return operationMap;
	}

	public LinkedList<PExpression> getVariables() {
		return variables;
	}

	public LinkedList<PExpression> getConstants() {
		return constants;
	}

	public LinkedList<PSet> getSets() {
		return sets;
	}
}
