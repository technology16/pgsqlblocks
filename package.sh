#!/bin/bash
mvn clean
mvn package -P win32
mvn package -P linux86
mvn package -P win32_64
mvn package -P linux64
