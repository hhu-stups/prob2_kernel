package de.prob.model.eventb.algorithm.graph

import de.prob.animator.domainobjects.EventB
import de.prob.model.eventb.Event
import de.prob.model.eventb.MachineModifier
import de.prob.model.eventb.Variant
import de.prob.model.eventb.algorithm.AlgorithmPrettyPrinter
import de.prob.model.eventb.algorithm.Assertion
import de.prob.model.eventb.algorithm.Assignments
import de.prob.model.eventb.algorithm.Block
import de.prob.model.eventb.algorithm.ITranslationAlgorithm
import de.prob.model.eventb.algorithm.If
import de.prob.model.eventb.algorithm.LoopInformation
import de.prob.model.eventb.algorithm.Statement
import de.prob.model.eventb.algorithm.While
import de.prob.model.representation.ModelElementList

class OptimizedGenerationAlgorithm implements ITranslationAlgorithm {

	ControlFlowGraph graph
	Map<Statement, Integer> pcInformation
	final Set<Statement> generated = [] as Set
	Map<Statement, LoopInformation> loopInfo = [:]
	List<IGraphTransformer> transformers
	Map<String, Integer> assertCtr = [:]

	def OptimizedGenerationAlgorithm(List<IGraphTransformer> transformers=[]) {
		this.transformers = transformers
	}

	@Override
	public MachineModifier run(MachineModifier machineM, Block algorithm) {
		graph = transformers.inject(new ControlFlowGraph(algorithm)) { ControlFlowGraph g, IGraphTransformer t -> t.transform(g) }
		pcInformation = new PCCalculator(graph, true).pcInformation
		machineM = machineM.addComment(new AlgorithmPrettyPrinter(algorithm).prettyPrint())
		if (graph.entryNode) {
			machineM = machineM.var_block("pc", "pc : NAT", "pc := 0")
			machineM = addAssertions(machineM, graph.woAssertions)
			machineM =  addNode(machineM, graph.entryNode)
			def loops = []
			loopInfo.each { k, v -> loops << v }
			def loopI = new ModelElementList<LoopInformation>(loops)
			machineM = new MachineModifier(machineM.getMachine().set(LoopInformation.class, loopI), machineM.typeEnvironment)
		}
		return machineM
	}

	def MachineModifier addAssertions(MachineModifier machineM, Block b) {
		b.statements.each {
			machineM = addAssertions(machineM, it)
		}
		machineM
	}

	def MachineModifier addAssertions(MachineModifier machineM, Assignments a) {
		addAssertionsForNode(machineM, a)
	}

	def MachineModifier addAssertions(MachineModifier machineM, While w) {
		machineM = addAssertionsForNode(machineM, w)
		addAssertions(machineM, w.block)
	}

	def MachineModifier addAssertions(MachineModifier machineM, If i) {
		machineM = addAssertionsForNode(machineM, i)
		machineM = addAssertions(machineM, i.Then)
		addAssertions(machineM, i.Else)
	}

	def MachineModifier addAssertionsForNode(MachineModifier machineM, Statement stmt) {
		graph.assertions[stmt].each { Assertion assertion ->
			List<String> preds = []
			if (graph.nodes.contains(stmt)) {
				if (pcInformation[stmt] != null) {
					preds << "pc = ${pcInformation[stmt]}"
				} else {
					Set<Edge> inE = graph.inEdges(stmt)
					inE.each { Edge e ->
						def pred = "pc = ${pcInformation[e.from]}"
						def rcond = e.conditions.collect { it.getCode() }.iterator().join(" & ")
						pred = rcond == "" ? pred : "$pred & $rcond"
						preds << pred
					}
				}
			} else {
				assert stmt instanceof If
				println stmt
				Set<Edge> allEdges = [] as Set
				graph.outgoingEdges.inject(allEdges) { Set<Edge> all, entry -> all.addAll(entry.value); all }
				Set<List<EventB>> conds = [] as Set
				allEdges.findAll { Edge e -> e.conditions.contains(stmt.condition) }.each { Edge e ->
					def i = e.conditions.indexOf(stmt.condition)
					def cond = e.conditions[0..i-1]
					if (!conds.contains(cond)) {
						conds << cond
						def pred = "pc = ${pcInformation[e.from]}"
						def rcond = cond.collect { it.getCode() }.iterator().join(" & ")
						pred = rcond = "" ? pred : "$pred & $rcond"
						preds << pred
					}
				}
			}
			preds.each { String pred ->
				def name = graph.namingWAssertions.getName(assertion)
				if (assertCtr[name] != null) {
					name = name + "_" + assertCtr[name]++
				} else {
					assertCtr[name] = 0
				}
				machineM = machineM.invariant(name, "$pred => (${assertion.assertion.getCode()})", false, assertion.toString())
			}
		}
		machineM
	}

