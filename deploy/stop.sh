#!/bin/bash
# 停止若依后端: sh stop.sh

APP_DIR=/www/wwwroot/43.160.234.29
PID_FILE="$APP_DIR/app.pid"

if [ ! -f "$PID_FILE" ]; then
  pkill -f "ruoyi-admin.jar" && echo "已按进程名停止" || echo "没有在运行"
  exit 0
fi

PID=$(cat "$PID_FILE")
if kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  sleep 2
  if kill -0 "$PID" 2>/dev/null; then
    kill -9 "$PID"
  fi
  echo "已停止 pid=$PID"
else
  echo "进程不存在 pid=$PID"
fi
rm -f "$PID_FILE"
