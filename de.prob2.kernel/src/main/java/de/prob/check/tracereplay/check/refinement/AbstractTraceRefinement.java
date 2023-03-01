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

	/**
	 * Better use with non-static parameters
	 */
	@Deprecated
	public AbstractTraceRefinement(Injector injector, List<PersistentTransition> transitionList, Path adaptFrom) {
		this.injector = injector;
		this.transitionList = transitionList;
		this.adaptFrom = adaptFrom;
		this.maxBreadth = 10;
		this.maxDepth = 5;
	}

	@Deprecated
	public abstract List<PersistentTransition> refineTrace() throws IOException, TraceConstructionError, BCompoundException;

	public abstract TraceRefinementResult refineTraceExtendedFeedback() throws IOException, TraceConstructionError, BCompoundException;
}
