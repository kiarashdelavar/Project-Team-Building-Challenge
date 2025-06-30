# Frog Autonomous Navigation System – Team Building Challenge

This is a team software project built during the 1st year, Q4 module *Project Team Building Challenge* at Saxion University of Applied Sciences. The main objective was to develop an autonomous navigation system in Java for a space exploration vehicle called **The Frog**, navigating on a simulated asteroid environment.

## Project Description

An asteroid mission goes wrong after an avalanche blocks the return path to base and disables the Frog's navigation system. Our task as a team was to create a custom Java application (called the **Pilot**) to communicate with and control The Frog remotely.

The Frog must explore the map, find TNT and a Detonator, avoid obstacles, and drop the items near the base to clear the rocks — all with limited energy and using real-time radar data. The project involved message-based communication, real-time driving logic, and integration with a database for mission data tracking.

## Key Features

- Autonomous driving using real-time sensor and radar input
- Communication with The Frog using a custom protocol via `SaSaCommunicator`
- Radar-based obstacle detection and navigation
- Energy and charging management system
- Automated item pickup and drop logic (TNT and Detonator)
- Logging mission data to a database
- Sprint-based Scrum development using GitLab

## Technologies Used

- Java 21
- IntelliJ IDEA Ultimate Edition
- SaSaCommunicator Java Library (provided)
- JDBC (for database handling)
- GitLab (for version control and collaboration)
- Maven (for project build and dependency management)

## Project Structure

## Project Structure

```plaintext
FrogUpdated/
├── .idea/                         # IntelliJ project settings
├── .mvn/                          # Maven wrapper files
├── doc/                           # Project documentation
├── lib/                           # External libraries (e.g., SaSaCommunicator)
├── resources/                     # General resources
├── src/
│   └── main/
│       ├── java/
│       │   └── nl.saxion.ptbc/
│       │       ├── classes/
│       │       │   ├── Frog.java
│       │       │   ├── Location.java
│       │       │   ├── Map.java
│       │       │   ├── Obstacle.java
│       │       │   └── RadarPoint.java
│       │       ├── CSVLoader/
│       │       │   ├── CSVLoaderController.java
│       │       │   └── CSVLoaderException.java
│       │       ├── database/
│       │       │   ├── ObstacleDatabaseHandler.java
│       │       │   ├── SQLiteConnection.java
│       │       │   ├── SQLiteCreateTable.java
│       │       │   ├── SQLiteDropTable.java
│       │       │   ├── SQLiteInsertInTable.java
│       │       │   └── SQLiteSelectTable.java
│       │       ├── frog/
│       │       │   └── StartFrogApp.java
│       │       ├── groundControl/
│       │       │   ├── GroundControlApp.java
│       │       │   ├── GroundControlController.java
│       │       │   └── GroundControlUtils.java
│       │       ├── missionLog/
│       │       │   ├── MissionLog.java
│       │       │   ├── MissionLogController.java
│       │       │   ├── MissionLogDAO.java
│       │       │   ├── MissionStatus.java
│       │       │   └── ReplayMission.java
│       │       ├── pilot/
│       │       │   ├── PilotApp.java
│       │       │   ├── RadarSystem.java
│       │       │   └── RadarView.java
│       │       └── module-info.java
│       └── resources/             # Application-specific resources
├── target/                        # Maven build output
├── .gitignore                     # Git ignored files
├── mvnw / mvnw.cmd                # Maven wrapper scripts
├── pom.xml                        # Maven project descriptor
├── sasa.db                        # SQLite database file
├── obstacles.db-journal           # SQLite journal (auto-generated)
└── README.md                      # Project documentation

```


## How the System Works

1. The **PilotApp** (Java application) connects to The Frog via the SaSaServer using the `SaSaCommunicator` library.
2. The Pilot sends commands like `DRIVE`, `RADAR ON`, or `STATUS ON` to The Frog.
3. The Frog replies with:
   - Status data (position, angle, energy, charging)
   - Radar data (obstacle detection)
   - Events like picking up items or dropping them at the base
4. Based on this input, the Pilot decides how to move next and logs useful mission data.
5. The mission ends when the Frog returns to base with both the TNT and Detonator dropped near the obstruction.

## Learning Objectives

- Apply object-oriented programming and algorithm design in a real-world scenario
- Work with Java GUI, messaging protocols, and file/database handling
- Collaborate in a team using Scrum methodology and GitLab
- Deliver intermediate demos and reflect on team contributions
- Build a functional and intelligent autonomous system

## Team Members

This project was completed by a team of five students:

- Kiarash Delavar
- Kian Kamphuis
- Max De Croon 
- Emran Mohammadi 
- Elham Dawlati 

Each member contributed to design, implementation, code reviews, and sprint planning.

## How to Run

> Prerequisites:
- Java JDK 21
- IntelliJ IDEA Ultimate (or any Maven-compatible IDE)
- Frog executable simulator provided by the course
- SaSaCommunicator library (included in `lib/`)

### Steps:
1. Clone the repository
2. Open in IntelliJ IDEA
3. Make sure the simulator is running
4. Run `PilotApp` class from `nl.saxion.ptbc`
5. Observe Frog behavior and logs

---

**Note**: The Frog and SaSaServer executables are part of the course material and not included in this repository. You must have access to the provided simulation to run the full experience.

---

