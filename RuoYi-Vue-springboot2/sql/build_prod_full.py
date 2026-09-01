#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Concatenate sql/prod_install_order.txt into sql/prod_full.sql."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent
ORDER = ROOT / "prod_install_order.txt"
OUT = ROOT / "prod_full.sql"

HEADER = r"""-- =============================================================================
-- Fresh-server one-shot import. DROPs RuoYi/biz tables. Do NOT run on live data.
-- Database name ry-vue must match application-prod.yml JDBC url.
--
-- CLI:
--   mysql -uroot -p --default-character-set=utf8mb4 < sql/prod_full.sql
-- BT Panel / Navicat: import this file as a whole (do not execute statement-by-statement).
-- Contains DELIMITER procedures; if phpMyAdmin fails, use the mysql CLI above.
--
-- Default admin: admin / admin123    change password after first login.
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ry-vue`;

"""

FOOTER = r"""
SET FOREIGN_KEY_CHECKS = 1;

SELECT COUNT(*) AS table_count
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';
SELECT user_name, nick_name FROM sys_user WHERE user_id = 1;
SELECT menu_id, menu_name, parent_id FROM sys_menu WHERE menu_id IN (2024, 2025, 2032, 2038, 2297) ORDER BY menu_id;
SELECT COUNT(*) AS biz_config_count FROM sys_config WHERE config_key LIKE 'biz.%';
"""


def files_in_order():
    names = []
    for raw in ORDER.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        names.append(line)
    return names


def main():
    parts = [HEADER]
    for name in files_in_order():
        path = ROOT / name
        if not path.is_file():
            raise SystemExit("missing %s" % path)
        body = path.read_text(encoding="utf-8").rstrip() + "\n"
        parts.append("\n-- ---------- %s ----------\n" % name)
        parts.append(body)
    parts.append(FOOTER)
    OUT.write_text("".join(parts), encoding="utf-8")
    print("wrote %s (%s bytes)" % (OUT, OUT.stat().st_size))


if __name__ == "__main__":
    main()
