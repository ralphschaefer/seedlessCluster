#!/usr/bin/env bash

# java -DINSTANCE=1 -DREMOTEHOST=127.0.0.11 -jar ./target/scala-2.13/clusterDemo.jar

tmux new-session "java -DINSTANCE=1 -DREMOTEHOST=127.0.0.11 -jar ./target/scala-2.13/clusterDemo.jar" \; \
     split-window -v "sleep 1; java -DINSTANCE=2 -DREMOTEHOST=127.0.0.12 -jar ./target/scala-2.13/clusterDemo.jar" \; \
     split-window -h "sleep 2; java -DINSTANCE=3 -DREMOTEHOST=127.0.0.13 -jar ./target/scala-2.13/clusterDemo.jar" \; \
     select-pane -t 0 \; \
     split-window -h  "sleep 3; java -DINSTANCE=4 -DREMOTEHOST=127.0.0.14 -jar ./target/scala-2.13/clusterDemo.jar" \;