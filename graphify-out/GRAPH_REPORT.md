# Graph Report - C:\Users\AndreiStan\Documents\GitHub\T620-SPF-NIDMOT-SW  (2026-08-04)

## Corpus Check
- Corpus is ~18,790 words - fits in a single context window. You may not need a graph.

## Summary
- 445 nodes · 871 edges · 20 communities (18 shown, 2 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 74 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Drive Odometry & Kinematics
- Autonomous Commands & Operator Interface
- Shooter Subsystem & Commands
- Drive Constants & Swerve Config
- REV/SPARK Motor Controllers
- Swerve Module Control
- Indexer Subsystem & Commands
- Intake Pivot Control
- PathPlanner Navigation
- CI/CD & Documentation
- Intake Roller Subsystem
- Gyro IO Interface
- Robot Lifecycle
- Hood Subsystem
- IDE & Tool Config
- Gradle Build Scripts
- Main Entry Point
- Indexer Constants

## God Nodes (most connected - your core abstractions)
1. `Drive` - 39 edges
2. `Shooter` - 25 edges
3. `Hood` - 22 edges
4. `Module` - 21 edges
5. `IntakePivot` - 20 edges
6. `Robot` - 16 edges
7. `Indexer` - 16 edges
8. `RobotContainer` - 15 edges
9. `ModuleIOSpark` - 15 edges
10. `ModuleIO` - 14 edges

## Surprising Connections (you probably didn't know these)
- `AdvantageKit License (Littleton Robotics BSD-style)` --semantically_similar_to--> `WPILib License (FIRST BSD-style)`  [INFERRED] [semantically similar]
  AdvantageKit-License.md → WPILib-License.md
- `Gradle Build Step` --conceptually_related_to--> `DriveConstants.java`  [INFERRED]
  .github/workflows/build.yml → src/main/java/frc/robot/subsystems/drive/DRIVE_BRINGUP.md
- `AdvantageKit v26.0.2 Spark Swerve Template` --references--> `AdvantageKit License (Littleton Robotics BSD-style)`  [INFERRED]
  src/main/java/frc/robot/subsystems/drive/DRIVE_BRINGUP.md → AdvantageKit-License.md
- `Robot` --references--> `RobotContainer`  [EXTRACTED]
  src/main/java/frc/robot/Robot.java → src/main/java/frc/robot/RobotContainer.java
- `RobotContainer` --references--> `Drive`  [EXTRACTED]
  src/main/java/frc/robot/RobotContainer.java → src/main/java/frc/robot/subsystems/drive/Drive.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Drive Characterization Pipeline (FF + Wheel Radius + PID Tuning)** — src_main_java_frc_robot_subsystems_drive_drive_bringup_md_ff_characterization, src_main_java_frc_robot_subsystems_drive_drive_bringup_md_wheel_radius_characterization, src_main_java_frc_robot_subsystems_drive_drive_bringup_md_driveconstants, src_main_java_frc_robot_subsystems_drive_drive_bringup_md_advantagescope [INFERRED 0.85]
- **Drive Hardware Configuration Entities (MAXSwerve, SPARK MAX, Pigeon2, CAN IDs)** — src_main_java_frc_robot_subsystems_drive_drive_bringup_md_rev_maxswerve, src_main_java_frc_robot_subsystems_drive_drive_bringup_md_spark_max, src_main_java_frc_robot_subsystems_drive_drive_bringup_md_pigeon2_imu, src_main_java_frc_robot_subsystems_drive_drive_bringup_md_can_id_assignment, src_main_java_frc_robot_subsystems_drive_drive_bringup_md_driveconstants [INFERRED 0.85]

## Communities (20 total, 2 thin omitted)

### Community 0 - "Drive Odometry & Kinematics"
Cohesion: 0.08
Nodes (30): AutoLogOutput, ChassisSpeeds, Direction, GyroIOInputsAutoLogged, Lock, Matrix, N1, N3 (+22 more)

### Community 1 - "Autonomous Commands & Operator Interface"
Cohesion: 0.07
Nodes (23): CommandXboxController, HoodIOInputsAutoLogged, IntakePivotIOInputsAutoLogged, LoggedDashboardChooser, Autos, Command, HoodCommands, Command (+15 more)

### Community 2 - "Shooter Subsystem & Commands"
Cohesion: 0.10
Nodes (12): ShooterIOInputsAutoLogged, Command, ShooterCommands, Override, PIDController, Shooter, AutoLog, ShooterIO (+4 more)

### Community 3 - "Drive Constants & Swerve Config"
Cohesion: 0.09
Nodes (24): DCMotorSim, RobotConfig, DriveConstants, DCMotor, Rotation2d, Translation2d, DriveMotor, NEO_V1 (+16 more)

### Community 4 - "REV/SPARK Motor Controllers"
Cohesion: 0.11
Nodes (17): AbsoluteEncoder, Debouncer, DoubleConsumer, Notifier, RelativeEncoder, REVLibError, SparkClosedLoopController, Override (+9 more)

### Community 5 - "Swerve Module Control"
Cohesion: 0.12
Nodes (10): ModuleIOInputsAutoLogged, Alert, Rotation2d, SwerveModulePosition, SwerveModuleState, Module, AutoLog, Rotation2d (+2 more)

### Community 6 - "Indexer Subsystem & Commands"
Cohesion: 0.13
Nodes (11): IndexerIOInputsAutoLogged, IndexerCommands, Command, Indexer, Override, IndexerIO, IndexerIOInputs, AutoLog (+3 more)

### Community 7 - "Intake Pivot Control"
Cohesion: 0.11
Nodes (9): Override, IntakePivotIO, IntakePivotIOInputs, AutoLog, IdleMode, IntakePivotIOSpark, IdleMode, Override (+1 more)

### Community 8 - "PathPlanner Navigation"
Cohesion: 0.19
Nodes (13): GoalEndState, LocalADStar, LoggableInputs, LogTable, Pair, PathConstraints, Pathfinder, PathPlannerPath (+5 more)

### Community 9 - "CI/CD & Documentation"
Cohesion: 0.11
Nodes (22): GitHub Actions Build Job, Gradle Build Step, WPILib roboRIO Cross-Compile Container (2024-22.04), AdvantageKit License (Littleton Robotics BSD-style), AdvantageKit v26.0.2 Spark Swerve Template, AdvantageScope (Log Viewer / Signal Plotting Tool), CAN ID Assignment (Drive Harley-2025 Spreadsheet), Drive Subsystem Bringup Procedure (+14 more)

### Community 10 - "Intake Roller Subsystem"
Cohesion: 0.13
Nodes (10): IntakeRollerIOInputsAutoLogged, IntakeRoller, Override, IntakeRollerIO, IntakeRollerIOInputs, AutoLog, IntakeRollerIOSpark, Override (+2 more)

### Community 11 - "Gyro IO Interface"
Cohesion: 0.15
Nodes (13): AHRS, Angle, AngularVelocity, Pigeon2, GyroIO, GyroIOInputs, AutoLog, Rotation2d (+5 more)

### Community 12 - "Robot Lifecycle"
Cohesion: 0.23
Nodes (4): LoggedRobot, Command, Override, Robot

### Community 13 - "Hood Subsystem"
Cohesion: 0.22
Nodes (6): HoodIOInputs, AutoLog, HoodIOSpark, IdleMode, Override, SparkBase

### Community 14 - "IDE & Tool Config"
Cohesion: 0.20
Nodes (9): enabledPlugins, mattpocock-skills@mattpocock-skills, extraKnownMarketplaces, mattpocock-skills, autoUpdate, source, $schema, repo (+1 more)

### Community 15 - "Gradle Build Scripts"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **19 isolated node(s):** `$schema`, `source`, `repo`, `autoUpdate`, `mattpocock-skills@mattpocock-skills` (+14 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drive` connect `Drive Odometry & Kinematics` to `Autonomous Commands & Operator Interface`, `Intake Roller Subsystem`, `Gyro IO Interface`, `Swerve Module Control`?**
  _High betweenness centrality (0.180) - this node is a cross-community bridge._
- **Why does `RobotContainer` connect `Autonomous Commands & Operator Interface` to `Drive Odometry & Kinematics`, `Shooter Subsystem & Commands`, `Indexer Subsystem & Commands`, `Intake Roller Subsystem`, `Robot Lifecycle`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Why does `ModuleIO` connect `Swerve Module Control` to `Drive Odometry & Kinematics`, `Autonomous Commands & Operator Interface`, `Drive Constants & Swerve Config`, `REV/SPARK Motor Controllers`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **What connects `$schema`, `source`, `repo` to the rest of the system?**
  _19 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drive Odometry & Kinematics` be split into smaller, more focused modules?**
  _Cohesion score 0.07796610169491526 - nodes in this community are weakly interconnected._
- **Should `Autonomous Commands & Operator Interface` be split into smaller, more focused modules?**
  _Cohesion score 0.06954887218045112 - nodes in this community are weakly interconnected._
- **Should `Shooter Subsystem & Commands` be split into smaller, more focused modules?**
  _Cohesion score 0.0953058321479374 - nodes in this community are weakly interconnected._
