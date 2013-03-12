package de.prob.model.eventb;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import de.prob.model.representation.AbstractElement;
import de.prob.model.representation.AbstractModel;
import de.prob.model.representation.Machine;
import de.prob.model.representation.RefType;
import de.prob.model.representation.RefType.ERefType;
import de.prob.model.representation.StateSchema;
import de.prob.statespace.StateSpace;

public class EventBModel extends AbstractModel {

	private AbstractElement mainComponent;
	private final BStateSchema schema = new BStateSchema();;

	@Inject
	public EventBModel(final StateSpace statespace) {
		this.statespace = statespace;
	}

	@Override
	public StateSchema getStateSchema() {
		return this.schema;
	}

	public void addMachines(final Collection<EventBMachine> collection) {
		put(Machine.class, collection);
	}

	public void addContexts(final Collection<Context> contexts) {
		put(Context.class, contexts);
	}

	public void setModelFile(final File modelFile) {
		this.modelFile = modelFile;
	}

	public void isFinished() {
		calculateGraph();
		statespace.setModel(this);
	}

	public void setMainComponent(final AbstractElement mainComponent) {
		this.mainComponent = mainComponent;
	}

	@Override
	public AbstractElement getMainComponent() {
		return mainComponent;
	}

	public String getMainComponentName() {
		if (mainComponent instanceof Context) {
			return ((Context) mainComponent).getName();
		}
		if (mainComponent instanceof Machine) {
			return ((Machine) mainComponent).getName();
		}
		return "";
	}

	@Override
	public AbstractElement getComponent(final String name) {
		for (Machine machine : getChildrenOfType(Machine.class)) {
			if (machine.getName().equals(name)) {
				return machine;
			}
		}
		for (Context context : getChildrenOfType(Context.class)) {
			if (context.getName().equals(name)) {
				return context;
			}
		}
		return null;
	}

	public void calculateGraph() {
		for (Machine machine : getChildrenOfType(Machine.class)) {
			graph.addVertex(machine.getName());
		}
		for (Context context : getChildrenOfType(Context.class)) {
			graph.addVertex(context.getName());
		}

		for (Machine machine : getChildrenOfType(Machine.class)) {
			for (Machine refinement : machine.getChildrenOfType(Machine.class)) {
				graph.addEdge(new RefType(ERefType.REFINES), machine.getName(),
						refinement.getName());
			}
			for (Context seen : machine.getChildrenOfType(Context.class)) {
				graph.addEdge(new RefType(ERefType.SEES), machine.getName(),
						seen.getName());
			}
		}
		Set<Context> contexts = getChildrenOfType(Context.class);
		for (Context context : contexts) {
			for (Context seen : context.getChildrenOfType(Context.class)) {
				graph.addEdge(new RefType(ERefType.EXTENDS), context.getName(),
						seen.getName());
			}
		}
	}

	@Override
	public Map<String, AbstractElement> getComponents() {
		Map<String, AbstractElement> components = new HashMap<String, AbstractElement>();
		for (Machine machine : getChildrenOfType(Machine.class)) {
			components.put(machine.getName(), machine);
		}
		for (Context context : getChildrenOfType(Context.class)) {
			components.put(context.getName(), context);
		}
		return components;
	}

}
