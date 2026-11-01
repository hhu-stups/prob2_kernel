package de.prob.model.brules;

import java.util.Map;

import de.hhu.stups.prob.translator.BString;
import de.hhu.stups.prob.translator.Translator;
import de.hhu.stups.prob.translator.exceptions.TranslationException;
import de.prob.animator.domainobjects.AbstractEvalResult;
import de.prob.animator.domainobjects.EvalResult;
import de.prob.animator.domainobjects.IdentifierNotInitialised;

public interface OperationStatus {

	boolean isExecuted();

	boolean isNotExecuted();

	boolean isDisabled();

	/**
	 * Converts the evaluated status variable of an operation into its status.
	 *
	 * @param evalResult       result of evaluating the status variable
	 * @param statusesByBValue all statuses, indexed by their B string value
	 * @param notInitialised   status if the variable is not initialised yet
	 * @throws IllegalArgumentException if the result is not a valid status
	 */
	static <T extends OperationStatus> T fromEvalResult(AbstractEvalResult evalResult, Map<String, T> statusesByBValue, T notInitialised) {
		if (evalResult instanceof IdentifierNotInitialised) {
			return notInitialised;
		}
		T status = null;
		if (evalResult instanceof EvalResult result) {
			try {
				if (Translator.translate(result.getValue()) instanceof BString value) {
					status = statusesByBValue.get(value.stringValue());
				}
			} catch (TranslationException e) {
				throw new IllegalArgumentException("Invalid operation status: " + evalResult, e);
			}
		}
		if (status == null) {
			throw new IllegalArgumentException("Invalid operation status: " + evalResult);
		}
		return status;
	}
}
