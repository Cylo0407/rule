DROP TABLE IF EXISTS `rule_structure`;

CREATE TABLE IF NOT EXISTS `rule_structure`
(
    `id`      bigint(20)   NOT NULL AUTO_INCREMENT,
    `title`   varchar(256) NOT NULL,
    `chapter` varchar(256) DEFAULT NULL,
    `section` varchar(256) DEFAULT NULL,
    `text`    longtext     NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `rule_top_laws`;

CREATE TABLE IF NOT EXISTS `rule_top_laws`
(
    `id`    bigint(20)   NOT NULL AUTO_INCREMENT,
    `title` varchar(256) NOT NULL,
    `laws`  text DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `penalty_case_structure`;

CREATE TABLE IF NOT EXISTS `penalty_case_structure`
(
    `id`     bigint(20)   NOT NULL AUTO_INCREMENT,
    `title`  varchar(256) NOT NULL,
    `doc_id` varchar(20)  NOT NULL,
    `text`   text         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `penalty_case_top_laws`;

CREATE TABLE IF NOT EXISTS `penalty_case_top_laws`
(
    `id`     bigint(20)   NOT NULL AUTO_INCREMENT,
    `doc_id` varchar(20)  NOT NULL,
    `title`  varchar(256) NOT NULL,
    `laws`   text DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `interpretation_structure`;

CREATE TABLE IF NOT EXISTS `interpretation_structure`
(
    `id`     bigint(20)   NOT NULL AUTO_INCREMENT,
    `title`  varchar(256) NOT NULL,
    `doc_id` varchar(20)  NOT NULL,
    `text`   text         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `interpretation_top_laws`;

CREATE TABLE IF NOT EXISTS `interpretation_top_laws`
(
    `id`     bigint(20)   NOT NULL AUTO_INCREMENT,
    `doc_id` varchar(20)  NOT NULL,
    `title`  varchar(256) NOT NULL,
    `laws`   text DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
