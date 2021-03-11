package de.prob.check.tracereplay.check.ui;

import de.prob.check.tracereplay.check.exploration.TraceExplorer;

import java.util.List;
import java.util.Map;

public interface ToManyOptionsIdentifierMapping {



	Map<String, String> askForMapping(List<String> oldInfo, List<String> newInfo, String name, TraceExplorer.MappingNames section);


}
