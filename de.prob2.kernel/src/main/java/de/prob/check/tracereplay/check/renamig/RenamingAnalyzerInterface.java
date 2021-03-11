package de.prob.check.tracereplay.check.renamig;

import java.util.List;
import java.util.Map;

public interface RenamingAnalyzerInterface {


	void calculateDelta() throws DeltaCalculationException;
	Map<String, String> getResultTypeIIInit();
	Map<String, List<RenamingDelta>> getResultTypeIIWithCandidates();
	List<RenamingDelta> getResultTypeIIAsDeltaList();
}
