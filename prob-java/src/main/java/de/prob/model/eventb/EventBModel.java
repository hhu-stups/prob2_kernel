package de.prob.model.eventb;

import com.google.inject.Inject;
import de.prob.animator.command.AbstractCommand;
import de.prob.animator.command.LoadEventBProjectCommand;
import de.prob.animator.domainobjects.EvaluationException;
import de.prob.animator.domainobjects.EventB;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.model.eventb.theory.Theory;
import de.prob.model.eventb.translate.EventBModelTranslator;
import de.prob.model.representation.*;
import de.prob.scripting.StateSpaceProvider;
import de.prob.statespace.FormalismType;
import de.prob.statespace.Language;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eventb.core.ast.extension.IFormulaExtension;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class EventBModel extends AbstractModel {
	private final AbstractElement mainComponent;
	private final Set<IFormulaExtension> extensions;

	@Inject
	public EventBModel(final StateSpaceProvider stateSpaceProvider) {
		this(stateSpaceProvider, Collections.emptyMap(), new DependencyGraph(), null, null, Collections.emptySet());
	}

	EventBModel(
		StateSpaceProvider stateSpaceProvider,
		Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children,
		DependencyGraph graph,
		File modelFile,
		AbstractElement mainComponent,
		Set<IFormulaExtension> extensions
	) {
		super(stateSpaceProvider, children, graph, modelFile);
		this.mainComponent = mainComponent;
		this.extensions = extensions;
	}

	public ModelElementList<EventBMachine> getMachines() {
		return getChildrenAndCast(Machine.class, EventBMachine.class);
	}

	public EventBModel withMachines(final ModelElementList<EventBMachine> machines) {
		return this.set(Machine.class, machines);
	}

	public ModelElementList<Context> getContexts() {
		return getChildrenOfType(Context.class);
	}

	public EventBModel withContexts(final ModelElementList<Context> contexts) {
		return this.set(Context.class, contexts);
	}

	public EventBModel setModelFile(final File modelFile) {
		return new EventBModel(stateSpaceProvider, getChildren(), getGraph(), modelFile, getMainComponent(), getExtensions());
	}

	public EventBModel setMainComponent(AbstractElement mainComponent) {
		return new EventBModel(stateSpaceProvider, getChildren(), getGraph(), getModelFile(), mainComponent, getExtensions());
	}

	public Set<IFormulaExtension> getExtensions() {
		return this.extensions;
	}

	public EventBModel withExtensions(Set<IFormulaExtension> extensions) {
		return new EventBModel(stateSpaceProvider, getChildren(), getGraph(), getModelFile(), getMainComponent(), Collections.unmodifiableSet(new HashSet<>(extensions)));
	}

	public ModelElementList<Theory> getTheories() {
		return this.getChildrenOfType(Theory.class);
	}

	public EventBModel withTheories(final ModelElementList<Theory> theories) {
		return this.set(Theory.class, theories);
	}

	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new EventB(formula, getExtensions(), expand);
	}

	@Override
	public IEvalElement formulaFromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		return EventB.fromIdentifier(identifier, expansion);
	}

	public EventBModel set(Class<? extends AbstractElement> clazz, ModelElementList<? extends AbstractElement> elements) {
		return new EventBModel(stateSpaceProvider, assoc(clazz, elements), getGraph(), getModelFile(), getMainComponent(), getExtensions());
	}

	public <T extends AbstractElement> EventBModel addTo(Class<T> clazz, T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBModel(stateSpaceProvider, assoc(clazz, list.addElement(element)), getGraph(), getModelFile(), getMainComponent(), getExtensions());
	}

	public <T extends AbstractElement> EventBModel removeFrom(Class<T> clazz, T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBModel(stateSpaceProvider, assoc(clazz, list.removeElement(element)), getGraph(), getModelFile(), getMainComponent(), getExtensions());
	}

	public <T extends AbstractElement> EventBModel replaceIn(Class<T> clazz, T oldElement, T newElement) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBModel(stateSpaceProvider, assoc(clazz, list.replaceElement(oldElement, newElement)), getGraph(), getModelFile(), getMainComponent(), getExtensions());
	}

	public EventBModel addMachine(final EventBMachine machine) {
		return new EventBModel(stateSpaceProvider, assoc(Machine.class, getMachines().addElement(machine)), getGraph().addVertex(machine.getName()), getModelFile(), getMainComponent(), getExtensions());
	}

	public EventBModel addContext(final Context context) {
		return new EventBModel(stateSpaceProvider, assoc(Context.class, getContexts().addElement(context)), getGraph().addVertex(context.getName()), getModelFile(), getMainComponent(), getExtensions());
	}

	public EventBModel addRelationship(final String element1, final String element2, final DependencyGraph.ERefType relationship) {
		return new EventBModel(stateSpaceProvider, getChildren(), getGraph().addEdge(element1, element2, relationship), getModelFile(), getMainComponent(), getExtensions());
	}

	public EventBModel removeRelationship(final String element1, final String element2, final DependencyGraph.ERefType relationship) {
		return new EventBModel(stateSpaceProvider, getChildren(), getGraph().removeEdge(element1, element2, relationship), getModelFile(), getMainComponent(), getExtensions());
	}

	public EventBModel calculateDependencies() {
		DependencyGraph graph = new DependencyGraph();
		for (final EventBMachine m : getMachines()) {
			graph = graph.addVertex(m.getName());
			if (m.getRefinesMachine() != null) {
				graph = graph.addEdge(m.getName(), m.getRefinesMachine().getName(), DependencyGraph.ERefType.REFINES);
			}
			for (final Context c : m.getSees()) {
				graph = graph.addEdge(m.getName(), c.getName(), DependencyGraph.ERefType.SEES);
			}
		}
		for (final Context c : getContexts()) {
			graph = graph.addVertex(c.getName());
			for (final Context c2 : c.getExtends()) {
				graph = graph.addEdge(c.getName(), c2.getName(), DependencyGraph.ERefType.EXTENDS);
			}
		}
		return new EventBModel(stateSpaceProvider, getChildren(), graph, getModelFile(), getMainComponent(), getExtensions());
	}

	@Override
	public FormalismType getFormalismType() {
		return FormalismType.B;
	}

	@Override
	public Language getLanguage() {
		return Language.EVENT_B;
	}

	@Override
	public boolean checkSyntax(final String formula) {
		try {
			final EventB element = (EventB)parseFormula(formula);
			element.ensureParsed();
			return true;
		} catch (EvaluationException e) {
			return false;
		}
	}

	@Override
	public AbstractCommand getLoadCommand() {
		return new LoadEventBProjectCommand(new EventBModelTranslator(this));
	}

	@Override
	public AbstractElement getComponent(String name) {
		final AbstractElement e = getContexts().getElement(name);
		if (e == null) {
			return getMachines().getElement(name);
		} else {
			return e;
		}
	}

	public EventBMachine getMachine(String name) {
		return getMachines().getElement(name);
	}

	public Context getContext(String name) {
		return getContexts().getElement(name);
	}

	@Override
	public AbstractElement getMainComponent() {
		return this.mainComponent;
	}

	/**
	 * @deprecated Use {@link #getMainComponent()} instead.
	 *     The main component might be a {@link Context} and not an {@link EventBMachine}.
	 * @return the most concrete machine
	 */
	@Deprecated
	public EventBMachine getTopLevelMachine(){
		return getMachines().getElement(getGraph().getStart());
	}

	/**
	 * @return all Events of the machine
	 */
	public ModelElementList<Event> getEventList(){
		return this.getMainComponent().getChildrenOfType(Event.class);
	}

	/**
	 * Traces an event back to its origins and returns the original event, return event if it was never refined
	 * @param event the event to trace
	 * @return the origin event
	 */
	public static Event findEventOrigin(Event event){
		if (event.getParentEvent() == null) {
			return new Event("skip", Event.EventType.ORDINARY, Event.Inheritance.EXTENDS);
		} else {
			return event.getParentEvent();
		}
	}

	/**
	 * An event can be renamend. This method creates a map from the most recent name to the original one
	 * TODO can this cause problems in longer refinement chains?
	 * @return a map mapping current -&gt; original name
	 */
	public Map<String, String> pairNameChanges(){
		return getEventList().stream().collect(toMap(BEvent::getName, entry -> findEventOrigin(entry).getName()));
	}

	/**
	 * @return the events that were introduced by skip
	 */
	public List<String> introducedBySkip(){
		return pairNameChanges().entrySet().stream()
				.filter(s -> s.getValue().equals("skip"))
				.map(Map.Entry::getKey)
				.collect(toList());

	}

	/**
	 * @return the events that refine others
	 */
	public List<Event> refinedEvents(){
		return this.getMainComponent().getChildrenOfType(Event.class)
				.stream()
				.filter(entry -> entry.getInheritance() != Event.Inheritance.EXTENDS)
				.collect(toList());

	}

	/**
	 * @return the events that formally 'extend' others
	 */
	public List<String> extendEvents(){
		return refinedEvents().stream()
				.filter(event -> !event.getWitnesses().isEmpty())
				.map(BEvent::getName)
				.collect(toList());

	}

}
