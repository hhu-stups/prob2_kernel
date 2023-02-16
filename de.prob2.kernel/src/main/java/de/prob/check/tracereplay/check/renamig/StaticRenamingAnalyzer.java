package de.prob.check.tracereplay.check.renamig;

import com.google.inject.Injector;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.IdentifierMatcher;
import de.prob.check.tracereplay.check.TraceCheckerUtils;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.check.tracereplay.check.ui.MappingFactoryInterface;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.Transition;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Deprecated
public class StaticRenamingAnalyzer implements RenamingAnalyzerInterface{

	private final Set<String> simpleCandidates;
	private final Map<String, Set<String>> complexCandidates;

	private final Map<String, OperationInfo> oldInfos;
	private final Map<String, OperationInfo> newInfos;

	private final MappingFactoryInterface mappingFactoryInterface;

	private final Map<String, List<RenamingDelta>> resultsWithCandidates = new HashMap<>();
	private final List<RenamingDelta> resultsWithDelta = new ArrayList<>();

	private final boolean initIsTypeIorIICandidate;

	private final List<PersistentTransition> trace;

	private final Set<String> oldVars;

	private boolean initWasSet = false;

	public StaticRenamingAnalyzer(Set<String> simpleCandidates, Map<String, Set<String>> complexCandidates,
								  Boolean initIsTypeIorIICandidate, Map<String, OperationInfo> oldInfos, Map<String, OperationInfo> newInfos, MappingFactoryInterface mappingFactoryInterface, Injector injector, List<PersistentTransition> trace, Set<String> oldVars){
		this.simpleCandidates = simpleCandidates;
		this.complexCandidates = complexCandidates;
		this.oldInfos = oldInfos;
		this.newInfos = newInfos;
		this.mappingFactoryInterface = mappingFactoryInterface;
		this.initIsTypeIorIICandidate = initIsTypeIorIICandidate;
		this.trace = trace;
		this.oldVars = oldVars;
	}

	public void calculateDelta()  {

		ArrayList<String> simpleCandidatesOptionalChoice= new ArrayList<>(simpleCandidates);


		Map<String, List<RenamingDelta>> firstResults = simpleCandidatesOptionalChoice.stream()
				.collect(toMap(entry -> entry,
						entry -> IdentifierMatcher.calculateVarMappings(entry, newInfos.get(entry), oldInfos.get(entry), mappingFactoryInterface).stream()
								.filter(new RemoveNotPureRenamed())
								.map(innerEntry -> new RenamingDelta(entry, entry, innerEntry))
								.collect(toList())));

		List<RenamingDelta> firstResultsWithOnlyOnePartner = firstResults.values().stream()
				.filter(renamingDeltas -> renamingDeltas.size() == 1)
				.map(renamingDeltas -> renamingDeltas.get(0))
				.filter(entry -> !entry.isPointless())
				.collect(toList());




		resultsWithDelta.addAll(firstResultsWithOnlyOnePartner);


		Map<String, List<RenamingDelta>> firstResultsWithOnlyMorePartners = firstResults.entrySet().stream()
				.filter(entry -> entry.getValue().size()>1)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		if(initIsTypeIorIICandidate){
			List<RenamingDelta> resultForInit = extractOldInit(trace, oldVars).stream()
					.filter(entry -> !entry.isPointless())
					.collect(toList());
			if(!resultForInit.isEmpty()){
				firstResultsWithOnlyMorePartners.put(Transition.INITIALISE_MACHINE_NAME, resultForInit);
				initWasSet = true;

			}
		}

		resultsWithCandidates.putAll(firstResultsWithOnlyMorePartners);


		Map<String, List<RenamingDelta>> secondResult = complexCandidates.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry ->
						entry.getValue().stream()
								.flatMap(innerEntry ->
										IdentifierMatcher.calculateVarMappings(entry.getKey(), newInfos.get(innerEntry), oldInfos.get(entry.getKey()), mappingFactoryInterface).stream()
												.map(innermostEntry -> new RenamingDelta(entry.getKey(), innerEntry, innermostEntry))
								.filter(RenamingDelta::isPointless)).collect(toList())));

		Map<String, List<RenamingDelta>> cleansed = secondResult.entrySet()
				.stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		resultsWithCandidates.putAll(cleansed);

	}

	@Override
	public boolean initWasSet() {
		return initWasSet;
	}


	private static class RemoveNotPureRenamed implements Predicate<Map<TraceExplorer.MappingNames, Map<String, String>>> {
		/**
		 * Evaluates this predicate on the given argument.
		 *
		 * @param mappingNamesMapMap the input argument
		 * @return {@code true} if the input argument matches the predicate,
		 * otherwise {@code false}
		 */
		@Override
		public boolean test(Map<TraceExplorer.MappingNames, Map<String, String>> mappingNamesMapMap) {

			List<Map.Entry<String, String>> allKeysOfInnerMap = mappingNamesMapMap.entrySet()
					.stream()
					.flatMap(entry -> entry.getValue().entrySet().stream())
					.collect(toList());


			Map<String, String> seen = new HashMap<>();


			for(Map.Entry<String, String> entry : allKeysOfInnerMap) {
				if(seen.containsKey(entry.getKey()) && !seen.get(entry.getKey()).equals(entry.getValue())){
					return false;
				}else if(!seen.containsKey(entry.getKey())){
					seen.put(entry.getKey(), entry.getValue());
				}
			}


			return true;
		}
	}


	private static List<RenamingDelta> extractOldInit(List<PersistentTransition> transitionList, Set<String> newInit){
		Map<String, String> assignedVariables = transitionList.stream()
				.filter(entry -> entry.getOperationName().equals(Transition.INITIALISE_MACHINE_NAME))
				.findFirst()
				.get()
				.getDestinationStateVariables();

		Set<Map<String, String>> mappings = TraceCheckerUtils.allDiagonals(new ArrayList<>(assignedVariables.keySet()), new ArrayList<>(newInit));

		return mappings.stream()
				.map(entry -> new RenamingDelta(Transition.INITIALISE_MACHINE_NAME, entry))
				.collect(toList());
	}


	@Override
	public Map<String, String> getResultTypeIIInit() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, List<RenamingDelta>> getResultTypeIIWithCandidates() {
		return resultsWithCandidates;
	}

	@Override
	public List<RenamingDelta> getResultTypeIIAsDeltaList() {
		return resultsWithDelta;
	}


}
