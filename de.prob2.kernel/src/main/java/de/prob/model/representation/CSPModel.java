package de.prob.model.representation;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadCSPCommand;
import de.prob.animator.domainobjects.CSP;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.annotations.Home;
import de.prob.cli.OsSpecificInfo;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.Language;

public class CSPModel extends AbstractModel {
	private final Path cspmfPath;
	private final String content;
	private final CSPElement mainComponent;
	
	@Inject
	public CSPModel(final StateSpaceProvider ssProvider, final OsSpecificInfo osInfo, final @Home Path proBHome) {
		this(ssProvider, proBHome.resolve(osInfo.getCspmfName()));
	}

	private CSPModel(final StateSpaceProvider ssProvider, final Path cspmfPath) {
		this(ssProvider, cspmfPath, null, null, null);
	}

	public CSPModel(final StateSpaceProvider ssProvider, final Path cspmfPath, String content, File modelFile, CSPElement mainComponent) {
		super(ssProvider, Collections.emptyMap(), new DependencyGraph(), modelFile);
		this.cspmfPath = cspmfPath;
		this.content = content;
		this.mainComponent = mainComponent;
	}

	public CSPModel create(final String content, final File modelFile) {
		return new CSPModel(getStateSpaceProvider(), this.getCspmfPath(), content, modelFile, new CSPElement(modelFile.getName()));
	}

	/**
	 * DO NOT CALL THIS METHOD - for internal use only by {@link CSP}.
	 * 
	 * @return path to the {@code cspmf} binary to use when parsing formulas
	 */
	public Path getCspmfPath() {
		return this.cspmfPath;
	}

	public String getContent() {
		return content;
	}

	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new CSP(formula, this);
	}

	@Override
	public IEvalElement formulaFromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		// TODO This only handles syntactically valid identifiers and not arbitrary strings
		return this.parseFormula(String.join(".", identifier), expansion);
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.CSP;
	}

	@Override
	public Language getLanguage() {
		return Language.CSP;
	}

	@Override
	public boolean checkSyntax(final String formula) {
		try {
			CSP element = (CSP) parseFormula(formula, FormulaExpand.TRUNCATE);
			element.printProlog(new PrologTermStringOutput());

			return true;
		} catch (EvaluationException e) {
			return false;
		}

	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadCSPCommand(getModelFile().getAbsolutePath());
	}

	@Override
	public AbstractElement getComponent(String name) {
		if (mainComponent != null && name != null && name.equals(mainComponent.getName())) {
			return mainComponent;
		}

		return null;
	}

	@Override
	public Object getProperty(String name) {
		AbstractElement component = getComponent(name);
		return component != null ? component : super.getProperty(name);
	}

	public Object getAt(String name) {
		return getComponent(name);
	}
}
