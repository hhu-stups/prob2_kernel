package de.prob.model.eventb.translate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.io.MoreFiles;

import de.prob.exception.ProBError;
import de.prob.model.eventb.EventBModel;
import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.scripting.EventBFactory;

import org.eventb.core.ast.extension.IFormulaExtension;
import org.xml.sax.SAXException;

public class EventBDatabaseTranslator {
	private EventBModel model;

	public EventBDatabaseTranslator(EventBModel m, final String fileName) throws IOException {
		File modelFile = new File(fileName);
		String fullFileName = modelFile.getAbsolutePath();
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			SAXParser saxParser = parserFactory.newSAXParser();

			this.model = m.setModelFile(modelFile);

			String directory = fullFileName.substring(0, fullFileName.lastIndexOf(File.separatorChar));
			String workspacePath = directory.substring(0, directory.lastIndexOf(File.separatorChar));

			File theoryFile = new File(directory + File.separator + "TheoryPath.tcl");
			Set<IFormulaExtension> typeEnv;
			if (!theoryFile.exists()) {
				typeEnv = new HashSet<>();
			} else {
				TheoryXmlHandler theoryHandler = new TheoryXmlHandler(this.model, workspacePath);
				saxParser.parse(theoryFile, theoryHandler);
				typeEnv = theoryHandler.getTypeEnv();
				this.model = theoryHandler.getModel();
			}

			if (EventBFactory.CHECKED_RODIN_CONTEXT_EXTENSION.equals(MoreFiles.getFileExtension(modelFile.toPath()))) {
				final ContextXmlHandler xmlHandler = new ContextXmlHandler(this.model, fullFileName, typeEnv);
				saxParser.parse(modelFile, xmlHandler);
				this.model = xmlHandler.getModel();
			} else {
				final MachineXmlHandler xmlHandler = new MachineXmlHandler(this.model, fullFileName, typeEnv);
				saxParser.parse(modelFile, xmlHandler);
				this.model = xmlHandler.getModel();
			}
		} catch (FileNotFoundException | NoSuchFileException e) {
			throw new EventBFileNotFoundException(fullFileName, "Translated .bcm or .bcc file could not be found. Try to clean the Rodin project", true, e);
		} catch (ParserConfigurationException | SAXException e) {
			throw new ProBError("XML parsing error while loading Event-B file from Rodin project", e);
		}
	}

	/**
	 * @deprecated Use {@link AbstractModel#getMainComponent()} instead.
	 * @return the model's main component
	 */
	@Deprecated
	public AbstractElement getMainComponent() {
		return this.getModel().getMainComponent();
	}

	public EventBModel getModel() {
		return model;
	}
}
