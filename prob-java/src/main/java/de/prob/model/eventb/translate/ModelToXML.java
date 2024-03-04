package de.prob.model.eventb.translate;

import de.prob.animator.domainobjects.EventB;
import de.prob.model.eventb.Context;
import de.prob.model.eventb.Event;
import de.prob.model.eventb.EventBMachine;
import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.theory.Theory;
import de.prob.model.representation.ModelElementList;
import de.prob.scripting.EventBFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public final class ModelToXML {

	private int ctr = 0;

	private static void writeXml(Path path, Consumer<Document> consumer) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		consumer.accept(document);

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		// TODO: standalone has no effect?
		// transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		Files.createDirectories(path.getParent());
		try (BufferedWriter w = Files.newBufferedWriter(path)) {
			transformer.transform(new DOMSource(document), new StreamResult(w));
		}
	}

	private static void child(Node node, String tagName) {
		child(node, tagName, Collections.emptyMap());
	}

	private static void child(Node node, String tagName, String content) {
		child(node, tagName, child -> child.setTextContent(content));
	}

	private static void child(Node node, String tagName, Map<String, String> attributes) {
		child(node, tagName, attributes, child -> {});
	}

	private static void child(Node node, String tagName, Consumer<? super Element> childBuilder) {
		child(node, tagName, Collections.emptyMap(), childBuilder);
	}

	private static void child(Node node, String tagName, Map<String, String> attributes, Consumer<? super Element> childBuilder) {
		Document document = node instanceof Document ? (Document) node : node.getOwnerDocument();
		Element child = document.createElement(tagName);
		attributes.forEach(child::setAttribute);
		childBuilder.accept(child);
		node.appendChild(child);
	}

	private static Map<String, String> attrs(Object... kvs) {
		Map<String, String> attrs = new HashMap<>();
		for (int i = 0; i < kvs.length; i += 2) {
			Object key = Objects.requireNonNull(kvs[i], "key");
			Object value = kvs[i + 1];
			if (value != null) {
				attrs.put(key.toString(), value.toString());
			}
		}
		return attrs;
	}

	private String genName() {
		return "n" + this.ctr++;
	}

	public File writeToRodin(EventBModel model, String name, String path) {
		Objects.requireNonNull(model, "model");
		Path dir = Paths.get(path, name);
		try {
			Files.createDirectories(dir);
			createProjectFile(name, dir);
			extractTheories(model.getTheories(), dir);
			for (EventBMachine m : model.getMachines()) {
				extractMachine(m, dir);
			}
			for (Context c : model.getContexts()) {
				extractContext(c, dir);
			}
			return dir.toFile();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createProjectFile(String modelName, Path dir) throws Exception {
		writeXml(dir.resolve(".project"), document -> child(document, "projectDescription", projectDescription -> {
			child(projectDescription, "name", modelName);
			child(projectDescription, "comment");
			child(projectDescription, "projects");
			child(projectDescription, "buildSpec", buildSpec -> child(buildSpec, "buildCommand", buildCommand -> {
				child(buildCommand, "name", "org.rodinp.core.rodinbuilder");
				child(buildCommand, "arguments");
			}));
			child(projectDescription, "natures", natures -> child(natures, "nature", "org.rodinp.core.rodinnature"));
		}));
	}

	private void extractTheories(ModelElementList<Theory> theories, Path dir) throws Exception {
		if (theories.isEmpty()) {
			return;
		}

		Map<String, List<String>> collected = new HashMap<>();
		for (Theory t : theories) {
			collected.computeIfAbsent(t.getParentDirectoryName(), k -> new ArrayList<>()).add(t.getName());
		}

		writeXml(dir.resolve("TheoryPath.tul"), document -> child(document, "org.eventb.theory.core.theoryLanguageRoot",
			attrs("org.eventb.core.configuration", "org.eventb.theory.core.tul"),
			root -> collected.forEach((parentDir, theoryNames) -> {
				String absoluteDir = File.separator + parentDir;
				child(root, "org.eventb.theory.core.availableTheoryProject",
					attrs("name", genName(), "org.eventb.theory.core.availableTheoryProject", absoluteDir),
					project -> theoryNames.forEach(name -> {
						String expandedName = absoluteDir + File.separator + name + ".dtf|org.eventb.theory.core.deployedTheoryRoot#" + name;
						child(project, "org.eventb.theory.core.availableTheory",
							attrs("name", genName(), "org.eventb.theory.core.availableTheory", expandedName));
					}));
			})));
	}

	private void extractMachine(EventBMachine machine, Path dir) throws Exception {
		writeXml(dir.resolve(machine.getName() + "." + EventBFactory.RODIN_MACHINE_EXTENSION), document -> {
			child(document, "org.eventb.core.machineFile",
				attrs("org.eventb.core.configuration", "org.eventb.core.fwd", "version", 5, "org.eventb.core.comment", machine.getComment()),
				machineFile -> {
					machine.getSees().forEach(it -> child(machineFile, "org.eventb.core.seesContext", attrs("name", genName(), "org.eventb.core.target", it.getName())));
					Optional.ofNullable(machine.getRefinesMachine()).ifPresent(it -> child(machineFile, "org.eventb.core.refinesMachine", attrs("name", genName(), "org.eventb.core.target", it.getName())));
					machine.getVariables().forEach(it -> child(machineFile, "org.eventb.core.variable", attrs("name", genName(), "org.eventb.core.identifier", it.getName())));
					Optional.ofNullable(machine.getVariant()).ifPresent(it -> child(machineFile, "org.eventb.core.variant", attrs("name", genName(), "org.eventb.core.expression", ((EventB) it.getExpression()).toUnicode())));
					machine.getInvariants().forEach(it -> child(machineFile, "org.eventb.core.invariant",
						attrs("name", genName(),
							"org.eventb.core.label", it.getName(),
							"org.eventb.core.predicate", ((EventB) it.getPredicate()).toUnicode(),
							"org.eventb.core.theorem", it.isTheorem(),
							"org.eventb.core.comment", it.getComment())));
					machine.getEvents().forEach(it -> extractEvent(it, machineFile));
				});
		});
	}

	private void extractEvent(Event event, Element machineFile) {
		int convergence = event.getType() == Event.EventType.ORDINARY ? 0
			: event.getType() == Event.EventType.CONVERGENT ? 1
			: 2;
		boolean extended = event.getInheritance() == Event.Inheritance.EXTENDS;
		child(machineFile, "org.eventb.core.event",
			attrs("name", genName(),
				"org.eventb.core.convergence", convergence,
				"org.eventb.core.extended", extended,
				"org.eventb.core.label", event.getName(),
				"org.eventb.core.comment", event.getComment()),
			eventElement -> {
				if (!"INITIALISATION".equals(event.getName()) && event.getParentEvent() != null) {
					child(eventElement, "org.eventb.core.refinesEvent", attrs("name", genName(), "org.eventb.core.target", event.getParentEvent().getName()));
				}
				event.getParameters().forEach(it -> child(eventElement, "org.eventb.core.parameter", attrs("name", genName(), "org.eventb.core.identifier", it.getName(), "org.eventb.core.comment", it.getComment())));
				event.getGuards().forEach(it -> child(eventElement, "org.eventb.core.guard", attrs("name", genName(), "org.eventb.core.label", it.getName(), "org.eventb.core.predicate", ((EventB) it.getPredicate()).toUnicode(), "org.eventb.core.theorem", it.isTheorem(), "org.eventb.core.comment", it.getComment())));
				event.getWitnesses().forEach(it -> child(eventElement, "org.eventb.core.witness", attrs("name", genName(), "org.eventb.core.label", it.getName(), "org.eventb.core.predicate", it.getPredicate().toUnicode(), "org.eventb.core.comment", it.getComment())));
				event.getActions().forEach(it -> child(eventElement, "org.eventb.core.action", attrs("name", genName(), "org.eventb.core.assignment", ((EventB) it.getCode()).toUnicode(), "org.eventb.core.label", it.getName(), "org.eventb.core.comment", it.getComment())));
			});
	}

	private void extractContext(Context context, Path dir) throws Exception {
		writeXml(dir.resolve(context.getName() + "." + EventBFactory.RODIN_CONTEXT_EXTENSION), document ->
			child(document, "org.eventb.core.contextFile",
				attrs("org.eventb.core.configuration", "org.eventb.core.fwd", "version", 3, "org.eventb.core.comment", context.getComment()),
				contextFile -> {
					context.getExtends().forEach(it -> child(contextFile, "org.eventb.core.extendsContext", attrs("name", genName(), "org.eventb.core.target", it.getName())));
					context.getSets().forEach(it -> child(contextFile, "org.eventb.core.carrierSet", attrs("name", genName(), "org.eventb.core.identifier", it.getName(), "org.eventb.core.comment", it.getComment())));
					context.getConstants().forEach(it -> child(contextFile, "org.eventb.core.constant", attrs("name", genName(), "org.eventb.core.identifier", it.getName(), "org.eventb.core.comment", it.getComment())));
					context.getAxioms().forEach(it -> child(contextFile, "org.eventb.core.axiom", attrs("name", genName(), "org.eventb.core.label", it.getName(), "org.eventb.core.predicate", ((EventB) it.getPredicate()).toUnicode(), "org.eventb.core.theorem", it.isTheorem(), "org.eventb.core.comment", it.getComment())));
				}));
	}
}
