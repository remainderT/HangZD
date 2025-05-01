DROP DATABASE IF EXISTS hangzd;
CREATE DATABASE hangzd  DEFAULT CHARACTER SET utf8mb4;

USE `hangzd`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                        `username`      varchar(256) NOT NULL COMMENT '用户名',
                        `password`      varchar(512) NOT NULL COMMENT '密码',
                        `mail`          varchar(64)  NOT NULL COMMENT '邮箱',
                        `salt`          varchar(20)  NOT NULL COMMENT '盐',
                        `avatar`        varchar(60)     DEFAULT NULL COMMENT '头像',
                        `phone`         varchar(20)     DEFAULT NULL COMMENT '手机号',
                        `introduction`  varchar(1024)   DEFAULT NULL COMMENT '个人简介',
                        `tags`          varchar(1024)   DEFAULT NULL COMMENT '个人标签',
                        `like_count`    int(11)         DEFAULT 0 COMMENT '点赞数',
                        `collect_count`  int(11)         DEFAULT 0 COMMENT '收藏问题的数量',
                        `useful_count` int(11)          DEFAULT 0 COMMENT '回答有用数量',
                        `user_type` ENUM('user', 'admin') NOT NULL COMMENT '用户类型',
                        `status` tinyint(4)        DEFAULT 0    COMMENT '状态',
                        `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                        `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                        `active_days`   int     DEFAULT 1      not null comment '活跃天数',
                        `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                        `last_active_time` datetime     DEFAULT NULL COMMENT '最后活跃时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY idx_unique_username (username) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和管理员';

DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                            `category_id` bigint(20)   NOT NULL COMMENT '分类ID',
                            `title` varchar(256)       NOT NULL COMMENT '标题',
                            `content` varchar(2048)    DEFAULT NULL COMMENT '内容',
                            `user_id` bigint(20)       NOT NULL COMMENT '发布人ID',
                            `username` varchar(256)    NOT NULL COMMENT '用户名',
                            `images` varchar(600)      DEFAULT NULL COMMENT '照片路径，最多10张，多张以","隔开',
                            `like_count` int(11)       DEFAULT 0 COMMENT '点赞数',
                            `answer_count` int(11)    DEFAULT 0 COMMENT '回答数',
                            `view_count` int(11)       DEFAULT 0 COMMENT '浏览量',
                            `solved_flag` tinyint(1)   DEFAULT 0 COMMENT '是否解决 0：未解决 1：已解决',
                            `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                            `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                            `del_flag`   tinyint(1)    DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                            PRIMARY KEY (`id`),
                            KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题';


DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                           `user_id` bigint(20)       NOT NULL COMMENT '发布人ID',
                           `username` varchar(256)    NOT NULL COMMENT '用户名',
                           `question_id` bigint(20)   NOT NULL COMMENT '问题ID',
                           `content` varchar(2048)    DEFAULT NULL COMMENT '内容',
                           `images` varchar(600)      DEFAULT NULL COMMENT '照片路径，最多10张，多张以","隔开',
                           `like_count` int(11)       DEFAULT 0 COMMENT '点赞数',
                           `useful` tinyint(1)        DEFAULT 0 COMMENT '是否有用',
                           `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                           `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                           `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                           `answered` tinyint(1)   DEFAULT 0 COMMENT '是否已回答 0：未回答 1：已回答',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回答';

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                           `from_id` bigint(20) NOT NULL COMMENT '发送人ID 1就是系统消息',
                           `to_id` bigint(20) NOT NULL COMMENT '接收者ID',
                           `type` ENUM('system', 'like', 'answer', 'useful') NOT NULL COMMENT '消息类型',
                           `content` text   DEFAULT NULL COMMENT '内容',
                           `status` int(11) DEFAULT 0 COMMENT '0-未读;1-已读',
                           `generate_id` bigint(20) NOT NULL COMMENT '产生消息的ID',
                           `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                           `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                           `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                           PRIMARY KEY (`id`),
                           KEY `index_to_id` (`to_id`),
                           KEY `index_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息';


DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                            `user_1` bigint(20) NOT NULL COMMENT '用户1ID',
                            `user_2` bigint(20) NOT NULL COMMENT '用户2ID',
                            `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '可见状态 1 user1 删除 2 user2 删除 3 都删除' ,
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                            `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话';

DROP TABLE IF EXISTS `user_action`;
CREATE TABLE `user_action` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                        `user_id` bigint(20)  unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
                        `entity_type` ENUM('user', 'question', 'answer') NOT NULL  COMMENT '实体类型',
                        `entity_id` bigint(20)  unsigned NOT NULL DEFAULT '0' COMMENT '实体ID',
                        `collect_stat` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '收藏状态: 0-未收藏，1-已收藏',
                        `like_stat` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '点赞状态: 0-未点赞，1-点赞',
                        `last_view_time` datetime     DEFAULT NULL COMMENT '上次浏览时间',
                        `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                        `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                        `del_flag`   tinyint(1)    DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                        PRIMARY KEY (`id`),
                        KEY `idx_user_entity_type_id` (`user_id`, `entity_type`, `entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为';