package de.prob.model.eventb.translate;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.io.MoreFiles;

import de.prob.animator.domainobjects.EventB;
import de.prob.model.eventb.Context;
import de.prob.model.eventb.Event;
import de.prob.model.eventb.EventBAction;
import de.prob.model.eventb.EventBAxiom;
import de.prob.model.eventb.EventBConstant;
import de.prob.model.eventb.EventBGuard;
import de.prob.model.eventb.EventBInvariant;
import de.prob.model.eventb.EventBMachine;
import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.EventBVariable;
import de.prob.model.eventb.EventParameter;
import de.prob.model.eventb.Variant;
import de.prob.model.eventb.Witness;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.DependencyGraph.ERefType;
import de.prob.model.representation.ModelElementList;
import de.prob.scripting.EventBFactory;

import org.eventb.core.ast.extension.IFormulaExtension;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MachineXmlHandler extends DefaultHandler {
	private EventBModel model;
	private final Set<IFormulaExtension> typeEnv;
	private EventBMachine machine;
	private final List<String> seesNames = new ArrayList<>();
	private final String directoryPath;

	private final List<Context> sees = new ArrayList<>();
	private final List<EventBMachine> refines = new ArrayList<>();
	private final List<EventBInvariant> invariants = new ArrayList<>();
	private final List<EventBVariable> variables = new ArrayList<>();
	private final List<Event> events = new ArrayList<>();
	private final List<Variant> variant = new ArrayList<>();

	// For extracting internal contexts
	private Context internalContext;
	private List<Context> extendedContexts;
	private List<de.prob.model.representation.Set> sets;
	private List<EventBAxiom> axioms;
	private List<EventBConstant> constants;
	private boolean extractingContext = false;

	// For extracting events
	private Event event;
	private List<Event> refinesForEvent;
	private List<EventBAction> actions;
	private List<EventBGuard> guards;
	private List<EventParameter> parameters;
	private List<Witness> witnesses;
	private boolean extractingEvent = false;

	private final Map<String, Map<String, EventBAxiom>> axiomCache = new HashMap<>();
	private final Map<String, Map<String, EventBInvariant>> invariantCache = new HashMap<>();
	private final Map<String, Map<String, Event>> eventCache = new HashMap<>();

	public MachineXmlHandler(EventBModel model, final String fileName, final Set<IFormulaExtension> typeEnv) {
		this.model = model;
		this.typeEnv = typeEnv;

		Path path = Paths.get(fileName);
		String name = MoreFiles.getNameWithoutExtension(path);
		directoryPath = path.getParent().toString();
		machine = new EventBMachine(name);

		axiomCache.put(name, new HashMap<>());
		invariantCache.put(name, new HashMap<>());
		eventCache.put(name, new HashMap<>());
	}





	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		// String name;
		switch (qName) {
			case "org.eventb.core.scRefinesMachine":
				addRefinedMachine(attributes);
				break;
			case "org.eventb.core.scSeesContext":
				addSeesContext(attributes);
				break;
			case "org.eventb.core.scInternalContext":
				beginContextExtraction(attributes);
				break;
			case "org.eventb.core.scExtendsContext":
				if (extractingContext) {
					addExtendedContext(attributes);
				}
				break;
			case "org.eventb.core.scAxiom":
				if (extractingContext) {
					addAxiom(attributes);
				}
				break;
			case "org.eventb.core.scConstant":
				if (extractingContext) {
					addConstant(attributes);
				}
				break;
			case "org.eventb.core.scCarrierSet":
				if (extractingContext) {
					addSet(attributes);
				}
				break;
			case "org.eventb.core.scInvariant":
				addInvariant(attributes);
				break;
			case "org.eventb.core.scVariable":
				addVariable(attributes);
				break;
			case "org.eventb.core.scVariant":
				addVariant(attributes);
				break;
			case "org.eventb.core.scEvent":
				// name = attributes.getValue("org.eventb.core.scName");
				beginEventExtraction(attributes);
				break;
			case "org.eventb.core.scAction":
				if (extractingEvent) {
					addAction(attributes);
				}
				break;
			case "org.eventb.core.scGuard":
				if (extractingEvent) {
					addGuard(attributes);
				}
				break;
			case "org.eventb.core.scParameter":
				if (extractingEvent) {
					addEventParameter(attributes);
				}
				break;
			case "org.eventb.core.scRefinesEvent":
				if (extractingEvent) {
					addRefinedEvent(attributes);
				}
				break;
			case "org.eventb.core.scWitness":
				if (extractingEvent) {
					addWitness(attributes);
				}
				break;
		}
	}



	private void addWitness(final Attributes attributes) {
		String name = attributes.getValue("org.eventb.core.label");
		String predicate = attributes.getValue("org.eventb.core.predicate");
		witnesses.add(new Witness(name, predicate, typeEnv));
	}

	private void addRefinedEvent(final Attributes attributes) {

		String target = attributes.getValue("org.eventb.core.scTarget");
		String internalName = target.substring(target.lastIndexOf('#') + 1);

		if (internalName.endsWith("\\\\")) {
			internalName = internalName.substring(0, internalName.length() - 1);
		} else {
			internalName = internalName.replace("\\", "");
		}

		String fileSource = target.substring(0, target.indexOf('|'));

		String refinedMachineName = fileSource.substring(fileSource.lastIndexOf('/') + 1, fileSource.lastIndexOf('.'));
		refinesForEvent.add(eventCache.get(refinedMachineName).get(internalName));
	}

	private void addEventParameter(final Attributes attributes) {
		String name = attributes.getValue("name");
		parameters.add(new EventParameter(name));
	}

	private void addGuard(final Attributes attributes) {
		String name = attributes.getValue("org.eventb.core.label");
		String predicate = attributes.getValue("org.eventb.core.predicate");
		boolean theorem = "true".equals(attributes
				.getValue("org.eventb.core.theorem"));
		guards.add(new EventBGuard(name, predicate, theorem, typeEnv));
	}

	private void addAction(final Attributes attributes) {
		String name = attributes.getValue("org.eventb.core.label");
		String assignment = attributes.getValue("org.eventb.core.assignment");
		actions.add(new EventBAction(name, assignment, typeEnv));
	}

	private void beginEventExtraction(final Attributes attributes) {
		String crazyRodinInternalName = attributes.getValue("name");
		String name = attributes.getValue("org.eventb.core.label");
		String convergence = attributes.getValue("org.eventb.core.convergence");
		String extended = attributes.getValue("org.eventb.core.extended");

		Event.EventType eventType;
		if ("0".equals(convergence)) {
			eventType = Event.EventType.ORDINARY;
		} else if ("1".equals(convergence)) {
			eventType = Event.EventType.CONVERGENT;
		} else {
			eventType = Event.EventType.ANTICIPATED;
		}


		if(Boolean.parseBoolean(extended)){
			event = new Event(name, eventType, Event.Inheritance.EXTENDS);
		}else{
			event = new Event(name, eventType, Event.Inheritance.NONE);
		}


		eventCache.get(machine.getName()).put(crazyRodinInternalName, event);

		extractingEvent = true;

		refinesForEvent = new ArrayList<>();
		guards = new ArrayList<>();
		actions = new ArrayList<>();
		witnesses = new ArrayList<>();
		parameters = new ArrayList<>();
	}

	private void addVariant(final Attributes attributes) {
		String expression = attributes.getValue("org.eventb.core.expression");
		variant.add(new Variant(expression, typeEnv));
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) throws SAXException {

		if (extractingContext
				&& "org.eventb.core.scInternalContext".equals(qName)) {
			endContextExtraction();
		}
		if (extractingEvent && "org.eventb.core.scEvent".equals(qName)) {
			endEventExtraction();
		}
	}

	private void endEventExtraction() {
		event = event.withActions(new ModelElementList<>(actions));
		event = event.withGuards(new ModelElementList<>(guards));
		event = event.withParameters(new ModelElementList<>(parameters));
		event = event.withParentEvent(refinesForEvent.isEmpty() ? null : refinesForEvent.get(0)); // TODO Throw an error if more than one event is refined
		event = event.withWitnesses(new ModelElementList<>(witnesses));

		events.add(event);
		extractingEvent = false;
	}

	private void addVariable(final Attributes attributes) {
		String name = attributes.getValue("name");
		boolean concrete = "true".equals(attributes
				.getValue("org.eventb.core.concrete"));
		String unit = attributes.getValue("de.prob.units.unitPragmaAttribute");
		if (concrete) {
			variables.add(new EventBVariable(name, unit));
		}
	}

	private void addRefinedMachine(final Attributes attributes) throws SAXException {
		String target = attributes.getValue("org.eventb.core.scTarget");
		String machineName = target.substring(target.lastIndexOf('/') + 1,
				target.lastIndexOf('.'));

		model = model.addRelationship(machine.getName(), machineName, ERefType.REFINES);

		AbstractElement component = model.getComponent(machineName);
		if (component != null) {
			EventBMachine mch = (EventBMachine) component;
			refines.add(mch);
		} else {
			try {
				SAXParserFactory parserFactory = SAXParserFactory.newInstance();
				parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				SAXParser saxParser = parserFactory.newSAXParser();

				String fileName = directoryPath + File.separatorChar
						+ machineName + "." + EventBFactory.CHECKED_RODIN_MACHINE_EXTENSION;
				MachineXmlHandler handler = new MachineXmlHandler(model,
						fileName, typeEnv);
				saxParser.parse(new File(fileName), handler);

				axiomCache.putAll(handler.getAxiomCache());
				invariantCache.putAll(handler.getInvariantCache());
				eventCache.putAll(handler.getEventCache());

				refines.add(handler.getMachine());

				model = handler.getModel();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (ParserConfigurationException e) {
				throw new SAXException(e);
			}
		}
	}

	private void addInvariant(final Attributes attributes) {
		String source = attributes.getValue("org.eventb.core.source");
		String internalName = source.substring(source.lastIndexOf('#') + 1);
		internalName = internalName.replace("\\", "");
		String filePath = source.substring(0, source.indexOf('|'));
		String machineName = filePath.substring(filePath.lastIndexOf('/') + 1,
				filePath.lastIndexOf('.'));
		if (machineName.equals(machine.getName())) {
			String label = attributes.getValue("org.eventb.core.label");
			String predicate = attributes.getValue("org.eventb.core.predicate");
			boolean theorem = "true".equals(attributes.getValue("org.eventb.core.theorem"));
			EventBInvariant inv = new EventBInvariant(label, predicate,
					theorem, typeEnv);
			invariants.add(inv);
			invariantCache.get(machine.getName()).put(internalName, inv);
		}
	}

	private void addSet(final Attributes attributes) {
		String name = attributes.getValue("name");
		sets.add(new de.prob.model.representation.Set(new EventB(name)));
	}

	private void addConstant(final Attributes attributes) {
		String name = attributes.getValue("name");
		boolean symbolic = "true".equals(attributes
				.getValue("de.prob.symbolic.symbolicAttribute"));
		String unit = attributes.getValue("de.prob.units.unitPragmaAttribute");
		constants.add(new EventBConstant(name, symbolic, unit));
	}

	private void addExtendedContext(final Attributes attributes) {
		String source = attributes.getValue("org.eventb.core.scTarget");
		String contextName = source.substring(source.lastIndexOf('#') + 1);

		model.addRelationship(internalContext.getName(), contextName,
				ERefType.EXTENDS);

		Context extended = (Context) model.getComponent(contextName);
		extendedContexts.add(extended);

	}

	private void addAxiom(final Attributes attributes) {
		String source = attributes.getValue("org.eventb.core.source");
		String internalName = source.substring(source.lastIndexOf('#') + 1);
		internalName = internalName.replace("\\", "");
		String filePath = source.substring(0, source.indexOf('|'));
		String contextName = filePath.substring(filePath.lastIndexOf('/') + 1,
				filePath.lastIndexOf('.'));
		if (contextName.equals(internalContext.getName())) {
			String label = attributes.getValue("org.eventb.core.label");
			String predicate = attributes.getValue("org.eventb.core.predicate");
			boolean theorem = "true".equals(attributes.getValue("org.eventb.core.theorem"));
			EventBAxiom axiom = new EventBAxiom(label, predicate, theorem,
					typeEnv);
			axioms.add(axiom);
			axiomCache.get(internalContext.getName()).put(internalName, axiom);
		}
	}

	private void addSeesContext(final Attributes attributes) {
		String target = attributes.getValue("org.eventb.core.scTarget");
		String contextName = target.substring(target.lastIndexOf('/') + 1,
				target.lastIndexOf('.'));

		model = model.addRelationship(machine.getName(), contextName, ERefType.SEES);

		seesNames.add(contextName);

		AbstractElement context = model.getComponent(contextName);
		if (context != null) {
			sees.add((Context) context);
		}
	}

	private void beginContextExtraction(final Attributes attributes) {
		String name = attributes.getValue("name");
		if (model.getComponent(name) != null) {
			extractingContext = false;
			return;
		}
		extractingContext = true;

		internalContext = new Context(name);

		axiomCache.put(name, new HashMap<>());

		extendedContexts = new ArrayList<>();
		axioms = new ArrayList<>();
		sets = new ArrayList<>();
		constants = new ArrayList<>();
	}

	private void endContextExtraction() throws SAXException {
		internalContext = internalContext.withAxioms(new ModelElementList<>(axioms));
		internalContext = internalContext.withConstants(new ModelElementList<>(constants));
		internalContext = internalContext.withExtends(new ModelElementList<>(extendedContexts));
		internalContext = internalContext.withSets(new ModelElementList<>(sets));

		ProofExtractor extractor = new ProofExtractor(internalContext,
				directoryPath + File.separatorChar + internalContext.getName());
		internalContext = internalContext.withProofs(extractor.getProofs());

		model = model.addContext(internalContext);
		if (seesNames.contains(internalContext.getName())) {
			sees.add(internalContext);
		}
		extractingContext = false;

	}

	@Override
	public void endDocument() throws SAXException {
		machine = machine.withInvariants(new ModelElementList<>(invariants));
		machine = machine.withRefinesMachine(refines.isEmpty() ? null : refines.get(0)); // TODO Throw an error if more than one machine is refined?
		machine = machine.withSees(new ModelElementList<>(sees));
		machine = machine.withVariables(new ModelElementList<>(variables));
		machine = machine.withVariant(variant.isEmpty() ? null : variant.get(0)); // TODO Throw an error if there is more than one variant?
		machine = machine.withEvents(new ModelElementList<>(events));

		ProofExtractor proofExtractor = new ProofExtractor(machine,
				directoryPath + File.separatorChar + machine.getName());
		machine = machine.withProofs(proofExtractor.getProofs());
		model = model.addMachine(machine).withMainComponent(machine);

	}

	public Map<String, Map<String, EventBAxiom>> getAxiomCache() {
		return axiomCache;
	}

	public Map<String, Map<String, EventBInvariant>> getInvariantCache() {
		return invariantCache;
	}

	public Map<String, Map<String, Event>> getEventCache() {
		return eventCache;
	}

	public EventBMachine getMachine() {
		return machine;
	}

	public EventBModel getModel() {
		return model;
	}
}
