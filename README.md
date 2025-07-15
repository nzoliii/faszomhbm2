![fhbm2_title](https://raw.githubusercontent.com/nzoliii/faszomhbm2/master/static_banners/fhbm2_title.png)

# Description
The sequel to [FaszomHBM](https://github.com/nzoliii/faszomhbm).<br>

I named this mod Faszom HBM because when I first started messing with Hbm’s Nuclear Tech Mod, I had no idea how anything worked, and it just kept making me mad, so I kept saying “faszom hbm,” which is basically the Hungarian version of “fucking hbm.” That saying stuck with me when I made my own fork.

This one’s called Faszom HBM 2 because the first version was a complete mess. I had no clue what I was doing, broke everything, and it got super unstable. So I re-forked HBM and rewrote my changes, still not properly, but at least it kinda works now.

This mod is based off of [MrNorwood's Community Edition mod](https://github.com/MisterNorwood/Hbm-s-Nuclear-Tech-CE) which is based off of [TheSlize's Community Edition<br>
mod](https://github.com/TheSlize/Hbm-s-Nuclear-Tech-GIT) which is based off of [Alcater's NTM Extended Edition mod](https://github.com/Alcatergit/Hbm-s-Nuclear-Tech-GIT) which is based off of [TheOriginalGolem's fork](https://github.com/TheOriginalGolem/Hbm-s-Nuclear-Tech-GIT) which<br>
is based off of [Drillgon200's port](https://github.com/Drillgon200/Hbm-s-Nuclear-Tech-GIT) which is based off of the official [HBM's Nuclear Tech Mod](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT).<br>
Due to the amount and complexity of the recipes of this mod [JEI (Just Enough Items)](https://www.curseforge.com/minecraft/mc-mods/jei) is strongly recommended.<br>

# Features
HBM's Nuclear Tech Mod is all about technology, atomic science and nuclear weapons. It features an expansive tech tree of various processing and utility machines, craftable bombs and missiles, guns, and abandoned structures to explore. Nukes are no longer just green TNT blocks. You will have to synthesize new types of explosives and enrich uranium before you can send nuclear missiles towards villagers. This version of the HBM mods takes things to the next level (no it isn't). You can now hear real vomit sounds from me and my friends (yes, we are idiots). There are new items exclusive to the Faszom HBM series including nicotine pouches, abált szalonna, my pálinka Som, and The Copper Pig. The main menu has been retextured inspired by the Terraria Calamity Mod. In summary, this mod aims to be the funniest version of the HBM mods. You can find out the rest of the features by downloading this mod from [Curseforge (I don't really update the Curseforge page)](https://www.curseforge.com/minecraft/mc-mods/faszomhbm2), or by building it from source.

# Development guide:
For development Java 17 is used.

We use [Jabel](https://github.com/bsideup/jabel) to target Java 8 bytecode seamlessly (make sure you don't use APIs introduced in Java 9+)

**General quickstart:**
1. Clone this repository.
2. Prepare JDK (preferably 17+).
3. Run task `setupDecompWorkspace` (this will prepare the workspace, including MC sources deobfuscation)
4. Ensure everything is OK. Run task `runClient` (should open minecraft client with mod loaded)

- Always use `gradlew` (Linux/MACOS) or `gradlew.bat` (Win) and not `gradle` for tasks. So each dev will have consistent environment.

**Development quirks for Apple M-chip machines:**

Since there are no natives for ARM arch, therefore you will have to use x86_64 JDK (the easiest way to get the right one is IntelliJ SDK manager)

You can use one of the following methods:
- GRADLE_OPTS env variable `export GRADLE_OPTS="-Dorg.gradle.java.home=/path/to/your/desired/jdk"`
- additional property in gradle.properties (~/.gradle or pwd) `org.gradle.java.home=/path/to/your/desired/jdk`
- direct usage with -D param in terminal `./gradlew -Dorg.gradle.java.home=/path/to/your/desired/jdk wantedTask`

**Troubleshooting:**

1. If you see that even when using x86_64 JDK in logs gradle treats you as ARM machine. Do following:
    1. Clear workspace `git fetch; git clean -fdx; git reset --hard HEAD` (IMPORTANT: will sync local to git, and remove all progress)
    2. Clear gradle cache `rm -rf ~/.gradle` (IMPORTANT: will erase WHOLE gradle cache)
    3. Clear downloaded JVMs `rm -rf /path/to/used/jvm`
       (path to used jvm can be found in /run/logs/latest.log like this `Java is OpenJDK 64-Bit Server VM, version 1.8.0_442, running on Mac OS X:x86_64:15.3.2, installed at /this/is/the/path`)
    4. Repeat quickstart.