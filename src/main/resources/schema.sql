-- ═══════════════════════════════════════════════════════════════════════
-- schema.sql — auto-run by Spring Boot on startup
--
-- Setup (do this ONCE manually in pgAdmin or psql, before running the app):
--
--   CREATE DATABASE practice_db;
--
-- Then `./gradlew bootRun` and this file will create the tables / sequences
-- below. It uses "IF NOT EXISTS" everywhere, so it's safe to run repeatedly.
-- ═══════════════════════════════════════════════════════════════════════

-- ─── Sequence for the notice PK ───────────────────────────────────────
CREATE SEQUENCE IF NOT EXISTS seq_co_notice_m START WITH 1 INCREMENT BY 1;

-- ─── Notice board table (co_notice_m) ─────────────────────────────────
-- Mirrors the saas-olv `co_smp_board_m` convention:
--   notice_sn : primary key (uses seq_co_notice_m)
--   notice_title: title (required)
--   notice_cn: content body
--   use_yn: soft-delete flag ('Y' visible / 'N' deleted)
--   data_reg_id: created by (user id)
--   data_reg_dt: created at
--   data_chg_id: last updated by
--   data_chg_dt: last updated at
CREATE TABLE IF NOT EXISTS co_notice_m (
    notice_sn    BIGINT       NOT NULL DEFAULT nextval('seq_co_notice_m'),
    notice_title VARCHAR(200) NOT NULL,
    notice_cn    TEXT,
    use_yn       CHAR(1)      DEFAULT 'Y',
    data_reg_id  VARCHAR(20),
    data_reg_dt  TIMESTAMP    DEFAULT NOW(),
    data_chg_id  VARCHAR(20),
    data_chg_dt  TIMESTAMP,
    CONSTRAINT pk_co_notice_m PRIMARY KEY (notice_sn)
);

-- ─── Admin (manager) table for login practice (ur_mngr_m) ─────────────
-- Simplified from the olv manager table naming:
--   mngr_no   : primary key
--   web_id    : login ID (unique)
--   mngr_nm   : display name
--   mngr_enpw : password hash = Base64(SHA-256(web_id + password)) — eGov style
--   mngr_stcd : status ('20' = active, same code olv checks at login)
CREATE SEQUENCE IF NOT EXISTS seq_ur_mngr_m START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS ur_mngr_m (
    mngr_no      BIGINT       NOT NULL DEFAULT nextval('seq_ur_mngr_m'),
    web_id       VARCHAR(20)  NOT NULL,
    mngr_nm      VARCHAR(50)  NOT NULL,
    mngr_enpw    VARCHAR(100) NOT NULL,
    mngr_stcd    CHAR(2)      DEFAULT '20',
    data_reg_dt  TIMESTAMP    DEFAULT NOW(),
    CONSTRAINT pk_ur_mngr_m PRIMARY KEY (mngr_no),
    CONSTRAINT uk_ur_mngr_m_web_id UNIQUE (web_id)
);
