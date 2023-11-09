package de.prob.check.tracereplay.check.refinement;

import de.prob.check.tracereplay.PersistentTransition;

import java.util.ArrayList;
import java.util.List;

public class TraceConnector {

	private final List<PersistentTransition> traceOld;
	private final List<PersistentTransition> traceNew;
	private final List<String> introducedBySkip;
	public TraceConnector(List<PersistentTransition> traceOld, List<PersistentTransition> traceNew, List<String> introducedBySkip) {

		this.introducedBySkip = introducedBySkip;
		this.traceOld = traceOld;
		this.traceNew = traceNew;
	}

	/**
	 * Connects the two traces. Inserts placeholders where needed.
	 * @return the connected trace
	 */
	public List<Pair<PersistentTransition, PersistentTransition>> connect(){

		int i = 0;
		int z = 0;
		List<Pair<PersistentTransition, PersistentTransition>> results = new ArrayList<>();

		while(z < traceNew.size()){
			if(introducedBySkip.contains(traceNew.get(z).getOperationName())){
				results.add(new Pair<>(new SkipTransition(), traceNew.get(z)));
			}else{
				if(isPartner(traceOld.get(i), traceNew.get(z)))
				{
					results.add(new Pair<>(traceOld.get(i), traceNew.get(z)));
					i++;
				}else{
					results.add(new Pair<>(new StutteringTransition(), traceNew.get(z)));
				}
			}
			z++;
		}
		return results;
	}


	/**
	 * Checks if two transitions are in a refinement relationship
	 * @param oldT first transition
	 * @param newT second transition
	 * @return yes - in a relationship
	 */
	public boolean isPartner(PersistentTransition oldT, PersistentTransition newT){
		return
				oldT.getDestinationStateVariables().entrySet()
						.stream()
						.filter(entry -> newT.getDestinationStateVariables().containsKey(entry.getKey()) && newT.getDestinationStateVariables().containsValue(entry.getValue()))
						.count() == oldT.getDestinationStateVariables().size();
	}


	static class SkipTransition extends PersistentTransition{

		public SkipTransition() {
			super("skip");
		}


	}

	static class StutteringTransition extends PersistentTransition{

		public StutteringTransition() {
			super("stuttering");
		}
	}

	public static class Pair<T, E>{
		public final T first;
		public final E second;

		public Pair(T first, E second){
			this.first = first;
			this.second = second;
		}

		public E getSecond() {
			return second;
		}

		public T getFirst() {
			return first;
		}
	}


}
