package de.prob.model.brules;

import de.prob.animator.command.GetVersionCommand;
import de.prob.statespace.Trace;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RuleValidationReport {

	public static void reportVelocity(final Trace trace, final Path path, final Locale locale) throws IOException {
		String templatePath = String.format("de/prob/model/brules/validation_report_%s.html.vm", locale.getLanguage());
		initVelocityEngine();
		VelocityContext context = getVelocityContext(trace);
		try (final Writer writer = Files.newBufferedWriter(path)) {
			Velocity.mergeTemplate(templatePath, String.valueOf(StandardCharsets.UTF_8), context, writer);
		}
	}

	private static void initVelocityEngine() {
		Properties p = new Properties();
		p.setProperty("resource.loaders", "class");
		p.setProperty("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);
	}

	private static VelocityContext getVelocityContext(final Trace trace) {
		RuleResults ruleResults = new RuleResults(((RulesModel) trace.getModel()).getRulesProject(),
			trace.getCurrentState(), -1);
		RuleResults.ResultSummary resultSummary = ruleResults.getSummary();

		Map<String, List<RuleResult>> sortedClassificationRuleResults = ruleResults.getRuleResultsForClassifications();
		for (List<RuleResult> ruleResult : sortedClassificationRuleResults.values()) {
			ruleResult.sort(Comparator.comparing(RuleResult::getRuleName));
		}

		List<RuleResult> sortedRuleResults = ruleResults.getRuleResultsWithoutClassification();
		sortedRuleResults.sort(Comparator.comparing(RuleResult::getRuleName));

		GetVersionCommand versionCommand = new GetVersionCommand();
		trace.getStateSpace().execute(versionCommand);

		VelocityContext context = new VelocityContext();
		context.put("machineName", trace.getModel().getModelFile().getName());
		context.put("numberOfRules", resultSummary.numberOfRules);
		context.put("rulesChecked", resultSummary.numberOfRules - resultSummary.numberOfRulesNotChecked);
		context.put("rulesSucceeded", resultSummary.numberOfRulesSucceeded);
		context.put("rulesFailed", resultSummary.numberOfRulesFailed);
		context.put("rulesDisabled", resultSummary.numberOfRulesDisabled);
		context.put("classificationMap", sortedClassificationRuleResults);
		context.put("Collectors", Collectors.class);
		context.put("noClassification", sortedRuleResults);
		context.put("status_SUCCESS", RuleStatus.SUCCESS);
		context.put("status_FAIL", RuleStatus.FAIL);
		context.put("status_NOT_CHECKED", RuleStatus.NOT_CHECKED);
		context.put("status_DISABLED", RuleStatus.DISABLED);
		context.put("probCliVersion", versionCommand.getVersion().toString());
		context.put("localDateTime", LocalDateTime.now().withNano(0));
		return context;
	}
}
