SET NAMES utf8 ;

--
-- Table structure for table `user`
--
create table user
(
    id int auto_increment
        primary key,
    username varchar(50) null comment '用户名',
    password varchar(50) null comment '密码',
    open_id varchar(50) null comment '用户的openid',
    salt varchar(50) null comment 'MD5盐' ,
    email varchar(100) null comment '电子邮箱',
    user_type int default 0 null comment '0-普通用户; 1-超级管理员; 2-版主;',
    status int default 0 null comment '0-未激活; 1-已激活;',
    activation_code varchar(100) null comment '激活码',
    header_url varchar(200) default 'https://s1.ax1x.com/2020/03/25/8XATWq.jpg' null comment '头像',
    create_time timestamp default now() null comment '创建时间',
    current_size int default  0 null comment '当前网盘容量（单位KB)',
    max_size int default  1048576 null comment '最大容量（单位KB)',
    register_type int default 0 null comment '0是网站注册，1是github注册，2是qq注册',
    root_folder int not null comment '用户的根文件夹'
)
    charset=utf8;

create index index_email
    on user (email);

create index index_username
    on user (username);

--
-- Table structure for table `login_ticket`
--
DROP TABLE IF EXISTS `login_ticket`;
SET character_set_client = utf8mb4 ;
CREATE TABLE `login_ticket` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `ticket` varchar(45) NOT NULL,
    `status` int(11) DEFAULT '0' COMMENT '0表示无效，1表示有效;',
    `expired` timestamp NOT NULL,
    PRIMARY KEY (`id`),
    KEY `index_ticket` (`ticket`(20))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `message`
--
DROP TABLE IF EXISTS `message`;
SET character_set_client = utf8mb4 ;
CREATE TABLE `message` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `from_id` int(11) DEFAULT NULL,
                           `to_id` int(11) DEFAULT NULL,
                           `conversation_id` varchar(45) NOT NULL,
                           `content` text,
                           `status` int(11) DEFAULT NULL COMMENT '0-未读;1-已读;2-删除;',
                           `create_time` timestamp NULL DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           KEY `index_from_id` (`from_id`),
                           KEY `index_to_id` (`to_id`),
                           KEY `index_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table structure for table `comment`
--
DROP TABLE IF EXISTS `comment`;
SET character_set_client = utf8mb4 ;
create table if not exists comment
(
    id int not null auto_increment
        primary key,
    user_id int null comment  '评论人ID',
    entity_type int null comment '实体类型: 1-帖子；2-帖子下面的评论；3-用户；4-博客; 5-博客下面的评论',
    entity_id int null comment '实体ID',
    target_id int default 0 null comment '回复的目标ID',
    content text null comment '内容',
    status int default 0 null comment '0表示无效，1表示有效',
    create_time timestamp default now() null comment '创建时间'
)
    charset=utf8;

create index index_entity_id
    on comment (entity_id);
create index index_user_id
    on comment (user_id);


--
-- Table structure for table `discuss_post`
--
DROP TABLE IF EXISTS `discuss_post`;
SET character_set_client = utf8mb4 ;
CREATE TABLE `discuss_post` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    user_id int null,
    `category_id` int default -1 null,
    `title` varchar(100) DEFAULT NULL,
    `content` longtext,
    `type` int(11) DEFAULT 0 COMMENT '0-普通; 1-置顶;',
    `status` int(11) DEFAULT 0 COMMENT '0-正常; 1-精华; 2-拉黑;',
    `create_time` timestamp DEFAULT now() NULL ,
    `comment_count` int(11) DEFAULT 0 NULL,
    `score` double DEFAULT 0 null,
    tags varchar(200) default '-1' null comment '帖子对应的标签',
    PRIMARY KEY (`id`),
    KEY `index_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists blog
