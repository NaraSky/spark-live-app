# 1. 创建数据库
CREATE DATABASE IF NOT EXISTS spark_live_user CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

USE spark_live_user;

# 2. 存储过程
DELIMITER $$

CREATE PROCEDURE create_t_user_100()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE table_name VARCHAR(30);
    DECLARE sql_text VARCHAR(3000);
    DECLARE table_body TEXT;

    SET table_body = '
    (
        user_id BIGINT NOT NULL DEFAULT -1 COMMENT ''用户id'',
        nick_name VARCHAR(35) DEFAULT NULL COMMENT ''昵称'',
        avatar VARCHAR(255) DEFAULT NULL COMMENT ''头像'',
        true_name VARCHAR(20) DEFAULT NULL COMMENT ''真实姓名'',
        sex TINYINT(1) DEFAULT NULL COMMENT ''性别 0男，1女'',
        born_date DATETIME DEFAULT NULL COMMENT ''出生时间'',
        work_city INT(9) DEFAULT NULL COMMENT ''工作地'',
        born_city INT(9) DEFAULT NULL COMMENT ''出生地'',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (user_id)
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;';

    WHILE i < 100
        DO
            IF i < 10 THEN
                SET table_name = CONCAT('t_user_0', i);
            ELSE
                SET table_name = CONCAT('t_user_', i);
            END IF;

            SET @sql_text = CONCAT('CREATE TABLE ', table_name, table_body);
            PREPARE stmt FROM @sql_text;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            SET i = i + 1;
        END WHILE;
END$$

DELIMITER ;

# 3. 创建表
CALL spark_live_user.create_t_user_100();


# 4. 容量预估
SELECT table_schema                            AS '数据库',
       table_name                              AS '表名',
       table_rows                              AS '记录数',
       TRUNCATE(data_length / 1024 / 1024, 2)  AS '数据容量（MB）',
       TRUNCATE(index_length / 1024 / 1024, 2) AS '索引容量（MB）'
FROM information_schema.tables
WHERE table_schema = 'spark_live_user'
ORDER BY data_length DESC, index_length DESC;