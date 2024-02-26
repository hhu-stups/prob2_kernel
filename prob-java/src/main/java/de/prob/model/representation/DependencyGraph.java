package de.prob.model.representation;

import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.krukow.clj_lang.IPersistentMap;
import com.github.krukow.clj_lang.PersistentHashMap;
import com.github.krukow.clj_lang.PersistentHashSet;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * A simple graph implementation intended to display the relationships between
 * the components in a model. The nodes in the graph are represented as the
 * string element name of the component and the edges are the type of
 * relationship as defined by {@link ERefType}. It is a multigraph, which means
 * that there can be multiple edges between two nodes.
 *
 *
 * Only edges can be removed
 *
 *
 * @author joy
 *
 */
public class DependencyGraph {
	/**
	 * RefType is used for both ClassicalBModels and EventBModels
	 *
	 * ClassicalB: SEES, USES, REFINES, INCLUDES, IMPORTS EventB: SEES, REFINES,
	 * EXTENDS
	 *
	 * @author joy
	 *
	 */
	public enum ERefType {
		SEES, USES, REFINES, INCLUDES, IMPORTS, EXTENDS
	}

	public static class Node {
		final String elementName;
		final PersistentHashSet<Edge> outEdges;

		public Node(final String elementName) {
			this(elementName, PersistentHashSet.emptySet());
		}

		private Node(final String elementName, PersistentHashSet<Edge> edges) {
			this.elementName = elementName;
			outEdges = edges;
		}

		public String getElementName() {
			return elementName;
		}

		public Set<Edge> getOutEdges() {
			return outEdges;
		}

		/**
		 * @param edge
		 *            to be added.
		 * @return node
		 */
		public Node addEdge(final Edge edge) {
			return new Node(elementName, outEdges.cons(edge));
		}

		public Node removeEdge(final Edge edge) {
			return new Node(elementName, outEdges.disjoin(edge));
		}

		@Override
		public boolean equals(final Object that) {
			if (this == that) {
				return true;
			}
			if (that instanceof Node) {
				return getElementName().equals(((Node) that).getElementName());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return getElementName().hashCode();
		}


		@Override
		public String toString(){
			return elementName;
		}
	}

	public static class Edge {
		final Node from;
		final Node to;
		final ERefType relationship;

		public Edge(final Node from, final Node to, final ERefType relationship) {
			this.from = from;
			this.to = to;
			this.relationship = relationship;
		}

		public Node getFrom() {
			return from;
		}

		public Node getTo() {
			return to;
		}

