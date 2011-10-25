#!/bin/bash
find . -name "*SNAPSHOT.jar" -exec cp {}  $JBOSS_HOME/modules/org/jboss/as/jdr/main/ \;
