package de.prob.animator;


import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import de.prob.CliConfiguration;
import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.ComposedCommand;
import de.prob.animator.command.GetErrorItemsCommand;
import de.prob.animator.command.GetTotalNumberOfErrorsCommand;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.clistarter.client.CliClient;
import de.prob.clistarter.exception.CliError;
import de.prob.exception.ProBError;
import de.prob.statespace.AnimationSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class AnimatorImpl implements IAnimator {

	private static int counter = 0;
	private final String id = "animator" + counter++;

	private final CliClient cliClient;
	private final Logger logger = LoggerFactory.getLogger(AnimatorImpl.class);
	private final CommandProcessor processor;
	private final GetErrorItemsCommand getErrorItems;
	public static final boolean DEBUG = false;
	private final AnimationSelector animations;
	private boolean busy = false;
	private final Collection<IWarningListener> warningListeners = new ArrayList<>();

	@Inject
	public AnimatorImpl(final CliClient cliClient, final CliConfiguration configuration, final CommandProcessor processor,
						final GetErrorItemsCommand getErrorItems, final AnimationSelector animations) {
		this.cliClient = cliClient;
		this.processor = processor;
		this.getErrorItems = getErrorItems;
		this.animations = animations;
		processor.configure(cliClient, configuration);
	}

	@Override
	public synchronized void execute(final AbstractCommand command) {
		if (DEBUG && !command.getSubcommands().isEmpty()) {
			List<AbstractCommand> cmds = command.getSubcommands();
			for (AbstractCommand abstractCommand : cmds) {
				execute(abstractCommand);
			}
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
		return MoreObjects.toStringHelper(AnimatorImpl.class).addValue(cliClient).toString();
	}

	@Override
	public void execute(final AbstractCommand... commands) {
		execute(new ComposedCommand(commands));
	}

	@Override
	public void sendInterrupt() {
		logger.info("Sending an interrupt to the CLI");
		cliClient.interruptCLI();
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
		cliClient.shutdownCLI();
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
