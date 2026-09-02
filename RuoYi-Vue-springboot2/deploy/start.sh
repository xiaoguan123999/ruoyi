#!/bin/bash
# Upload to /home/ruoyi/start.sh
# Usage: /home/ruoyi/start.sh
set -euo pipefail

APP_HOME=/home/ruoyi
JAR="$APP_HOME/ruoyi-admin.jar"
LOG="$APP_HOME/app.log"
PID_FILE="$APP_HOME/app.pid"
SECRET_FILE="$APP_HOME/token.secret"

if [ ! -f "$JAR" ]; then
  echo "missing $JAR"
  exit 1
fi

mkdir -p "$APP_HOME/uploadPath"

if [ ! -f "$SECRET_FILE" ]; then
  openssl rand -hex 32 > "$SECRET_FILE"
  chmod 600 "$SECRET_FILE"
fi

export MYSQL_USER='ry-vue'
export MYSQL_PASSWORD='mbsk45icas7SH4y4'
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=
export TOKEN_SECRET="$(tr -d '\n' < "$SECRET_FILE")"
export RUOYI_PROFILE="$APP_HOME/uploadPath"
export REFERER_DOMAINS='api.rgoslz.com,rgoslz.com,www.rgoslz.com,129.226.55.48'
export R2_ENDPOINT='https://f71f5e85dae20a74d1b683bde66520d7.r2.cloudflarestorage.com'
export R2_BUCKET='project-bucket'
export R2_ACCESS_KEY='8591f70cef503691d7ef27228b111443'
export R2_SECRET_KEY='a6b9f4ca585bb7e486d5a92f0afa389d18de434312e96855810cd507e9193e86'
export R2_PUBLIC_URL='https://pub-ff6db24eced24830803721122df49551.r2.dev'

if [ -f "$PID_FILE" ]; then
  old="$(cat "$PID_FILE" || true)"
  if [ -n "${old:-}" ] && kill -0 "$old" 2>/dev/null; then
    echo "already running pid=$old"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

JAVA_BIN=""
if command -v java >/dev/null 2>&1; then
  JAVA_BIN="$(command -v java)"
else
  shopt -s nullglob
  for c in /www/server/java/jdk1.8*/bin/java /www/server/java/*/bin/java /usr/lib/jvm/jre-1.8*/bin/java /usr/lib/jvm/java-1.8*/bin/java; do
    if [ -x "$c" ]; then
      JAVA_BIN="$c"
      break
    fi
  done
  shopt -u nullglob
fi
if [ -z "$JAVA_BIN" ]; then
  echo "java not found. run: yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel"
  exit 1
fi
echo "using $JAVA_BIN"
"$JAVA_BIN" -version

cd "$APP_HOME"
nohup "$JAVA_BIN" -Xms1g -Xmx4g -jar "$JAR" \
  --spring.profiles.active=druid,prod \
  >> "$LOG" 2>&1 &
echo $! > "$PID_FILE"
echo "started pid=$(cat "$PID_FILE") log=$LOG"
