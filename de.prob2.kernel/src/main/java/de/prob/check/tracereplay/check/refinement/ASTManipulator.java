package de.prob.check.tracereplay.check.refinement;


import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.prob.statespace.Transition;

import java.util.*;

public class ASTManipulator extends DepthFirstAdapter{


	private final Start start;
	private final NodeCollector nodeCollector;


	public ASTManipulator(Start start, NodeCollector nodeCollector){
		this.start = start;
		this.nodeCollector = nodeCollector;
		start.apply(this);
	}

	@Override
	public void caseAAbstractMachineParseUnit(AAbstractMachineParseUnit node)
	{
			List<PMachineClause> modifiedList = dealWithMachineClauses(node.getMachineClauses());
			modifiedList.forEach(entry -> entry.apply(this));

			AAbstractMachineParseUnit aAbstractMachineParseUnit = new AAbstractMachineParseUnit();
			aAbstractMachineParseUnit.setMachineClauses(modifiedList);
			aAbstractMachineParseUnit.setVariant(node.getVariant());
			aAbstractMachineParseUnit.setHeader(node.getHeader());

			node.replaceBy(aAbstractMachineParseUnit);

	}

	@Override
	public void caseARefinementMachineParseUnit(ARefinementMachineParseUnit node)
	{
		List<PMachineClause> modifiedList = dealWithMachineClauses(node.getMachineClauses());
		modifiedList.forEach(entry -> entry.apply(this));

		AAbstractMachineParseUnit aAbstractMachineParseUnit = new AAbstractMachineParseUnit();
		aAbstractMachineParseUnit.setMachineClauses(modifiedList);
		aAbstractMachineParseUnit.setHeader(node.getHeader());
		aAbstractMachineParseUnit.setVariant(new AMachineMachineVariant()); //Todo add variant to node collector

		node.replaceBy(aAbstractMachineParseUnit);
	}


	public List<PMachineClause> dealWithMachineClauses(List<PMachineClause> clauses){
		List<PMachineClause> result = new ArrayList<>(clauses);
		boolean hasInvariant = clauses.stream().anyMatch(entry -> entry instanceof AInvariantMachineClause);
		boolean hasProperties = clauses.stream().anyMatch(entry -> entry instanceof APropertiesMachineClause);

		boolean hasVariables = clauses.stream().anyMatch(entry -> entry instanceof AVariablesMachineClause);
		boolean hasConstants = clauses.stream().anyMatch(entry -> entry instanceof AConstantsMachineClause);

		boolean hasSets = clauses.stream().anyMatch(entry -> entry instanceof ASetsMachineClause);


		if(!hasSets && !nodeCollector.getSets().isEmpty()){
			ASetsMachineClause aSetsMachineClause = new ASetsMachineClause();
			result.add(aSetsMachineClause);
		}

		if(!hasInvariant && nodeCollector.getInvariant()!= null){
			AInvariantMachineClause aInvariantMachineClause = new AInvariantMachineClause();
			result.add(aInvariantMachineClause);
		}

		if(!hasProperties && nodeCollector.getProperties()!= null){
			APropertiesMachineClause aPropertiesMachineClause = new APropertiesMachineClause();
			result.add(aPropertiesMachineClause);
		}

		if(!hasVariables && !nodeCollector.getVariables().isEmpty()){
			AVariablesMachineClause aVariablesMachineClause = new AVariablesMachineClause();
			result.add(aVariablesMachineClause);
		}

		if(!hasConstants && !nodeCollector.getConstants().isEmpty()){
			AConstantsMachineClause aConstantsMachineClause = new AConstantsMachineClause();
			result.add(aConstantsMachineClause);
		}



		return result;

	}


	@Override
	public void caseAInvariantMachineClause(AInvariantMachineClause node)
	{
		if(nodeCollector.getInvariant()!=null) {
			AConjunctPredicate aConjunctPredicate = new AConjunctPredicate();
			aConjunctPredicate.setLeft(node.getPredicates());
			aConjunctPredicate.setRight(nodeCollector.getInvariant());
			node.setPredicates(aConjunctPredicate);
		}
	}

	@Override
	public void caseAPropertiesMachineClause(APropertiesMachineClause node)
	{
		if(nodeCollector.getProperties()!=null) {
			AConjunctPredicate aConjunctPredicate = new AConjunctPredicate();
			aConjunctPredicate.setLeft(node.getPredicates());
			aConjunctPredicate.setRight(nodeCollector.getProperties());
			node.setPredicates(aConjunctPredicate);
		}
	}

	@Override
	public void caseAVariablesMachineClause(AVariablesMachineClause node)
	{
		node.getIdentifiers().addAll(nodeCollector.getVariables());
	}

	@Override
	public void caseAConstantsMachineClause(AConstantsMachineClause node)
	{
		node.getIdentifiers().addAll(nodeCollector.getConstants());
	}

	@Override
	public void caseASetsMachineClause(ASetsMachineClause node)
	{
		node.getSetDefinitions().addAll(nodeCollector.getSets());
	}

	@Override
	public void caseAOperation(AOperation node) {
		AParallelSubstitution aParallelSubstitution = new AParallelSubstitution();
		PSubstitution pSubstitution = nodeCollector.getOperationMap().get(node.getOpName().toString());
		aParallelSubstitution.getSubstitutions().addAll(Arrays.asList( node.getOperationBody(), pSubstitution));
		node.setOperationBody(aParallelSubstitution);
	}

	@Override
	public void caseAInitialisationMachineClause(AInitialisationMachineClause node)
	{
		AParallelSubstitution aParallelSubstitution = new AParallelSubstitution();
		PSubstitution pSubstitution = nodeCollector.getOperationMap().get(Transition.INITIALISE_MACHINE_NAME);
		aParallelSubstitution.getSubstitutions().addAll(Arrays.asList(pSubstitution, node.getSubstitutions()));

		node.setSubstitutions(aParallelSubstitution);
	}

	public Start getStart() {
		return start;
	}




}
