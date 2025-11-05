#!/bin/bash

PATH_TO_CODE_BASE=$(pwd)

MAIN_CLASS="ar.edu.itba.pod.client.longestWait.Client"

java "$@" -cp "lib/jars/*" "$MAIN_CLASS"
