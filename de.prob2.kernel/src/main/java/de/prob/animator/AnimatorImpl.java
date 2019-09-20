package de.prob.animator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.ComposedCommand;
import de.prob.animator.command.GetErrorItemsCommand;
import de.prob.animator.command.GetTotalNumberOfErrorsCommand;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.cli.ProBInstance;
import de.prob.exception.CliError;
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
	public static final boolean DEBUG = false;
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
		if (DEBUG && !command.getSubcommands().isEmpty()) {
			List<AbstractCommand> cmds = command.getSubcommands();
			for (AbstractCommand abstractCommand : cmds) {
				execute(abstractCommand);
			}
		}

		if (command.blockAnimator()) {
			logger.trace("Blocking animator");
			startTransaction();
		}
		logger.trace("Starting execution of {}", command);
		do {
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
				try {
					command.processResult(((YesResult) result).getBindings());
				} catch (RuntimeException e) {
					this.kill();
					throw new CliError("Exception while processing command result", e);
				}
			} else {
				logger.trace("Execution unsuccessful, processing error");
				command.processErrorResult(result, errorItemsCommand.getErrors());
			}
			logger.trace("Executed {} (completed: {}, interrupted: {})", command, command.isCompleted(), command.isInterrupted());
			
			if (!command.isCompleted() && Thread.currentThread().isInterrupted()) {
				logger.info("Stopping execution of {} because this thread was interrupted", command);
				break;
			}
		} while (!command.isCompleted());
		logger.trace("Done executing {}", command);
		if (command.blockAnimator()) {
			endTransaction();
			logger.trace("Unblocked animator");
		}
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
	public void execute(final AbstractCommand... commands) {
		execute(new ComposedCommand(commands));
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
