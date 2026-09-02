#!/bin/bash
set -euo pipefail
APP_HOME=/home/ruoyi
PID_FILE="$APP_HOME/app.pid"
if [ ! -f "$PID_FILE" ]; then
  echo "not running"
  exit 0
fi
pid="$(cat "$PID_FILE")"
if kill -0 "$pid" 2>/dev/null; then
  kill "$pid"
  echo "stopped pid=$pid"
else
  echo "stale pid file"
fi
rm -f "$PID_FILE"
