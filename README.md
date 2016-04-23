# big-data-box
This project aims at providing visualization and development APIs for Big Data in Box.

---
## Requirements
* [`maven3`](http://maven.apache.org/)
* [`jdk8`](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

---
## Setup for Development
[`eclipse-j2ee`](https://eclipse.org/downloads/) is highly recommended as development tool.
```bash
mvn eclipse:eclipse -Dwtpversion=2.0
```

---
## How to Run
```bash
mvn exec:java -Dexec.mainClass=<package.MainClass> -Dexec.classpathScope=compile -Dexec.args="arg1 arg2 ..."
```
