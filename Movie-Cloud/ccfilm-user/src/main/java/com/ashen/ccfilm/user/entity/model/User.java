package com.ashen.ccfilm.user.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author ashen
 * @since 2020-11-23
 */
@TableName("cc_user")
public class User extends Model<User> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键编号
     */
    @TableId(value = "UUID", type = IdType.AUTO)
    private Integer uuid;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户密码
     */
    private String userPwd;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户性别 0-男，1-女
     */
    private Integer userSex;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户住址
     */
    private String address;

    /**
     * 头像URL
     */
    private String headUrl;

    /**
     * 个人介绍
     */
    private String biography;

    /**
     * 生活状态 0-单身，1-热恋中，2-已婚，3-为人父母
     */
    private Integer lifeState;

    /**
     * 创建时间
     */
    private LocalDateTime beginTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    public Integer getUuid() {
        return uuid;
    }

    public void setUuid(Integer uuid) {
        this.uuid = uuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getUserSex() {
        return userSex;
    }

    public void setUserSex(Integer userSex) {
        this.userSex = userSex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Integer getLifeState() {
        return lifeState;
    }

    public void setLifeState(Integer lifeState) {
        this.lifeState = lifeState;
    }

    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    protected Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "User{" +
                ", uuid=" + uuid +
                ", userName=" + userName +
                ", userPwd=" + userPwd +
                ", nickName=" + nickName +
                ", userSex=" + userSex +
                ", birthday=" + birthday +
                ", email=" + email +
                ", userPhone=" + userPhone +
                ", address=" + address +
                ", headUrl=" + headUrl +
                ", biography=" + biography +
                ", lifeState=" + lifeState +
                ", beginTime=" + beginTime +
                ", updateTime=" + updateTime +
                "}";
    }
}
