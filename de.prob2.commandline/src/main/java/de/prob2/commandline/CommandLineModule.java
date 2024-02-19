package de.prob2.commandline;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

final class CommandLineModule extends AbstractModule {
	CommandLineModule() {
		super();
	}
	
	@Override
	protected void configure() {
		bind(CommandLineParser.class).to(DefaultParser.class);
	}
	
	/**
	 * @return an {@link Option} object containing the available command line options for the ProB Groovy shell
	 */
	@Provides
	private static Options getCommandlineOptions() {
		Options options = new Options();
		options.addOption(null, "maxCacheSize", true, "set the cache size for the states in the StateSpace");
		
		OptionGroup mode = new OptionGroup();
		mode.setRequired(true);
		// TODO: add modelchecking option
		// mode.addOption(new Option("mc", "modelcheck", false, "start ProB model checking"));
		mode.addOption(new Option(null, "script", true, "run a Groovy script or all .groovy files from a directory"));
		options.addOptionGroup(mode);
		
		return options;
	}
}
