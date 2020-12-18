package de.prob.statespace

import java.nio.file.Paths

import de.prob.animator.command.CheckInitialisationStatusCommand
import de.prob.animator.command.CheckInvariantStatusCommand
import de.prob.animator.command.ExploreStateCommand
import de.prob.cli.CliTestCommon
import de.prob.scripting.ClassicalBFactory

import spock.lang.Specification

class StateSpaceAsAnimatorTest extends Specification {
	private static StateSpace s
	private static State root
	private static State firstState

	def setupSpec() {
		final path = Paths.get("groovyTests", "machines", "scheduler.mch").toString()
		final factory = CliTestCommon.injector.getInstance(ClassicalBFactory.class)
		s = factory.extract(path).load([:])
		root = s.root
		firstState = root.$initialise_machine()
	}

	def cleanupSpec() {
		s.kill()
	}

	def "sending an interrupt while the animator is not doing anything will not disturb further commands"() {
		given:
		final cmd = new ExploreStateCommand(s, "root", [])

		when:
		s.sendInterrupt()
		s.execute(cmd)

		then:
		noExceptionThrown()
	}

	def "it is possible to execute a single command"() {
		given:
		final cmd = new CheckInitialisationStatusCommand(root.id)

		when:
		s.execute(cmd)

		then:
		!cmd.result
	}

	def "it is possible to execute multiple commmands"() {
		given:
		final cmd = new CheckInitialisationStatusCommand(firstState.id)
		final cmd2 = new CheckInvariantStatusCommand(firstState.id)

		when:
		s.execute(cmd, cmd2)

		then:
		cmd.result
		!cmd2.invariantViolated
	}

	def "it is possible to start and end transactions"() {
		expect:
		!s.busy
		s.startTransaction()
		s.busy
		s.endTransaction()
		!s.busy
		s.withTransaction {
			assert s.busy
		}
		!s.busy
	}

	def "it is possible to notify animation listeners that the animator is busy"() {
		given:
		final animations = CliTestCommon.injector.getInstance(AnimationSelector.class)
		animations.addNewAnimation(new Trace(s))
		final IAnimationChangeListener mylistener = Mock()

		when:
		animations.registerAnimationChangeListener(mylistener)
		s.startTransaction()
		s.endTransaction()

		then:
		1 * mylistener.animatorStatus(false)
		1 * mylistener.animatorStatus(true)
		1 * mylistener.animatorStatus(false)
	}
}
