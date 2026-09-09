#!/bin/sh
# Attach historical members with no parent under top member 10001.
# Run on the server, one step at a time. Send each step output for confirmation
# before running the next step.
#
#   sh sql/biz_member_attach_orphan_to_top.sh preview
#   sh sql/biz_member_attach_orphan_to_top.sh backup
#   APPLY=YES sh sql/biz_member_attach_orphan_to_top.sh apply
#   sh sql/biz_member_attach_orphan_to_top.sh verify
#
# Optional env: MYSQL_HOST MYSQL_PORT MYSQL_USER MYSQL_PWD MYSQL_DB MYSQL_BIN TOP_ID BACKUP_DIR
set -eu

HOST="${MYSQL_HOST:-127.0.0.1}"
PORT="${MYSQL_PORT:-3306}"
USER="${MYSQL_USER:-ry-vue}"
DB="${MYSQL_DB:-ry-vue}"
TOP_ID="${TOP_ID:-10001}"
BACKUP_DIR="${BACKUP_DIR:-.}"
STEP="${1:-help}"

if [ -z "${MYSQL_PWD:-}" ] && [ -n "${MYSQL_PASSWORD:-}" ]; then
  MYSQL_PWD="$MYSQL_PASSWORD"
fi
export MYSQL_PWD="${MYSQL_PWD:-mbsk45icas7SH4y4}"

find_bin() {
  name="$1"
  if [ -n "${MYSQL_BIN:-}" ] && [ "$name" = mysql ]; then
    echo "$MYSQL_BIN"
    return 0
  fi
  if [ -n "${MYSQLDUMP_BIN:-}" ] && [ "$name" = mysqldump ]; then
    echo "$MYSQLDUMP_BIN"
    return 0
  fi
  for c in "/www/server/mysql/bin/$name" "/usr/bin/$name" "/usr/local/mysql/bin/$name"; do
    if [ -x "$c" ]; then
      echo "$c"
      return 0
    fi
  done
  command -v "$name" 2>/dev/null || true
}

MYSQL_CLIENT="$(find_bin mysql)"
MYSQLDUMP_CLIENT="$(find_bin mysqldump)"

require_mysql() {
  if [ -z "$MYSQL_CLIENT" ]; then
    echo "mysql client not found" >&2
    exit 1
  fi
}

run_sql() {
  "$MYSQL_CLIENT" \
    --default-character-set=utf8mb4 \
    --host="$HOST" \
    --port="$PORT" \
    --user="$USER" \
    --password="$MYSQL_PWD" \
    --table \
    "$DB"
}

sql_prelude() {
  cat <<EOF
SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;
SET @top_id := ${TOP_ID};
SET @top_id_str := CAST(@top_id AS CHAR CHARSET utf8mb4) COLLATE utf8mb4_general_ci;
EOF
}

print_banner() {
  echo "======== $1 ========"
  echo "db=${DB} host=${HOST}:${PORT} user=${USER} top_id=${TOP_ID}"
  echo "time=$(date '+%F %T')"
  echo
}

usage() {
  cat <<'EOF'
Usage (one step at a time, send output before next step):

  sh sql/biz_member_attach_orphan_to_top.sh preview
  sh sql/biz_member_attach_orphan_to_top.sh backup
  APPLY=YES sh sql/biz_member_attach_orphan_to_top.sh apply
  sh sql/biz_member_attach_orphan_to_top.sh verify

preview  read-only counts, no writes
backup   dump biz_member only
apply    write parent_id / ancestors (needs APPLY=YES)
verify   check chain after apply
EOF
}