(
    id int not null auto_increment
        primary key,
    appreciation bit default 0 not null comment '赞赏',
    commentabled bit default 0 not null comment '评论',
    recommend bit default 0 not null comment '推荐',
    published bit default 0 not null comment '发布',
    category_id int default -1 comment '种类id',
    flag varchar(255) null comment '标签：原创 转载 翻译',
    description varchar(500) null comment '描述',
    content longtext null comment '内容',
    create_time timestamp null comment '创建时间',
    share_statement bit default 0 not null comment '转载',
    title varchar(255) null comment '标题',
    update_time timestamp default now() null comment '更新时间',
    views int default 0 null comment  '预览人数',
    user_id int null comment '发布用户ID',
    tags varchar(200) default '-1' null comment '博客对应的标签',
)
    charset=utf8;


DROP TABLE IF EXISTS `site_setting`;
create table site_setting
(
    id int not null auto_increment,
    setting int not null,
    description varchar(10) not null,
    constraint site_setting_pk
        primary key (id)
);
insert into site_setting(setting, description) values (0,'允许注册');


DROP TABLE IF EXISTS `file_system`;
create table file_system
(
    id int auto_increment,
    absolute_path varchar(200) null,
    fastdfs_name varchar(200) null,
    valid int ,
    constraint file_system_pk
        primary key (id)
);


DROP TABLE IF EXISTS `OSSStorage`;
create table OSSStorage
(
    id int auto_increment,
    file_name varchar(100) null,
    url varchar(200) null,
    constraint OSSStorage_pk
        primary key (id)
);


create table if not exists ebook_content
(
    id int auto_increment comment '条目ID'
        primary key,
    content_id varchar(100) comment '内容ID',
    content longtext null comment '内容',
    file_id int null comment '对应的文件的ID'
);

create table if not exists ebook
(
    id int auto_increment comment '书ID'
        primary key,
    ebook_name varchar(100) null comment '书名',
    file_id int comment '对应的文件ID',
    header longtext null comment '书的目录内容'
);






create table if not exists category
(
    id int auto_increment
        primary key,
    name varchar(255) null comment '种类名',
    entity_type int null comment '实体类型: 1-帖子；2-帖子下面的评论；3-用户；4-博客; 5-博客下面的评论',
    user_id int 0 comment '类别所属用户ID - 只存在于博客中',
    count int 0 comment '属于该类别的实体个数 - 只存在于博客中'
);


create table if not exists tag
(
    id int not null auto_increment
        primary key,
    name varchar(255) not null comment '标签名',
    entity_type int null comment '实体类型: 1-帖子；2-帖子下面的评论；3-用户；4-博客; 5-博客下面的评论',
    count int 0 comment '属于该类别的实体个数 - 只存在于博客中'
);


create table blog_tag
(
    id int auto_increment,
    blog_id int null comment '博客ID',
    tag_id int null comment '标签ID',
    constraint blog_tag_pk
        primary key (id)
);

create table feed
(
	id int auto_increment,
	feed_type int null comment '点赞/评论/发布',
	create_time timestamp null,
	user_id int null,
	user_name varchar(100) null,
	entity_type int null comment '帖子/博客',
	entity_id int null,
	constraint feed_pk
		primary key (id)
);


create table if not exists file_folder
(
    id int auto_increment comment '文件夹ID'
        primary key,
    file_folder_name varchar(255) null comment '文件夹名称',
    parent_folder_id int default 0 null comment '父文件夹ID',
    user_id int null comment '所属用户ID',
    create_time timestamp not null comment '创建时间'
)
    charset=utf8;



create table if not exists my_file
(
    id int auto_increment comment '文件ID'
        primary key,
    my_file_name varchar(255) null comment '文件名',
    show_path varchar(255) null comment '在线预览路径',
    user_id int null comment '用户ID',
    my_file_path varchar(255) default '/' null comment '文件存储路径',
    download_time int default 0 null comment '下载次数',
    upload_time datetime null comment '上传时间',
    parent_folder_id int null comment '父文件夹ID',
    size int null comment '文件大小',
    type int null comment '文件类型',
    postfix varchar(255) null comment '文件后缀'
)
    charset=utf8;