	def MachineModifier addNode(MachineModifier machineM, Statement stmt) {
		if (generated.contains(stmt)) {
			return machineM
		}
		generated << stmt
		final pcs = pcInformation

		def branch = stmt instanceof While || stmt instanceof If
		graph.outEdges(stmt).each { final Edge outEdge ->
			def name = extractName(outEdge)
			def node = null
			if (branch && outEdge.to instanceof Assignments) {
				generated << outEdge.to
				if (!graph.outEdges(outEdge.to).isEmpty()) {
					node = graph.outEdges(outEdge.to).first().to
				}
			}
			final nextN = node ?: outEdge.to

			machineM = machineM.event(name: name, comment: stmt.toString()) {
				guard "pc = ${pcs[stmt]}"
				outEdge.conditions.each { guard it }
				if (stmt instanceof Assignments) {
					stmt.assignments.each { action it }
				} else if (branch && outEdge.to instanceof Assignments) {
					outEdge.to.assignments.each { action it }
				}
				if (pcs[nextN] != null) {
					action "pc := ${pcs[nextN]}"
				}
			}

			machineM = addNode(machineM, nextN)
			if (outEdge.loopToWhile) {
				addLoopInfo(outEdge, machineM.getMachine().getEvent(name))
			}
		}

		if (graph.outEdges(stmt) == []) {
			def name = graph.nodeMapping.getName(stmt)
			if (stmt instanceof Assignments && !stmt.assignments.isEmpty()) {
				throw new IllegalArgumentException("Algorithm must deadlock on empty assignments block")
			}

			machineM = machineM.event(name: name) { guard "pc = ${pcs[stmt]}" }
		}

		machineM
	}

	def String extractName(Edge e) {
		List<Statement> statements = graph.edgeMapping[e]
		if (statements.size() == 1 && statements[0] instanceof Assignments) {
			assert e.conditions.isEmpty()
			return "${graph.nodeMapping.getName(statements[0])}"
		}
		assert e.conditions.size() == statements.size()
		[statements, e.conditions].transpose().collect { l ->
			extractName(l[0], l[1])
		}.iterator().join("_")
	}

	def String extractName(While s, EventB condition) {
		def name = graph.nodeMapping.getName(s)
		if (condition == s.condition) {
			return "enter_$name"
		}
		if (condition == s.notCondition) {
			return "exit_$name"
		}
		return "unknown_branch_$name"
	}

	def String extractName(If s, EventB condition) {
		def name = graph.nodeMapping.getName(s)
		if (condition == s.condition) {
			return "${name}_then"
		}
		if (condition == s.elseCondition) {
			return "${name}_else"
		}
		return "unknown_branch_$name"
	}

	def addLoopInfo(Edge edge, Event loopEvent) {
		def w = edge.to
		def name = graph.nodeMapping.getName(w)
		assert w instanceof While
		if (loopInfo[w] == null && w.variant != null) {
			loopInfo[w] = new LoopInformation(name, w, new Variant(w.variant, name), [])
		} else if (loopInfo[w] != null && !loopInfo[w].loopStatements.contains(loopEvent)) {
			loopInfo[w] = loopInfo[w].add(loopEvent)
		}
	}
}
