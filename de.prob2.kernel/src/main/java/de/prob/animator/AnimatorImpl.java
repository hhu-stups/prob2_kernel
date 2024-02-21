package de.prob.animator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.ComposedCommand;
import de.prob.animator.command.GetErrorItemsCommand;
import de.prob.animator.command.GetTotalNumberOfErrorsCommand;
import de.prob.animator.command.ResetProBCommand;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.cli.ProBInstance;
import de.prob.exception.ProBError;
import de.prob.statespace.AnimationSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnimatorImpl implements IAnimator {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnimatorImpl.class);

	private static int counter = 0;
	private final String id = "animator" + counter++;

	private final ProBInstance cli;
	private final CommandProcessor processor;
	private final GetErrorItemsCommand getErrorItems;
	private final AnimationSelector animations;
	private boolean busy = false;
	private final Collection<IWarningListener> warningListeners = new ArrayList<>();

	@Inject
	public AnimatorImpl(final ProBInstance cli, final CommandProcessor processor,
			final GetErrorItemsCommand getErrorItems, final AnimationSelector animations) {
		this.cli = cli;
		this.processor = processor;
		this.getErrorItems = getErrorItems;
		this.animations = animations;
		processor.configure(cli);
	}

	@Override
	public void execute(final AbstractCommand command) {
		if (command instanceof ComposedCommand && command.getSubcommands().isEmpty()) {
			// Optimization: an empty ComposedCommand has no effect,
			// so return right away to avoid an unnecessary communication with the CLI.
			LOGGER.trace("Skipping execution of no-op ComposedCommand {}", command);
			return;
		}

		final IPrologResult result;
		final List<ErrorItem> errors;
		// Prevent multiple threads from communicating over the same connection at the same time.
		synchronized (this) {
			LOGGER.trace("Starting execution of {}", command);
			result = processor.sendCommand(command);
			errors = getErrorItems();
		}

		final Optional<ErrorItem.Type> worstErrorType = errors.stream()
			.map(ErrorItem::getType)
			.max(ErrorItem.Type::compareTo);
		// If any error with type ERROR or worse was returned,
		// the command is considered unsuccessful (even if Prolog said yes).
		// The exact behavior in that case can be overridden by individual commands,
		// but normally a ProBError exception containing the error information is thrown.
		final boolean anyErrorIsFatal = worstErrorType.isPresent() && worstErrorType.get().compareTo(ErrorItem.Type.ERROR) >= 0;

		if (result instanceof YesResult && !anyErrorIsFatal) {
			LOGGER.trace("Execution successful, processing result");
			if (!errors.isEmpty()) {
				assert worstErrorType.isPresent();
				//noinspection NonStrictComparisonCanBeEquality
				if (worstErrorType.get().compareTo(ErrorItem.Type.MESSAGE) <= 0) {
					LOGGER.info("ProB returned messages:");
					for (final ErrorItem error : errors) {
						LOGGER.info("{}", error);
					}
				} else {
					LOGGER.warn("ProB reported warnings:");
					for (final ErrorItem error : errors) {
						LOGGER.warn("{}", error);
					}
				}
				this.warningListeners.forEach(listener -> listener.warningsOccurred(errors));
			}
			command.processResult(((YesResult) result).getBindings());
		} else {
			LOGGER.trace("Execution unsuccessful, processing error");
			command.processErrorResult(result, errors);
		}
		LOGGER.trace("Done executing {}", command);
	}

	private List<ErrorItem> getErrorItems() {
		final IPrologResult errorResult = processor.sendCommand(getErrorItems);
		if (errorResult instanceof YesResult) {
			getErrorItems.processResult(((YesResult) errorResult).getBindings());
			return getErrorItems.getErrors();
		} else {
			throw new ProBError("Error getter command must return yes, not " + errorResult.getClass());
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(AnimatorImpl.class).addValue(cli).toString();
	}

	@Override
	public void sendInterrupt() {
		LOGGER.info("Sending an interrupt to the CLI");
		cli.sendInterrupt();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void startTransaction() {
		busy = true;
		animations.notifyAnimatorStatus(id, busy);
	}

	@Override
	public void endTransaction() {
		busy = false;
		animations.notifyAnimatorStatus(id, busy);
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public void kill() {
		cli.shutdown();
	}

	@Override
	public void resetProB() {
		this.execute(new ResetProBCommand());
	}

	@Override
	public long getTotalNumberOfErrors() {
		GetTotalNumberOfErrorsCommand command = new GetTotalNumberOfErrorsCommand();
		execute(command);
		return command.getTotalNumberOfErrors().longValueExact();
	}

	@Override
	public void addWarningListener(final IWarningListener listener) {
		this.warningListeners.add(listener);
	}

	@Override
	public void removeWarningListener(final IWarningListener listener) {
		this.warningListeners.remove(listener);
	}

	@Override
	public void addConsoleOutputListener(final IConsoleOutputListener listener) {
		this.cli.addConsoleOutputListener(listener);
	}

	@Override
	public void removeConsoleOutputListener(final IConsoleOutputListener listener) {
		this.cli.removeConsoleOutputListener(listener);
	}
}
