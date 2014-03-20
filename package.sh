#!/bin/bash
mvn assembly:single -P win32
mvn assembly:single -P linux86
mvn assembly:single -P win32_64
mvn assembly:single -P linux64
