#!/bin/bash

classpath="$HOME/code/github/gatk/build/classes/main"
classpath="$HOME/code/github/htsjdk/build/classes/main:$classpath"
classpath="$HOME/code/github/gatk/build/install/gatk/lib:$classpath"
classpath="$HOME/code/github/gatk/build/libs/gatk-package-4.alpha.2-171-g92cb860-SNAPSHOT-local.jar:$classpath"
classpath=".:"$classpath

echo "$classpath"

java -Djava.library.path=. -classpath $classpath HelloJNI wgEncodeUwRepliSeqBg02esG1bAlnRep1.bam
