package de.prob.statespace

import java.nio.file.Paths

import de.prob.cli.CliTestCommon
import de.prob.scripting.ClassicalBFactory
import spock.lang.Specification

class TraceConstructionTest extends Specification {
	private static StateSpace s

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
	}

	def cleanupSpec() {
		s.kill()
	}

	def "can create Trace from StateSpace"() {
		expect:
		// The != null check is always true; this simply tests that no exception is thrown.
		new Trace(s) != null
	}

	def "cannot create Trace with null parameter"() {
		when:
		new Trace(null)

		then:
		thrown(RuntimeException)
	}

	def "there are accessor methods for current and previous states, and for the current transition"() {
		when:
		final t = new Trace(s).$initialise_machine()

		then:
		t.currentState.id == "0"
		t.previousState.id == "root"
		t.currentTransition.name == "\$initialise_machine"
	}

	def "you can view the transitions from the trace (which will not be evaluated)"() {
		given:
		final t = new Trace(s).$initialise_machine()

		when:
		final outtrans = t.nextTransitions

		then:
		outtrans.size() == 4
		outtrans.every {!it.evaluated}
	}

	def "the list of transitions can be accessed from the trace"() {
		given:
		final t = new Trace(s).$initialise_machine().new("pp=PID1")

		when:
		final transitions = t.transitionList

		then:
		transitions.collect {it.name} == ["\$initialise_machine", "new"]
		transitions.every {!it.evaluated}
	}

	def "A trace can be copied (everything identical except UUID)"() {
		given:
		final t = new Trace(s).$initialise_machine()

		when:
		final t2 = t.copy()

		then:
		t.current == t2.current
		t.head == t2.head
		t.transitionList == t2.transitionList
		t.UUID != t2.UUID
	}
}
