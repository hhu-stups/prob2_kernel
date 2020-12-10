package de.prob.check.tracereplay.check;

import java.util.List;
import java.util.Map;

public interface IDeltaFinder {


	Map<String, Map<String, String>> getResultTypeII();
	Map<String, String> getResultTypeIIInit();
	Map<String, List<Delta>> getResultTypeIIWithCandidatesAsDeltaMap();
}
