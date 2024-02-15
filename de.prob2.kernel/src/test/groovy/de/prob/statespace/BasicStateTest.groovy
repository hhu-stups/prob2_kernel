package de.prob.statespace

import java.nio.file.Paths

import de.prob.cli.CliTestCommon
import de.prob.scripting.ClassicalBFactory
import spock.lang.Specification

class BasicStateTest extends Specification {
	private static StateSpace s
	private static State root
	private static State firstState
	private static State secondState

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
		root = s.root
		firstState = root.$initialise_machine()
		secondState = firstState.new("pp=PID1")
	}

	def cleanupSpec() {
		s.kill()
	}

	def "toString is id"() {
		expect:
		root.toString() == "root"
	}

	def "id is id"() {
		expect:
		root.getId() == "root"
		firstState.getId() == "0"
	}

	def "you can also get the numerical id"() {
		expect:
		root.numericalId() == -1
		firstState.numericalId() == 0
	}

	def "it is possible to get the state representation of a state"() {
		when:
		def rep = secondState.getStateRep()

		then:
		firstState.eval(rep).getValue() == "FALSE"
		secondState.eval(rep).getValue() == "TRUE"
	}

	def "equivalence in a state is based on id and state space"() {
		when:
		def sameroot = new State("root", s)
		def otherroot = new State("root", Mock(StateSpace))

		then:
		root == sameroot
		sameroot == root
		root != otherroot
		otherroot != root
	}

	def "a state is not equal to something else"() {
		expect:
		root != "I'm not a State!"
	}

	def "hashcode is based also on id and state space"() {
		when:
		def sameroot = new State("root", s)
		def otherroot = new State("root", Mock(StateSpace))

		then:
		root.hashCode() == sameroot.hashCode()
		root.hashCode() != otherroot.hashCode()
	}
}
