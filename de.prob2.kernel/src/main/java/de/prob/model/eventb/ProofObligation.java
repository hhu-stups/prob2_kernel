package de.prob.model.eventb;

import java.util.List;
import java.util.stream.Collectors;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.Named;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.util.Tuple2;

public class ProofObligation extends AbstractElement implements Named {

	private final String name;
	private final int confidence;
	private final String description;
	private final String sourceName;
	private final List<? extends PrologTerm> sources;

	public ProofObligation(final String sourceName, final String name, final int confidence, final String description, final List<? extends PrologTerm> sources) {
		this.sourceName = sourceName;
		this.name = name;
		this.confidence = confidence;
		this.description = description;
		this.sources = sources;
	}

	/**
	 * @deprecated Use {@link #ProofObligation(String, String, int, String, List)} instead.
	 */
	@Deprecated
	public ProofObligation(final String sourceName, final String name,
			final boolean discharged, final String description,
			final List<Tuple2<String, String>> elements) {
		this(sourceName, name, discharged ? 1000 : 0, description, sourcesFromTuples(elements));
	}

	@Deprecated
	private static List<PrologTerm> sourcesFromTuples(final List<Tuple2<String, String>> elements) {
		return elements.stream()
			.map(tuple -> new CompoundPrologTerm(tuple.getFirst(), new CompoundPrologTerm(tuple.getSecond())))
			.collect(Collectors.toList());
	}

	@Override
	public String getName() {
		return name;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Get the confidence of the proof for this PO.
	 * See org.eventb.core.seqprover.IConfidence in the Rodin source code for details about the possible valid confidence values.
	 * If you simply want to know whether the PO was proven or not,
	 * use {@link #isDischarged()} and/or {@link #isReviewed()} instead.
	 * 
	 * @return the confidence for this PO's proof
	 */
	public int getConfidence() {
		return this.confidence;
	}

	public boolean isDischarged() {
		return this.getConfidence() > 500 && this.getConfidence() <= 1000;
	}

	public boolean isReviewed() {
		return this.getConfidence() > 100 && this.getConfidence() <= 500;
	}

	/**
	 * This method writes the source elements contained in a Proof Obligation in
	 * the given {@link IPrologTermOutput}. If certain elements are needed for a
	 * given proof obligation, then this proof obligation must override this
	 * method.
	 *
	 * @param pto the {@link IPrologTermOutput} to write to
	 */
	public void toProlog(final IPrologTermOutput pto) {
		pto.openTerm("po");
		pto.printAtom(sourceName);
		pto.printAtom(description);
		pto.openList();
		for (final PrologTerm source : this.sources) {
			pto.printTerm(source);
		}
		pto.closeList();
		
		if (this.isDischarged()) {
			pto.printAtom("true");
		} else if (this.isReviewed()) {
			pto.printAtom("reviewed");
		} else {
			pto.printAtom("false");
		}
		
		pto.closeTerm();
	}
}
