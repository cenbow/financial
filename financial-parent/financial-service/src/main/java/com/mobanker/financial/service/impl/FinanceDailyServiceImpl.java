package com.mobanker.financial.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.dao.FinanceDailyDao;
import com.mobanker.financial.dao.FinanceDepositWithdrawDao;
import com.mobanker.financial.dao.FinanceFinancingUserDao;
import com.mobanker.financial.dao.FinanceIncomeDao;
import com.mobanker.financial.dao.FinanceInvestUserDao;
import com.mobanker.financial.dao.FinanceSubmitTenderDao;
import com.mobanker.financial.dao.FinanceTenderCfgDao;
import com.mobanker.financial.dao2.YydUsersInfoDao;
import com.mobanker.financial.entity.FinanceDaily;
import com.mobanker.financial.entity.FinanceFinancingUser;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceDailyService;
import com.mobanker.financial.service.FinanceUidMappingService;
import com.mobanker.financial.vo.DailyTenderAmountVo;
import com.mobanker.financial.vo.YydDataVo;
import com.mobanker.financial.vo.YydRegisterVo;
import com.mobanker.framework.service.impl.BaseServiceImpl;

@Service
public class FinanceDailyServiceImpl extends BaseServiceImpl<FinanceDaily> implements FinanceDailyService {
	Logger logger = LoggerFactory.getLogger("dailyJoblog");
	
	@Resource
	private FinanceUidMappingService financeUidMappingService;
	@Resource
	private FinanceDailyDao financeDailyDao;
	@Resource
	private FinanceTenderCfgDao financeTenderCfgDao;
	@Resource
	private FinanceSubmitTenderDao financeSubmitTenderDao;
	@Resource
	private FinanceInvestUserDao financeInvestUserDao;
	@Resource
	private FinanceDepositWithdrawDao financeDepositWithdrawDao;
	@Resource
	private YydUsersInfoDao yydUsersInfoDao;
	@Resource
	private FinanceCommonCfgService financeCommonCfgService;
	@Resource
	private FinanceFinancingUserDao financeFinancingUserDao;
	@Resource
	private FinanceIncomeDao financeIncomeDao;
	
	private List<String> AndroidInvestUidList = null;//某日安卓投资用户
	
	private List<String> IOSInvestUidList = null;
	private List<String> AndroidDepoUidList = null;
	
	private List<String> allAndroidDepoUidList = null;
	
	private List<String> IOSDepoUidList = null;
	
	private List<String> allIOSDepoUidList = null;
	
	private List<String> allAndroidInvestUidList = null;
	
	private List<String> allIOSInvestUidList = null;
	
	private List<String> newUserInvestNumList = null;
	
	private List<String> iosNewAddUserDepoNumList = null; //ios某日新增用户数量
	private List<String> androidNewAddUserDepoNumList = null;//android某日新增用户
	private List<String> iosNewInvestUserList = null;
	private List<String> androidNewInvestUserList = null;
	
	private Set<String> withdrawNumList = null;
	private List<String> oldUserInvestNumList = null;
	
	private Set<String> allRechargeUserNum = null;
	
	/**
	 * dateList中的日期格式为yyyy-MM-dd
	 */
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Integer generatorDaily(List<String> dateList) {
		logger.debug("财务日报数据统计{}",JSONObject.toJSONString(dateList));
		if(dateList == null || dateList.size() == 0){
			logger.error("-----> 生成日报传入的dateList为空");
			return 0;
		}
		logger.debug("delete"+dateList.get(0)+"date");
		this.deleteByDaily(dateList.get(0));
		logger.debug("delete financedaily success");
		
		Integer num = 0;
		
		for(String date : dateList){
			logger.debug("===the next into=="+date);
			generatorDaily(date);
			num++;
		}
		logger.debug("----"+num);
		return num;
	}
	
	/**
	 * 插入 ? 姿势
	 * @param date
	 */
	
