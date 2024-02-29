package de.prob.animator;

import java.util.List;
import java.util.function.Supplier;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.ComposedCommand;

/**
 * This interface provides the methods needed to access the ProB Prolog core (probcli).
 * The user can specific tasks to execute via the {@link AbstractCommand}
 * abstraction and either the {@link #execute(AbstractCommand)} or
 * {@link #execute(AbstractCommand...)} methods. If an execution should be
 * broken off, the {@link #sendInterrupt()} method should be called.
 * 
 * @author joy
 * 
 */
public interface IAnimator {
	/**
	 * Takes an {@link AbstractCommand} and executes it.
	 * 
	 * @param command
	 *            an {@link AbstractCommand} to execute
	 */
	void execute(AbstractCommand command);

	/**
	 * Takes multiple commands and executes them.
	 * 
	 * @param commands
	 *            multiple {@link AbstractCommand}s to execute
	 */
	default void execute(AbstractCommand... commands) {
		this.execute(new ComposedCommand(commands));
	}

	/**
	 * Takes multiple commands and executes them.
	 * 
	 * @param commands
	 *            multiple {@link AbstractCommand}s to execute
	 */
	default void execute(List<? extends AbstractCommand> commands) {
		this.execute(new ComposedCommand(commands));
	}

	/**
	 * Interrupt any commands that are currently being executed.
	 */
	void sendInterrupt();

	/**
	 * Kills the underlying probcli
	 */
	void kill();

	/**
	 * <p>Signals the {@link IAnimator} that a transaction is beginning. The
	 * {@link IAnimator} can then set a flag indicating that it is busy because
	 * the {@link IAnimator} is likely to be blocked for a long period of time.</p>
	 * 
	 * <p>Note: In most cases you should use {@link #withTransaction(Runnable)} instead of this method, to ensure that the transaction is properly ended when an exception is thrown by the code inside the transaction.</p>
	 */
	void startTransaction();

	/**
	 * <p>Signals the {@link IAnimator} that a transaction has ended. The
	 * {@link IAnimator} can then reset the flag indicating that it is busy.</p>
	 * 
	 * <p>Note: In most cases you should use {@link #withTransaction(Runnable)} instead of this method, to ensure that the transaction is properly ended when an exception is thrown by the code inside the transaction.</p>
	 */
	void endTransaction();

	/**
	 * Execute the given code as part of a transaction. This is a convenience method that calls {@link #startTransaction()} before calling {@code c}, and {@link #endTransaction()} when {@code c} returns or throws an exception.
	 * 
	 * @param c the code to execute
	 * @param <R> the return type of {@code c}
	 * @return the value returned by {@code c}
	 */
	default <R> R withTransaction(final Supplier<R> c) {
		this.startTransaction();
		try {
			return c.get();
		} finally {
			this.endTransaction();
		}
	}

	/**
	 * Execute the given code as part of a transaction. This is a convenience method that calls {@link #startTransaction()} before calling {@code r}, and {@link #endTransaction()} when {@code r} returns or throws an exception.
	 * 
	 * @param r the code to execute
	 */
	default void withTransaction(final Runnable r) {
		this.withTransaction(() -> {
			r.run();
			return null;
		});
	}

	/**
	 * @return <code>true</code> if the animator is busy and <code>false</code>
	 *         otherwise. While <code>true</code>, the caller of the
	 *         {@link IAnimator} should not call
	 *         {@link #execute(AbstractCommand...)}.
	 */
	boolean isBusy();

	/**
	 * @return unique id associated with this instance of the animator. All
	 *         implementations should ensure that this id is unique.
	 */
	String getId();

	/**
	 * Reset this animator's ProB instance to a clean state as if it was newly started.
	 * This unloads any loaded model and resets all preferences to their default values,
	 * among other things.
	 * (This does not reset {@link #getTotalNumberOfErrors()},
	 * which can intentionally only be reset by completely restarting probcli.)
	 */
	void resetProB();

	long getTotalNumberOfErrors();

	void addWarningListener(final IWarningListener listener);
	void removeWarningListener(final IWarningListener listener);

	void addConsoleOutputListener(final IConsoleOutputListener listener);
	void removeConsoleOutputListener(final IConsoleOutputListener listener);
}
