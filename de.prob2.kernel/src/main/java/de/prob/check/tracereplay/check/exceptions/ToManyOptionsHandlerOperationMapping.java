package de.prob.check.tracereplay.check.exceptions;

import de.prob.check.tracereplay.check.exploration.TraceExplorer;
import de.prob.statespace.OperationInfo;

import java.util.Map;

public interface ToManyOptionsHandlerOperationMapping {

	Map<TraceExplorer.MappingNames, Map<String, String>> askForMapping(OperationInfo oldInfo, OperationInfo newInfo, String name, TraceExplorer.MappingNames section);

}
