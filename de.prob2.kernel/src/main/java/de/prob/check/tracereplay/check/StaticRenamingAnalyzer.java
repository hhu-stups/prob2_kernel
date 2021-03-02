package de.prob.check.tracereplay.check;

import de.prob.check.tracereplay.check.exceptions.MappingFactoryInterface;
import de.prob.statespace.OperationInfo;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class StaticRenamingAnalyzer implements RenamingAnalyzerInterface{

	private final Set<String> simpleCandidates;
	private final Map<String, Set<String>> complexCandidates;

	private final Map<String, OperationInfo> oldInfos;
	private final Map<String, OperationInfo> newInfos;

	private final MappingFactoryInterface mappingFactoryInterface;

	private final Map<String, List<RenamingDelta>> resultsWithCandidates = new HashMap<>();
	private final List<RenamingDelta> resultsWithDelta = new ArrayList<>();


	public StaticRenamingAnalyzer(Set<String> simpleCandidates, Map<String, Set<String>> complexCandidates,
								  Map<String, OperationInfo> oldInfos, Map<String, OperationInfo> newInfos, MappingFactoryInterface mappingFactoryInterface){
		this.simpleCandidates = simpleCandidates;
		this.complexCandidates = complexCandidates;
		this.oldInfos = oldInfos;
		this.newInfos = newInfos;
		this.mappingFactoryInterface = mappingFactoryInterface;
	}

	public void calculateDelta(){

		Map<String, List<RenamingDelta>> firstResults = simpleCandidates.stream()
				.collect(toMap(entry -> entry,
						entry -> TraceExplorer.calculateVarMappings(entry, newInfos.get(entry), oldInfos.get(entry), mappingFactoryInterface).stream()
								.map(innerEntry -> new RenamingDelta(entry, entry, innerEntry))
								.filter(RenamingDelta::isPointless).collect(toList())));

		List<RenamingDelta> firstResultsWithOnlyOnePartner = firstResults.values().stream()
				.filter(renamingDeltas -> renamingDeltas.size() == 1)
				.map(renamingDeltas -> renamingDeltas.get(0))
				.collect(toList());

		resultsWithDelta.addAll(firstResultsWithOnlyOnePartner);


		Map<String, List<RenamingDelta>> firstResultsWithOnlyMorePartners = firstResults.entrySet().stream()
				.filter(entry -> entry.getValue().size()>1)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		resultsWithCandidates.putAll(firstResultsWithOnlyMorePartners);


		Map<String, List<RenamingDelta>> secondResult = complexCandidates.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry ->
						entry.getValue().stream()
								.flatMap(innerEntry ->
										TraceExplorer.calculateVarMappings(entry.getKey(), newInfos.get(innerEntry), oldInfos.get(entry.getKey()), mappingFactoryInterface).stream()
												.map(innermostEntry -> new RenamingDelta(entry.getKey(), innerEntry, innermostEntry))
								.filter(RenamingDelta::isPointless)).collect(toList())));

		Map<String, List<RenamingDelta>> cleansed = secondResult.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).collect(toMap(entry -> entry.getKey(), entry -> entry.getValue()));

		resultsWithCandidates.putAll(cleansed);

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
