import de.prob.statespace.TraceDecorator

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

def eval(script) {
	engine.eval(script)
}