	private void generatorDaily(String date){
		logger.debug("into daily service generatorDaily"+date);
		//先查询出来数据再插入
		FinanceDaily entity = new FinanceDaily();
		BigDecimal actiToReg = BigDecimal.ZERO; //激活到注册转化率
		BigDecimal regDayRatio = BigDecimal.ZERO; //注册日环比增长
		BigDecimal regToInvest = BigDecimal.ZERO; //注册到投资转化
		BigDecimal depoDayRatio = BigDecimal.ZERO; //所有充值用户的用户数量
		BigDecimal depoNumDayRatio = BigDecimal.ZERO; //所有用户充值总额
		BigDecimal depoARPUDayRatio = BigDecimal.ZERO; //充值ARPU日环比
		BigDecimal investHoldDepo = BigDecimal.ZERO; //投资用户占充值用户比
		BigDecimal tenderNumHoldDepoNum = BigDecimal.ZERO; //标的额占充值额比
		BigDecimal tenderNumHoldDepoNumDayRatio = BigDecimal.ZERO; //标的额占充值额比环比
		BigDecimal investNumHoldDepoNumDayRatio = BigDecimal.ZERO; //投资额占充值额比环比
		BigDecimal iosNewRegDayRatio = BigDecimal.ZERO; //iOS注册日环比增长
		BigDecimal iosNewDepoDayRatio = BigDecimal.ZERO; //iOS新增充值用户日环比
		BigDecimal iosNewRegToDepoDayRatio = BigDecimal.ZERO; //iOS新增注册到充值转化环比
		BigDecimal iosNewDepoNumDayRatio = BigDecimal.ZERO; //iOS新增充值额日环比
		BigDecimal androidNewRegDayRatio = BigDecimal.ZERO; //android注册日环比增长
		BigDecimal androidNewDepoDayRatio = BigDecimal.ZERO; //android新增充值用户日环比
		BigDecimal androidNewRegToDepo = BigDecimal.ZERO; //android新增注册到充值转化
		BigDecimal androidNewRegToDepoDayRatio = BigDecimal.ZERO; //android新增注册到充值转化环比
		BigDecimal androidNewDepoNumDayRatio = BigDecimal.ZERO; //android新增充值额日环比
		BigDecimal tenderFinishDayRatio = BigDecimal.ZERO; //标的完成比例日环比 
		BigDecimal bidOldCycle = BigDecimal.ZERO; //投标老用户占比
		BigDecimal bidNewCycle = BigDecimal.ZERO; //投标新用户占比
		
		String date2 = getYesterdayByDate(date);  //获取上一日时间
		
		BigDecimal savingpotAvailableBalance = getSavingpotAvailableBalance();
		//TODO
		getIosAndAndroidInvestUser(date);
		getIosAndAndroidDepoUser(date);
		getAllIosAndAndroidDepoUser(date, date2);
		getAllIosAndAndroidInvestUser(date, date2);
		
		BigDecimal newUserInvestNum = getNewUserInvestNum(date);
		BigDecimal oldUserInvestNum = getOldUserInvestNum(date);
		//获取当日标的完成比
		BigDecimal tenderFinish = tenderAmountANDaddAmountDaily(date); 
		//获取上一日标的完成比
		BigDecimal yesterdayTenderFinsh = tenderAmountANDaddAmountDaily(date2); 
		if (yesterdayTenderFinsh.compareTo(BigDecimal.ZERO)!=0) {
			//标的完成比例日环比 = (当日标的完成比 - 上一日标的完成比) / 上一日标的完成比
			tenderFinishDayRatio = (tenderFinish.subtract(yesterdayTenderFinsh)).divide(yesterdayTenderFinsh,2); //标的完成比例日环比
		} else if (yesterdayTenderFinsh.compareTo(BigDecimal.ZERO)==0) {
			tenderFinishDayRatio = (tenderFinish.subtract(yesterdayTenderFinsh)).negate();
		}
		BigDecimal newUserAddTotalAmount2 = getNewUserAddTotalAmount(date);
		BigDecimal oldUserAddTotalAmount2 = getOldUserAddTotalAmount(date);
		BigDecimal allUserAddTotalAmount2 = getAllUserAddTotalAmount(date);
		if (allUserAddTotalAmount2.compareTo(BigDecimal.ZERO)!=0) {
			//投标老用户占比 = 老用户投资额/完成标的额（成功投标过的算作老用户）
			bidOldCycle = oldUserAddTotalAmount2.divide(allUserAddTotalAmount2,2);
			//投标新用户占比 = 新用户投资额/完成标的额（未成功投标过的算作新用户）
			bidNewCycle = newUserAddTotalAmount2.divide(allUserAddTotalAmount2,2);
		} else if (allUserAddTotalAmount2.compareTo(BigDecimal.ZERO)==0) {
			bidOldCycle = oldUserAddTotalAmount2.negate();
			bidNewCycle = newUserAddTotalAmount2.negate();
		}
		
		//获取当日所有充值用户的用户数量
		BigDecimal nowTotalUserRechargeNum = getAllRechargeUserNum(date);
		//当日所有用户充值总金额
		BigDecimal nowRechargeTotalAmount = getRechargeAmount(date);
		
		//获取上一日日所有充值用户的用户数量
		BigDecimal yesterdayTotalUserRechargeNum = getAllRechargeUserNum(date2);
		if (yesterdayTotalUserRechargeNum.compareTo(BigDecimal.ZERO)!=0) {
			depoDayRatio = (nowTotalUserRechargeNum.subtract(yesterdayTotalUserRechargeNum)).divide(yesterdayTotalUserRechargeNum,2);
		} else if (yesterdayTotalUserRechargeNum.compareTo(BigDecimal.ZERO)==0){
			depoDayRatio = nowTotalUserRechargeNum.negate();
		}
	
		//上一日所有用户充值总金额
		BigDecimal yesterDayRechargeTotalAmount = getRechargeAmount(date2);
		if (yesterDayRechargeTotalAmount.compareTo(BigDecimal.ZERO)!=0) {
			//说明 yesterDayRechargeTotalAmount不为零
			depoNumDayRatio = (nowRechargeTotalAmount.subtract(yesterDayRechargeTotalAmount)).divide(yesterDayRechargeTotalAmount,2);
		} else  if (yesterDayRechargeTotalAmount.compareTo(BigDecimal.ZERO)==0) {
			//说明yesterDayRechargeTotalAmount为零
			depoNumDayRatio = nowRechargeTotalAmount.negate(); //如果除数为零 取其相反数
		}
		
		//充值ARPU
		BigDecimal depoARPU = getDepoARPU(date);
		//充值ARPU日环比
		BigDecimal yesterdayDepoARPU = getDepoARPU(date2);
		if (yesterdayDepoARPU.compareTo(BigDecimal.ZERO)!=0) {
			depoARPUDayRatio = (depoARPU.subtract(yesterdayDepoARPU)).divide(yesterdayDepoARPU,2);
		} else if (yesterdayDepoARPU.compareTo(BigDecimal.ZERO)==0) {
			depoARPUDayRatio = (depoARPU.subtract(yesterdayDepoARPU)).negate();
		}
		
		//投资ARPU
		BigDecimal investARPU = getInvestARPU(date);
		
		
		//获取某日所有投资用户数量
		BigDecimal allTenderUserNum = getAllTenderUserNum(date);
		//获取某日所有充值用户数量
		BigDecimal allRechargeUserNum = getAllRechargeUserNum(date);
		//投资用户占充值用户比 = 投资用户/充值用户
		if (allRechargeUserNum.compareTo(BigDecimal.ZERO)!=0) {
			investHoldDepo = allTenderUserNum.divide(allRechargeUserNum,2);
		} else if (allRechargeUserNum.compareTo(BigDecimal.ZERO)==0) {
			investHoldDepo = allTenderUserNum.negate();
		}
		
		//获取标的额占充值额比
		tenderNumHoldDepoNum = getTenderNumHoldDepoNum(date);
		BigDecimal yesterdayTenderNumHoldDepoNum = getTenderNumHoldDepoNum(date2);
		if (yesterdayTenderNumHoldDepoNum.compareTo(BigDecimal.ZERO)!=0) {
			//标的额占充值额比环比 = 当日标的额占充值额比-前1日标的额占充值额比）/前1日标的额占充值额比
			tenderNumHoldDepoNumDayRatio = (tenderNumHoldDepoNum.subtract(yesterdayTenderNumHoldDepoNum)).divide(yesterdayTenderNumHoldDepoNum,2);
		} else if (yesterdayTenderNumHoldDepoNum.compareTo(BigDecimal.ZERO)==0) {
			tenderNumHoldDepoNumDayRatio = (tenderNumHoldDepoNum.subtract(yesterdayTenderNumHoldDepoNum)).negate();
		}
		
		//投资额占充值额比
		BigDecimal investNumHoldDepoNum = getInvestNumHoldDepoNum(date);
		
		BigDecimal yesterdayInvestNumHoldDepoNum = getInvestNumHoldDepoNum(date2);
		if (yesterdayInvestNumHoldDepoNum.compareTo(BigDecimal.ZERO)!=0) {
			//投资额占充值额比环比 = （当日投资额占充值额比-前1日投资额占充值额比）/前1日投资额占充值额比
			investNumHoldDepoNumDayRatio = (investNumHoldDepoNum.subtract(yesterdayInvestNumHoldDepoNum)).divide(yesterdayInvestNumHoldDepoNum,2);
		} else if (yesterdayInvestNumHoldDepoNum.compareTo(BigDecimal.ZERO)==0) {
			investNumHoldDepoNumDayRatio = (investNumHoldDepoNum.subtract(yesterdayInvestNumHoldDepoNum)).negate();
		}
		
		//获取某日注册用户数量
		BigDecimal RegUserNum = getRegUserNum(date);
		//获取某日激活用户数量
		BigDecimal ActivUserNum = getActivUserNum(date);
		if (ActivUserNum.compareTo(BigDecimal.ZERO)!=0) {
			//激活到注册转化率 = 注册/激活
			actiToReg = RegUserNum.divide(ActivUserNum,2);
		} else if (ActivUserNum.compareTo(BigDecimal.ZERO)==0) {
			actiToReg = RegUserNum.negate();
		}
		
		//获取上一注册日的注册用户数
		BigDecimal yesterdayRegUserNum = getRegUserNum(date2);
		if (yesterdayRegUserNum.compareTo(BigDecimal.ZERO)!=0) {
			//注册日环比增长 = （当日注册-前1日注册）/前1日注册
			regDayRatio = (RegUserNum.subtract(yesterdayRegUserNum)).divide(yesterdayRegUserNum,2);
		} else if (yesterdayRegUserNum.compareTo(BigDecimal.ZERO)==0) {
			regDayRatio = (RegUserNum.subtract(yesterdayRegUserNum)).negate();
		}
		
		//注册到投资转化 = 投资用户/注册用户
		if (RegUserNum.compareTo(BigDecimal.ZERO)!=0) {
			regToInvest = allTenderUserNum.divide(RegUserNum,2);
		} else if (RegUserNum.compareTo(BigDecimal.ZERO)==0) {
			regToInvest = allTenderUserNum.negate();
		}
		
		BigDecimal regUserNum = getRegUserNum(date);
		BigDecimal newFinanceRegUserNum = getFinanceRegUserNum(date);
		
		BigDecimal newUserAddTotalAmount = getNewUserAddTotalAmount(date);
		
		BigDecimal oldUserAddTotalAmount = getOldUserAddTotalAmount(date);
		
		BigDecimal allTenderAmount = getAllTenderAmount(date);
		
		BigDecimal allUserAddTotalAmount = getAllUserAddTotalAmount(date);
		
		BigDecimal allInvestUsers = getAllInvestUsers();
		
		BigDecimal totalInvestAmount = getTotalInvestAmount();
		
		BigDecimal totalTenderAmount = getTotalTenderAmount();
		BigDecimal withdrawNum = getWithdrawNum(date);
		BigDecimal withdrawAmount = getWithdrawAmount(date);
		BigDecimal totalDepositNum = getTotalDepositNum();
		BigDecimal totalDepositAmount = getTotalDepositAmount();
		
		BigDecimal iosRegUserNum = getIOSRegUserNum(date);
		
		BigDecimal iosFinanceReg = getIosFinanceRegUserNum(date);
		
		BigDecimal iosActivUserNum = getIOSActivUserNum(date);
		
		BigDecimal iosDepositNum = getIOSDepositNum(date);
		BigDecimal iosDepositAmount = getIOSDepositAmount(date);
		
		BigDecimal iosInvestNum = getIOSInvestNum(date);
		
		BigDecimal iosInvestAmount = getIOSInvestAmount(date);
		
		BigDecimal iosTotalInvestNum = getIOSTotalInvestNum(date, date2);
		BigDecimal iosTotalInvestAmount = getIOSTotalInvestAmount(date, date2);
		
		BigDecimal yesterdayiosRegUserNum = getIOSRegUserNum(date2);
		
		if (yesterdayiosRegUserNum.compareTo(BigDecimal.ZERO)!=0) {
			//iOS注册日环比增长 = （当日注册-前1日注册）/前1日注册
			iosNewRegDayRatio = (iosRegUserNum.subtract(yesterdayiosRegUserNum)).divide(yesterdayiosRegUserNum,2);
		} else if (yesterdayiosRegUserNum.compareTo(BigDecimal.ZERO)==0) {
			iosNewRegDayRatio = (iosRegUserNum.subtract(yesterdayiosRegUserNum)).negate();
		}
		
		
		BigDecimal iosNewRegToDepo = getIOSNewRegToDepo(date);
		
		BigDecimal yesterdayiosNewRegToDepo = getIOSNewRegToDepo(date2);
		
		if (yesterdayiosNewRegToDepo.compareTo(BigDecimal.ZERO)!=0) {
			//iOS新增注册到充值转化环比 = （当日转化-前1日转化）/前1日转化
			iosNewRegToDepoDayRatio = (iosNewRegToDepo.subtract(yesterdayiosNewRegToDepo)).divide(yesterdayiosNewRegToDepo,2);
		} else if (yesterdayiosNewRegToDepo.compareTo(BigDecimal.ZERO)==0) {
			iosNewRegToDepoDayRatio = (iosNewRegToDepo.subtract(yesterdayiosNewRegToDepo)).negate();
		}
		
		BigDecimal iosNewDepositNum = getIOSNewDepositNum(date);
		BigDecimal yesterdayiosNewDepositNum = getIOSNewDepositNum(date2);
		
		if (yesterdayiosNewDepositNum.compareTo(BigDecimal.ZERO)!=0) {
			//iOS新增充值用户日环比 = （当日充值-前1日充值）/前1日充值
			iosNewDepoDayRatio = (iosNewDepositNum.subtract(yesterdayiosNewDepositNum)).divide(yesterdayiosNewDepositNum,2);
		} else if (yesterdayiosNewDepositNum.compareTo(BigDecimal.ZERO)==0) {
			iosNewDepoDayRatio = (iosNewDepositNum.subtract(yesterdayiosNewDepositNum)).negate();
		}
		
		BigDecimal iosNewDepositAmount = getIOSNewDepositAmount(date);
		BigDecimal yesterdayiosNewDepositAmount = getIOSNewDepositAmount(date2);
		
		if (yesterdayiosNewDepositAmount.compareTo(BigDecimal.ZERO)!=0) {
			//iOS新增充值额日环比 = （当日充值额-前1日充值额）/前1日充值额
			iosNewDepoNumDayRatio = (iosNewDepositAmount.subtract(yesterdayiosNewDepositAmount)).divide(yesterdayiosNewDepositAmount,2);
		} else if (yesterdayiosNewDepositAmount.compareTo(BigDecimal.ZERO)==0) {
			iosNewDepoNumDayRatio = (iosNewDepositAmount.subtract(yesterdayiosNewDepositAmount)).negate();
		}
		
		
		BigDecimal androidActivUserNum = getAndroidActivUserNum(date);
		BigDecimal androidRegUserNum = getAndroidRegUserNum(date);
		BigDecimal androidfinanceReg = getAndroidFinanceRegUserNum(date);
		
		BigDecimal androidDepositNum = getAndroidDepositNum(date);
		
		BigDecimal androidDepositAmount = getAndroidDepositAmount(date);
		
		BigDecimal androidInvestNum = getAndroidInvestNum(date);
		BigDecimal androidInvestAmount = getAndroidInvestAmount(date);
		
		BigDecimal androidTotalInvestNum = getAndroidTotalInvestNum(date, date2);
		BigDecimal androidTotalInvestAmount = getAndroidTotalInvestAmount(date, date2);
		BigDecimal yesterDayandroidRegUserNum = getAndroidRegUserNum(date2);
		
		if (yesterDayandroidRegUserNum.compareTo(BigDecimal.ZERO)!=0) {
			//android注册日环比增长 = （当日注册-前1日注册）/前1日注册
			androidNewRegDayRatio = (androidRegUserNum.subtract(yesterDayandroidRegUserNum)).divide(yesterDayandroidRegUserNum,2);
		} else if (yesterDayandroidRegUserNum.compareTo(BigDecimal.ZERO)==0) {
			androidNewRegDayRatio = (androidRegUserNum.subtract(yesterDayandroidRegUserNum)).negate();
		}
		
		
		BigDecimal androidNewDepositNum = getAndroidNewDepositNum(date);
		BigDecimal yesterdayandroidNewDepositNum = getAndroidNewDepositNum(date2);
		
		if (yesterdayandroidNewDepositNum.compareTo(BigDecimal.ZERO)!=0) {
			//android新增充值用户日环比 = （当日充值-前1日充值）/前1日充值
			androidNewDepoDayRatio = (androidNewDepositNum.subtract(yesterdayandroidNewDepositNum)).divide(yesterdayandroidNewDepositNum,2);
		} else if (yesterdayandroidNewDepositNum.compareTo(BigDecimal.ZERO)==0) {
			androidNewDepoDayRatio = (androidNewDepositNum.subtract(yesterdayandroidNewDepositNum)).negate();
		}
		
		androidNewRegToDepo = getAndroidNewRegToDepo(date);
		BigDecimal yesterdayandroidNewRegToDepo = getAndroidNewRegToDepo(date2);
		if (yesterdayandroidNewRegToDepo.compareTo(BigDecimal.ZERO)!=0) {
			//android新增注册到充值转化环比 = （当日转化-前1日转化）/前1日转化
			androidNewRegToDepoDayRatio = (androidNewRegToDepo.subtract(yesterdayandroidNewRegToDepo)).divide(yesterdayandroidNewRegToDepo,2);
		} else if (yesterdayandroidNewRegToDepo.compareTo(BigDecimal.ZERO)==0) {
			androidNewRegToDepoDayRatio = (androidNewRegToDepo.subtract(yesterdayandroidNewRegToDepo)).negate();
		}
		
		BigDecimal androidNewDepositAmount = getAndroidNewDepositAmount(date);
		BigDecimal yesterdayandroidNewDepositAmount = getAndroidNewDepositAmount(date2);
		
		if (yesterdayandroidNewDepositAmount.compareTo(BigDecimal.ZERO)!=0) {
			//android新增充值额日环比 = （当日充值额-前1日充值额）/前1日充值额
			androidNewDepoNumDayRatio = (androidNewDepositAmount.subtract(yesterdayandroidNewDepositAmount)).divide(yesterdayandroidNewDepositAmount,2);
		} else if (yesterdayandroidNewDepositAmount.compareTo(BigDecimal.ZERO)==0) {
			androidNewDepoNumDayRatio = (androidNewDepositAmount.subtract(yesterdayandroidNewDepositAmount)).negate();
		}
		
		entity.setDailyDate(DateUtils.convert(date));
		entity.setNewActi(ActivUserNum);    //新增激活
		entity.setNewReg(regUserNum);       //新增注册
		entity.setNewFinanceReg(newFinanceRegUserNum);  //新增理财注册
		entity.setActiReg(actiToReg);   //激活到注册转化率
		entity.setRegDayRatio(regDayRatio);  //注册日环比增长
		//TODO
		entity.setNewInvest(newUserInvestNum); //新增投资用户数量
		entity.setNewInvestAmount(newUserAddTotalAmount); //新增用户投资额
		entity.setRegInvest(regToInvest);  //注册到投资转化
		entity.setOldInvest(oldUserInvestNum); //老用户投资数量
		entity.setOldInvestAmount(oldUserAddTotalAmount);  //老用户投资额
		entity.setTenderAmount(allTenderAmount);  //当日标的额
		entity.setTenderFinishAmount(allUserAddTotalAmount);  //完成标的额 = 当日实际投资额
		entity.setTenderFinish(tenderFinish); //标的完成比例
		entity.setTenderFinishDayRatio(tenderFinishDayRatio); //标的完成比例日环比
		entity.setBidOldCycle(bidOldCycle);  //投标老用户占比
		entity.setBidNewCycle(bidNewCycle);  //投标新用户占比
		entity.setTotalInvestCount(allInvestUsers);  //累计投资用户
		entity.setTotalInvestAmount(totalInvestAmount);  //累计投资金额
		entity.setTotalTenderAmount(totalTenderAmount); //累计标的金额
		entity.setDepositCount(nowTotalUserRechargeNum); //充值用户数量
		entity.setDepoDayRatio(depoDayRatio); //充值用户日环比
		entity.setDepositAmount(nowRechargeTotalAmount); //充值额
		entity.setDeponumDayRatio(depoNumDayRatio); //充值额日环比
		entity.setDepoArpu(depoARPU); //充值ARPU
		entity.setDepoArpuDayRatio(depoARPUDayRatio); //充值ARPU日环比
		entity.setWithdrawCount(withdrawNum); //提现用户数量
		entity.setWithdrawAmount(withdrawAmount); //用户提现金额
		entity.setInvestCount(allTenderUserNum);  //投资用户
		entity.setInvestAmount(allUserAddTotalAmount); //投资额
		entity.setInvestArpu(investARPU); //投资ARPU
		entity.setInvestHoldDepo(investHoldDepo); //投资用户占充值用户比
		entity.setTendernumHoldDeponum(tenderNumHoldDepoNum); //标的额占充值额比
		entity.setTendernumHoldDeponumDayRatio(tenderNumHoldDepoNumDayRatio); //标的额占充值额比环比
		entity.setInvestnumHoldDeponum(investNumHoldDepoNum); //投资额占充值额比
		entity.setInvestnumHoldDeponumDayRatio(investNumHoldDepoNumDayRatio); //投资额占充值额比环比
		entity.setTotalDepositCount(totalDepositNum); //累计充值用户数量
		entity.setTotalDepositAmount(totalDepositAmount); //累计用户充值金额
		entity.setIosActi(iosActivUserNum); //IOS激活量
		entity.setIosReg(iosRegUserNum);  //IOS注册量
		entity.setIosFinanceReg(iosFinanceReg);  //ios理财注册量
		entity.setIosDeposit(iosDepositNum); //IOS新增充值用户数量
		entity.setIosDepositAmount(iosDepositAmount); //iOS新增充值额
		entity.setIosInvest(iosInvestNum); //iOS新增投资用户数量
		entity.setIosInvestAmount(iosInvestAmount); //iOS新增投资金额
		entity.setIosTotalInvest(iosTotalInvestNum); //iOS累计投资用户
		entity.setIosTotalInvestAmount(iosTotalInvestAmount); //iOS累计投资金额
		entity.setIosNewRegDayRatio(iosNewRegDayRatio); //iOS注册日环比增长
		entity.setIosNewDepoDayRatio(iosNewDepoDayRatio); //iOS新增充值用户日环比
		entity.setIosNewRegDepo(iosNewRegToDepo); //iOS新增注册到充值转化
		entity.setIosNewRegDepoDayRatio(iosNewRegToDepoDayRatio); //iOS新增注册到充值转化环比
		entity.setIosNewDeponumDayRatio(iosNewDepoNumDayRatio); //iOS新增充值额日环比
		entity.setAndroidActi(androidActivUserNum);  //android激活量
		entity.setAndroidReg(androidRegUserNum); //android注册量
		entity.setAndroidFinanceReg(androidfinanceReg); //android理财注册量
		entity.setAndroidDeposit(androidDepositNum); //android新增充值用户数量
		entity.setAndroidDepositAmount(androidDepositAmount); //android新增充值额
		entity.setAndroidInvest(androidInvestNum); //android新增投资用户数量
		entity.setAndroidInvestAmount(androidInvestAmount); //android新增投资金额
		entity.setAndroidTotalInvest(androidTotalInvestNum); //android累计投资用户
		entity.setAndroidTotalInvestAmount(androidTotalInvestAmount); //android累计投资金额
		entity.setAndroidNewRegDayRatio(androidNewRegDayRatio); //android注册日环比增长
		entity.setAndroidNewDepoDayRatio(androidNewDepoDayRatio); //android新增充值用户日环比
		entity.setAndroidNewRegDepo(androidNewRegToDepo); //android新增注册到充值转化
		entity.setAndroidNewRegDepoDayRatio(androidNewRegToDepoDayRatio); //android新增注册到充值转化环比
		entity.setAndroidNewDeponumDayRatio(androidNewDepoNumDayRatio); //android新增充值额日环比
		entity.setSavingpotAvailableBalance(savingpotAvailableBalance);
		
		
		int res = financeDailyDao.insert(entity);
		
		if (res==1) {
			logger.debug("添加日报表成功", entity);
		} else {
			logger.error("添加日报表出错,数据库操作失败", entity);
		}
	}
	
	
	/**
	 * 获取某日android理财注册用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getAndroidFinanceRegUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> list = new ArrayList<String>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		List<YydRegisterVo> androidFinanceRegUserNumList = yydUsersInfoDao.getAndroidFinanceRegUserNum(map);
		for (YydRegisterVo vo : androidFinanceRegUserNumList) {
			String register_version = vo.getRegisterVersion();
			if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
				register_version += ".0";
			}
			if (!StringUtils.isEmpty(register_version)) {
				String replaceVersion = register_version.replace(".", "");
				int regVersion = Integer.parseInt(replaceVersion);
				if (regVersion >= appVersion){
					list.add(vo.getUserId());
				}
			}
		}
		return new BigDecimal(list.size());
	}

	/**
	 * 获取某日 ios理财注册用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getIosFinanceRegUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> list = new ArrayList<String>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		List<YydRegisterVo> iosFinanceRegUserNumList = yydUsersInfoDao.getIosFinanceRegUserNum(map);
		for (YydRegisterVo vo : iosFinanceRegUserNumList) {
			String register_version = vo.getRegisterVersion();
			if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
				register_version += ".0";
			}
			if (!StringUtils.isEmpty(register_version)) {
				String replaceVersion = register_version.replace(".", "");
				int regVersion = Integer.parseInt(replaceVersion);
				if (regVersion >= appVersion){
					list.add(vo.getUserId());
				}
			}
		}
		return new BigDecimal(list.size());
	}

	private BigDecimal getSavingpotAvailableBalance() {
		BigDecimal amount= financeIncomeDao.getSavingpotAvailableBalance();
		return amount;
	}

	/**
	 * 获取某日理财注册用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getFinanceRegUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> list = new ArrayList<String>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		List<YydRegisterVo> financeRegUserNumList = yydUsersInfoDao.getFinanceRegUserNum(map);
		for (YydRegisterVo vo : financeRegUserNumList) {
			String register_version = vo.getRegisterVersion();
			if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
				register_version += ".0";
			}
			if (!StringUtils.isEmpty(register_version)) {
				String replaceVersion = register_version.replace(".", "");
				int regVersion = Integer.parseInt(replaceVersion);
				if (regVersion >= appVersion){
					list.add(vo.getUserId());
				}
			}
		}
		return new BigDecimal(list.size());
	}

	/**
	 * 获取ios和android投资用户
	 * 
	 */
	private void getIosAndAndroidInvestUser(String date) {
		AndroidInvestUidList = new ArrayList<String>();
		IOSInvestUidList = new ArrayList<String>();
		String ver = "";
		String ver2 = "";
		Integer intVer = 0;
		Integer intVer2 = 0;
		Set<String> list = financeSubmitTenderDao.getDataByDate(date);
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		ver = version.replace(".","");
		intVer = Integer.parseInt(ver);
		for (String uid : list){
			String userId = financeUidMappingService.getYYDUid(uid);
			YydDataVo vo = yydUsersInfoDao.getDataByUid(userId);
			if (vo!=null) {
				String appVersion = vo.getAppVersion();
				if (!StringUtils.isEmpty(appVersion) && appVersion.length()<5) {
					appVersion += ".0";
				}
				if (!StringUtils.isEmpty(appVersion)) {
					ver2 = appVersion.replace(".", "");
					intVer2 = Integer.parseInt(ver2);
				}
			}
			
			if(vo!=null && !StringUtils.isEmpty(vo.getDownloadChannel()) && (vo.getDownloadChannel().contains("ios") || vo.getDownloadChannel().contains("appstore")) && intVer2>=intVer) {
				IOSInvestUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
				
			}
			if (vo!=null && !StringUtils.isEmpty(vo.getDownloadChannel()) && vo.getDownloadChannel().contains("android") && intVer2>=intVer) {
				AndroidInvestUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
			}
			logger.debug("=========ver2:"+ver2+"===intVer2:"+intVer2);
			ver2 = "";
			intVer2 = 0;
		}
	}
	
