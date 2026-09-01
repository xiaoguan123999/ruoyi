#!/usr/bin/env bash
# Fresh empty MySQL import. DROPs RuoYi/biz tables. Do NOT run on live data.
#
# Usage:
#   MYSQL_HOST=127.0.0.1 MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PWD='secret' MYSQL_DB='ry-vue' ./sql/prod_install.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
DB="${MYSQL_DB:-ry-vue}"
HOST="${MYSQL_HOST:-127.0.0.1}"
PORT="${MYSQL_PORT:-3306}"
USER="${MYSQL_USER:-root}"

MYSQL_ARGS=(--default-character-set=utf8mb4 --host="$HOST" --port="$PORT" --user="$USER")
if [ -n "${MYSQL_PWD:-}" ]; then
  MYSQL_ARGS+=(--password="$MYSQL_PWD")
fi

echo "==> create database \`${DB}\`"
mysql "${MYSQL_ARGS[@]}" -e "CREATE DATABASE IF NOT EXISTS \`${DB}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

while IFS= read -r line || [ -n "$line" ]; do
  case "$line" in
    ""|\#*) continue ;;
  esac
  file="$ROOT/$line"
  if [ ! -f "$file" ]; then
    echo "missing: $file" >&2
    exit 1
  fi
  echo "==> $line"
  mysql "${MYSQL_ARGS[@]}" "$DB" < "$file"
done < "$ROOT/prod_install_order.txt"

echo "==> done. db=${DB}"
echo "Default admin: admin / admin123  (change password after login)"
