package de.prob.statespace

import java.nio.file.Paths

import de.prob.cli.CliTestCommon
import de.prob.scripting.ClassicalBFactory
import spock.lang.Specification

class BasicStateSpaceTest extends Specification {
	private static StateSpace s

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
	}

	def cleanupSpec() {
		s.kill()
	}

	def "the id of a state space is the id of its animator"() {
		expect:
		s.id == s.animator.id
	}

	def "the to string method is implemented"() {
		expect:
		s.toString() != null
	}

	def "it is possible to print the operations for a given state"() {
		expect:
		s.printOps(s.root) != null
	}

	def "it is possible to print a state"() {
		expect:
		s.printState(s.root) != null
	}

	def "if states are not explored by default, not all transitions may be shown"() {
		when:
		s.root.explored = false

		then:
		s.printOps(s.root).contains("Possibly not all transitions shown.")

		cleanup:
		s.root.explored = true
	}
}