	/**
	 * 获取ios和Android的充值用户
	 * 
	 */
	private void getIosAndAndroidDepoUser(String date) {
		AndroidDepoUidList = new ArrayList<String>();
		IOSDepoUidList = new ArrayList<String>();
		String ver = "";
		String ver2 = "";
		Integer intVer = 0;
		Integer intVer2 = 0;
		Set<String> list = financeDepositWithdrawDao.getDataByDate(date);
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		ver = version.replace(".","");
		intVer = Integer.parseInt(ver);
		
		for (String uid: list) {
			String userId = financeUidMappingService.getYYDUid(uid);
			YydDataVo vo = yydUsersInfoDao.getDataByUid(userId);
			if (vo!=null) {
				String appVersion = vo.getAppVersion();
				if (!StringUtils.isEmpty(appVersion) && appVersion.length()<5) {
					appVersion += ".0";
				}
				if (!StringUtils.isEmpty(appVersion)) {
					ver2 = appVersion.replace(".", "");
					intVer2 = Integer.parseInt(ver2);
				}
			}
			
			if(vo != null && !StringUtils.isEmpty(vo.getDownloadChannel()) && (vo.getDownloadChannel().contains("ios")|| vo.getDownloadChannel().contains("appstore")) && intVer2>=intVer) {
				IOSDepoUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
			}
			if (vo!=null && !StringUtils.isEmpty(vo.getDownloadChannel()) && vo.getDownloadChannel().contains("android") && intVer2>=intVer) {
				AndroidDepoUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
			}
			logger.debug("=========ver2:"+ver2+"===intVer2:"+intVer2);
			ver2 = "";
			intVer2 = 0;
		}
	}
	
