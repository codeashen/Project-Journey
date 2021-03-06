-- ----------------------------
-- Table structure for cc_order
-- ----------------------------
DROP TABLE IF EXISTS `cc_order`;
CREATE TABLE cc_order(
  UUID VARCHAR(100) PRIMARY KEY COMMENT '主键编号',
  cinema_id INT COMMENT '影院编号',
  field_id INT COMMENT '放映场次编号',
  film_id INT COMMENT '电影编号',
  seats_ids VARCHAR(50) COMMENT '已售座位编号',
  seats_name VARCHAR(200) COMMENT '已售座位名称',
  film_price DOUBLE COMMENT '影片售价',
  order_price DOUBLE COMMENT '订单总金额',
  order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  order_user INT COMMENT '下单人',
  order_status INT DEFAULT 0 COMMENT '0-待支付,1-已支付,2-已关闭'
) COMMENT '订单信息表' ENGINE = INNODB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Records of cc_order
-- ----------------------------
INSERT INTO cc_order(UUID,cinema_id,field_id,film_id,seats_ids,seats_name,film_price,order_price,order_user)
	VALUES('415sdf58ew12ds5fe1',1,1,2,'1,2,3,4','第一排1座,第一排2座,第一排3座,第一排4座',63.2,126.4,1);
	
	


-- ----------------------------
-- Table structure for cc_order_2017
-- ----------------------------
DROP TABLE IF EXISTS `cc_order_2017`;
CREATE TABLE cc_order_2017(
  UUID VARCHAR(100) PRIMARY KEY COMMENT '主键编号',
  cinema_id INT COMMENT '影院编号',
  field_id INT COMMENT '放映场次编号',
  film_id INT COMMENT '电影编号',
  seats_ids VARCHAR(50) COMMENT '已售座位编号',
  seats_name VARCHAR(200) COMMENT '已售座位名称',
  film_price DOUBLE COMMENT '影片售价',
  order_price DOUBLE COMMENT '订单总金额',
  order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  order_user INT COMMENT '下单人',
  order_status INT DEFAULT 0 COMMENT '0-待支付,1-已支付,2-已关闭'
) COMMENT '订单信息表' ENGINE = INNODB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Records of cc_order_2017
-- ----------------------------
INSERT INTO cc_order_2017(UUID,cinema_id,field_id,film_id,seats_ids,seats_name,film_price,order_price,order_user,order_time)
	VALUES('329123812gnfn31',1,1,2,'1,2,3,4','第一排1座,第一排2座,第一排3座,第一排4座',63.2,126.4,1,'2017-05-03 12:13:42');
	
	
-- ----------------------------
-- Table structure for cc_order_2018
-- ----------------------------
DROP TABLE IF EXISTS `cc_order_2018`;
CREATE TABLE cc_order_2018(
  UUID VARCHAR(100) PRIMARY KEY COMMENT '主键编号',
  cinema_id INT COMMENT '影院编号',
  field_id INT COMMENT '放映场次编号',
  film_id INT COMMENT '电影编号',
  seats_ids VARCHAR(50) COMMENT '已售座位编号',
  seats_name VARCHAR(200) COMMENT '已售座位名称',
  film_price DOUBLE COMMENT '影片售价',
  order_price DOUBLE COMMENT '订单总金额',
  order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  order_user INT COMMENT '下单人',
  order_status INT DEFAULT 0 COMMENT '0-待支付,1-已支付,2-已关闭'
) COMMENT '订单信息表' ENGINE = INNODB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Records of cc_order_2018
-- ----------------------------
INSERT INTO cc_order_2018(UUID,cinema_id,field_id,film_id,seats_ids,seats_name,film_price,order_price,order_user,order_time)
	VALUES('124583135asdf81',1,1,2,'1,2,3,4','第一排1座,第一排2座,第一排3座,第一排4座',63.2,126.4,1,'2018-02-12 11:53:42');
	
