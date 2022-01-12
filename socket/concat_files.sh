#!/bin/bash

for dir in $(find . -type f -name "*.java")
do
  (
    echo $dir
    echo $dir >> result.java
    cat $dir >> result.java
    echo "" >> result.java
    echo "" >> result.java
  )
done
