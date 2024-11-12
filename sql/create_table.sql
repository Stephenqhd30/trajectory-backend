# 数据库初始化
# @author stephen qiu
#

-- 创建库
create
    database if not exists trajectory;

-- 切换库
use trajectory;

-- 用户表
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userGender   tinyint      default 2                 not null comment '性别（0-男，1-女，2-保密）',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userPhone    varchar(256)                           null comment '手机号码',
    userEmail    varchar(256)                           null comment '用户邮箱',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    editTime     datetime     default CURRENT_TIMESTAMP null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;


-- 帖子表
create table post
(
    id         bigint auto_increment comment 'id'
        primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    cover      varchar(1024)                      null comment '封面图片',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '帖子' collate = utf8mb4_unicode_ci;

create index idx_userId
    on post (userId);

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id
        bigint
        auto_increment
        comment
            'id'
        primary
            key,
    postId
        bigint
        not
            null
        comment
            '帖子 id',
    userId
        bigint
        not
            null
        comment
            '创建用户 id',
    createTime
        datetime
        default
            CURRENT_TIMESTAMP
        not
            null
        comment
            '创建时间',
    updateTime
        datetime
        default
            CURRENT_TIMESTAMP
        not
            null
        on
            update
            CURRENT_TIMESTAMP
        comment
            '更新时间',
    index
        idx_postId
        (
         postId
            ),
    index idx_userId
        (
         userId
            )
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id
        bigint
        auto_increment
        comment
            'id'
        primary
            key,
    postId
        bigint
        not
            null
        comment
            '帖子 id',
    userId
        bigint
        not
            null
        comment
            '创建用户 id',
    createTime
        datetime
        default
            CURRENT_TIMESTAMP
        not
            null
        comment
            '创建时间',
    updateTime
        datetime
        default
            CURRENT_TIMESTAMP
        not
            null
        on
            update
            CURRENT_TIMESTAMP
        comment
            '更新时间',
    index
        idx_postId
        (
         postId
            ),
    index idx_userId
        (
         userId
            )
) comment '帖子收藏';

-- 标签表
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       not null comment '标签名称',
    userId     bigint                             not null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint  default 0                 null comment '0-不是父标签，1-是父标签',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '标签表';

-- 文件上传日志记录表
create table log_files
(
    id               bigint auto_increment comment 'id'
        primary key,
    fileKey          varchar(255)                        not null comment '文件唯一摘要值',
    fileName         varchar(255)                        not null comment '文件存储名称',
    fileOriginalName varchar(255)                        not null comment '文件原名称',
    fileSuffix       varchar(255)                        not null comment '文件扩展名',
    fileSize         bigint                              not null comment '文件大小',
    fileUrl          varchar(255)                        not null comment '文件地址',
    fileOssType      varchar(20)                         not null comment '文件OSS类型',
    createTime       datetime  default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint   default 0                 not null comment '逻辑删除（0表示未删除，1表示已删除）',
    constraint log_files_pk
        unique (fileKey)
) comment '文件上传日志记录表' collate = utf8mb4_general_ci
                               row_format = DYNAMIC;


-- 图表信息表
create table chart
(
    id         bigint auto_increment comment 'id'
        primary key,
    goal       text                               null comment '分析目标',
    `name`     varchar(128)                       null comment '图表名称',
    chartData  text                               null comment '图表数据',
    chartType  varchar(256)                       null comment '图表类型',
    genChart   text                               null comment '生成的图表数据',
    genResult  text                               null comment '生成的分析结论',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '图表信息' collate = utf8mb4_unicode_ci;
