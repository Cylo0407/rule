-- 把webplus换成自己的schema名称

CREATE TABLE `webplus`.`penalty_case`
(
    `case_id`    INT          NOT NULL,
    `case_title` VARCHAR(100) NOT NULL,
    `doc_id`     VARCHAR(15)  NOT NULL,
    PRIMARY KEY (`case_id`),
    UNIQUE INDEX `case_id_UNIQUE` (`case_id` ASC)
);

CREATE TABLE `webplus`.`interpretation_text`
(
    `interpretation_id`    INT          NOT NULL AUTO_INCREMENT,
    `interpretation_title` VARCHAR(100) NOT NULL,
    `doc_id`               VARCHAR(15)  NOT NULL,
    PRIMARY KEY (`interpretation_id`),
    UNIQUE INDEX `idinterpretation_text_UNIQUE` (`interpretation_id` ASC)
);
CREATE TABLE `webplus`.`interpretation_content`
(
    `id`      INT         NOT NULL AUTO_INCREMENT,
    `doc_id`  VARCHAR(45) NOT NULL,
    `content` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `id_UNIQUE` (`id` ASC)
);
CREATE TABLE `webplus`.`penalty_content`
(
    `id`      INT         NOT NULL AUTO_INCREMENT,
    `doc_id`  VARCHAR(45) NOT NULL,
    `content` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `id_UNIQUE` (`id` ASC)
);