#!/bin/bash
grep '<version>' pom.xml | head -n 1 | sed -e 's|<version>||g' -e 's|</version>||g' |  xargs echo | sed 's/-SNAPSHOT$//'