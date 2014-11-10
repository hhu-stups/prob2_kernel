package de.prob.statespace;


/**
 * This class is used within the {@link Trace} object to create the linked list
 * of operations. Each TraceElement references a particular operation that has
 * been executed.
 * 
 * @author joy
 * 
 */
public class TraceElement {
	private final int index;
	private final State currentState;
	private final OpInfo transition;

	private final TraceElement previous;

	public TraceElement(final State stateId) {
		this.currentState = stateId;

		// FOR THE FIRST STATE ID, EVERYTHING IS
		transition = null;
		previous = null;
		index = 0;
	}

	public TraceElement(final OpInfo edge, final TraceElement previous) {
		this.currentState = edge.getDestId();
		this.transition = edge;

		this.previous = previous;
		index = previous.getIndex() + 1;
	}

	/**
	 * @return the {@link State} source corresponding to this element
	 */
	public State getSrc() {
		if (transition != null) {
			return transition.getSrcId();
		}
		return currentState;
	}

	/**
	 * @return the {@link State} destination corresponding to this element
	 */
	public State getDest() {
		if (transition != null) {
			return transition.getDestId();
		}
		return null;
	}

	/**
	 * @return the {@link OpInfo} operation corresponding to this element
	 */
	public OpInfo getOp() {
		return transition;
	}

	/**
	 * @return returns the pointer to the previous TraceElement
	 */
	public TraceElement getPrevious() {
		return previous;
	}

	/**
	 * @return the current {@link State} that is associated with the
	 *         TraceElement. If the destination is null, this is source.
	 *         Otherwise, this is destination.
	 */
	public State getCurrentState() {
		return currentState;
	}

	/**
	 * @return int index in list of TraceElements
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		if (transition == null) {
			return currentState.getId();
		} else {
			return transition.toString();
		}
	}
}