preview_sql() {
  sql_prelude
  cat <<'EOF'
SELECT 'counts' AS step;
SELECT
  (SELECT COUNT(*) FROM biz_member) AS members,
  (SELECT COUNT(*) FROM biz_member WHERE member_id = @top_id) AS top_exists,
  (SELECT parent_id FROM biz_member WHERE member_id = @top_id) AS top_parent_id,
  (SELECT ancestors FROM biz_member WHERE member_id = @top_id) AS top_ancestors,
  (SELECT COUNT(*) FROM biz_member WHERE member_id <> @top_id AND parent_id IS NULL) AS orphan_roots,
  (SELECT COUNT(*) FROM biz_member
    WHERE member_id <> @top_id
      AND FIND_IN_SET(@top_id_str, IFNULL(ancestors, '0')) = 0) AS not_under_top,
  (SELECT COUNT(*) FROM biz_member WHERE parent_id = @top_id) AS top_direct_now,
  (SELECT COUNT(*) FROM biz_member WHERE parent_id = @top_id)
    + (SELECT COUNT(*) FROM biz_member WHERE member_id <> @top_id AND parent_id IS NULL) AS top_direct_after;

SELECT 'samples' AS step;
SELECT member_id, parent_id, ancestors
FROM biz_member
WHERE member_id IN (10001, 10002, 10003, 10005, 32340, 35698, 36107, 42671)
ORDER BY member_id;

SELECT 'chain_health' AS step;
SELECT COUNT(*) AS ancestor_mismatch
FROM biz_member m
JOIN biz_member p ON p.member_id = m.parent_id
WHERE m.ancestors <> CONCAT(IFNULL(NULLIF(p.ancestors, ''), '0'), ',', p.member_id);

SELECT COUNT(*) AS missing_parent
FROM biz_member m
LEFT JOIN biz_member p ON p.member_id = m.parent_id
WHERE m.parent_id IS NOT NULL AND p.member_id IS NULL;
EOF
}

apply_sql() {
  sql_prelude
  cat <<'EOF'
SELECT ancestors INTO @top_anc FROM biz_member WHERE member_id = @top_id;
SET @top_prefix := CONCAT(IFNULL(NULLIF(@top_anc, ''), '0'), ',', @top_id_str);

START TRANSACTION;

UPDATE biz_member
SET parent_id = @top_id,
    ancestors = @top_prefix
WHERE member_id <> @top_id
  AND parent_id IS NULL;
SELECT ROW_COUNT() AS updated_roots;

UPDATE biz_member
SET ancestors = CONCAT(@top_prefix, SUBSTRING(ancestors, 2))
WHERE member_id <> @top_id
  AND parent_id IS NOT NULL
  AND IFNULL(ancestors, '') LIKE '0,%'
  AND FIND_IN_SET(@top_id_str, ancestors) = 0;
SELECT ROW_COUNT() AS updated_descendants;

SELECT
  (SELECT COUNT(*) FROM biz_member WHERE parent_id IS NULL) AS remaining_roots,
  (SELECT COUNT(*) FROM biz_member
    WHERE member_id <> @top_id
      AND FIND_IN_SET(@top_id_str, IFNULL(ancestors, '0')) = 0) AS still_not_under_top,
  (SELECT COUNT(*) FROM biz_member WHERE parent_id = @top_id) AS top_direct_count;

COMMIT;
EOF
}

