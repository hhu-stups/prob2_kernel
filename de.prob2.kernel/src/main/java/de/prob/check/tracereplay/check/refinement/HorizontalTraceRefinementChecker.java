package de.prob.check.tracereplay.check.refinement;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.check.tracereplay.PersistentTransition;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HorizontalTraceRefinementChecker {

	public static void refineTrace(File target, List<PersistentTransition> transitionList) throws IOException, BCompoundException {

		BParser parser = new BParser(target.toString());
		Start result = parser.parseFile(target, false);
	//	OperationsFinder operationsFinder = new OperationsFinder();
	//	operationsFinder.explore(result);



	}

}
