package de.prob.animator.domainobjects;

import java.util.Collection;
import java.util.Objects;

import com.google.common.base.MoreObjects;

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
	
	public static final EvalOptions DEFAULT = new EvalOptions();
	
	private final FormulaExpand expand;
	private final FormulaTranslationMode mode;
	
	private EvalOptions(final FormulaExpand expand, final FormulaTranslationMode mode) {
		this.expand = expand;
		this.mode = mode;
	}
	
	private EvalOptions() {
		this(FormulaExpand.TRUNCATE, FormulaTranslationMode.UNICODE);
	}
	
	public FormulaExpand getExpand() {
		return this.expand;
	}
	
	/**
	 * Change the expansion mode,
	 * i. e. whether evaluation results should be {@linkplain FormulaExpand#TRUNCATE truncated} after a certain length
	 * or {@linkplain FormulaExpand#EXPAND fully expanded}.
	 * 
	 * @param expand the expansion mode to use
	 * @return copy of {@code this} with the expansion mode changed
	 */
	public EvalOptions withExpand(final FormulaExpand expand) {
		return new EvalOptions(expand, this.getMode());
	}
	
	/**
	 * <p>
	 * Determine the expansion mode based on {@link IEvalElement#expansion()} of all {@code formulas}.
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
		return new EvalOptions(this.getExpand(), mode);
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
		return this.getExpand() == other.getExpand()
			&& this.getMode() == other.getMode();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getExpand(), this.getMode());
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("expand", this.getExpand())
			.add("mode", this.getMode())
			.toString();
	}
}
