package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.*;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ASTManipulatorTest {

	@Test
	public void test_correctConstruction_1() throws IOException, BCompoundException {


		Path alphaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLightRef.ref");
		BParser alphaParser1 = new BParser(alphaFile.toString());

		Start alphaStart1 = alphaParser1.parseFile(alphaFile.toFile(), false);

		NodeCollector nodeCollector = new NodeCollector(alphaStart1);

		Path betaFile = Paths.get("src", "test", "resources", "de", "prob", "testmachines", "traces", "refinements",  "TrafficLight.mch");
		BParser betaParser1 = new BParser(betaFile.toString());

		Start betaStart1 = betaParser1.parseFile(betaFile.toFile(), false);

		ASTManipulator astManipulator = new ASTManipulator(betaStart1, nodeCollector);



		Start alphaStart2 = alphaParser1.parseFile(alphaFile.toFile(), false);

		Start betaStart2 = betaParser1.parseFile(betaFile.toFile(), false);


		ASTChecker astCheckerAlpha = new ASTChecker();
		astCheckerAlpha.check(alphaStart2);

		ASTChecker astCheckerBeta = new ASTChecker();
		astCheckerBeta.check(betaStart2);
		System.out.println(astCheckerBeta.childCounter.get(Types.INVARIANT));

		ASTChecker astCheckerCombined = new ASTChecker();
		astCheckerCombined.check(astManipulator.getStart());

		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.INVARIANT), astCheckerCombined.childCounter.get(Types.INVARIANT));
		Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.VARIABLES), astCheckerCombined.childCounter.get(Types.VARIABLES));
	//	Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.SETS), astCheckerCombined.childCounter.get(Types.SETS));
	//	Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.CONSTANTS), astCheckerCombined.childCounter.get(Types.CONSTANTS));
	//	Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, Types.PROPERTIES), astCheckerCombined.childCounter.get(Types.PROPERTIES));
		for(String entry : astCheckerCombined.operationChildCounter.keySet()){
			Assertions.assertEquals(getCombined(astCheckerAlpha, astCheckerBeta, entry), astCheckerCombined.operationChildCounter.get(entry));

		}

	}


	public int getCombined(ASTChecker a, ASTChecker b, String name){
		return a.operationChildCounter.get(name)+b.operationChildCounter.get(name);
	}

	public int getCombined(ASTChecker a, ASTChecker b, Types type){

		return a.childCounter.get(type)+b.childCounter.get(type);
	}

	static class ASTChecker extends DepthFirstAdapter{

		public void check(Start start){
			start.apply(this);
		}


		public Map<Types, Integer> childCounter = new HashMap<>();
		public Map<String, Integer> operationChildCounter = new HashMap<>();

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
				operationChildCounter.put(Transition.INITIALISE_MACHINE_NAME, 1);

			}
		}

		public List<PSubstitution> unfoldParallelSubstation(AParallelSubstitution substitution){

			List<PSubstitution> unfoldedSubs = substitution.getSubstitutions().stream()
					.filter(entry -> entry instanceof AParallelSubstitution)
					.flatMap(entry -> unfoldParallelSubstation((AParallelSubstitution) entry).stream())
					.collect(Collectors.toList());

			List<PSubstitution> subNotNeededUnfolded = substitution.getSubstitutions().stream().filter(entry -> !(entry instanceof AParallelSubstitution)).collect(Collectors.toList());


			List<PSubstitution> result = new ArrayList<>();
			result.addAll(unfoldedSubs);
			result.addAll(subNotNeededUnfolded);
			return result;
		}

	}

	enum Types{
		SETS, VARIABLES, INVARIANT, VARIANT, INCLUDES, IMPORTS, SEES, USES, PROPERTIES, CONSTANTS
	}
}
