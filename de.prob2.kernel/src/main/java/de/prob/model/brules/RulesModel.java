package de.prob.model.brules;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.krukow.clj_lang.PersistentHashMap;
import com.google.inject.Inject;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadRulesProjectCommand;
import de.prob.animator.domainobjects.ClassicalB;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.DependencyGraph;
import de.prob.model.representation.ModelElementList;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;

public class RulesModel extends AbstractModel {

	private RulesProject project;
	private Map<AbstractOperation, IEvalElement> operationStateFormulaCache = new HashMap<>();

	@Inject
	public RulesModel(StateSpaceProvider stateSpaceProvider) {
		super(stateSpaceProvider,
				PersistentHashMap
						.<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>>emptyMap(),
				new DependencyGraph(), null);
	}

	public RulesModel(StateSpaceProvider stateSpaceProvider, File file, RulesProject project) {
		super(stateSpaceProvider,
				PersistentHashMap
						.<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>>emptyMap(),
				new DependencyGraph(), file);
		this.project = project;
	}

	@Override
	public AbstractElement getComponent(String name) {
		throw new AssertionError();
	}

	public RulesModel create(File file, RulesProject project) {
		return new RulesModel(stateSpaceProvider, file, project);
	}

	@Override
	public IEvalElement parseFormula(String formula, FormulaExpand expand) {
		try {
			return new ClassicalB(new BParser().parseFormula(formula), expand);
		} catch (BCompoundException e) {
			throw new EvaluationException(e.getMessage(), e.getFirstException());
		}
	}

	@Override
	public <T extends AbstractElement> ModelElementList<T> getChildrenOfType(final Class<T> c) {
		return new ModelElementList<>();
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.B;
	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadRulesProjectCommand(project, modelFile);
	}

	public RulesProject getRulesProject() {
		return this.project;
	}

	public IEvalElement getEvalElement(AbstractOperation abstractOperation) {
		if (operationStateFormulaCache.containsKey(abstractOperation)) {
			return operationStateFormulaCache.get(abstractOperation);
		} else {
			String name = abstractOperation.getName();
			IEvalElement evalElement = this.parseFormula(name, FormulaExpand.EXPAND);
			operationStateFormulaCache.put(abstractOperation, evalElement);
			return evalElement;
		}
	}
}
