#!/bin/bash
mvn clean
mvn package -P Windows-32
mvn package -P Linux-32
mvn package -P Windows-64
mvn package -P Linux-64
mvn package -P Macosx-64
