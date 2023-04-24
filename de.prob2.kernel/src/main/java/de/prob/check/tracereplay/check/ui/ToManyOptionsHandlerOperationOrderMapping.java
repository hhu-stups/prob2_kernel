package de.prob.check.tracereplay.check.ui;

import de.prob.statespace.OperationInfo;

import java.util.List;
import java.util.Map;

@Deprecated
public interface ToManyOptionsHandlerOperationOrderMapping {

	Map<OperationInfo, List<OperationInfo>> askForMapping(OperationInfo oldInfo, List<OperationInfo> candidates);

}
