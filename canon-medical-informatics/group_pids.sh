#!/bin/sh

JAVA_OPTS="$JAVA_OPTS -server -Xms1G -Xmx1G -XX:+AlwaysPreTouch -XX:+OptimizeStringConcat -XX:+UseStringDeduplication -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Djava.awt.headless=true"
$JAVA_HOME/bin/java $JAVA_OPTS -cp ./target/classes canon.medical.informatics.ParsePatientName $@
