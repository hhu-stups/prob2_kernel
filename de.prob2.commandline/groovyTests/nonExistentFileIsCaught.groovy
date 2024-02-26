import java.nio.file.NoSuchFileException

def caught1 = false
try {
	s = api.b_load("blah.mch")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught1 = true
}
assert caught1

def caught2 = false
try {
	s = api.b_load("blah.ref")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught2 = true
}
assert caught2

def caught3 = false
try {
	s = api.eventb_load("blub.buc")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught3 = true
}
assert caught3

def caught4 = false
try {
	s = api.eventb_load("blub.bum")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught4 = true
}
assert caught4

def caught5 = false
try {
	s = api.eventb_load("blub.bcc")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught5 = true
}
assert caught5

def caught6 = false
try {
	s = api.eventb_load("blub.bcm")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught6 = true
}
assert caught6

def caught7 = false
try {
	s = api.csp_load("blub.csp")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught7 = true
}
assert caught7

def caught8 = false
try {
	s = api.tla_load("blub.tla")
} catch (FileNotFoundException | NoSuchFileException e) {
	caught8 = true
}
assert caught8

"a FileNotFoundException is thrown if the specified model does not exist"
