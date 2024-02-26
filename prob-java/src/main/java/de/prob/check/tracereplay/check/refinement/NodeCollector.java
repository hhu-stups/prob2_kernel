package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.prob.statespace.Transition;

import java.util.*;

public final class NodeCollector extends DepthFirstAdapter{

	private PPredicate invariant;
	private PPredicate properties;
	private final LinkedList<PExpression> variables;
	private final LinkedList<PExpression> constants;
	private final LinkedList<PSet> sets;
	private final Map<String, PSubstitution> operationMap;

	private final LinkedList<PMachineReference> includesClauses;
	private final LinkedList<PMachineReference> extendsClauses;
	private final LinkedList<PMachineReference> importsClauses;
	private final LinkedList<PMachineReferenceNoParams> seesClause;
	private final LinkedList<POperationReference> promotesClause;
	private final LinkedList<PMachineReferenceNoParams> usesClause;



	public NodeCollector(Start start){
		variables = new LinkedList<>();
		constants = new LinkedList<>();
		sets = new LinkedList<>();
		operationMap = new HashMap<>();
		includesClauses = new LinkedList<>();
		extendsClauses = new LinkedList<>();
		importsClauses = new LinkedList<>();
		seesClause = new LinkedList<>();
		promotesClause = new LinkedList<>();
		usesClause = new LinkedList<>();
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

	@Override
	public void caseAIncludesMachineClause(AIncludesMachineClause node)
	{
		includesClauses.addAll(node.getMachineReferences());
	}

	@Override
	public void caseAExtendsMachineClause(AExtendsMachineClause node)
	{
		extendsClauses.addAll(node.getMachineReferences());
	}

	@Override
	public void caseAImportsMachineClause(AImportsMachineClause node)
	{
		importsClauses.addAll(node.getMachineReferences());
	}


	@Override
	public void caseASeesMachineClause(ASeesMachineClause node)
	{
		seesClause.addAll(node.getMachineNames());
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node)
	{
		usesClause.addAll(node.getMachineNames());
	}

	@Override
	public void caseAPromotesMachineClause(APromotesMachineClause node)
	{
		promotesClause.addAll(node.getOperationNames());
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

	public LinkedList<PMachineReference> getIncludesClauses() {
		return includesClauses;
	}

	public LinkedList<PMachineReference> getExtendsClauses() {
		return extendsClauses;
	}

	public LinkedList<PMachineReference> getImportsClauses() {
		return importsClauses;
	}

	public LinkedList<PMachineReferenceNoParams> getSeesClause() {
		return seesClause;
	}

	public LinkedList<POperationReference> getPromotesClause() {
		return promotesClause;
	}

	public LinkedList<PMachineReferenceNoParams> getUsesClause() {
		return usesClause;
	}
}
