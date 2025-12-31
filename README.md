# GitHub Actions Monitor CLI

Interactive command-line tool for monitoring GitHub Actions workflow runs in real-time.

## Overview

This project is a Java-based CLI tool that regularly queries GitHub workflow runs for a given repository and reports updates into the terminal. Each workflow run, job, and step is displayed in a concise, hierarchical format:

```bash
WORKFLOW | ID: 12345 | Name: CI Test | Branch: main | Commit: abcdef123 | Status: SUCCESS
  JOB | ID: 54321 | Name: build | Status: SUCCESS
    STEP | Name: Checkout code | Status: SUCCESS
    STEP | Name: Run tests | Status: SUCCESS
```

It reports:

- Workflows being queued or completed
- Jobs being started and finished
- Steps within jobs being started and completed
- Timestamps for start and completion
- Branch names, commit SHAs, and actor names

State is persisted between runs, so only new events are shown on subsequent runs.

## Technologies Used

- Java 21
- Spring Boot 3
- Picocli + JLine3: interactive CLI with command history and autocomplete
- WebClient: asynchronous GitHub API calls
- Jackson: JSON persistence for last-seen workflow runs
- Threading: background monitoring without blocking CLI
- Optional: Apache Commons for Levenshtein distance-based command suggestions

## Features

- Graceful termination via Ctrl+C
- Polling interval: 15 seconds
- First run: displays workflows from the last hour
- Subsequent runs: displays only new workflow runs since last execution
- Hierarchical display with indentation for readability
- Duplicate events are ignored within a single session
- JSON state file persisted in working directory

## Getting Started

### Prerequisites

- Java 21 or higher installed
- GitHub Personal Access Token (PAT) with `repo` scope for private repos or `public_repo` for public repos

### Installation

1. Clone the repository:

```bash
git clone https://github.com/TheAnh3/gh-actions-monitor.git
cd gh-actions-monitor
```
## Build the Project

The project is built using **Maven**.

2. Run a standard build:

```bash
mvn clean package
```
In some environments, the build may stop or fail during test execution (for example due to missing environment configuration or network-related issues).
In such cases, the project can be built while skipping tests:

```bash
mvn clean package -DskipTests
```

After a successful build, the executable JAR file will be created in the target directory:

```bash
target/gh-actions-monitor-0.0.1-SNAPSHOT.jar
```
## Run the Application

3. Navigate to the project root directory and start the CLI tool using:

```bash
java -jar target/gh-actions-monitor-0.0.1-SNAPSHOT.jar
```
This launches an interactive command-line shell.

## CLI Commands

After starting the application, an interactive command-line shell is opened.

To list all available commands, type:

```bash
help
```

Available Commands: Command	Description
- *monitor* <owner/repo> <token> = Start monitoring GitHub Actions for a repository
- *stop* = Stop the active monitoring process
- *status* = Show current monitoring status
- *clear* = Clear the terminal output
- *exit* = Exit the application

**The CLI provides**:

- *command history*
- *tab completion*
- *inline help*
- *smart command suggestions (Levenshtein distance)*

First Run vs Subsequent Runs
First Run

*If the repository has no stored state(:

- only workflow runs from the last hour are shown
- prevents flooding the output with historical data

**Subsequent Runs**:

- last processed workflow run ID is loaded from disk
- only newer workflow runs are reported
- duplicate events are avoided

*State Persistence*: 
Monitoring state is stored locally in a JSON file.

```bash
{
  "TheAnh3/gh-actions-monitor": 20609051079
}
```
**Details**:
- file name: *monitor_state.json*
- location: current working directory
- format: repository → last workflow run ID
- State is written atomically to prevent corruption.

**Graceful Shutdown**: 
Shutdown methods:
- stop command
- Ctrl+D + Ctrl+C
