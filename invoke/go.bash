#!/bin/bash

java -jar ./target/demo-1.0.0-SNAPSHOT-standalone.jar  <<EOF
  { :a 1
    :b 2 }
EOF
