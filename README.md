# ProB 2.0

**IMPORTANT:** The layout of the repository has changed! The Eclipse/Rodin plugin was moved to a separate repository: http://github.com/bendisposto/prob2-plugin. This repository only contains the Kernel of ProB 2.0.

The last version before the restructuring is tagged as preRestructure.

The project is intended for internal usage, do not rely on any of the features or interfaces in this project.

The sourcecode of the current ProB2 Java API release is located at https://gitlab.cs.uni-duesseldorf.de/stups/prob/prob2_kernel
and mirrored to https://github.com/hhu-stups/prob2_kernel

## Documentation

* [Tutorial](https://prob.hhu.de/w/index.php/Tutorial13)
* Developer Handbook: [HTML](https://www3.hhu.de/stups/handbook/prob2/prob_handbook.html) [PDF](https://www3.hhu.de/stups/handbook/prob2/prob_handbook.pdf)

## Bugs

Please report bugs and feature requests at [prob-issues on GitHub](https://github.com/hhu-stups/prob-issues/issues).

## Setting up a development environment

The ProB 2 Java API requires Java 8 or later (tested using Java 8, 11, and 17)
and is compatible with 64-bit versions of Windows, macOS, and most Linux distributions (glibc on x86_64).

ProB 2 is built using [Gradle](https://gradle.org/).
We recommend running Gradle via the Gradle wrapper (`gradlew`) included in the repo
to ensure that the expected Gradle version is used.
Alternatively you can install a compatible Gradle version yourself,
e. g. via your package manager.

Note that the Gradle project is located in the subdirectory "de.prob2.kernel".
You need to `cd` into this directory before running Gradle,
or the build will not work.
If you are using an IDE,
the Gradle project in the subdirectory might be detected automatically,
otherwise you need to select the "de.prob2.kernel" directory when importing the project.

To run the test suite: `./gradlew check`

## License

The ProB 2.0 source code is distributed under the [Eclipse Public License - v 2.0](LICENSE).

ProB 2.0 comes with ABSOLUTELY NO WARRANTY OF ANY KIND ! This software is
distributed in the hope that it will be useful but WITHOUT ANY WARRANTY.
The author(s) do not accept responsibility to anyone for the consequences of
using it or for whether it serves any particular purpose or works at all. No
warranty is made about the software or its performance.


(c) 2012-2020 Jens Bendisposto et.al., all rights reserved
