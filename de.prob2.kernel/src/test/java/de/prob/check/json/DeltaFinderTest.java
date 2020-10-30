package de.prob.check.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.prob.animator.command.CompareTwoOperations;
import de.prob.animator.command.PrepareOperations;
import de.prob.check.tracereplay.check.CheckerInterface;
import de.prob.check.tracereplay.check.DeltaFinder;
import de.prob.check.tracereplay.check.PrepareOperationsInterface;
import de.prob.check.tracereplay.check.Triple;
import de.prob.exception.ProBError;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.*;


public class DeltaFinderTest {

	CheckerInterface fakeCheckerInterface = (prepareOperations, candidate) -> {
		return new HashMap<>();
	};


	PrepareOperationsInterface fakePrepareOperationsInterface = (operation) -> {
		return new Triple<>(new ListPrologTerm(), new ListPrologTerm(), new CompoundPrologTerm("a"));
	};


	@Test
	void checkDeterministicPairs_test(){
		Map<String, CompoundPrologTerm> oldOperation = new HashMap<>();
		Map<String, CompoundPrologTerm> newOperation = new HashMap<>();
		Set<String> candidates = new HashSet<>();

		Map<String, Map<String, String>> result = DeltaFinder.checkDeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);


		Assert.assertEquals(new HashMap<>(), result);


	}


	@Test
	void checkDeterministicPairs_test_one_gets_removed(){

		CheckerInterface fakeCheckerInterface = (prepareOperations, candidate) -> {
			if(prepareOperations.getThird().equals(new CompoundPrologTerm("inc"))){
				return new HashMap<>();
			}else{
				Map<String, String> result = new HashMap<>();
				result.put("a", "b");
				return result;
			}
		};


		PrepareOperationsInterface fakePrepareOperationsInterface = (operation) ->
				new Triple<>(new ListPrologTerm(), new ListPrologTerm(), operation);

		Map<String, CompoundPrologTerm> oldOperation = new HashMap<>();
		oldOperation.put("inc", new CompoundPrologTerm("inc"));
		oldOperation.put("dec", new CompoundPrologTerm("dec"));
		oldOperation.put("getFloors", new CompoundPrologTerm("getFloors"));

		Map<String, CompoundPrologTerm> newOperation = new HashMap<>();
		oldOperation.put("inc", new CompoundPrologTerm("inc"));
		oldOperation.put("dec", new CompoundPrologTerm("dec"));
		oldOperation.put("getFloors", new CompoundPrologTerm("getFloors"));

		Set<String> candidates = new HashSet<>(Arrays.asList("inc", "dec", "getFloors"));

		Map<String, Map<String, String>> result = DeltaFinder.checkDeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);

		Map<String, String> expectedInner = new HashMap<>();
		expectedInner.put("a", "b");

		Map<String, Map<String, String>> expectedOuter = new HashMap<>();
		expectedOuter.put("dec", expectedInner);
		expectedOuter.put("getFloors", expectedInner);

		Assert.assertEquals(expectedOuter, result);
	}


	@Test
	void checkNondeterministicPairs_test_one_gets_removed(){

		CheckerInterface fakeCheckerInterface = (prepareOperations, candidate) -> {

			if(candidate.equals(new CompoundPrologTerm("inc23")) ||
					candidate.equals(new CompoundPrologTerm("deccc")) ){
				return new HashMap<>();
			}else{
				Map<String, String> result = new HashMap<>();
				result.put("a", "b");
				return result;
			}
		};


		PrepareOperationsInterface fakePrepareOperationsInterface = (operation) ->
				new Triple<>(new ListPrologTerm(), new ListPrologTerm(), operation);

		Map<String, CompoundPrologTerm> oldOperation = new HashMap<>();
		oldOperation.put("inc", new CompoundPrologTerm("inc"));
		oldOperation.put("dec", new CompoundPrologTerm("dec"));
		oldOperation.put("getFloors", new CompoundPrologTerm("getFloors"));

		Map<String, CompoundPrologTerm> newOperation = new HashMap<>();
		newOperation.put("inccc", new CompoundPrologTerm("inccc"));
		newOperation.put("inc4", new CompoundPrologTerm("inc4"));
		newOperation.put("inc23", new CompoundPrologTerm("inc23"));
		newOperation.put("deccc", new CompoundPrologTerm("deccc"));
		newOperation.put("GetFloors", new CompoundPrologTerm("GetFloors"));
		newOperation.put("getFloor", new CompoundPrologTerm("getFloor"));
		newOperation.put("currentFloors", new CompoundPrologTerm("currentFloors"));

		Map<String, Set<String>> candidates = new HashMap<>();

		candidates.put("inc", new HashSet<>(Arrays.asList("inccc", "inc4", "inc23")));
		candidates.put("dec", new HashSet<>(Collections.singletonList("deccc")));
		candidates.put("getFloors", new HashSet<>(Arrays.asList("GetFloors", "getFloor", "currentFloors")));


		Map<String, Map<String, Map<String, String>>> result = DeltaFinder.checkNondeterministicPairs(oldOperation, newOperation, candidates,
				fakeCheckerInterface, fakePrepareOperationsInterface);

		Map<String, String> expectedInner = new HashMap<>();
		expectedInner.put("a", "b");

		Map<String, Map<String, String>> expectedOuter1 = new HashMap<>();
		expectedOuter1.put("GetFloors", expectedInner);
		expectedOuter1.put("currentFloors", expectedInner);
		expectedOuter1.put("getFloor", expectedInner);



		Map<String, Map<String, String>> expectedOuter2 = new HashMap<>();
		expectedOuter2.put("inccc", expectedInner);
		expectedOuter2.put("inc4", expectedInner);


		Map<String, Map<String, Map<String, String>>> expectedResult = new HashMap<>();
		expectedResult.put("getFloors", expectedOuter1);
		expectedResult.put("inc", expectedOuter2);




		Assert.assertEquals(expectedResult, result);


	}
}