	/**
	 * 获取累计所有ios和Android的充值用户
	 * @param date1
	 * @param date2
	 */
	private void getAllIosAndAndroidDepoUser(String date1, String date2) {
		allAndroidDepoUidList = new ArrayList<String>();
		allIOSDepoUidList = new ArrayList<String>();
		String ver = "";
		String ver2 = "";
		Integer intVer = 0;
		Integer intVer2 = 0;
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		ver = version.replace(".", "");
		intVer = Integer.parseInt(ver);
		Set<String> list = financeDepositWithdrawDao.getDataByDate(date1);
		Set<String> list2 = financeDepositWithdrawDao.getDataByDate(date2);
		list.addAll(list2);
		Set<String> set = new HashSet<String>(list);
		for (String uid: set) {
			String userId = financeUidMappingService.getYYDUid(uid);
			YydDataVo vo = yydUsersInfoDao.getDataByUid(userId);
			if (vo!=null) {
				String appVersion = vo.getAppVersion();
				if (!StringUtils.isEmpty(appVersion) && appVersion.length()<5) {
					appVersion += ".0";
				}
				if (!StringUtils.isEmpty(appVersion)) {
					ver2 = appVersion.replace(".", "");
					intVer2 = Integer.parseInt(ver2);
				}
			}
			
			if(vo!=null && !StringUtils.isEmpty(vo.getDownloadChannel()) && (vo.getDownloadChannel().contains("ios")|| vo.getDownloadChannel().contains("appstore")) && intVer2>=intVer) {
				allIOSDepoUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
			}
			if (vo!=null && !StringUtils.isEmpty(vo.getDownloadChannel()) && vo.getDownloadChannel().contains("android") && intVer2>=intVer) {
				allAndroidDepoUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
			}
			logger.debug("=========ver2:"+ver2+"===intVer2:"+intVer2);
			ver2 = "";
			intVer2 = 0;
		}
	}
	
