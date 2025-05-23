# <u>**HBM Nuclear Tech Mod Community Edtion**</u>
An attempt at a "definitive" port of NTM to 1.12. Came from nessesity as other developers have failed to update and maintain other forks. Heavy WIP as there is much content to port

#### Discord:
[Warfactory Offical](https://discord.gg/VqVYYWEGrE)

## Development guide.
For development Java17/21 is used.

We have [Jabel](https://github.com/bsideup/jabel) to target Java8 bytecode seamlessly (make sure you don't use APIs introduced in Java9+)


### General quickstart
1. Clone this repository.
2. Prepare JDK (preferably 17+).
3. Run task `setupDecompWorkspace` (this will setup workspace, including MC sources deobfuscation)
4. Ensure everything is OK. Run task `runClient` (should open minecraft client with mod loaded)


- Always use `gradlew` (Linux/MACOS) or `gradlew.bat` (Win) and not `gradle` for tasks. So each dev will have consistent environment.
### Development quirks for Apple M-chip machines.

Since there are no natives for ARM arch, therefore you will have to use x86_64 JDK (the easiest way to get the right one is IntelliJ SDK manager)

You can use one of the following methods:
- GRADLE_OPTS env variable `export GRADLE_OPTS="-Dorg.gradle.java.home=/path/to/your/desired/jdk"`
- additional property in gradle.properties (~/.gradle or pwd) `org.gradle.java.home=/path/to/your/desired/jdk`
- direct usage with -D param in terminal `./gradlew -Dorg.gradle.java.home=/path/to/your/desired/jdk wantedTask`

#### Troubleshooting:

1. If you see that even when using x86_64 JDK in logs gradle treats you as ARM machine. Do following:
    1. Clear workspace `git fetch; git clean -fdx; git reset --hard HEAD` (IMPORTANT: will sync local to git, and remove all progress)
    2. Clear gradle cache `rm -rf ~/.gradle` (IMPORTANT: will erase WHOLE gradle cache)
    3. Clear downloaded JVMs `rm -rf /path/to/used/jvm`
       (path to used jvm can be found in /run/logs/latest.log like this `Java is OpenJDK 64-Bit Server VM, version 1.8.0_442, running on Mac OS X:x86_64:15.3.2, installed at /this/is/the/path`)
    4. Repeat quickstart.
