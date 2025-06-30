package de.prob.model.eventb.translate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.prob.model.eventb.EventBModel;
import de.prob.model.eventb.theory.Theory;
import de.prob.model.representation.ModelElementList;

import org.eventb.core.ast.extension.IFormulaExtension;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TheoryXmlHandler extends DefaultHandler {
	private final String workspacePath;
	private final Set<IFormulaExtension> typeEnv = new HashSet<>();
	private EventBModel model;
	private ModelElementList<Theory> theories = new ModelElementList<>();
	private final HashMap<String, Theory> theoryMap = new HashMap<>();

	public TheoryXmlHandler(final EventBModel model, final String workspacePath) {
		this.model = model;
		this.workspacePath = workspacePath;
	}

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes)
					throws SAXException {
		if ("org.eventb.theory.core.scAvailableTheory".equals(qName)) {
			String path = attributes
					.getValue("org.eventb.theory.core.availableTheory");
			path = path.substring(0, path.indexOf('|'));

			if (!theoryMap.containsKey(path)) {
				try {
					SAXParserFactory parserFactory = SAXParserFactory
							.newInstance();
					parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
					SAXParser saxParser = parserFactory.newSAXParser();

					String dir = path.substring(path.indexOf('/') + 1,
							path.lastIndexOf('/'));
					String name = path.substring(path.lastIndexOf('/') + 1,
							path.lastIndexOf('.'));

					Path theoryFile = Paths.get(workspacePath + path);
					model = model.addFile(theoryFile);
					TheoryExtractor extractor = new TheoryExtractor(
							workspacePath, dir, name, theoryMap);
					saxParser.parse(theoryFile.toFile(), extractor);
					if (extractor.getMappingFile() != null) {
						model = model.addFile(extractor.getMappingFile());
					}
					theories = theories.addMultiple(extractor.getTheories());
					typeEnv.addAll(extractor.getTypeEnv());
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} catch (ParserConfigurationException e) {
					throw new SAXException(e);
				}
			} else {
				theories = theories.addElement(theoryMap.get(path));
			}
		}
	}

	public Set<IFormulaExtension> getTypeEnv() {
		return typeEnv;
	}

	@Override
	public void endDocument() {
		model = model.withExtensions(typeEnv).withTheories(theories);
	}

	public EventBModel getModel() {
		return model;
	}

}
