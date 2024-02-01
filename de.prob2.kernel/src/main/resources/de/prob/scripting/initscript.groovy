import de.prob.animator.domainobjects.ClassicalB
import de.prob.animator.domainobjects.EvalResult
import de.prob.animator.domainobjects.EventB
import de.prob.statespace.TraceDecorator

// This extends String's as operator to allow creating EvalElements from strings.
// For example, "x" as ClassicalB creates a ClassicalB object by parsing the formula "x".
// The same works for the EventB and CSP types.
final oldStringAsType = String.metaClass.getMetaMethod("asType", [Class] as Class<?>[])
String.metaClass.asType = {Class<?> type -> 
	if (type == ClassicalB) {
		new ClassicalB(delegate)
	} else if (type == EventB) {
		new EventB(delegate)
	} else {
		oldStringAsType.invoke(delegate, [type] as Object[])
	}
}

// Redirect print and println to our own buffered console
inConsole = true
Script.metaClass.print = {s ->
	__console.append(s)
	if (!inConsole) {
		System.out.print(s)
	}
}
Script.metaClass.println = {s ->
	__console.append(s + "\n")
	if (!inConsole) {
		System.out.println(s)
	}
}

// I have no idea why the same method exists under two names...
// It seems that appendToTrace was never used anywhere.
// execTrace was used in the past by the servlet UI,
// but seems to be unused now as well...
// Maybe we can remove this whole code at some point?

def execTrace(t, c) {
	final proxy = new TraceDecorator(t)
	c.resolveStrategy = Closure.DELEGATE_FIRST
	c.delegate = proxy
	c()
}

def appendToTrace(t, c) {
	execTrace(t, c)
}

final oldEvalResultAsType = EvalResult.metaClass.getMetaMethod("asType", [Class] as Class<?>[])
EvalResult.getMetaClass().asType = {Class<?> clazz ->
	if (clazz == Integer) {
		Integer.valueOf(delegate.value)
	} else if (clazz == Double) {
		Double.valueOf(delegate.value)
	} else if (clazz == String) {
		delegate.value
	} else {
		oldEvalResultAsType.invoke(delegate, [clazz] as Object[])
	}
}

def eval(script) {
	engine.eval(script)
}
