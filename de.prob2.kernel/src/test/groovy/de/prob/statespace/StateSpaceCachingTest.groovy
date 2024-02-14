package de.prob.statespace

import java.nio.file.Paths

import de.prob.cli.CliTestCommon
import de.prob.scripting.ClassicalBFactory
import spock.lang.Specification

class StateSpaceCachingTest extends Specification {

	static StateSpace s

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
	}

	def cleanupSpec() {
		s.kill()
	}

	def "if a state does not exist in the state space (on the prolog side) and illegal argument exception is thrown"() {
		when:
		s.getState("bum!")

		then:
		thrown(IllegalArgumentException)
	}

	def "states can also be accessed via integer values whereby root is -1"() {
		expect:
		s.getState(-1) == s.root
	}

	def "states can also be accessed via integer values if the states exist"() {
		when:
		s.root.explore()

		then:
		s.getState(0) == s.getState("0")
	}

	def "states cannot be accessed via negative integer except -1 (root)"() {
		when:
		s.getState(-100)

		then:
		thrown(IllegalArgumentException)
	}

	def "states can be accessed via integer via the getAt method"() {
		when:
		s.root.explore()

		then:
		s[0].id == "0"
		s[0] == s[0]
	}

	def "states that do not exist in the prolog kernel cannot be accessed via integer"() {
		when:
		s.getState(500) // we have not reached this during the exploration we have done so far in the tests

		then:
		thrown(IllegalArgumentException)
	}
}
