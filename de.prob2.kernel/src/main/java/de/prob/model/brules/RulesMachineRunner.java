package de.prob.model.brules;

import java.io.File;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.Main;
import de.prob.animator.ReusableAnimator;
import de.prob.cli.CliVersionNumber;
import de.prob.scripting.Api;
import de.prob.scripting.ExtractedModel;

public class RulesMachineRunner {

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

	public static RulesMachineRunner getInstance() {
		if (rulesMachineRunner == null) {
			rulesMachineRunner = Main.getInjector().getInstance(RulesMachineRunner.class);
		}
		return rulesMachineRunner;
	}

	public CliVersionNumber getVersion() {
		return this.cliVersion;
	}

	public ExecuteRun createRulesMachineExecuteRun(RulesProject rulesProject, File mainMachineFile,
			Map<String, String> proBCorePreferences, boolean continueAfterErrors, ReusableAnimator animator) {
		ExtractedModel<RulesModel> extract = this.rulesFactory.extract(mainMachineFile, rulesProject);
		return new ExecuteRun(extract, proBCorePreferences, continueAfterErrors, animator != null ? animator : this.animatorProvider.get());
	}

}