verify_sql() {
  sql_prelude
  cat <<'EOF'
SELECT 'coverage' AS step;
SELECT COUNT(*) AS remaining_roots FROM biz_member WHERE parent_id IS NULL;
SELECT member_id, parent_id, ancestors FROM biz_member WHERE parent_id IS NULL;
SELECT COUNT(*) AS still_not_under_top
FROM biz_member
WHERE member_id <> @top_id
  AND FIND_IN_SET(@top_id_str, IFNULL(ancestors, '0')) = 0;
SELECT COUNT(*) AS top_direct FROM biz_member WHERE parent_id = @top_id;

SELECT 'samples' AS step;
SELECT member_id, parent_id, ancestors
FROM biz_member
WHERE member_id IN (10001, 10002, 10003, 10005, 32340, 35698, 36107, 42671)
ORDER BY member_id;

SELECT 'chain_health' AS step;
SELECT COUNT(*) AS ancestor_mismatch
FROM biz_member m
JOIN biz_member p ON p.member_id = m.parent_id
WHERE m.ancestors <> CONCAT(IFNULL(NULLIF(p.ancestors, ''), '0'), ',', p.member_id);

SELECT COUNT(*) AS missing_parent
FROM biz_member m
LEFT JOIN biz_member p ON p.member_id = m.parent_id
WHERE m.parent_id IS NOT NULL AND p.member_id IS NULL;

SELECT COUNT(*) AS parent_is_self FROM biz_member WHERE parent_id = member_id;
SELECT COUNT(*) AS self_in_ancestors
FROM biz_member
WHERE FIND_IN_SET(CAST(member_id AS CHAR CHARSET utf8mb4) COLLATE utf8mb4_general_ci, IFNULL(ancestors, '0')) > 0;

SELECT MAX(CHAR_LENGTH(ancestors)) AS max_anc_len
FROM biz_member;

SELECT 'team_count' AS step;
SELECT
  (SELECT COUNT(*) FROM biz_member t
    WHERE FIND_IN_SET('10001', t.ancestors) AND IFNULL(t.test_flag, '0') = '0') AS find_10001,
  (SELECT COUNT(*) FROM biz_member t
    WHERE (t.ancestors = '0,10001' OR t.ancestors LIKE '0,10001,%')
      AND IFNULL(t.test_flag, '0') = '0') AS prefix_10001,
  (SELECT COUNT(*) FROM biz_member t
    WHERE FIND_IN_SET('35698', t.ancestors) AND IFNULL(t.test_flag, '0') = '0') AS find_35698,
  (SELECT COUNT(*) FROM biz_member t
    WHERE (t.ancestors = '0,10001,35698' OR t.ancestors LIKE '0,10001,35698,%')
      AND IFNULL(t.test_flag, '0') = '0') AS prefix_35698;

WITH RECURSIVE chain AS (
  SELECT member_id, parent_id, ancestors,
         CAST('0' AS CHAR(500) CHARSET utf8mb4) COLLATE utf8mb4_general_ci AS walked,
         0 AS d
  FROM biz_member
  WHERE parent_id IS NULL
  UNION ALL
  SELECT c.member_id, c.parent_id, c.ancestors,
         CONCAT(p.walked, ',', CAST(p.member_id AS CHAR CHARSET utf8mb4) COLLATE utf8mb4_general_ci),
         p.d + 1
  FROM biz_member c
  JOIN chain p ON c.parent_id = p.member_id
  WHERE p.d < 64
)
SELECT COUNT(*) AS walk_rows,
       SUM(walked <> ancestors) AS walk_mismatch,
       MAX(d) AS max_walk_depth
FROM chain;
EOF
}

do_preview() {
  print_banner "STEP preview (read-only)"
  echo "Send the output below for confirmation. After OK, run backup, then apply."
  echo
  preview_sql | run_sql
}

do_backup() {
  print_banner "STEP backup"
  if [ -z "$MYSQLDUMP_CLIENT" ]; then
    echo "mysqldump not found" >&2
    exit 1
  fi
  mkdir -p "$BACKUP_DIR"
  stamp="$(date '+%Y%m%d_%H%M%S')"
  file="${BACKUP_DIR}/biz_member_before_attach_top_${stamp}.sql"
  echo "writing $file"
  "$MYSQLDUMP_CLIENT" \
    --default-character-set=utf8mb4 \
    --host="$HOST" \
    --port="$PORT" \
    --user="$USER" \
    --password="$MYSQL_PWD" \
    --single-transaction \
    --quick \
    --no-tablespaces \
    --set-gtid-purged=OFF \
    "$DB" biz_member > "$file"
  gzip -f "$file"
  gz="${file}.gz"
  chmod 600 "$gz" 2>/dev/null || true
  echo "ok file=$gz size=$(du -h "$gz" | awk '{print $1}')"
  echo
  echo "Send the backup path above for confirmation. After OK, run apply."
}

do_apply() {
  print_banner "STEP apply"
  if [ "${APPLY:-}" != "YES" ]; then
    echo "apply will update biz_member.parent_id / ancestors."
    echo "After preview and backup are confirmed, run:"
    echo "  APPLY=YES sh $0 apply"
    exit 1
  fi
  echo "updating parent_id / ancestors ..."
  echo
  apply_sql | run_sql
  echo
  echo "Send the output above for confirmation. After OK, run verify."
}

do_verify() {
  print_banner "STEP verify"
  echo "Send the output below for confirmation."
  echo
  verify_sql | run_sql
}

case "$STEP" in
  preview) require_mysql; do_preview ;;
  backup) require_mysql; do_backup ;;
  apply) require_mysql; do_apply ;;
  verify) require_mysql; do_verify ;;
  help|-h|--help) usage ;;
  *) usage; exit 1 ;;
esac
