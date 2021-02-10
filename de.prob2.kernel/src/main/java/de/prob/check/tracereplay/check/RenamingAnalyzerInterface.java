package de.prob.check.tracereplay.check;

import java.util.List;
import java.util.Map;

public interface RenamingAnalyzerInterface {


	Map<String, Map<String, String>> getResultTypeII();
	Map<String, String> getResultTypeIIInit();
	Map<String, List<RenamingDelta>> getResultTypeIIWithCandidatesAsDeltaMap();
}
