package de.prob.model.brules;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.exception.ProBError;
import de.prob.model.classicalb.ClassicalBMachine;
import de.prob.scripting.ExtractedModel;
import de.prob.scripting.ModelFactory;

public class RulesModelFactory implements ModelFactory<RulesModel> {

	private final Provider<RulesModel> modelCreator;

	@Inject
	public RulesModelFactory(final Provider<RulesModel> modelCreator) {
		this.modelCreator = modelCreator;
	}

	public ExtractedModel<RulesModel> extract(File runnerFile, RulesProject rulesProject) {
		RulesModel rulesModel = modelCreator.get();

		rulesModel = rulesModel.create(runnerFile, rulesProject);
		return new ExtractedModel<>(rulesModel,
				new ClassicalBMachine(rulesProject.getBModels().get(0).getMachineName()));
	}
	
	@Override
	public ExtractedModel<RulesModel> extract(final String fileName) throws IOException {
		final File file = new File(fileName);
		final RulesProject rulesProject = new RulesProject();
		final ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setAddLineNumbers(true);
		rulesProject.setParsingBehaviour(parsingBehaviour);
		rulesProject.parseProject(file);
		rulesProject.checkAndTranslateProject();
		if (rulesProject.hasErrors()) {
			throw new ProBError(new BCompoundException(rulesProject.getBExceptionList()));
		}
		return this.extract(file, rulesProject);
	}
}