	/**
	 * 获取累计所有ios和android的投资用户
	 * @param date1
	 * @param date2
	 */
	private void getAllIosAndAndroidInvestUser(String date1, String date2) {
		allAndroidInvestUidList = new ArrayList<String>();
		allIOSInvestUidList = new ArrayList<String>();
		String ver = "";
		String ver2 = "";
		Integer intVer = 0;
		Integer intVer2 = 0;    
		logger.debug("=========");
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		ver = version.replace(".", "");
		intVer = Integer.parseInt(ver);
		Set<String> list = financeSubmitTenderDao.getDataByDate(date1);
		Set<String> list2 = financeSubmitTenderDao.getDataByDate(date2);
		list.addAll(list2);
		Set<String> set = new HashSet<String>(list);
		for (String uid: set) {
			String userId = financeUidMappingService.getYYDUid(uid);
			YydDataVo vo = yydUsersInfoDao.getDataByUid(userId);
			if (vo!=null) {
				String appVersion = vo.getAppVersion();
				if (!StringUtils.isEmpty(appVersion) && appVersion.length()<5) {
					appVersion += ".0";
				}
				if (!StringUtils.isEmpty(appVersion)) {
					ver2 = appVersion.replace(".", "");
					intVer2 = Integer.parseInt(ver2);
				}
			}
			
			if(vo!=null && !StringUtils.isEmpty(vo.getDownloadChannel()) && (vo.getDownloadChannel().contains("ios")|| vo.getDownloadChannel().contains("appstore")) && intVer2>=intVer) {
				allIOSInvestUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
			}
			if (vo!=null && !StringUtils.isEmpty(vo.getDownloadChannel()) && vo.getDownloadChannel().contains("android") && intVer2>=intVer) {
				allAndroidInvestUidList.add(uid);
				ver2 = "";
				intVer2 = 0;
			}
			logger.debug("=========ver2:"+ver2+"===intVer2:"+intVer2);
			ver2 = "";
			intVer2 = 0;
		}
	}
	
	
	
	/**
	 * 获取某日所有标的 的金额  放出去的钱
	 * @param date
	 * @return
	 */
	private BigDecimal getAllTenderAmount(String date) {
		logger.debug("-----getAllTenderAmount"+date);
		BigDecimal allTenderAmount = financeTenderCfgDao.getgetAllTenderAmount(date);
		logger.debug("result getAllTenderAmount"+allTenderAmount);
		return allTenderAmount;
	}
	
	/**
	 * 标的完成比例查询
	 * @param date
	 * @return
	 */
	private BigDecimal tenderAmountANDaddAmountDaily(String date) {
		logger.debug("--------tenderAmountANDaddAmountDaily"+date);
		/**
		 * 查询date中标的总金额和标的号
		 */
		List<DailyTenderAmountVo> amountVo = financeTenderCfgDao.getDailyTenderTotalAmount(date);
		logger.debug("-------查询date中标的总金额和标的号"+amountVo.size()+"-------"+amountVo);
		BigDecimal totalAmonut = BigDecimal.ZERO; //某日标的的总金额
		BigDecimal addAmount = BigDecimal.ZERO; //某日标的实际投入金额
		for (DailyTenderAmountVo vo : amountVo) {
			//统计标的总金额
			totalAmonut = totalAmonut.add(vo.getTotalAmount());
			//根据标的号查询标的被投多少钱
			BigDecimal amount = financeSubmitTenderDao.getTotalAmountBySid(vo.getId());
			logger.debug("-----根据标的号查询标的被投多少钱"+amount);
			addAmount = addAmount.add(amount);
		}
		logger.debug("==========="+totalAmonut+"======="+addAmount);
		if (totalAmonut.compareTo(BigDecimal.ZERO)>0) {
			return addAmount.divide(totalAmonut,2);
		} else {
			return addAmount.negate();
		}
		
	}
	
	/**
	 * 根据当前时间获取前一日时间
	 * @param date
	 * @return
	 */
	@SuppressWarnings("static-access")
	private String getYesterdayByDate(String date) {
		Date convert = DateUtils.convert(date);
		Calendar calendar = new GregorianCalendar(); 
		calendar.setTime(convert);
		calendar.add(calendar.DATE,-1);
		convert = calendar.getTime();
		String string = DateUtils.convert(convert);
		String date2 = string.substring(0,10);
		return date2;
	}
	
	
	/**
	 * 获取 累计所有投资用户
	 * @return
	 */
	private BigDecimal getAllInvestUsers(){
		List<String> allInvestUsers = financeSubmitTenderDao.getAllInvestUsers();
		return new BigDecimal(allInvestUsers.size());
	}
	
	/**
	 * 获取累计所有投资金额
	 * @return
	 */
	private BigDecimal getTotalInvestAmount() {
		BigDecimal totalInvestAmount = financeSubmitTenderDao.getTotalInvestAmount();
		return totalInvestAmount;
	}
	
	/**
	 * 获取累计所有标的金额
	 * @return
	 */
	private BigDecimal getTotalTenderAmount() {
		BigDecimal totalTenderAmount = financeTenderCfgDao.getTotalTenderAmount();
		return totalTenderAmount;
	}
	
	/**
	 * 获取累计充值用户数量
	 * @return
	 */
	private BigDecimal getTotalDepositNum() {
		List<String> totalDepositNum = financeDepositWithdrawDao.getTotalDepositNum();
		return new BigDecimal(totalDepositNum.size());
	}
	
	/**
	 * 获取用户累计充值金额
	 * @return
	 */
	private BigDecimal getTotalDepositAmount() {
		BigDecimal totalDepositAmount = financeDepositWithdrawDao.getTotalDepositAmount();
		return totalDepositAmount;
	}
	
	/**
	 * 获取某日所有投资用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getAllTenderUserNum(String date) {
		Integer allTenderUserNum = financeSubmitTenderDao.getAllTenderUserNum(date);
		return new BigDecimal(allTenderUserNum);
	}
	
	/**
	 * 获取某日老用户投资人数
	 * @return
	 */
	private BigDecimal getOldUserInvestNum(String date) {
		oldUserInvestNumList = new ArrayList<String>();
		logger.debug("============>>getOldUserInvestNum"+date);
		//获取某日所有投资用户
		Set<String> list = financeSubmitTenderDao.getDataByDate(date);
		logger.debug("===========>>getOldUserInvestNum listsize"+list.size());
		logger.debug("===========>>getOldUserInvestNum list"+list);
		for (String uid : list) {
			Integer gethistoryInvestNum = financeSubmitTenderDao.gethistoryInvestNum(uid);
			logger.debug("===========>>gethistoryInvestNum"+gethistoryInvestNum);
			Integer count = financeSubmitTenderDao.getUserInvestNum(uid, date);
			logger.debug("==========>>count"+count);
			if (count!=0 && gethistoryInvestNum>count) {
				oldUserInvestNumList.add(uid);
				logger.debug("==========>>"+uid);
			}
		}
		logger.debug("----oldUserInvestNumList"+oldUserInvestNumList.size());
		return new BigDecimal(oldUserInvestNumList.size());
	}
	
