# PvP AI (Fabric 1.21.11) - steps 1-2 scaffold

Scope: singleplayer, LAN host, or servers YOU own (add them to the allowlist). Out-of-scope = AI refuses to run.

Build: install Gradle 9.x (or open in IntelliJ), run `gradle wrapper --gradle-version 9.2.1`, then `./gradlew runClient`.
Keys: ] = toggle AI (starts OFF every launch), [ = config menu.
Working now: scope gate, observation builder, Strict controller, action applier + toggle masking, HUD, Cloth Config screen.
ML and Smart currently fall back to Strict. Next: metrics/Elo/tier (3), Python trainer + ONNX (4), Smart + online learning (5).
