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
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class EventBModel extends AbstractModel {
	@Inject
	public EventBModel(final StateSpaceProvider stateSpaceProvider) {
		this(stateSpaceProvider, Collections.emptyMap(), new DependencyGraph(), null);
	}

	private EventBModel(StateSpaceProvider stateSpaceProvider, Map<Class<? extends AbstractElement>, ModelElementList<? extends AbstractElement>> children, DependencyGraph graph, File modelFile) {
		super(stateSpaceProvider, children, graph, modelFile);
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
		return new EventBModel(getStateSpaceProvider(), getChildren(), getGraph(), modelFile);
	}

	public ModelElementList<Theory> getTheories() {
		return this.getChildrenOfType(Theory.class);
	}

	public EventBModel withTheories(final ModelElementList<Theory> theories) {
		return this.set(Theory.class, theories);
	}

	@Override
	public IEvalElement parseFormula(final String formula, final FormulaExpand expand) {
		return new EventB(formula, Collections.emptySet(), expand);
	}

	@Override
	public IEvalElement formulaFromIdentifier(final List<String> identifier, final FormulaExpand expansion) {
		return EventB.fromIdentifier(identifier, expansion);
	}

	public EventBModel set(Class<? extends AbstractElement> clazz, ModelElementList<? extends AbstractElement> elements) {
		return new EventBModel(getStateSpaceProvider(), assoc(clazz, elements), getGraph(), getModelFile());
	}

	public <T extends AbstractElement> EventBModel addTo(Class<T> clazz, T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBModel(getStateSpaceProvider(), assoc(clazz, list.addElement(element)), getGraph(), getModelFile());
	}

	public <T extends AbstractElement> EventBModel removeFrom(Class<T> clazz, T element) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBModel(getStateSpaceProvider(), assoc(clazz, list.removeElement(element)), getGraph(), getModelFile());
	}

	public <T extends AbstractElement> EventBModel replaceIn(Class<T> clazz, T oldElement, T newElement) {
		ModelElementList<T> list = getChildrenOfType(clazz);
		return new EventBModel(getStateSpaceProvider(), assoc(clazz, list.replaceElement(oldElement, newElement)), getGraph(), getModelFile());
	}

	public EventBModel addMachine(final EventBMachine machine) {
		return new EventBModel(getStateSpaceProvider(), assoc(Machine.class, getMachines().addElement(machine)), getGraph().addVertex(machine.getName()), getModelFile());
	}

	public EventBModel addContext(final Context context) {
		return new EventBModel(getStateSpaceProvider(), assoc(Context.class, getContexts().addElement(context)), getGraph().addVertex(context.getName()), getModelFile());
	}

	public EventBModel addRelationship(final String element1, final String element2, final DependencyGraph.ERefType relationship) {
		return new EventBModel(getStateSpaceProvider(), getChildren(), getGraph().addEdge(element1, element2, relationship), getModelFile());
	}

	public EventBModel removeRelationship(final String element1, final String element2, final DependencyGraph.ERefType relationship) {
		return new EventBModel(getStateSpaceProvider(), getChildren(), getGraph().removeEdge(element1, element2, relationship), getModelFile());
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
		return new EventBModel(getStateSpaceProvider(), getChildren(), graph, getModelFile());
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
			final EventB element = (EventB)parseFormula(formula, FormulaExpand.TRUNCATE);
			element.ensureParsed();
			return true;
		} catch (EvaluationException e) {
			return false;
		}
	}

	@Override
	public AbstractCommand getLoadCommand(final AbstractElement mainComponent) {
		return new LoadEventBProjectCommand(new EventBModelTranslator(this, mainComponent));
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
	public Object getProperty(String name) {
		final AbstractElement component = getComponent(name);
		if (component != null) {
			return component;
		} else {
			return super.getProperty(name);
		}
	}

	public AbstractElement getAt(String name) {
		return getComponent(name);
	}

	/**
	 * @return the most concrete machine
	 */
	public EventBMachine getTopLevelMachine(){
		return getMachines().get(getGraph().getStart());
	}

	/**
	 * @return all Events of the machine
	 */
	public ModelElementList<Event> getEventList(){
		return getTopLevelMachine().getChildrenOfType(Event.class);
	}

	/**
	 * Traces an event back to its origins and returns the original event, return event if it was never refined
	 * @param event the event to trace
	 * @return the origin event
	 */
	public static Event findEventOrigin(Event event){
		if (event.getRefinesEvent() == null) {
			return new Event("skip", Event.EventType.ORDINARY, true);
		} else {
			return event.getRefinesEvent();
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
		return getTopLevelMachine().getChildrenOfType(Event.class)
				.stream()
				.filter(entry -> !entry.isExtended())
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
