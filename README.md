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
export MAVEN_OPTS="-Xms4g -Xmx4g"

# Build index
mvn exec:java -Dexec.classpathScope=compile -Dexec.mainClass=sewm.bdbox.search.InfomallIndexer -Dexec.args="--data=/Volumes/HPT8_56T/data --index=/Volumes/HPT8_56T/index --create"
```
