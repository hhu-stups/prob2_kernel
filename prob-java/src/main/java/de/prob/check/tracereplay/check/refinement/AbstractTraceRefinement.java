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
	protected final int maxDepth;
	protected final int maxBreadth;
	protected final Path adaptFrom;


	public AbstractTraceRefinement(Injector injector, List<PersistentTransition> transitionList, Path adaptFrom, int maxBreadth, int maxDepth) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.adaptFrom = adaptFrom;
		this.maxBreadth = maxBreadth;
		this.maxDepth = maxDepth;
	}

	public AbstractTraceRefinement(Injector injector, List<PersistentTransition> transitionList, Path adaptFrom) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.adaptFrom = adaptFrom;
		this.maxBreadth = 10;
		this.maxDepth = 5;
	}

	@Deprecated
	public List<PersistentTransition> refineTrace() throws IOException, TraceConstructionError, BCompoundException {
		return this.refineTraceExtendedFeedback().getResultTracePersistentTransition();
	}

	public abstract TraceRefinementResult refineTraceExtendedFeedback() throws IOException, TraceConstructionError, BCompoundException;
}
