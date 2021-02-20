package de.prob.check.tracereplay.check.exceptions;

import de.prob.statespace.OperationInfo;

import java.util.List;
import java.util.Map;

public interface ToManyOptionsHandlerOperationOrderMapping {

	Map<OperationInfo, List<OperationInfo>> askForMapping(OperationInfo oldInfo, List<OperationInfo> candidates);

}
