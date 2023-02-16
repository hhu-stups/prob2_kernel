package de.prob.check.tracereplay.check;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Injector;

import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.exploration.PersistenceDelta;
import de.prob.check.tracereplay.check.exploration.ReplayOptions;
import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.check.tracereplay.check.renamig.DeltaCalculationException;
import de.prob.check.tracereplay.check.renamig.DynamicRenamingAnalyzer;
import de.prob.check.tracereplay.check.renamig.RenamingAnalyzerInterface;
import de.prob.check.tracereplay.check.renamig.RenamingDelta;
import de.prob.check.tracereplay.check.renamig.StaticRenamingAnalyzer;
import de.prob.check.tracereplay.check.ui.MappingFactoryInterface;
import de.prob.check.tracereplay.check.ui.ProgressMemoryInterface;
import de.prob.statespace.OperationInfo;
import de.prob.statespace.StateSpace;

import static java.util.stream.Collectors.toMap;


public class TraceChecker {

	private final TypeFinder typeFinder;
	private final TraceModifier traceModifier;
	private final Map<String, OperationInfo> oldOperationInfos;
	private final Map<String, OperationInfo> newOperationInfos;
	private final RenamingAnalyzerInterface renamingAnalyzer;


	@Deprecated
	public TraceChecker(List<PersistentTransition> transitionList,
						Map<String, OperationInfo> oldInfos,
						Map<String, OperationInfo> newInfos,
						Set<String> newVars,
						Set<String> oldVars,
						String newPath,
						Injector injector,
						MappingFactoryInterface mappingFactory,
						ReplayOptions replayOptions,
						ProgressMemoryInterface progressMemoryInterface) throws IOException, DeltaCalculationException {

			this(transitionList, oldInfos, newInfos, oldVars, newVars, null, newPath, injector, mappingFactory, replayOptions, progressMemoryInterface);

	}

	@Deprecated
	public TraceChecker(List<PersistentTransition> transitionList,
						Map<String, OperationInfo> oldInfos,
						Map<String, OperationInfo> newInfos,
						Set<String> oldVars,
						Set<String> newVars,
						String oldPath,
						String newPath,
						Injector injector,
						MappingFactoryInterface mappingFactory,
						ReplayOptions replayOptions,
						ProgressMemoryInterface progressMemoryInterface)
			throws IOException, DeltaCalculationException {

		this.oldOperationInfos = oldInfos;
		this.newOperationInfos = newInfos;

		typeFinder = new TypeFinder(transitionList, oldInfos, newInfos, oldVars, newVars);
		typeFinder.check();
		progressMemoryInterface.nextStep();


		if(oldPath == null){
			renamingAnalyzer = new StaticRenamingAnalyzer(typeFinder.getTypeIorII(), typeFinder.getTypeIIPermutation(), typeFinder.getInitIsTypeIorIICandidate(), oldInfos, newInfos, mappingFactory, injector, transitionList , oldVars);
		}else{
			renamingAnalyzer = new DynamicRenamingAnalyzer(typeFinder.getTypeIorII(), typeFinder.getTypeIIPermutation(),
					typeFinder.getInitIsTypeIorIICandidate(), oldPath, newPath, injector, oldInfos);

		}

		renamingAnalyzer.calculateDelta();
		progressMemoryInterface.nextStep();


		traceModifier = new TraceModifier(transitionList);
		traceModifier.insertMultipleUnambiguousChanges(renamingAnalyzer.getResultTypeIIAsDeltaList());
		traceModifier.insertAmbiguousChanges(renamingAnalyzer.getResultTypeIIWithCandidates());


		boolean initSet = renamingAnalyzer.initWasSet();
		TraceExplorer traceExplorer  = new TraceExplorer(initSet, mappingFactory, replayOptions, progressMemoryInterface);


		Set<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>> selectedMappingsToResultsKeys =
				IdentifierMatcher.generateAllPossibleMappingVariations(transitionList, newOperationInfos, oldOperationInfos, typeFinder.getTypeIII(), mappingFactory);

		StateSpace stateSpace = TraceCheckerUtils.createStateSpace(newPath, injector);

		Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>>> results = traceModifier.getChangelogPhase2()
				.entrySet()
				.stream()
				.map(entry ->
				{
					Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, List<PersistenceDelta>> result =
							traceExplorer.replayTrace(entry.getValue(), stateSpace, typeFinder.getTypeIV(), selectedMappingsToResultsKeys);
					return new AbstractMap.SimpleEntry<>(entry.getKey(), result);
				})
				.collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

		traceModifier.setChangelogPhase3(results);
		progressMemoryInterface.nextStep();
		Map<Set<RenamingDelta>, Map<Map<String, Map<TraceExplorer.MappingNames, Map<String, String>>>, Map<String, TraceAnalyser.AnalyserResult>>> typeIVResults =
				TraceAnalyser.performTypeIVAnalysing(traceExplorer.getUpdatedTypeIV(), results, traceModifier.getChangelogPhase2());
		progressMemoryInterface.nextStep();
		traceModifier.setChangelogPhase4(typeIVResults);



		traceModifier.setUngracefulTraces(traceExplorer.getUngracefulTraces());

	}




	public TypeFinder getTypeFinder() {
		return typeFinder;
	}

	public TraceModifier getTraceModifier() {
		return traceModifier;
	}

	public Map<String, OperationInfo> getOldOperationInfos(){
		return oldOperationInfos;
	}

	public Map<String, OperationInfo> getNewOperationInfos(){
		return newOperationInfos;
	}

	public RenamingAnalyzerInterface getRenamingAnalyzer(){
		return renamingAnalyzer;
	}


}
