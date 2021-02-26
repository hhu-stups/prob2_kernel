package de.prob.check.tracereplay.check.exceptions;

import de.prob.check.tracereplay.check.TraceExplorer;
import de.prob.statespace.OperationInfo;

import java.util.List;
import java.util.Map;

public interface ToManyOptionsIdentifierMapping {



	Map<String, String> askForMapping(List<String> oldInfo, List<String> newInfo, String name, TraceExplorer.MappingNames section);


}