package de.prob.check.tracereplay.check.refinement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.*;
import de.prob.statespace.Transition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ASTManipulatorTest {

	@Test
	public void test_correctConstruction_1() throws BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLightRef.ref");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile());

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.mch");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile());

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);



		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile());

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile());


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());

		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.INVARIANT), astCheckerCombined.childCounter.get(Types.INVARIANT));
		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.VARIABLES), astCheckerCombined.childCounter.get(Types.VARIABLES));
		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}


	@Test
	public void test_correctConstruction_2() throws BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",   "EmptyMachine.mch");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile());

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces",   "ContainsClauses.mch");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile());

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);



		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile());

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile());


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());

		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.INVARIANT), astCheckerCombined.childCounter.get(Types.INVARIANT));
		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.VARIABLES), astCheckerCombined.childCounter.get(Types.VARIABLES));
		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.SETS), astCheckerCombined.childCounter.get(Types.SETS));
		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.CONSTANTS), astCheckerCombined.childCounter.get(Types.CONSTANTS));
		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.PROPERTIES), astCheckerCombined.childCounter.get(Types.PROPERTIES));
		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}

	@Test
	public void test_correctConstruction_3() throws BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.mch");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile());

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLightRef.ref");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile());

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);



		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile());

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile());


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());

		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.INVARIANT), astCheckerCombined.childCounter.get(Types.INVARIANT));
		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.VARIABLES), astCheckerCombined.childCounter.get(Types.VARIABLES));
		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}


	@Test
	public void test_correctConstruction_4() throws BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withIncludes",  "Abstract.mch");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile());

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withIncludes", "RefRef.ref");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile());

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);

		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile());

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile());


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());

		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.INCLUDES), astCheckerCombined.childCounter.get(Types.INCLUDES));

		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}

	@Test
	public void test_correctConstruction_5() throws BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withSees",  "Abstract.mch");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile());

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withSees", "RefRef.ref");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile());

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);


		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile());

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile());


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());


		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.SEES), astCheckerCombined.childCounter.get(Types.SEES));

		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}


	@Test
	public void test_correctConstruction_6() throws BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withSeesSameMachine",  "Abstract.mch");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile());

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withSeesSameMachine", "RefRef.ref");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile());

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);

		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile());

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile());


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());

		Assertions.assertEquals(1, astCheckerCombined.childCounter.get(Types.SEES));

		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}

	@Test
	public void test_correctConstruction_7() throws BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withSameIncludes",  "Abstract.mch");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile());

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements", "withSameIncludes", "RefRef.ref");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile());

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);


		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile());

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile());


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());



			Assertions.assertEquals(1, astCheckerCombined.childCounter.get(Types.INCLUDES));
		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}




	int getCombined(ASTChecker a, ASTChecker b, String name){
		int scoreA = 0;
		int scoreB = 0;
		if(a.operationChildCounter.containsKey(name)){
			scoreA = a.operationChildCounter.get(name);
		}
		if(b.operationChildCounter.containsKey(name)){
			scoreB = b.operationChildCounter.get(name);
		}
		return scoreA+scoreB;	}

	int getCombined(ASTChecker a, ASTChecker b, Types type){

		int scoreA = 0;
		int scoreB = 0;
		if(a.childCounter.containsKey(type)){
			scoreA = a.childCounter.get(type);
		}
		if(b.childCounter.containsKey(type)){
			scoreB = b.childCounter.get(type);
		}
		return scoreA+scoreB;
	}

	static class ASTChecker extends DepthFirstAdapter{

		public void check(Start start){
			start.apply(this);
		}


		public final Map<Types, Integer> childCounter = new HashMap<>();
		public final Map<String, Integer> operationChildCounter = new HashMap<>();

		@Override
		public void caseAInvariantMachineClause(AInvariantMachineClause node)
		{
			if(node.getPredicates() instanceof AConjunctPredicate){
				childCounter.put(Types.INVARIANT, unfoldConjunction((AConjunctPredicate) node.getPredicates()).size());

			}else{
				childCounter.put(Types.INVARIANT, 1);
			}
		}


		public List<PPredicate> unfoldConjunction(AConjunctPredicate predicate){
			List<PPredicate> result = new ArrayList<>();
			if(predicate.getLeft() != null && predicate.getLeft() instanceof AConjunctPredicate)
			{
				result.addAll(unfoldConjunction((AConjunctPredicate) predicate.getLeft()));
			}

			if(predicate.getLeft() !=null && !(predicate.getLeft() instanceof AConjunctPredicate)) {
				result.add(predicate.getLeft());
			}

			if(predicate.getRight() != null && predicate.getRight() instanceof AConjunctPredicate)
			{
				result.addAll(unfoldConjunction((AConjunctPredicate) predicate.getRight()));
			}

			if(predicate.getRight() !=null && !(predicate.getRight() instanceof AConjunctPredicate)) {
				result.add(predicate.getRight());
			}

			return result;
		}

		@Override
		public void caseAPropertiesMachineClause(APropertiesMachineClause node)
		{
			if(node.getPredicates() instanceof AConjunctPredicate){
				childCounter.put(Types.PROPERTIES, unfoldConjunction((AConjunctPredicate) node.getPredicates()).size());

			}else{
				childCounter.put(Types.PROPERTIES, 1);
			}
		}

		@Override
		public void caseAVariablesMachineClause(AVariablesMachineClause node)
		{
			childCounter.put(Types.VARIABLES, node.getIdentifiers().size());
		}

		@Override
		public void caseAConstantsMachineClause(AConstantsMachineClause node)
		{
			childCounter.put(Types.CONSTANTS, node.getIdentifiers().size());

		}

		@Override
		public void caseASetsMachineClause(ASetsMachineClause node)
		{
			childCounter.put(Types.SETS, node.getSetDefinitions().size());

		}

		@Override
		public void caseAOperation(AOperation node) {
			if(node.getOperationBody() instanceof AParallelSubstitution){
				List<PSubstitution> result = unfoldParallelSubstation((AParallelSubstitution) node.getOperationBody());

				operationChildCounter.put(node.getOpName().toString(), result.size());
			}else{
				operationChildCounter.put(node.getOpName().toString(), 1);

			}
		}

		@Override
		public void caseAInitialisationMachineClause(AInitialisationMachineClause node)
		{
			if(node.getSubstitutions() instanceof AParallelSubstitution){
				List<PSubstitution> result = unfoldParallelSubstation((AParallelSubstitution) node.getSubstitutions());
				operationChildCounter.put(Transition.INITIALISE_MACHINE_NAME, result.size());
			}else{
				if(node.getSubstitutions()==null){
					operationChildCounter.put(Transition.INITIALISE_MACHINE_NAME, 0);

				}else{
					operationChildCounter.put(Transition.INITIALISE_MACHINE_NAME, 1);
				}

			}
		}

		public List<PSubstitution> unfoldParallelSubstation(AParallelSubstitution substitution){

			List<PSubstitution> unfoldedSubs = substitution.getSubstitutions().stream()
					.filter(entry -> entry instanceof AParallelSubstitution)
					.flatMap(entry -> unfoldParallelSubstation((AParallelSubstitution) entry).stream())
					.collect(Collectors.toList());

			List<PSubstitution> subNotNeededUnfolded = substitution.getSubstitutions().stream()
					.filter(Objects::nonNull)
					.filter(entry -> !(entry instanceof AParallelSubstitution))

					.collect(Collectors.toList());


			List<PSubstitution> result = new ArrayList<>();
			result.addAll(unfoldedSubs);
			result.addAll(subNotNeededUnfolded);
			return result;
		}


		@Override
		public void caseAIncludesMachineClause(AIncludesMachineClause node)
		{
			childCounter.put(Types.INCLUDES, node.getMachineReferences().size());

		}

		@Override
		public void caseAExtendsMachineClause(AExtendsMachineClause node)
		{
			childCounter.put(Types.EXTENDS, node.getMachineReferences().size());

		}

		@Override
		public void caseAImportsMachineClause(AImportsMachineClause node)
		{
			childCounter.put(Types.IMPORTS, node.getMachineReferences().size());

		}


		@Override
		public void caseASeesMachineClause(ASeesMachineClause node)
		{
			childCounter.put(Types.SEES, node.getMachineNames().size());

		}

		@Override
		public void caseAUsesMachineClause(AUsesMachineClause node)
		{
			childCounter.put(Types.USES, node.getMachineNames().size());

		}

		@Override
		public void caseAPromotesMachineClause(APromotesMachineClause node)
		{
			childCounter.put(Types.PROMOTES, node.getOperationNames().size());

		}


	}

	enum Types{
		SETS, VARIABLES, INVARIANT, VARIANT, INCLUDES, IMPORTS, SEES, USES, EXTENDS, PROMOTES ,PROPERTIES, CONSTANTS
	}
}
