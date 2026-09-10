# Traffic Violation and E-Challan Management System

An in-memory Java 17/Maven application for registering vehicles, issuing electronic challans, calculating fines, accepting payments, and classifying vehicles by violation history.

## Build and test

```text
mvn clean package
```

## Run the demonstration

```text
mvn exec:java -Dexec.mainClass=com.mis.echallan.Main
```

The Jenkins pipeline runs checkout, tests, packaging, and publishes JUnit reports. Create a Jenkins Pipeline job pointed at this repository and select `Jenkinsfile` as the pipeline script.
