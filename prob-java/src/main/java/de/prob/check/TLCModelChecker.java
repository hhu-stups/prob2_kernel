package de.prob.check;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.statespace.StateSpace;
import de.tlc4b.TLC4B;
import de.tlc4b.TLCRunner;
import de.tlc4b.tlc.TLCResults;

import static de.tlc4b.TLC4BCliOptions.TLCOption.NOTLC;
import static de.tlc4b.TLC4BCliOptions.TLCOption.OUTPUT;

// TODO: add tests
public class TLCModelChecker extends CheckerBase {

	private final TLCModelCheckingOptions options;
	private final String machinePath;

	private TLCResults results = null;

	public TLCModelChecker(final String machinePath, final StateSpace stateSpace, final IModelCheckListener listener) {
		this(machinePath, stateSpace, listener, new TLCModelCheckingOptions(stateSpace));
	}

	public TLCModelChecker(final String machinePath, final StateSpace stateSpace, final IModelCheckListener listener,
	                       final TLCModelCheckingOptions options) {
		super(stateSpace, listener);
		this.options = options;
		this.machinePath = machinePath;
	}

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

	private String[] getCurrentOptions() {
		List<String> args = new ArrayList<>();
		args.add(machinePath);
		options.getOptions().forEach((key, value) -> {
			args.add(key.cliArg());
			// If the value is null, it's a boolean flag with no argument.
			if (value != null) {
				args.add(value);
			}
		});
		return args.toArray(new String[0]);
	}

	public static Exception checkTLCApplicable(final String path, final int timeOut) {
		return TLC4B.checkTLC4BIsApplicable(path, timeOut);
	}

	public static void generateTLAWithoutTLC(final String machinePath, final String outputPath) throws IOException, BCompoundException {
		TLC4B.run(new String[]{machinePath, NOTLC.cliArg(), OUTPUT.cliArg(), outputPath});
	}

	public TLCResults getResults() {
		return this.results;
	}
}
