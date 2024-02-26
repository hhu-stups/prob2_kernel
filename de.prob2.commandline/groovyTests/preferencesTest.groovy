import java.nio.file.Paths

final s = api.b_load(Paths.get(dir, "machines", "scheduler.mch").toString(), ["MAXINT":"10"])

final ps = s.preferenceInformation

final prefs1 = [:]
ps.each { prefs1[it.name] = it.defaultValue }

assert prefs1.size() > 0 // there are some preferences set

final prefs2 = s.currentPreferences
assert prefs2["MAXINT"] == "10"

s.changePreferences(["MAXINT": "12"])

final value = s.getCurrentPreference("MAXINT")
assert value == "12"

"the preferences for a model are as expected"
