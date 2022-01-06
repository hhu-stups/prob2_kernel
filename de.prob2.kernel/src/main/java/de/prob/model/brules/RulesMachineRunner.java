package de.prob.model.brules;

import java.io.File;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.animator.ReusableAnimator;
import de.prob.scripting.ExtractedModel;

@Singleton
public class RulesMachineRunner {
	private final Provider<ReusableAnimator> animatorProvider;
	private final RulesModelFactory rulesFactory;

	@Inject
	public RulesMachineRunner(Provider<ReusableAnimator> animatorProvider, RulesModelFactory rulesFactory) {
		this.animatorProvider = animatorProvider;
		this.rulesFactory = rulesFactory;
	}

	public ExecuteRun createRulesMachineExecuteRun(RulesProject rulesProject, File mainMachineFile,
			Map<String, String> proBCorePreferences, boolean continueAfterErrors, ReusableAnimator animator) {
		ExtractedModel<RulesModel> extract = this.rulesFactory.extract(mainMachineFile, rulesProject);
		return new ExecuteRun(extract, proBCorePreferences, continueAfterErrors, animator != null ? animator : this.animatorProvider.get());
	}

}
