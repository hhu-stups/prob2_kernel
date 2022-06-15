package de.prob.animator.command;


import de.prob.animator.domainobjects.VisBSVGObject;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetVisBSVGObjectsCommand extends AbstractCommand {

	private static final String PROLOG_COMMAND_NAME = "prob2_visb_svg_objects";
	private static final String LIST = "List";
	private List<VisBSVGObject> svgObjects;

	public GetVisBSVGObjectsCommand() {
	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm(PROLOG_COMMAND_NAME);
		pto.printVariable(LIST);
		pto.closeTerm();
	}

	@Override
	public void processResult(final ISimplifiedROMap<String, PrologTerm> bindings) {
		final PrologTerm resultTerm = bindings.get(LIST);
		ListPrologTerm list = (ListPrologTerm) resultTerm;
		List<VisBSVGObject> svgObjects = new ArrayList<>();
		for(PrologTerm svgObject : list) {
			BindingGenerator.getCompoundTerm(svgObject, "visb_svg_object", 3);
			String id = svgObject.getArgument(1).atomToString();
			String object = svgObject.getArgument(2).toString();
			ListPrologTerm attributes = (ListPrologTerm) svgObject.getArgument(3);
			Map<String, String> attributesMap = new HashMap<>();
			for(PrologTerm attribute : attributes) {
				BindingGenerator.getCompoundTerm(attribute, "svg_attribute", 2);
				String key = attribute.getArgument(1).atomToString();
				String value = attribute.getArgument(2).atomToString();
				attributesMap.put(key, value);
			}
			svgObjects.add(new VisBSVGObject(id, object, attributesMap));
		}
		this.svgObjects = svgObjects;
	}

	public List<VisBSVGObject> getSvgObjects() {
		return svgObjects;
	}

}
