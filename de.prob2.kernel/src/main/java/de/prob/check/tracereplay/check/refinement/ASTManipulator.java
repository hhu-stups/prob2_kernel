package de.prob.check.tracereplay.check.refinement;


import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.prob.statespace.Transition;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

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

	/**
	 * Looks wherever a node is missing but needed and adds it for later visiting
	 * @param clauses the nodes to investigate
	 * @return a modified list
	 */
	public List<PMachineClause> dealWithMachineClauses(List<PMachineClause> clauses){
		List<PMachineClause> result = new ArrayList<>(clauses);
		boolean hasInvariant = clauses.stream().anyMatch(entry -> entry instanceof AInvariantMachineClause);
		boolean hasProperties = clauses.stream().anyMatch(entry -> entry instanceof APropertiesMachineClause);
		boolean hasVariables = clauses.stream().anyMatch(entry -> entry instanceof AVariablesMachineClause);
		boolean hasConstants = clauses.stream().anyMatch(entry -> entry instanceof AConstantsMachineClause);
		boolean hasSets = clauses.stream().anyMatch(entry -> entry instanceof ASetsMachineClause);
		boolean hasSees = clauses.stream().anyMatch(entry -> entry instanceof ASeesMachineClause);
		boolean hasExtends = clauses.stream().anyMatch(entry -> entry instanceof AExtendsMachineClause);
		boolean hasUses = clauses.stream().anyMatch(entry -> entry instanceof AUsesMachineClause);
		boolean hasPromotes = clauses.stream().anyMatch(entry -> entry instanceof APromotesMachineClause);
		boolean hasIncludes = clauses.stream().anyMatch(entry -> entry instanceof AIncludesMachineClause);
		boolean hasImports = clauses.stream().anyMatch(entry -> entry instanceof AImportsMachineClause);

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

		if(!hasSees && !nodeCollector.getSeesClause().isEmpty()){
			ASeesMachineClause aSeesMachineClause = new ASeesMachineClause();
			result.add(aSeesMachineClause);
		}


		if(!hasExtends && !nodeCollector.getExtendsClauses().isEmpty()){
			AExtendsMachineClause aExtendsMachineClause = new AExtendsMachineClause();
			result.add(aExtendsMachineClause);
		}


		if(!hasUses && !nodeCollector.getUsesClause().isEmpty()){
			AUsesMachineClause aUsesMachineClause = new AUsesMachineClause();
			result.add(aUsesMachineClause);
		}


		if(!hasPromotes && !nodeCollector.getPromotesClause().isEmpty()){
			APromotesMachineClause aPromotesMachineClause = new APromotesMachineClause();
			result.add(aPromotesMachineClause);
		}

		if(!hasIncludes && !nodeCollector.getIncludesClauses().isEmpty()){
			AIncludesMachineClause aIncludesMachineClause = new AIncludesMachineClause();
			result.add(aIncludesMachineClause);
		}


		if(!hasImports && !nodeCollector.getImportsClauses().isEmpty()){
			AImportsMachineClause aImportsMachineClause = new AImportsMachineClause();
			result.add(aImportsMachineClause);
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

	@Override
	public void caseAIncludesMachineClause(AIncludesMachineClause node)
	{
		if(!nodeCollector.getIncludesClauses().isEmpty()) {
			LinkedList<PMachineReference> newList = compareAndEqualizeMachineReferences_PMachineReference(node.getMachineReferences(), nodeCollector.getIncludesClauses());
			List<PMachineReference> newListCopy = newList.stream().map(PMachineReference::clone).collect(Collectors.toList());
			node.setMachineReferences(newListCopy);

		}

	}

	@Override
	public void caseAExtendsMachineClause(AExtendsMachineClause node)
	{
		if(!nodeCollector.getExtendsClauses().isEmpty()) {
			LinkedList<PMachineReference> newList = compareAndEqualizeMachineReferences_PMachineReference(node.getMachineReferences(), nodeCollector.getExtendsClauses());
			List<PMachineReference> newListCopy = newList.stream().map(PMachineReference::clone).collect(Collectors.toList());
			node.setMachineReferences(newListCopy);
		}
	}

	@Override
	public void caseAImportsMachineClause(AImportsMachineClause node)
	{
		if(!nodeCollector.getImportsClauses().isEmpty()) {
			LinkedList<PMachineReference> newList = compareAndEqualizeMachineReferences_PMachineReference(node.getMachineReferences(), nodeCollector.getImportsClauses());
			List<PMachineReference> newListCopy = newList.stream().map(PMachineReference::clone).collect(Collectors.toList());
			node.setMachineReferences(newListCopy);
		}
	}


	@Override
	public void caseASeesMachineClause(ASeesMachineClause node)
	{
		if(!nodeCollector.getSeesClause().isEmpty()) {
			LinkedList<PMachineReferenceNoParams> expressions = compareAndEqualizeMachineReferences_PMachineReferenceNoParams(node.getMachineNames(), nodeCollector.getSeesClause());
			List<PMachineReferenceNoParams> newListCopy = expressions.stream().map(PMachineReferenceNoParams::clone).collect(Collectors.toList());
			node.setMachineNames(newListCopy);
		}
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node)
	{
		if(!nodeCollector.getUsesClause().isEmpty()) {
			LinkedList<PMachineReferenceNoParams> expressions = compareAndEqualizeMachineReferences_PMachineReferenceNoParams(node.getMachineNames(), nodeCollector.getUsesClause());
			List<PMachineReferenceNoParams> newListCopy = expressions.stream().map(PMachineReferenceNoParams::clone).collect(Collectors.toList());
			node.setMachineNames(newListCopy);
		}
	}

	@Override
	public void caseAPromotesMachineClause(APromotesMachineClause node)
	{
		if(!nodeCollector.getPromotesClause().isEmpty()) {
			LinkedList<POperationReference> expressions = compareAndEqualizeMachineReferences_POperationReference(node.getOperationNames(), nodeCollector.getPromotesClause());
			List<POperationReference> newListCopy = expressions.stream().map(POperationReference::clone).collect(Collectors.toList());
			node.setOperationNames(newListCopy);
		}

	}


	/**
	 * For promotes - removes double entries between two machines
	 * @param left  the entries of the first machine
	 * @param right the entries of the second machine
	 * @return the sorted entries
	 */
	public static LinkedList<POperationReference> compareAndEqualizeMachineReferences_POperationReference(LinkedList<POperationReference> left, LinkedList<POperationReference> right){
		Map<String, POperationReference> mapLeft = left.stream().collect(toMap(Object::toString, entry -> entry));
		Map<String, POperationReference> mapRight = right.stream().collect(toMap(Object::toString, entry -> entry));


		Set<String> missing = mapLeft.keySet().stream().filter(entry -> !mapRight.containsKey(entry)).collect(toSet());

		List<POperationReference> firstResult = new ArrayList<>(mapRight.values());
		List<POperationReference> secondResult = missing.stream().map(mapLeft::get).collect(Collectors.toList());


		LinkedList<POperationReference> result = new LinkedList<>();
		result.addAll(firstResult);
		result.addAll(secondResult);

		return result;
	}




	/**
	 * For includes, extends, imports - removes double entries between two machines does not respect parameter
	 * @param left  the entries of the first machine
	 * @param right the entries of the second machine
	 * @return the sorted entries
	 */
	public static LinkedList<PMachineReference> compareAndEqualizeMachineReferences_PMachineReference(LinkedList<PMachineReference> left, LinkedList<PMachineReference> right){
		Map<Set<String>, PMachineReference> mapLeft = createMap_PMachineReference(left);
		Map<Set<String>, PMachineReference> mapRight = createMap_PMachineReference(right);

		Set<Set<String>> leftSet = mapLeft.keySet().stream().map(HashSet::new).collect(toSet());
		Set<Set<String>> rightSet = mapRight.keySet().stream().map(HashSet::new).collect(toSet());

		Set<Set<String>> missing = leftSet.stream().filter(entry -> !rightSet.contains(entry)).collect(toSet());

		List<PMachineReference> firstResult = new ArrayList<>(mapRight.values());
		List<PMachineReference> secondResult = missing.stream().map(mapLeft::get).collect(Collectors.toList());


		LinkedList<PMachineReference> result = new LinkedList<>();
		result.addAll(firstResult);
		result.addAll(secondResult);

		return result;
	}

	public static Map<Set<String>, PMachineReference> createMap_PMachineReference(LinkedList<PMachineReference> left){
		return left.stream().collect(toMap(entry ->
				{
					if (entry instanceof AMachineReference) {
						return ((AMachineReference) entry).getMachineName().stream().map(Token::getText).collect(toSet());
					} else {
						return Collections.singleton(((AFileMachineReference) entry).getFile().getText());

					}
				}
		, entry -> entry));
	}

	/**
	 * For sees, uses - removes double entries between two machines
	 * @param left  the entries of the first machine
	 * @param right the entries of the second machine
	 * @return the sorted entries
	 */
	public static LinkedList<PMachineReferenceNoParams> compareAndEqualizeMachineReferences_PMachineReferenceNoParams(LinkedList<PMachineReferenceNoParams> left, LinkedList<PMachineReferenceNoParams> right){
		Map<Set<String>, PMachineReferenceNoParams> mapLeft = createMap_PMachineReferenceNoParams(left);
		Map<Set<String>, PMachineReferenceNoParams> mapRight = createMap_PMachineReferenceNoParams(right);

		Set<Set<String>> leftSet = mapLeft.keySet().stream().map(HashSet::new).collect(toSet());
		Set<Set<String>> rightSet = mapRight.keySet().stream().map(HashSet::new).collect(toSet());

		Set<Set<String>> missing = leftSet.stream().filter(entry -> !rightSet.contains(entry)).collect(toSet());

		List<PMachineReferenceNoParams> firstResult = new ArrayList<>(mapRight.values());
		List<PMachineReferenceNoParams> secondResult = missing.stream().map(mapLeft::get).collect(Collectors.toList());


		LinkedList<PMachineReferenceNoParams> result = new LinkedList<>();
		result.addAll(firstResult);
		result.addAll(secondResult);

		return result;
	}

	public static Map<Set<String>, PMachineReferenceNoParams> createMap_PMachineReferenceNoParams(LinkedList<PMachineReferenceNoParams> left){
		return left.stream().collect(toMap(entry ->
				{
					if (entry instanceof AMachineReferenceNoParams) {
						return ((AMachineReferenceNoParams) entry).getMachineName().stream().map(Token::getText).collect(toSet());
					} else {
						return Collections.singleton(((AFileMachineReferenceNoParams) entry).getFile().getText());

					}
				}
		, entry -> entry));
	}

	public Start getStart() {
		return start;
	}




}
