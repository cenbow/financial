package com.mobanker.financial.entity;

import java.math.BigDecimal;

import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="yyd_users_info")
public class YydUsersInfo{

    private int id;

    private int userId;

    private String niname;

    private String sex;

    private String birthday;

    private int typeId;

    private int status;

    private int inviteUserid;

    private BigDecimal inviteMoney;

    private String realname;

    private int realnameStatus;

    private int realnameTimes;

    private String education;

    private int educationStatus;

    private int eduTimes;

    private String phone;

    private int phoneStatus;

    private int videoStatus;

    private int tenderStatus;

    private String province;

    private String city;

    private String area;

    private int webStatus;

    private String question;

    private String answer;

    private String creditcard;

    private int creditcardStatus;

    private String qq;

    private String sina;

    private int approveVisitStatus;

    private int humanAmount;

    private int humanAmountStatus;

    private String humanAmountRank;

    private int distribute;

    private int shanghai;

    private int superTender;

    private String downloadChannel;

    private String regDeviceIdentify;

    private String humanAmountUpdatetime;

    private String appVersion;

    private int outsourcing;

    private int lawyerLetter;

    private int t1;

    private int t2;

    private int timesBorrow;

    private String outsourcingName;

    private String outsourcingRemark;

    private String lawyerRemark;

    private int phoneProvince;

    private int phoneCity;

    private int score;

    private int scoreTime;

    private String registerVersion;

    private int operator;

    private String phoneSim;

    private String addProduct;

    private String curProduct;

    private String addChannel;

    private String curChannel;

    private int uflag;

    private String appSource;

    private int uRankId;

    private int uCredit;

    private String promoter;

    private String promoterPhone;

    private String guarantor1;

    private String guarantorPhone1;

    private String referees;

    private String refereesPhone;

    private String refereesRelationship;

    private String uzoneHumanAmountUpdatetime;

    private String uzoneHumanAmountRemark;

    private Byte skipAlipayAuthorize;

    private Byte alipayVisited;

    private Byte moreInfoClicked;

    private int uzoneHumanAmount;

    private int uzoneHumanAmountStatus;

    private String uzoneHumanAmountRank;

    private int uStatus;

    private int isSendmsg;

    private int isLightningCert;

    private int isSendMail;
}
