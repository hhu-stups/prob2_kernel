package de.prob.check;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.animator.command.GetConstantsPredicateCommand;
import de.prob.statespace.StateSpace;
import de.tlc4b.TLC4B;
import de.tlc4b.TLC4BOption;
import de.tlc4b.TLCRunner;
import de.tlc4b.tlc.TLCResults;

// TODO: add tests
public class TLCModelChecker extends CheckerBase {

	private TLCModelCheckingOptions options;
	private final String machinePath;

	private TLCResults results = null;

	public TLCModelChecker(final String machinePath, final StateSpace stateSpace, final IModelCheckListener listener) {
		this(machinePath, stateSpace, listener, null);
	}

	public TLCModelChecker(final String machinePath, final StateSpace stateSpace, final IModelCheckListener listener,
	                       final TLCModelCheckingOptions options) {
		super(stateSpace, listener);
		this.options = options;
		this.machinePath = machinePath;
	}

	@Override
	public void execute() {
		TLCStats stats = new TLCStats(this);
		TLCRunner.addTLCMessageListener(stats);
		try {
			this.results = TLC4B.run(getCurrentOptions());
			stats.handleResults(results);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getProBConstants() {
		GetConstantsPredicateCommand getConstantsPredicateCommand = new GetConstantsPredicateCommand();
		this.getStateSpace().execute(getConstantsPredicateCommand);
		return getConstantsPredicateCommand.getPredicate();
	}

	private String[] getCurrentOptions() {
		List<String> args = new ArrayList<>();
		args.add(machinePath);
		if (options == null) {
			options = TLCModelCheckingOptions.fromPreferences(this.getStateSpace());
		}
		options.getOptions().forEach((key, value) -> {
			args.add(key.cliArg());
			// If the value is null, it's a boolean flag with no argument.
			if (value == null) {
				// Except for one special case: CONSTANTSSETUP has an argument,
				// but it's calculated lazily here so that TLCModelCheckingOptions doesn't depend on the StateSpace.
				if (key == TLC4BOption.CONSTANTSSETUP) {
					args.add(this.getProBConstants());
				}
			} else {
				args.add(value);
			}
		});
		return args.toArray(new String[0]);
	}

	public static Exception checkTLCApplicable(final String path, final int timeOut) {
		return TLC4B.checkTLC4BIsApplicable(path, timeOut);
	}

	public static void generateTLAWithoutTLC(final String machinePath, final String outputPath) throws IOException, BCompoundException {
		TLC4B.run(new String[]{machinePath, TLC4BOption.NOTLC.cliArg(), TLC4BOption.OUTPUT.cliArg(), outputPath});
	}

	public TLCResults getResults() {
		return this.results;
	}
}
