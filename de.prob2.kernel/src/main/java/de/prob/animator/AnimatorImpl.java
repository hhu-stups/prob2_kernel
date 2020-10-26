package de.prob.animator;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.ComposedCommand;
import de.prob.animator.command.GetErrorItemsCommand;
import de.prob.animator.command.GetTotalNumberOfErrorsCommand;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.cli.ProBInstance;
import de.prob.exception.ProBError;
import de.prob.statespace.AnimationSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnimatorImpl implements IAnimator {

	private static int counter = 0;
	private final String id = "animator" + counter++;

	private final ProBInstance cli;
	private final Logger logger = LoggerFactory.getLogger(AnimatorImpl.class);
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
	public synchronized void execute(final AbstractCommand command) {
		if (command instanceof ComposedCommand && command.getSubcommands().isEmpty()) {
			// Optimization: an empty ComposedCommand has no effect,
			// so return right away to avoid an unnecessary communication with the CLI.
			logger.trace("Skipping execution of no-op ComposedCommand {}", command);
			return;
		}

		logger.trace("Starting execution of {}", command);
		IPrologResult result = processor.sendCommand(command);
		final GetErrorItemsCommand errorItemsCommand = getErrorItems();

		if (result instanceof YesResult && (errorItemsCommand.getErrors().isEmpty() || errorItemsCommand.onlyWarningsOccurred())) {
			logger.trace("Execution successful, processing result");
			if (!errorItemsCommand.getErrors().isEmpty()) {
				logger.warn("ProB reported warnings:");
				for (final ErrorItem error : errorItemsCommand.getErrors()) {
					assert error.getType() == ErrorItem.Type.WARNING;
					logger.warn("{}", error);
				}
				this.warningListeners.forEach(listener -> listener.warningsOccurred(errorItemsCommand.getErrors()));
			}
			command.processResult(((YesResult) result).getBindings());
		} else {
			logger.trace("Execution unsuccessful, processing error");
			command.processErrorResult(result, errorItemsCommand.getErrors());
		}
		logger.trace("Done executing {}", command);
	}

	private synchronized GetErrorItemsCommand getErrorItems() {
		final IPrologResult errorResult = processor.sendCommand(getErrorItems);
		if (errorResult instanceof YesResult) {
			getErrorItems.processResult(((YesResult) errorResult).getBindings());
			return getErrorItems;
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
		logger.info("Sending an interrupt to the CLI");
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
	public long getTotalNumberOfErrors() {
		GetTotalNumberOfErrorsCommand command = new GetTotalNumberOfErrorsCommand();
		execute(command);
		return command.getTotalNumberOfErrors().longValue();
	}

	@Override
	public void addWarningListener(final IWarningListener listener) {
		this.warningListeners.add(listener);
	}

	@Override
	public void removeWarningListener(final IWarningListener listener) {
		this.warningListeners.remove(listener);
	}
}