	/**
	 * 获取某日老用户投资额
	 * @param date
	 */
	private BigDecimal getOldUserAddTotalAmount(String date) {
		BigDecimal addTotalAmount = BigDecimal.ZERO;
		for (String uid : oldUserInvestNumList) {
			BigDecimal addAmount = financeSubmitTenderDao.getUserAddTotalAmount(uid, date);
			addTotalAmount = addTotalAmount.add(addAmount);
		}
		return addTotalAmount;
	}
	
	/**
	 * 获取某日新增用户投资数量
	 * @param date
	 * @return
	 */
	private BigDecimal getNewUserInvestNum(String date) {
		newUserInvestNumList = new ArrayList<String>();
		//获取某日所有投资用户
		Set<String> list = financeSubmitTenderDao.getDataByDate(date);
		logger.debug("===========>>getNewUserInvestNum listsize"+list.size());
		logger.debug("===========>>getNewUserInvestNum list"+list);
		//TOTO
		for (String uid : list) {
			Integer gethistoryInvestNum = financeSubmitTenderDao.gethistoryInvestNum(uid);
			Integer count = financeSubmitTenderDao.getUserInvestNum(uid, date);
			if (count!=0 && gethistoryInvestNum==count) {
				newUserInvestNumList.add(uid);
			}
		}
		return new BigDecimal(newUserInvestNumList.size());
	}
	
	/**
	 * 获取某日新用户投资额
	 * @param date
	 */
	private BigDecimal getNewUserAddTotalAmount(String date) {
		BigDecimal addTotalAmount = BigDecimal.ZERO;
		for (String uid : newUserInvestNumList) {
			BigDecimal addAmount = financeSubmitTenderDao.getUserAddTotalAmount(uid, date);
			addTotalAmount = addTotalAmount.add(addAmount);
		}
		return addTotalAmount;
	}
	
	/**
	 * 获取某日所有用户投资总额
	 * @param date
	 * @return
	 */
	private BigDecimal getAllUserAddTotalAmount(String date) {
		BigDecimal addAmount = financeSubmitTenderDao.getAllUserAddTotalAmount(date);
		return addAmount;
	}
	
	/**
	 * 获取某日所有用户充值的 用户数量
	 * @param date
	 * @return
	 */
	//TODO
	private BigDecimal getAllRechargeUserNum(String date) {
		allRechargeUserNum = new HashSet<String>();
		HashSet<String> set = new HashSet<String>();
		allRechargeUserNum = financeDepositWithdrawDao.getDataByDate(date);
		//从中过滤出理财人
		List<FinanceFinancingUser> all = financeFinancingUserDao.getAll();
		Iterator<String> it = allRechargeUserNum.iterator(); 
		while (it.hasNext()) {
			String uid = it.next();
			logger.debug("======>> withdrawUid:"+uid);
			for (int j=0; j<all.size(); j++) {
				if (uid.equals(all.get(j).getUid())) {
					set.add(uid);
					logger.debug("==========>>addRemoveSet:"+set);
				}
			}
		}
		allRechargeUserNum.removeAll(set);
		logger.debug("======>>getAllRechargeUserNum list"+allRechargeUserNum.size());
		return new BigDecimal(allRechargeUserNum.size());
	}
	
	/**
	 * 获取某日所有用户充值总金额
	 * @param date
	 * @return
	 */
	private BigDecimal getRechargeAmount(String date) {
		BigDecimal amount = BigDecimal.ZERO;
		logger.debug("=========>>allRechargeUserNum:"+allRechargeUserNum.size());
		for (String uid : allRechargeUserNum) {
			BigDecimal rechargeAmount = financeDepositWithdrawDao.getgetRechargeAmount(uid, date);
			amount = amount.add(rechargeAmount);
		}
		logger.debug("========>>getRechargeAmount:"+amount);
		return amount;
	}
	
	/**
	 * 获取某日提现用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getWithdrawNum(String date) {
		withdrawNumList = new HashSet<String>();
		HashSet<String> set = new HashSet<String>();
		withdrawNumList = financeDepositWithdrawDao.getWithdrawNum(date);
		logger.debug("==========>>withdrawNumList"+withdrawNumList.size());
		//从中过滤出理财人
		List<FinanceFinancingUser> all = financeFinancingUserDao.getAll();
		Iterator<String> it = withdrawNumList.iterator(); 
		while (it.hasNext()) {
			String uid = it.next();
			logger.debug("======>> withdrawUid:"+uid);
			for (int j=0; j<all.size(); j++) {
				if (uid.equals(all.get(j).getUid())) {
					set.add(uid);
					logger.debug("==========>>addRemoveSet:"+set);
				}
			}
		}
		logger.debug("==========>>removeSetSize:"+set.size());
		withdrawNumList.removeAll(set);
		logger.debug("=====>>withdrawNumListSize"+withdrawNumList.size());
		logger.debug("=====>>withdrawNumListData"+withdrawNumList);
		return new BigDecimal(withdrawNumList.size());
	}
	
	/**
	 * 获取某日用户提现金额
	 * @param date
	 * @return
	 */
	private BigDecimal getWithdrawAmount(String date) {
		BigDecimal amount = BigDecimal.ZERO;
		for (String uid:withdrawNumList) {
			BigDecimal withdrawAmount = financeDepositWithdrawDao.getWithdrawAmount(uid, date);
			amount = amount.add(withdrawAmount);
		}
		
		return amount;
	}
	
	/**
	 * 获取某日的depoARPU
	 * @return
	 */
	private BigDecimal getDepoARPU(String date) {
		BigDecimal depoARPU = BigDecimal.ZERO;
		//获取当日所有充值用户的用户数量
		BigDecimal nowTotalUserRechargeNum = getAllRechargeUserNum(date);
		//当日所有用户充值总金额
		BigDecimal nowRechargeTotalAmount = getRechargeAmount(date);
		//充值ARPU = 充值额/充值用户
		if (nowTotalUserRechargeNum.compareTo(BigDecimal.ZERO)!=0) {
			depoARPU = nowRechargeTotalAmount.divide(nowTotalUserRechargeNum,2);
		} else if (nowTotalUserRechargeNum.compareTo(BigDecimal.ZERO)==0) {
			depoARPU = nowRechargeTotalAmount.negate();
		}
		return depoARPU;
	}
	
	/**
	 * 获取某日的investARPU
	 * @param date
	 * @return
	 */
	private BigDecimal getInvestARPU(String date) {
		BigDecimal investARPU = BigDecimal.ZERO;
		//获取某日所有用户的投资总额
		BigDecimal allUserAddTotalAmount = getAllUserAddTotalAmount(date);
		//获取某日所有投资用户的数量
		BigDecimal allUserAddTotalNum = getAllTenderUserNum(date);
		//投资ARPU = 投资额/投资用户
		if (allUserAddTotalNum.compareTo(BigDecimal.ZERO)!=0) {
			investARPU = allUserAddTotalAmount.divide(allUserAddTotalNum,2);
		} else if (allUserAddTotalNum.compareTo(BigDecimal.ZERO)==0) {
			investARPU = allUserAddTotalAmount.negate();
		}
		return investARPU;
	}
	
	/**
	 *  获取 标的额占充值额比
	 * @param date
	 */
	private BigDecimal getTenderNumHoldDepoNum(String date) {
		BigDecimal holdDepoNum = BigDecimal.ZERO;
		//获取某日标的金额
		BigDecimal allTenderAmount = getAllTenderAmount(date);
		//获取某日的充值金额
		BigDecimal rechargeAmount = getRechargeAmount(date);
		
		if (rechargeAmount.compareTo(BigDecimal.ZERO)!=0) {
			//说明当日充值金额不为零    标的额占充值额比 =  标的额/充值额
			holdDepoNum = allTenderAmount.divide(rechargeAmount,2);
		} else if (rechargeAmount.compareTo(BigDecimal.ZERO)==0) {
			holdDepoNum = allTenderAmount.negate();
		}
		return holdDepoNum;
	}
	
