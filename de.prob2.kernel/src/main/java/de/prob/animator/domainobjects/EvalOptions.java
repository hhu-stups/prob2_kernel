package de.prob.animator.domainobjects;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.statespace.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A set of options controlling the evaluation of formulas and how the results are formatted.</p>
 * <p>
 * {@link EvalOptions} objects are immutable.
 * There is no public constructor - to create a new set of options,
 * start with the {@link #DEFAULT} options
 * and change the desired settings using the {@code with...} methods.
 * </p>
 */
public final class EvalOptions {
	private static final Logger LOGGER = LoggerFactory.getLogger(EvalOptions.class);
	
	public static final EvalOptions DEFAULT = new EvalOptions(null, null, null, null, null);
	
	private final EvalExpandMode evalExpand;
	private final Duration timeout;
	private final FormulaExpand expand;
	private final FormulaTranslationMode mode;
	private final Language language;
	
	private EvalOptions(final EvalExpandMode evalExpand, final Duration timeout, final FormulaExpand expand, final FormulaTranslationMode mode, final Language language) {
		this.evalExpand = evalExpand;
		this.timeout = timeout;
		this.expand = expand;
		this.mode = mode;
		this.language = language;
	}
	
	public EvalExpandMode getEvalExpand() {
		return this.evalExpand;
	}
	
	/**
	 * Change the evaluation expansion mode,
	 * which controls if/when sets in the evaluation result should be expanded or left symbolic.
	 * Not to be confused with the pretty-print expand/truncate mode - see {@link #withExpand(FormulaExpand)}.
	 * 
	 * @param evalExpand the evaluation expansion mode to use
	 * @return copy of {@code this} with the evaluation expansion mode changed
	 */
	public EvalOptions withEvalExpand(final EvalExpandMode evalExpand) {
		return new EvalOptions(evalExpand, this.getTimeout(), this.getExpand(), this.getMode(), this.getLanguage());
	}
	
	public Duration getTimeout() {
		return this.timeout;
	}
	
	/**
	 * Change the per-formula evaluation timeout.
	 * 
	 * @param timeout the per-formula evaluation timeout
	 * @return copy of {@code this} with the timeout changed
	 */
	public EvalOptions withTimeout(final Duration timeout) {
		return new EvalOptions(this.getEvalExpand(), timeout, this.getExpand(), this.getMode(), this.getLanguage());
	}
	
	public FormulaExpand getExpand() {
		return this.expand;
	}
	
	/**
	 * Change the pretty-print expansion mode,
	 * i. e. whether evaluation results should be {@linkplain FormulaExpand#TRUNCATE truncated} after a certain length
	 * or {@linkplain FormulaExpand#EXPAND fully expanded}.
	 * Not to be confused with the evaluation expansion mode - see {@link #withEvalExpand(EvalExpandMode)}.
	 * 
	 * @param expand the pretty-print expansion mode to use
	 * @return copy of {@code this} with the pretty-print expansion mode changed
	 */
	public EvalOptions withExpand(final FormulaExpand expand) {
		return new EvalOptions(this.getEvalExpand(), this.getTimeout(), expand, this.getMode(), this.getLanguage());
	}
	
	/**
	 * <p>
	 * Determine the pretty-print expansion mode based on {@link IEvalElement#expansion()} of all {@code formulas}.
	 * If all formulas use the same expansion mode, that mode is chosen.
	 * If some formulas use different expansion modes,
	 * mode {@link FormulaExpand#EXPAND} is chosen and a warning is logged.
	 * </p>
	 * <p>
	 * This option should be considered semi-deprecated.
	 * In new code,
	 * please set an explicit expansion mode using {@link #withExpand(FormulaExpand)}
	 * </p>
	 * 
	 * @param formulas formulas from which to derive the expansion mode
	 * @return copy of {@code this} with the expansion mode changed
	 */
	public EvalOptions withExpandFromFormulas(final Collection<? extends IEvalElement> formulas) {
		if (formulas.isEmpty()) {
			// No formulas, so don't care
			return this;
		} else {
			// Determine expansion mode from formula list
			FormulaExpand expandTemp = formulas.iterator().next().expansion();
			for (final IEvalElement formula : formulas) {
				if (formula.expansion() != expandTemp) {
					LOGGER.warn("Using different expansion modes ({} and {}) inside a single evaluation command/call is no longer supported. For this evaluation, all formulas will be evaluated in EXPAND mode. To change this, ensure that all formulas in the list use the same expansion mode.", expandTemp, formula.expansion());
					expandTemp = FormulaExpand.EXPAND;
					break;
				}
			}
			return this.withExpand(expandTemp);
		}
	}
	
	public FormulaTranslationMode getMode() {
		return this.mode;
	}
	
	/**
	 * Change the translation mode,
	 * i. e. how to format special symbols in the string result
	 * (as {@linkplain FormulaTranslationMode#ASCII plain ASCII},
	 * {@linkplain FormulaTranslationMode#UNICODE Unicode symbols},
	 * {@linkplain FormulaTranslationMode#LATEX LaTeX syntax}, etc.).
	 * 
	 * @param mode the translation mode to use
	 * @return copy of {@code this} with the translation mode changed
	 */
	public EvalOptions withMode(final FormulaTranslationMode mode) {
		return new EvalOptions(this.getEvalExpand(), this.getTimeout(), this.getExpand(), mode, this.getLanguage());
	}
	
	public Language getLanguage() {
		return this.language;
	}
	
	public EvalOptions withLanguage(final Language language) {
		return new EvalOptions(this.getEvalExpand(), this.getTimeout(), this.getExpand(), this.getMode(), language);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		final EvalOptions other = (EvalOptions)obj;
		return this.getEvalExpand() == other.getEvalExpand()
			&& this.getTimeout() == other.getTimeout()
			&& this.getExpand() == other.getExpand()
			&& this.getMode() == other.getMode()
			&& this.getLanguage() == other.getLanguage();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getEvalExpand(), this.getTimeout(), this.getExpand(), this.getMode(), this.getLanguage());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("evalExpand", this.getEvalExpand())
			.add("timeout", this.getTimeout())
			.add("expand", this.getExpand())
			.add("mode", this.getMode())
			.add("language", this.getLanguage())
			.toString();
	}
	
	public void printProlog(final IPrologTermOutput pout) {
		if (this.getEvalExpand() != null) {
			pout.openTerm("eval_expand");
			this.getEvalExpand().printProlog(pout);
			pout.closeTerm();
		}
		
		if (this.getTimeout() != null) {
			pout.openTerm("timeout");
			pout.printNumber(this.getTimeout().toMillis());
			pout.closeTerm();
		}
		
		if (this.getExpand() != null) {
			pout.openTerm("truncate");
			pout.printAtom(this.getExpand().getPrologName());
			pout.closeTerm();
		}

		if (this.getMode() != null) {
			pout.openTerm("translation_mode");
			pout.printAtom(this.getMode().getPrologName());
			pout.closeTerm();
		}

		if (this.getLanguage() != null) {
			if (this.getLanguage().getTranslatedTo() != null) {
				throw new IllegalArgumentException("Cannot format evaluation results in " + this.getLanguage() + " syntax, because it is internally translated to " + this.getLanguage().getTranslatedTo());
			}

			pout.openTerm("language");
			pout.printAtom(this.getLanguage().getPrologName());
			pout.closeTerm();
		}
	}
}
