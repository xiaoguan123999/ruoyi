#!/bin/bash
# 若依后端启动脚本，上传到 /www/wwwroot/43.160.234.29/ 后执行:
#   sh start.sh          # 默认生产 druid,prod
#   sh start.sh test     # 测试   druid,test
#   sh start.sh prod     # 生产   druid,prod
#   sh start.sh dev      # 开发   druid,dev
# 生产/测试库账号、Redis、Token 可写在同目录 .env，R2 写在 .r2.env

APP_DIR=/www/wwwroot/43.160.234.29
JAR="$APP_DIR/ruoyi-admin.jar"
LOG="$APP_DIR/app.log"
PID_FILE="$APP_DIR/app.pid"
UPLOAD_DIR=/www/wwwroot/uploadPath
ENV="${1:-prod}"

case "$ENV" in
  dev|test|prod) ;;
  *)
    echo "环境只能是: dev | test | prod"
    echo "用法: sh start.sh [dev|test|prod]"
    exit 1
    ;;
esac

JAVA_CMD=""
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
fi
if [ -z "$JAVA_CMD" ]; then
  JAVA_CMD=$(command -v java)
fi
if [ -z "$JAVA_CMD" ]; then
  for d in /www/server/java/*/bin/java /usr/lib/jvm/*/bin/java; do
    if [ -x "$d" ]; then
      JAVA_CMD="$d"
      break
    fi
  done
fi

if [ -z "$JAVA_CMD" ]; then
  echo "未找到 java。在宝塔终端执行："
  echo "  apt-get update && apt-get install -y openjdk-8-jdk"
  echo "装完再执行: java -version && sh start.sh"
  exit 1
fi
echo "使用 Java: $JAVA_CMD"
echo "启动环境: druid,$ENV"

if [ ! -f "$JAR" ]; then
  echo "找不到 jar: $JAR"
  exit 1
fi

if [ -f "$PID_FILE" ]; then
  OLD_PID=$(cat "$PID_FILE")
  if kill -0 "$OLD_PID" 2>/dev/null; then
    echo "已经在运行, pid=$OLD_PID"
    echo "日志: $LOG"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

mkdir -p "$UPLOAD_DIR"
cd "$APP_DIR" || exit 1

if [ -f "$APP_DIR/.env" ]; then
  set -a
  . "$APP_DIR/.env"
  set +a
  echo "已加载 .env"
fi

if [ -f "$APP_DIR/.r2.env" ]; then
  set -a
  . "$APP_DIR/.r2.env"
  set +a
  echo "已加载 R2 环境变量"
fi

nohup "$JAVA_CMD" -Xms256m -Xmx512m -jar "$JAR" \
  --spring.profiles.active="druid,$ENV" \
  --server.port=8080 \
  --ruoyi.profile="$UPLOAD_DIR" \
  >> "$LOG" 2>&1 &

echo $! > "$PID_FILE"
echo "已启动 pid=$(cat "$PID_FILE")  profiles=druid,$ENV"
echo "看日志: tail -f $LOG"
echo "出现 Started RuoYiApplication 后打开 http://43.160.234.29"