	/**
	 * 获取 投资额占充值额比
	 * @param date
	 * @return
	 */
	private BigDecimal getInvestNumHoldDepoNum(String date) {
		//投资额占充值额比 = 投资额/充值额
		BigDecimal amount = BigDecimal.ZERO;
		//某日的投资总额
		BigDecimal allUserAddTotalAmount = getAllUserAddTotalAmount(date);
		//获取某日的充值金额
	    BigDecimal rechargeAmount = getRechargeAmount(date);
	    if (rechargeAmount.compareTo(BigDecimal.ZERO)!=0) {
			//说明当日充值金额不为零    标的额占充值额比 =  标的额/充值额
	    	amount = allUserAddTotalAmount.divide(rechargeAmount,2);
		} else if (rechargeAmount.compareTo(BigDecimal.ZERO)==0) {
			amount = allUserAddTotalAmount.negate();
		}
		return amount;
	}
	
	
	/**
	 * 获取某日注册用户数
	 * @param date
	 * @return
	 */
	private BigDecimal getRegUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> list = new ArrayList<String>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		List<YydRegisterVo> regUserNumList = yydUsersInfoDao.getRegUserNum(map);
		for (YydRegisterVo vo : regUserNumList) {
			String register_version = vo.getRegisterVersion();
			if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
				register_version += ".0";
			}
			if (!StringUtils.isEmpty(register_version)) {
				String replaceVersion = register_version.replace(".", "");
				int regVersion = Integer.parseInt(replaceVersion);
				if (regVersion >= appVersion){
					list.add(vo.getUserId());
				}
			}
			
		}
		return new BigDecimal(list.size());
	}
	
	/**
	 * 获取某日激活用户数
	 * @param date
	 * @return
	 */
	private BigDecimal getActivUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		Integer activUserNum = yydUsersInfoDao.getActivUserNum(map);
		return new BigDecimal(activUserNum);
	}
	
	/**
	 * 获取某日ios注册用户数
	 * @param date
	 * @return
	 */
	private BigDecimal getIOSRegUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> list = new ArrayList<String>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		List<YydRegisterVo> iosRegUserNumList = yydUsersInfoDao.getIOSRegUserNum(map);
		for (YydRegisterVo vo : iosRegUserNumList) {
			String register_version = vo.getRegisterVersion();
			if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
				register_version += ".0";
			}
			if (!StringUtils.isEmpty(register_version)) {
				String replaceVersion = register_version.replace(".", "");
				int regVersion = Integer.parseInt(replaceVersion);
				if (regVersion >= appVersion){
					list.add(vo.getUserId());
				}
			}
		}
		return new BigDecimal(list.size());
	}
	
	/**
	 * 获取某日ios激活用户数
	 * @param date
	 * @return
	 *
	 */
	private BigDecimal getIOSActivUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		Integer iosActivUserNum = yydUsersInfoDao.getIOSActivUserNum(map);
		return new BigDecimal(iosActivUserNum);
	}
	
	/**
	 * 获取某日ios的新增充值用户数量
	 * @return
	 */
	private BigDecimal getIOSDepositNum(String date) {
		iosNewAddUserDepoNumList = new ArrayList<String>();
		for (String uid:IOSDepoUidList) {
			//历史充值记录为0  date充值记录大于等于1
			//获取历史记录
			Integer historynum = financeDepositWithdrawDao.getRechargeDateByUidNODate(uid);
			//获取今天记录
			Integer newIosDeponum = financeDepositWithdrawDao.getIOSDepositNum(uid, date);
			if (historynum==newIosDeponum && newIosDeponum!=0) {
				iosNewAddUserDepoNumList.add(uid);
			}
		}
		return new BigDecimal(iosNewAddUserDepoNumList.size());
	}
	
	/**
	 * 获取某日 ios新增用户的充值金额
	 * @param date
	 * @return
	 */
	private BigDecimal getIOSDepositAmount(String date) {
		BigDecimal depositAmount = BigDecimal.ZERO;
		for (String uid:iosNewAddUserDepoNumList) {
			BigDecimal iosDepositAmount = financeDepositWithdrawDao.getIOSDepositAmount(uid, date);
			depositAmount = depositAmount.add(iosDepositAmount);
		}
		return depositAmount;
	}
	
	/**
	 * 获取某日ios新增投资用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getIOSInvestNum(String date) {
		iosNewInvestUserList = new ArrayList<String>();
		for (String uid : allIOSInvestUidList) {
			Integer gethistoryInvestNum = financeSubmitTenderDao.gethistoryInvestNum(uid);
			Integer nowNum = financeSubmitTenderDao.getIOSInvestNum(uid, date);
			if (gethistoryInvestNum==nowNum && nowNum!=0) {
				iosNewInvestUserList.add(uid);
			}
		}
		return new BigDecimal(iosNewInvestUserList.size());
	}
	
	/**
	 * 获取某日新增ios用户投资金额
	 * @return
	 */
	private BigDecimal getIOSInvestAmount(String date) {
		BigDecimal investAmount = BigDecimal.ZERO;
		for (String uid:iosNewInvestUserList) {
			BigDecimal iosInvestAmount = financeSubmitTenderDao.getIOSInvestAmount(uid, date);
			investAmount = investAmount.add(iosInvestAmount);
		}
		return investAmount;
	}
	
	
	/**
	 * 获取ios累计投资用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getIOSTotalInvestNum(String date, String date2) {
		//昨天的
		BigDecimal yesterdayIOSTotalInvestNum = financeDailyDao.getYesterdayIOSTotalInvestNum(date2);
		if(null==yesterdayIOSTotalInvestNum) {
			yesterdayIOSTotalInvestNum = BigDecimal.ZERO;
		}
		
		return new BigDecimal(IOSInvestUidList.size()).add(yesterdayIOSTotalInvestNum);
	}
	
	/**
	 * 获取ios用户累计投资金额
	 * @return
	 */
	private BigDecimal getIOSTotalInvestAmount(String date, String date2) {
		BigDecimal investAmount = BigDecimal.ZERO;
		//获取昨天ios用户累计投资金额
		BigDecimal yesterdayIOSTotalInvestAmount = financeDailyDao.getYesterdayIOSTotalInvestAmount(date2);
		if (null==yesterdayIOSTotalInvestAmount) {
			yesterdayIOSTotalInvestAmount = BigDecimal.ZERO;
		}
		for (String uid:IOSInvestUidList) {
			//获取今日ios的投资金额
			BigDecimal iosInvestAmount = financeSubmitTenderDao.getIOSTotalInvestAmount(uid, date);
			investAmount = investAmount.add(iosInvestAmount);
		}
		return investAmount.add(yesterdayIOSTotalInvestAmount);
	}
	
	/**
	 * 获取ios充值的新用户和老用户
	 * @return
	 */
	private Map<String, Object> getIOSNewAndOldDepositNum() {
		Map<String,Object> map = new HashMap<String, Object>();
		List<String> newDepositList = new ArrayList<String>();
		List<String> oldDepositList = new ArrayList<String>();
		for (String uid : allIOSDepoUidList) {
			Integer iosNewAndOldDepositNum = financeDepositWithdrawDao.getIOSNewAndOldDepositNum(uid);
			if (iosNewAndOldDepositNum==1) {
				//新用户
				newDepositList.add(uid);
			} else if (iosNewAndOldDepositNum>1) {
				//老用户
				oldDepositList.add(uid);
			}
		}
		map.put("newDepositList", newDepositList);
		map.put("oldDepositList", oldDepositList);
		return map;
	}
	
	/**
	 * 获取IOS充值的新用户
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BigDecimal getIOSNewDepositNum() {
		Map<String, Object> map = getIOSNewAndOldDepositNum();
		List<String> newDepositList = (List<String>)map.get("newDepositList");
		return new BigDecimal(newDepositList.size());
	}
	
	/**
	 * iOS新增注册到充值转化
	 * @param date
	 * @return
	 */
	private BigDecimal getIOSNewRegToDepo(String date) {
		//iOS新增注册到充值转化 = 新增充值用户/当日注册用户
		BigDecimal iosNewRegToDepo = BigDecimal.ZERO;
		BigDecimal iosNewDepositNum = getIOSNewDepositNum(); //新增充值用户
		BigDecimal iosRegUserNum = getIOSRegUserNum(date); //当日注册用户
		if (iosRegUserNum.compareTo(BigDecimal.ZERO)!=0) {
			iosNewRegToDepo = iosNewDepositNum.divide(iosRegUserNum,2);
		} else if (iosRegUserNum.compareTo(BigDecimal.ZERO)==0) {
			iosNewRegToDepo = iosNewDepositNum.negate();
		}
		return iosNewRegToDepo;
	}
	
	/**
	 * 获取某日ios新用户充值金额
	 * @param date
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BigDecimal getIOSNewDepositAmount(String date) {
		Map<String, Object> map = getIOSNewAndOldDepositNum();
		BigDecimal depositAmount = BigDecimal.ZERO;
		List<String> newDepositList = (List<String>)map.get("newDepositList");
		for (String uid : newDepositList) {
			BigDecimal iosDepositAmount = financeDepositWithdrawDao.getIOSDepositAmount(uid, date);
			depositAmount = depositAmount.add(iosDepositAmount);
		}
		return depositAmount;
	}
	
	/**
	 * 获取某日ios新充值用户数量
	 * @param date
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BigDecimal getIOSNewDepositNum(String date) {
		Map<String, Object> map = getIOSNewAndOldDepositNum();
		List<String> newDepositList = (List<String>)map.get("newDepositList");
		List<String> list = new ArrayList<String>();
		for (String uid : newDepositList) {
			Integer iosNewDepositNum = financeDepositWithdrawDao.getIOSNewDepositNum(uid, date);
			if (iosNewDepositNum==1) {
				list.add(uid);
			}
		}
		return new BigDecimal(list.size());
	}
	
	/**
	 * 获取某日Android注册用户数
	 * @param date
	 * @return
	 */
	private BigDecimal getAndroidRegUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> list = new ArrayList<String>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		List<YydRegisterVo> androidRegUserNumList = yydUsersInfoDao.getAndroidRegUserNum(map);
		for (YydRegisterVo vo : androidRegUserNumList) {
			String register_version = vo.getRegisterVersion();
			if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
				register_version += ".0";
			}
			if (!StringUtils.isEmpty(register_version)) {
				String replaceVersion = register_version.replace(".", "");
				int regVersion = Integer.parseInt(replaceVersion);
				if (regVersion >= appVersion){
					list.add(vo.getUserId());
				}
			}
		}
		return new BigDecimal(list.size());
	}
	
	/**
	 * 获取某日android激活用户数
	 * @param date
	 * @return
	 * 
	 */
	private BigDecimal getAndroidActivUserNum(String date) {
		Map<String, Object> map = new HashMap<String, Object>();
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		Integer androidActivUserNum = yydUsersInfoDao.getAndroidActivUserNum(map);
		return new BigDecimal(androidActivUserNum);
	}
	
	/**
	 * 获取某日android的新用户充值用户数量
	 * @return
	 */
	private BigDecimal getAndroidDepositNum(String date) {
		androidNewAddUserDepoNumList = new ArrayList<String>();
		for (String uid:AndroidDepoUidList) {
			//获取历史记录
			Integer historynum = financeDepositWithdrawDao.getRechargeDateByUidNODate(uid);
			Integer nownum = financeDepositWithdrawDao.getIOSDepositNum(uid, date);
			if (historynum==nownum && nownum!=0) {
				androidNewAddUserDepoNumList.add(uid);
			}
		}
		return new BigDecimal(androidNewAddUserDepoNumList.size());
	}
	
	/**
	 * 获取某日Android新用户的充值金额
	 * @param date
	 * @return
	 */
	private BigDecimal getAndroidDepositAmount(String date) {
		BigDecimal depositAmount = BigDecimal.ZERO;
		for (String uid:androidNewAddUserDepoNumList) {
			BigDecimal iosDepositAmount = financeDepositWithdrawDao.getIOSDepositAmount(uid, date);
			depositAmount = depositAmount.add(iosDepositAmount);
		}
		return depositAmount;
	}
	
	
	
	/**
	 * 获取某日Android投资用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getAndroidInvestNum(String date) {
		androidNewInvestUserList = new ArrayList<String>();
		for (String uid : AndroidInvestUidList) {
			Integer gethistoryInvestNum = financeSubmitTenderDao.gethistoryInvestNum(uid);
			Integer nowNum = financeSubmitTenderDao.getIOSInvestNum(uid, date);
			if (gethistoryInvestNum==nowNum && nowNum!=0) {
				androidNewInvestUserList.add(uid);
			}
		}
		return new BigDecimal(androidNewInvestUserList.size());
	}
	
	/**
	 * 获取某日新增Android用户投资金额
	 * @return
	 */
	private BigDecimal getAndroidInvestAmount(String date) {
		BigDecimal investAmount = BigDecimal.ZERO;
		for (String uid:androidNewInvestUserList) {
			BigDecimal iosInvestAmount = financeSubmitTenderDao.getIOSInvestAmount(uid, date);
			investAmount = investAmount.add(iosInvestAmount);
		}
		return investAmount;
	}
	
	
	
	/**
	 * 获取Android累计投资用户数量
	 * @param date
	 * @return
	 */
	private BigDecimal getAndroidTotalInvestNum(String date, String date2) {
		//获取昨日安卓累计投资用户人数
		BigDecimal yesterdayAndroidTotalInvestNum = financeDailyDao.getYesterdayAndroidTotalInvestNum(date2);
		if(null==yesterdayAndroidTotalInvestNum) {
			yesterdayAndroidTotalInvestNum = BigDecimal.ZERO;
		}
		List<String> iosInvestList = new ArrayList<String>();
		for (String uid : AndroidInvestUidList) {
			//获取某日Android的用户数量
			String data = financeSubmitTenderDao.getIOSTotalInvestNum(uid, date);
			if (!StringUtils.isEmpty(data)) {
				iosInvestList.add(uid);
			}
		}
		return new BigDecimal(iosInvestList.size()).add(yesterdayAndroidTotalInvestNum);
	}
	
	/**
	 * 获取Android用户累计投资金额
	 * @return
	 */
	private BigDecimal getAndroidTotalInvestAmount(String date, String date2) {
		BigDecimal investAmount = BigDecimal.ZERO;
		//获取昨日Android累计投资金额
		BigDecimal yesterdayAndroidTotalInvestAmount = financeDailyDao.getYesterdayAndroidTotalInvestAmount(date2);
		logger.debug("=====>yesterdayAndroidTotalInvestAmount1"+yesterdayAndroidTotalInvestAmount);
		if(null==yesterdayAndroidTotalInvestAmount) {
			yesterdayAndroidTotalInvestAmount = BigDecimal.ZERO;
			logger.debug("=====>yesterdayAndroidTotalInvestAmount2"+yesterdayAndroidTotalInvestAmount);
		}
		for (String uid:AndroidInvestUidList) {
			BigDecimal iosInvestAmount = financeSubmitTenderDao.getIOSTotalInvestAmount(uid, date);
			investAmount = investAmount.add(iosInvestAmount);
		}
		return investAmount.add(yesterdayAndroidTotalInvestAmount);
	}
	
	
	
	/**
	 * 获取android充值的新用户和老用户
	 * @return
	 */
	private Map<String, Object> getAndroidNewAndOldDepositNum() {
		Map<String,Object> map = new HashMap<String, Object>();
		List<String> newDepositList = new ArrayList<String>();
		List<String> oldDepositList = new ArrayList<String>();
		for (String uid : allAndroidDepoUidList) {
			Integer iosNewAndOldDepositNum = financeDepositWithdrawDao.getIOSNewAndOldDepositNum(uid);
			if (iosNewAndOldDepositNum==1) {
				//新用户
				newDepositList.add(uid);
			} else if (iosNewAndOldDepositNum>1) {
				//老用户
				oldDepositList.add(uid);
			}
		}
		map.put("newDepositList", newDepositList);
		map.put("oldDepositList", oldDepositList);
		return map;
	}
	
	/**
	 * 获取某日Android新用户充值金额
	 * @param date
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BigDecimal getAndroidNewDepositAmount(String date) {
		Map<String, Object> map = getAndroidNewAndOldDepositNum();
		BigDecimal depositAmount = BigDecimal.ZERO;
		List<String> newDepositList = (List<String>)map.get("newDepositList");
		for (String uid : newDepositList) {
			BigDecimal iosDepositAmount = financeDepositWithdrawDao.getIOSDepositAmount(uid, date);
			depositAmount = depositAmount.add(iosDepositAmount);
		}
		return depositAmount;
	}
	
	/**
	 * 获取某日Android新充值用户数量
	 * @param date
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BigDecimal getAndroidNewDepositNum(String date) {
		Map<String, Object> map = getAndroidNewAndOldDepositNum();
		List<String> newDepositList = (List<String>)map.get("newDepositList");
		List<String> list = new ArrayList<String>();
		for (String uid : newDepositList) {
			Integer iosNewDepositNum = financeDepositWithdrawDao.getIOSNewDepositNum(uid, date);
			if (iosNewDepositNum==1) {
				list.add(uid);
			}
		}
		return new BigDecimal(list.size());
	}
	
	/**
	 * 获取IOS充值的新用户
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BigDecimal getAndroidNewDepositNum() {
		Map<String, Object> map = getAndroidNewAndOldDepositNum();
		List<String> newDepositList = (List<String>)map.get("newDepositList");
		return new BigDecimal(newDepositList.size());
	}
	
	/**
	 * Android新增注册到充值转化
	 * @param date
	 * @return
	 */
	private BigDecimal getAndroidNewRegToDepo(String date) {
		//iOS新增注册到充值转化 = 新增充值用户/当日注册用户
		BigDecimal iosNewRegToDepo = BigDecimal.ZERO;
		BigDecimal iosNewDepositNum = getAndroidNewDepositNum(); //新增充值用户
		BigDecimal iosRegUserNum = getAndroidRegUserNum(date); //当日注册用户
		if (iosRegUserNum.compareTo(BigDecimal.ZERO)!=0) {
			iosNewRegToDepo = iosNewDepositNum.divide(iosRegUserNum,2);
		} else if (iosRegUserNum.compareTo(BigDecimal.ZERO)==0) {
			iosNewRegToDepo = iosNewDepositNum.negate();
		}
		return iosNewRegToDepo;
	}

	@Override
	public void deleteByDaily(String date) {
		logger.debug("------ fianceDaily deleteByDaily"+date);
		date += " 00:00:00";
		financeDailyDao.deleteByDaily(date);
		logger.debug("fianceDaily deleteDaily success");
	}
	
	
}
