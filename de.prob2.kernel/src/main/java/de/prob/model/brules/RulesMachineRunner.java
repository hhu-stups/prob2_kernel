package de.prob.model.brules;

import java.io.File;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.Main;
import de.prob.animator.ReusableAnimator;
import de.prob.cli.CliVersionNumber;
import de.prob.scripting.Api;
import de.prob.scripting.ExtractedModel;

@Singleton
public class RulesMachineRunner {

	@Deprecated
	private static RulesMachineRunner rulesMachineRunner; // singleton
	private final CliVersionNumber cliVersion;
	private final Provider<ReusableAnimator> animatorProvider;
	private final RulesModelFactory rulesFactory;

	@Inject
	public RulesMachineRunner(Api api, Provider<ReusableAnimator> animatorProvider, RulesModelFactory rulesFactory) {
		this.cliVersion = api.getVersion();
		this.animatorProvider = animatorProvider;
		this.rulesFactory = rulesFactory;
	}

	/**
	 * @deprecated Use dependency injection to get a {@link RulesMachineRunner} instance.
	 */
	@Deprecated
	public static RulesMachineRunner getInstance() {
		if (rulesMachineRunner == null) {
			rulesMachineRunner = Main.getInjector().getInstance(RulesMachineRunner.class);
		}
		return rulesMachineRunner;
	}

	/**
	 * @deprecated Inject an instance of {@link Api} and use {@link Api#getVersion()} instead.
	 */
	@Deprecated
	public CliVersionNumber getVersion() {
		return this.cliVersion;
	}

	public ExecuteRun createRulesMachineExecuteRun(RulesProject rulesProject, File mainMachineFile,
			Map<String, String> proBCorePreferences, boolean continueAfterErrors, ReusableAnimator animator) {
		ExtractedModel<RulesModel> extract = this.rulesFactory.extract(mainMachineFile, rulesProject);
		return new ExecuteRun(extract, proBCorePreferences, continueAfterErrors, animator != null ? animator : this.animatorProvider.get());
	}

}
