package de.prob.model.eventb;

import java.util.List;

import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.Named;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.unicode.UnicodeTranslator;

public class ProofObligation extends AbstractElement implements Named {

	private final String name;
	private final int confidence;
	private final String description;
	private final String sourceName;
	private final List<? extends PrologTerm> sources;
	private final List<IEvalElement> hypotheses;
	private final List<IEvalElement> selectedHypotheses;
	private final IEvalElement goal;

	@Deprecated
	public ProofObligation(final String sourceName, final String name, final int confidence, final String description, final List<? extends PrologTerm> sources) {
		this(sourceName, name, confidence, description, sources, null, null, null);
	}

	public ProofObligation(final String sourceName, final String name, final int confidence, final String description,
	                       final List<? extends PrologTerm> sources, final List<IEvalElement> hypotheses,
	                       final List<IEvalElement> selectedHypotheses, final IEvalElement goal) {
		this.sourceName = sourceName;
		this.name = name;
		this.confidence = confidence;
		this.description = description;
		this.sources = sources;
		this.hypotheses = hypotheses;
		this.selectedHypotheses = selectedHypotheses;
		this.goal = goal;
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

	public List<IEvalElement> getHypotheses() {
		return hypotheses;
	}

	public List<IEvalElement> getSelectedHypotheses() {
		return selectedHypotheses;
	}

	public IEvalElement getGoal() {
		return goal;
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

	public String getSequentPrettyPrint(boolean useUnicode) {
		StringBuilder prettyPrint = new StringBuilder();
		for (IEvalElement hyp : this.hypotheses) {
			prettyPrint.append(this.selectedHypotheses.contains(hyp) ? " ✔ " : "   ");
			String code = hyp.getCode();
			if (useUnicode) {
				code = UnicodeTranslator.toUnicode(code);
			}
			prettyPrint.append(code).append("\n");
		}
		prettyPrint.append("  ────────────────\n");
		String goalCode = this.goal.getCode();
		if (useUnicode) {
			goalCode = UnicodeTranslator.toUnicode(goalCode);
		}
		prettyPrint.append("   ").append(goalCode);
		return prettyPrint.toString();
	}

}
