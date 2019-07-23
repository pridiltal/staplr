#!/usr/bin/env bash

UBUNTUCP=/usr/share/java/bcprov.jar:/usr/share/java/commons-lang3.jar
GENTOOCP=/usr/share/bcprov/lib/bcprov.jar:/usr/share/commons-lang-3.6/lib/commons-lang.jar

java -cp $UBUNTUCP:$GENTOOCP:. com.gitlab.pdftk_java.pdftk "$@"
