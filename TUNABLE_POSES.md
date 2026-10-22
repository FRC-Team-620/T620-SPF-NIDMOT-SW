# Editing `Constants.Poses` via SmartDashboard / Elastic

this is a testing thing and is meant to be deleted

`Constants.Poses.hubBLUE` / `hubRED` ([Constants.java:35-38](src/main/java/frc/robot/Constants.java#L35-L38)) are `static final Pose2d` fields, so they're baked in at compile time. Publishing to NetworkTables alone won't change them — they need to be replaced with values read live from NT.

## Option 1: Plain SmartDashboard numbers

Matches the existing tuning style already used for Hood/IntakePivot/Indexer ([RobotContainer.java:211](src/main/java/frc/robot/RobotContainer.java#L211)).

- Replace each `Pose2d` constant with a method that calls `SmartDashboard.getNumber("Poses/hubBLUE/x", default)` (and `/y`, `/rotationDeg`) and builds a `Pose2d` from the three values.
- Simple, no new dependencies.
- Downside: three raw numeric keys per pose, no pose widget on Elastic, easy to typo a key.

## Option 2: AdvantageKit `LoggedTunableNumber`

The pattern from Littleton Robotics' AdvantageKit example projects (credited in the `Constants.java` header). Not currently in this repo — would need to be added as a small utility class.

- Wraps a tunable value, logs it to AdvantageScope/NT, and persists the last value set over NT.
- Works correctly across REAL/SIM/REPLAY modes, which plain SmartDashboard doesn't guarantee.
- More idiomatic given the codebase is already on AdvantageKit.

## Important caveat either way

`Autos.centerFrontShoot` ([Autos.java:29](src/main/java/frc/robot/commands/Autos.java#L29)) reads the pose only once, when the auto command is *built*. A value changed on Elastic takes effect the next time autonomous is constructed/selected — not instantaneously mid-match. That's normally the desired behavior for this kind of practice-field tuning.
