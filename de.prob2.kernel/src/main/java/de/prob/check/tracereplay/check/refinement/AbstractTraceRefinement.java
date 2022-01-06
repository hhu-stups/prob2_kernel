package de.prob.check.tracereplay.check.refinement;

import com.google.inject.Injector;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.check.tracereplay.PersistentTransition;
import de.prob.check.tracereplay.check.traceConstruction.TraceConstructionError;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractTraceRefinement {
	
	protected final Injector injector;
	protected final List<PersistentTransition> transitionList;
	protected final Path adaptFrom;
	
	public AbstractTraceRefinement(Injector injector, List<PersistentTransition> transitionList, Path adaptFrom) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.adaptFrom = adaptFrom;
	}
	public abstract List<PersistentTransition> refineTrace() throws IOException, TraceConstructionError, BCompoundException;
}
