-- 用户表
create table if not exists user
(
    id          bigint auto_increment comment 'id' primary key,
    account     varchar(256)                           not null comment '账号',
    password    varchar(512)                           not null comment '密码',
    email       varchar(512)                           not null comment '邮箱',
    name        varchar(256)                           null comment '用户昵称',
    avatar      varchar(1024)                          null comment '用户头像',
    profile     varchar(512)                           null comment '用户简介',
    role        varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (account),
    INDEX idx_userName (account)
) comment '用户' collate = utf8mb4_unicode_ci;