		public ERefType getRelationship() {
			return relationship;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || this.getClass() != obj.getClass()) {
				return false;
			}
			final Edge other = (Edge)obj;
			return Objects.equals(getFrom(), other.getFrom())
					&& Objects.equals(getTo(), other.getTo())
					&& Objects.equals(getRelationship(), other.getRelationship());
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.getFrom(), this.getTo(), this.getRelationship());
		}
	}

	final IPersistentMap<String, Node> graph;
	private final String start;

	public DependencyGraph() {
		this(PersistentHashMap.emptyMap(), "");
	}

	private DependencyGraph(IPersistentMap<String, Node> graph, String start) {
		this.graph = graph;
		this.start = start;
	}



	/**
	 * @param element
	 *            element to be added to the graph it is has not already been
	 *            added;
	 * @return the dependency graph
	 */
	public DependencyGraph addVertex(final String element) {
		if (!graph.containsKey(element)) {
			if(graph.count()==0){
				return new DependencyGraph(graph.assoc(element, new Node(element)), element);
			}else{
				return new DependencyGraph(graph.assoc(element, new Node(element)), getStart());
			}
		}
		return this;
	}

	/**
	 * @param element
	 *            to be found in the graph
	 * @return whether or not the element has been added to the graph
	 */
	public boolean containsVertex(final String element) {
		return graph.containsKey(element);
	}

	public Set<String> getVertices() {
		Set<String> vertices = new HashSet<>();
		for (Map.Entry<String, Node> entry : graph) {
			vertices.add(entry.getKey());
		}
		return vertices;
	}

	public Set<Edge> getEdges() {
		Set<Edge> set = new HashSet<>();
		for (Map.Entry<String, Node> entry : graph) {
			set.addAll(entry.getValue().getOutEdges());
		}
		return set;
	}

	public Set<Edge> getOutEdges(String name) {
		return graph.valAt(name).getOutEdges();
	}

	public Set<Edge> getIncomingEdges(String name) {
		return getEdges().stream()
			.filter(edge -> edge.getTo().getElementName().equals(name))
			.collect(Collectors.toSet());
	}

	/**
	 *
	 * @param from
	 *            source element
	 * @param to
	 *            destination element
	 * @param relationship
	 *            relationship between the two elements
	 * @return the dependency graph
	 */
	public DependencyGraph addEdge(final String from, final String to, final ERefType relationship) {
		DependencyGraph currentWithFrom = this.addVertex(from);
		DependencyGraph currentWithFromTo = currentWithFrom.addVertex(to);
		IPersistentMap<String, Node> newGraph = currentWithFromTo.graph;


		Node f = newGraph.valAt(from);
		Node t = newGraph.valAt(to);
		Edge e = new Edge(f, t, relationship);
		return new DependencyGraph(newGraph.assoc(from, f.addEdge(e)), currentWithFromTo.getStart());
	}

	public DependencyGraph removeEdge(String from, String to, ERefType relationship) {
		Node f = graph.valAt(from);
		Node t = graph.valAt(to);
		if (f == null || t == null) {
			throw new IllegalArgumentException("Nodes must be specified in order to be deleted.");
		}
		return new DependencyGraph(graph.assoc(from, f.removeEdge(new Edge(f, t, relationship))), getStart());
	}

	/**
	 * @param from
	 *            source node
	 * @param to
	 *            destination node
	 * @return the list of relationships between the two elements.
	 */
	public List<ERefType> getRelationships(final String from, final String to) {
		if (!graph.containsKey(from)) {
			throw new IllegalArgumentException("Element " + from + " is not in graph.");
		}
		if (!graph.containsKey(to)) {
			throw new IllegalArgumentException("Element " + to + " is not in graph.");
		}
		Node f = graph.valAt(from);
		Node t = graph.valAt(to);

		Set<Edge> edgeSet = f.getOutEdges();
		List<ERefType> relationships = new ArrayList<>();
		for (Edge edge : edgeSet) {
			if (edge.getFrom().equals(f) && edge.getTo().equals(t)) {
				relationships.add(edge.getRelationship());
			}
		}

		return relationships;
	}

	public String getStart(){
		return start;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Node> entry : graph) {
			List<String> s = new ArrayList<>();
			Set<Edge> outEdges = entry.getValue().getOutEdges();
			sb.append(entry.getKey());
			sb.append(" : ");
			for (Edge edge : outEdges) {
				s.add(edge.getRelationship() + " -> " + edge.getTo().getElementName());
			}
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Takes the start node and goes the refinement chain up from there. If the start node refines nothing only the
	 * start node is returned
	 * @return the refinement chain if there is any
	 */
	public List<Node> refinementChain(){
		if(start.isEmpty()){
			return emptyList();
		}
		List<Node> acc = new ArrayList<>();
		acc.add(graph.valAt(start));
		return refinementChainAux(start, acc);
	}

	private List<Node> refinementChainAux(String entryPoint, List<Node> acc){
		List<Node> refinement = graph.valAt(entryPoint).getOutEdges().stream()
				.filter(entry -> entry.getRelationship().equals(ERefType.REFINES))
				.map(Edge::getTo)
				.collect(toList());

		if(refinement.isEmpty()){
			return acc;
		}else{
			return refinementChainAux(refinement.get(0).getElementName(), Stream.of(acc, refinement)
					.flatMap(Collection::stream)
					.collect(toList()));
		}

	}


}
