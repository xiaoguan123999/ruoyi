#!/bin/bash
# Put this file at: /home/ruoyi/backup-mysql.sh
# BT panel cron: daily 00:00
#   /bin/bash /home/ruoyi/backup-mysql.sh
set -eu

DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME='ry-vue'
DB_USER='ry-vue'
DB_PASS='mbsk45icas7SH4y4'

BACKUP_DIR=/home/ruoyi/backup/mysql
KEEP_DAYS=7
LOG=/home/ruoyi/backup/mysql/backup.log

mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR" 2>/dev/null || true

MYSQLDUMP=""
for c in /www/server/mysql/bin/mysqldump /usr/bin/mysqldump /usr/local/mysql/bin/mysqldump; do
  if [ -x "$c" ]; then
    MYSQLDUMP="$c"
    break
  fi
done
if [ -z "$MYSQLDUMP" ]; then
  MYSQLDUMP="$(command -v mysqldump || true)"
fi
if [ -z "$MYSQLDUMP" ]; then
  echo "$(date '+%F %T') mysqldump not found" | tee -a "$LOG"
  exit 1
fi

STAMP="$(date '+%Y%m%d_%H%M%S')"
SQL="$BACKUP_DIR/${DB_NAME}_${STAMP}.sql"
FILE="$SQL.gz"

export MYSQL_PWD="$DB_PASS"
"$MYSQLDUMP" \
  -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  --events \
  --default-character-set=utf8mb4 \
  --hex-blob \
  --no-tablespaces \
  --set-gtid-purged=OFF \
  "$DB_NAME" > "$SQL"
unset MYSQL_PWD

gzip -9 "$SQL"
chmod 600 "$FILE"

find "$BACKUP_DIR" -type f -name "${DB_NAME}_*.sql.gz" -mtime +"$KEEP_DAYS" -delete

SIZE="$(du -h "$FILE" | awk '{print $1}')"
echo "$(date '+%F %T') ok file=$FILE size=$SIZE keep=${KEEP_DAYS}d" | tee -a "$LOG"
