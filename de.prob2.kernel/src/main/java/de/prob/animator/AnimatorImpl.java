package de.prob.animator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.ComposedCommand;
import de.prob.animator.command.GetErrorItemsCommand;
import de.prob.animator.command.GetTotalNumberOfErrorsCommand;
import de.prob.animator.command.IRawCommand;
import de.prob.animator.command.ResetProBCommand;
import de.prob.animator.domainobjects.ErrorItem;
import de.prob.cli.ProBInstance;
import de.prob.core.sablecc.node.ACallBackResult;
import de.prob.core.sablecc.node.AExceptionResult;
import de.prob.core.sablecc.node.AInterruptedResult;
import de.prob.core.sablecc.node.ANoResult;
import de.prob.core.sablecc.node.AProgressResult;
import de.prob.core.sablecc.node.AYesResult;
import de.prob.core.sablecc.node.PResult;
import de.prob.exception.ProBError;
import de.prob.exception.PrologException;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ProBResultParser;
import de.prob.parser.PrologTermGenerator;
import de.prob.parser.SimplifiedROMap;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.prolog.term.PrologTerm;
import de.prob.statespace.AnimationSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnimatorImpl implements IAnimator {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnimatorImpl.class);

	private static int counter = 0;
	private final String id = "animator" + counter++;

	private final ProBInstance cli;
	private final GetErrorItemsCommand getErrorItems;
	private final AnimationSelector animations;
	private boolean busy = false;
	private final Collection<IWarningListener> warningListeners = new CopyOnWriteArrayList<>();

	@Inject
	AnimatorImpl(ProBInstance cli, AnimationSelector animations) {
		this.cli = cli;
		this.getErrorItems = new GetErrorItemsCommand();
		this.animations = animations;
	}

	private static String shorten(final String s) {
		final String shortened = s.length() <= 200 ? s : (s.substring(0, 200) + "...");
		return shortened.endsWith("\n") ? shortened.substring(0, shortened.length()-1) : shortened;
	}

	private static IPrologResult extractResult(PResult topnode) {
		if (topnode instanceof ANoResult) {
			return new NoResult();
		} else if (topnode instanceof AInterruptedResult) {
			return new InterruptedResult();
		} else if (topnode instanceof AYesResult) {
			Map<String, PrologTerm> binding = BindingGenerator.createBinding(PrologTermGenerator.toPrologTerm(topnode));
			return new YesResult(new SimplifiedROMap<>(binding));
		} else if (topnode instanceof AExceptionResult) {
			AExceptionResult r = (AExceptionResult) topnode;
			String message = r.getString().getText();
			throw new PrologException(message);
		} else {
			throw new ProBError("Unhandled Prolog result: " + topnode.getClass());
		}
	}

	private IPrologResult sendCommand(final AbstractCommand command) {
		String query;
		if (command instanceof IRawCommand) {
			query = ((IRawCommand) command).getCommand();
			if (!query.endsWith(".")) {
				query += ".";
			}
		} else {
			PrologTermStringOutput pto = new PrologTermStringOutput();
			command.writeCommand(pto);
			pto.printAtom("true");
			query = pto.fullstop().toString();
		}
		String result = cli.send(query); // send the query and get Prolog's response

		PResult topnode = ProBResultParser.parse(result).getPResult();
		while (topnode instanceof AProgressResult || topnode instanceof ACallBackResult) {
			if (topnode instanceof AProgressResult ) {
				// enable the command to respond to the progress information (e.g., by updating progress bar)
				command.processProgressResult(PrologTermGenerator.toPrologTerm(topnode));
				result = cli.receive(); // receive next term by Prolog
			} else {
				final PrologTermStringOutput pout = new PrologTermStringOutput();
				command.processCallBack(PrologTermGenerator.toPrologTerm(topnode), pout);
				result = cli.send(pout.fullstop().toString());
			}
			topnode = ProBResultParser.parse(result).getPResult();
		}
		// command is finished, we can extract the result:
		IPrologResult extractResult = extractResult(topnode);
		if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
			String resultString = extractResult.toString();
			LOGGER.debug("Result: {}", shorten(resultString));
			LOGGER.trace("Full result: {}", resultString);
		}
		return extractResult;
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
			result = sendCommand(command);
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
		final IPrologResult errorResult = sendCommand(getErrorItems);
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
