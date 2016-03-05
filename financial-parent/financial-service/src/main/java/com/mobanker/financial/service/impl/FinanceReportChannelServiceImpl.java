package com.mobanker.financial.service.impl;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.mobanker.financial.dao.FinanceDepositWithdrawDao;
import com.mobanker.financial.dao.FinanceInvestUserDao;
import com.mobanker.financial.dao.FinanceReportChannelDao;
import com.mobanker.financial.dao.FinanceSubmitTenderDao;
import com.mobanker.financial.dao2.YydUsersInfoDao;
import com.mobanker.financial.entity.FinanceReportChannel;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceReportChannelService;
import com.mobanker.financial.service.FinanceUidMappingService;
import com.mobanker.financial.vo.DownloadChannelUserCountVo;
import com.mobanker.framework.dto.ResponseEntity;
import com.mobanker.framework.service.impl.BaseServiceImpl;
import com.mobanker.framework.constant.Constants;

@Service
public class FinanceReportChannelServiceImpl extends BaseServiceImpl<FinanceReportChannel> implements FinanceReportChannelService {
	
	Logger logger = LoggerFactory.getLogger("channelJoblog");
	
	@Resource
	private FinanceUidMappingService financeUidMappingService;
	@Resource
	private YydUsersInfoDao yydUsersInfoDao;
	@Resource
	private FinanceReportChannelDao financeReportChannelDao;
	@Resource
	private FinanceDepositWithdrawDao financeDepositWithdrawDao;
	@Resource
	private FinanceSubmitTenderDao financeSubmitTenderDao;
	@Resource
	private FinanceCommonCfgService financeCommonCfgService;
	@Resource
	private FinanceInvestUserDao financeInvestUserDao;
	
	  
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Integer generatorChannel(List<String> dateList) {
		logger.debug("渠道日报数据统计{}",JSONObject.toJSONString(dateList));
		if(dateList == null || dateList.size() == 0){
			logger.error("-----> 生成日报传入的dateList为空");
			return 0;
		}
		
		logger.debug("delete channel date"+dateList.get(0));
		this.deleteByDaily(dateList.get(0));
		logger.debug("--------删除渠道数据成功"+dateList.get(0));
		
		Integer num = 0;
		for(String date : dateList){
			logger.debug("====="+date);
			generatorChannelDaily(date);
			num++;
		}
		logger.debug("----"+num);
		return num;
		
	}
	/**
	 * 获取每天不同渠道的数据
	 */
	private void generatorChannelDaily(String date) {
		logger.debug("========>>generatorChannelDaily"+date);
		ResponseEntity resp = new ResponseEntity();
		FinanceReportChannel entity = new FinanceReportChannel();
		List<String> firstDepoNumList = new ArrayList<String>();
		List<String> firstInvestNumList = new ArrayList<String>();
		List<String> totalInvestNumsList = new ArrayList<String>();
		BigDecimal newDepoAmount = BigDecimal.ZERO;
		BigDecimal newInvestAmount = BigDecimal.ZERO;
		BigDecimal totalInvestAmount = BigDecimal.ZERO;
		List<DownloadChannelUserCountVo> userNumList = queryDownloadChannelUserNum(date);
		List<DownloadChannelUserCountVo> activNumList = queryDownloadChannelActivNum(date);
		
		logger.debug("========>>userNumListSize:"+userNumList.size());
		logger.debug("=======>>activNumList:"+activNumList.size());
		logger.debug("=======intoFor");
		//循环注册用户量
		for (int i=0; i<userNumList.size(); i++) {
			//获取注册渠道
			DownloadChannelUserCountVo userNum = userNumList.get(i);  
			//循环激活用户量
			for (int j=0; j<activNumList.size(); j++) {
				//获取激活渠道
				DownloadChannelUserCountVo activNum = activNumList.get(j);
				//判断注册渠道和激活是否一致
				if (!StringUtils.isEmpty(userNum.getDownloadChannel()) && !StringUtils.isEmpty(activNum.getDownloadChannel()) && userNum.getDownloadChannel().equals(activNum.getDownloadChannel())) {
					logger.debug("============>>suchChannelDownload"+userNum.getDownloadChannel()+"======="+activNum.getDownloadChannel());
					//获取该渠道的理财数据  理财注册量
					List<DownloadChannelUserCountVo> queryFinanceDownloadChannelUserNum = queryFinanceDownloadChannelUserNum(date, userNum.getDownloadChannel());
					
					//累计获取某日所有投资用户
					Set<String> uidSet = financeSubmitTenderDao.getChannelTotalInvestNums(date);
					Iterator<String> it = uidSet.iterator();
					while (it.hasNext()) {
						String uid = it.next();
						String userId = financeUidMappingService.getYYDUid(uid);
						String channel = yydUsersInfoDao.getChannelByUid(userId);
						//获取相同渠道的所有投资用户
						if (!StringUtils.isEmpty(channel) && channel.equals(userNum.getDownloadChannel())) {
							totalInvestNumsList.add(uid);
						}
					}
					logger.debug("=======>>totalInvestNumsListSize:"+totalInvestNumsList.size());
					logger.debug("=================>>totalInvestNumsList:"+totalInvestNumsList);
					//累计获取相同渠道下面所有用户所有投资金额
					for (String uid : totalInvestNumsList) {
						BigDecimal investAmount = financeSubmitTenderDao.getChannelTotalInvestAmount(uid, date);
						totalInvestAmount = totalInvestAmount.add(investAmount);
					}
					
					//获取某日所有充值用户
					Set<String> depoSet = financeDepositWithdrawDao.getDataByDate(date);
					Iterator<String> it2 = depoSet.iterator();
					while (it2.hasNext()) {
						String uid = it2.next();
						String userId = financeUidMappingService.getYYDUid(uid);
						String channel = yydUsersInfoDao.getChannelByUid(userId);
						//获取相同渠道下面的用户
						if (!StringUtils.isEmpty(channel) && channel.equals(userNum.getDownloadChannel())) {
							//根据uid获取该用户的所有充值次数
							Integer historynum = financeDepositWithdrawDao.getRechargeDateByUidNODate(uid);
							//获取今天充值次数
							Integer newIosDeponum = financeDepositWithdrawDao.getIOSDepositNum(uid, date);
							if (newIosDeponum!=0 && historynum == newIosDeponum) {
								//新充值用户
								firstDepoNumList.add(uid);
							}
						}
					}
					
					//获取某日所有投资用户
					Set<String> investSet = financeSubmitTenderDao.getDataByDate(date);
					Iterator<String> it3 = investSet.iterator();
					while (it3.hasNext()) {
						String uid = it3.next();
						String userId = financeUidMappingService.getYYDUid(uid);
						String channel = yydUsersInfoDao.getChannelByUid(userId);
						//获取相同渠道下面的用户
						if (!StringUtils.isEmpty(channel) && channel.equals(userNum.getDownloadChannel())) {
							//根据uid获取所有投资次数
							Integer gethistoryInvestNum = financeSubmitTenderDao.gethistoryInvestNum(uid);
							//获取某日投资次数
							Integer count = financeSubmitTenderDao.getUserInvestNum(uid, date);
							if (count!=0 && gethistoryInvestNum==count) {
								//新投资用户
								firstInvestNumList.add(uid);
							}
						}
					}
					
					for (String uid : firstDepoNumList) {
						//获取某日用户充值金额
						BigDecimal userRechargeAmount = financeDepositWithdrawDao.getUserRechargeAmount(uid, date);
						newDepoAmount = newDepoAmount.add(userRechargeAmount);
					}
					
					for (String uid : firstInvestNumList) {
						//获取某日新用户投资金额
						BigDecimal userAddTotalAmount = financeSubmitTenderDao.getUserAddTotalAmount(uid, date);
						newInvestAmount = newInvestAmount.add(userAddTotalAmount);
					}
					
					BigDecimal firstDepoNum = new BigDecimal(firstDepoNumList.size());
					BigDecimal firstInvestNum = new BigDecimal(firstInvestNumList.size());
					BigDecimal regNum = new BigDecimal(userNum.getCounts());
					BigDecimal actNum = new BigDecimal(activNum.getCounts());
					BigDecimal financeRegNum = new BigDecimal(queryFinanceDownloadChannelUserNum.size());
					BigDecimal totalInvestNum = new BigDecimal(totalInvestNumsList.size());
					
					logger.debug("============>>setChannelEntity");
					entity.setId(null);
					entity.setChannelDate(DateUtils.convert(date));   //插入渠道时间
					entity.setChannel(userNum.getDownloadChannel());  //渠道
					entity.setRegNum(regNum);  //所有注册量
					entity.setFinanceRegNum(financeRegNum);  //理财注册量
					entity.setActiNum(actNum); //激活量
					entity.setRegActiRatio(regNum.divide(actNum, 2)); //注册激活比
					entity.setFirstDepoNum(firstDepoNum); //首次充值用户
					if (regNum.compareTo(BigDecimal.ZERO)!=0) {
						entity.setRegDepoTransform(firstDepoNum.divide(regNum,2));  //注册到充值转化
						entity.setRegInvestTransform(firstInvestNum.divide(regNum,2));//注册到投资转化
					} else {
						entity.setRegDepoTransform(firstDepoNum.negate());//注册到充值转化
						entity.setRegInvestTransform(firstInvestNum.negate());//注册到投资转化
					}
					entity.setNewDepoAmount(newDepoAmount); //新户充值额
					entity.setFirstInvestNum(firstInvestNum);  //新投资用户
					entity.setNewInvestAmount(newInvestAmount); //新用户投资额
					if (firstInvestNum.compareTo(BigDecimal.ZERO)!=0) {
						entity.setNewInvestArpu(newInvestAmount.divide(firstInvestNum,2)); //新户投资arpu
					} else {
						entity.setNewInvestArpu(newInvestAmount.negate()); //新户投资arpu
					}
					entity.setTotalInvestNum(totalInvestNum); //累计所有投资用户
					entity.setTotalInvestAmount(totalInvestAmount); //累计投资金额
					if (totalInvestNum.compareTo(BigDecimal.ZERO)!=0) {
						entity.setTotalInvestArpu(totalInvestAmount.divide(totalInvestNum,2)); //投资arpu
					} else {
						entity.setTotalInvestArpu(totalInvestAmount.negate()); //投资arpu
					}
					entity.setAppVersion(userNum.getVersion());  //版本号
					
					logger.debug("=======>>insertChannelEntity------"+entity);
					try {
						int insert = financeReportChannelDao.insert(entity);
						
						//清空Temp变量
						firstDepoNumList.clear();
						firstInvestNumList.clear();
						totalInvestNumsList.clear();
						newDepoAmount = BigDecimal.ZERO;
						newInvestAmount = BigDecimal.ZERO;
						totalInvestAmount = BigDecimal.ZERO;
						
						if (1==insert) {
							logger.debug("======>>channelInsertSuccess---");
							resp.setStatus(Constants.System.OK);
							resp.setMsg("添加成功");
							firstDepoNumList.clear();
						} else {
							logger.debug("======>>channel insert fail----");
							resp.setStatus(Constants.System.FAIL);
							resp.setError("添加失败");
							resp.setMsg("数据添加失败");
						}
					} catch (Exception e) {
						resp.setStatus(Constants.System.FAIL);
						resp.setError("添加失败");
						resp.setMsg("数据添加失败");
						logger.debug("======>>><<<"+e.getMessage(),e);
					}
					
				}
			}
		}
	}

	
	/**
	 * 查询某日每个渠道的所有注册用户数量
	 * @param date
	 */
	private List<DownloadChannelUserCountVo> queryDownloadChannelUserNum(String date) {
		logger.debug("========>>queryDownloadChannelUserNum"+date);
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<DownloadChannelUserCountVo> list = new ArrayList<DownloadChannelUserCountVo>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		try {
			List<DownloadChannelUserCountVo> queryDownloadChannelUserNumList = yydUsersInfoDao.queryDownloadChannelUserNum(map);
			for (DownloadChannelUserCountVo vo : queryDownloadChannelUserNumList) {
				String register_version = vo.getVersion();
				if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
					register_version += ".0";
				}
				if (!StringUtils.isEmpty(register_version)) {
					String replaceVersion = register_version.replace(".", "");
					if (!StringUtils.isEmpty(replaceVersion)) {
						int regVersion = Integer.parseInt(replaceVersion);
						if (regVersion >= appVersion){
							list.add(vo);
						}
					}
				}
			}
			logger.debug("======>>queryDownloadChannelUserNumResultSize"+list.size());
			logger.debug("======>>queryDownloadChannelUserNumResult"+list);
			return list;
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			logger.debug("======>>queryDownloadChannelUserNumERROR");
		}
		return null;
	}
	
	
	
	/**
	 * 查询某日每个渠道的所有理财注册用户数量
	 * @param date
	 */
	//TODO
	private List<DownloadChannelUserCountVo> queryFinanceDownloadChannelUserNum(String date, String download_channel) {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<DownloadChannelUserCountVo> list = new ArrayList<DownloadChannelUserCountVo>();
		//获取要查询激活的版本号
		String version = financeCommonCfgService.getCommonCfgValueByCode("APP_VERSION");
		String rVersion = version.replace(".", "");
		int appVersion = Integer.parseInt(rVersion);
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		map.put("download_channel", download_channel);
		List<DownloadChannelUserCountVo> queryDownloadChannelUserNumList = yydUsersInfoDao.queryFinanceDownloadChannelUserNum(map);
		for (DownloadChannelUserCountVo vo : queryDownloadChannelUserNumList) {
			String register_version = vo.getVersion();
			if (!StringUtils.isEmpty(register_version) && register_version.length()<5) {
				register_version += ".0";
			}
			if (!StringUtils.isEmpty(register_version)) {
				String replaceVersion = register_version.replace(".", "");
				if (!StringUtils.isEmpty(replaceVersion)) {
					int regVersion = Integer.parseInt(replaceVersion);
					if (regVersion >= appVersion){
						list.add(vo);
					}
				}
			}
		}
		return list;
	}
	
	
	
	/**
	 * 查询某日每个渠道的激活用户数量
	 * @return
	 */
	//TODO
	private List<DownloadChannelUserCountVo> queryDownloadChannelActivNum(String date) {
		logger.debug("=====>>intoQueryDownloadChannelActivNum"+date);
		Map<String, Object> map = new HashMap<String, Object>();
		String beginDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		map.put("beginTime", beginDate);
		map.put("endTime", endDate);
		try {
			List<DownloadChannelUserCountVo> dDownloadChannelActivList = yydUsersInfoDao.queryDownloadChannelActivNum(map);
			logger.debug("======>>queryDownloadChannelActivNumSize2"+dDownloadChannelActivList.size());
			logger.debug("======>>queryDownloadChannelActivNumResult2"+dDownloadChannelActivList);
			return dDownloadChannelActivList;
		} catch (Exception e) {
			logger.debug("======>>queryDownloadChannelActivNumHasException");
			logger.debug("======>>queryDownloadChannelActivNumERROR"+e.getMessage(),e);
		}
		return null;
	}
	
	
	/**
	 * 获取渠道数据
	 */
	@Override
	public ResponseEntity getDateByParams(String beginTime, String endTime,
			String version, String channel, String sortField, String sortType) {
		logger.debug("------params:"+beginTime+"--"+endTime+"--"+version+"--"+channel);
		ResponseEntity resp = new ResponseEntity();
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("beginTime", beginTime);
		map.put("endTime", endTime);
		map.put("version", version);
		map.put("channel", channel);
		map.put("sortField", sortField);
		map.put("sortType", sortType);
		try {
			List<FinanceReportChannel> dateByParams = financeReportChannelDao.getDateByParams(map);
			resp.setData(dateByParams);
			resp.setStatus(Constants.System.OK);
			resp.setMsg("渠道查询成功");
		} catch (Exception e) {
			logger.debug(e.getMessage());
			resp.setStatus(Constants.System.FAIL);
			resp.setMsg("查询数据库出错");
			resp.setError("渠道查询失败");
		}
		return resp;
	}
	
	/**
	 * 根据日期删除渠道数据
	 */
	@Override
	public void deleteByDaily(String date) {
		logger.debug("----into channel deleteDaily"+date);
		date += " 00:00:00";
		financeReportChannelDao.deleteByDaily(date);
		logger.debug("channel deleteDaily success");
	}
	
	/**
	 * 根据渠道 查询数据
	 */
	@Override
	public ResponseEntity getDataByChannel(String channel, String beginTime, String endTime, String sortField, String sortType) {
		logger.debug("=====>> queryDataByChannel>>"+channel);
		ResponseEntity resp = new ResponseEntity();
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("channel", channel);
			map.put("beginTime", beginTime);
			map.put("endTime", endTime);
			map.put("sortField", sortField);
			map.put("sortType", sortType);
			List<FinanceReportChannel> list = financeReportChannelDao.getDataByChannel(map);
			logger.debug("queryDataByChannelResult>>"+list);
			logger.debug("queryDataByChannelResult size>>"+list.size());
			resp.setData(list);
			resp.setStatus(Constants.System.OK);
			resp.setMsg("根据渠道查询数据成功");
		} catch (Exception e) {
			logger.debug(e.getMessage());
			resp.setStatus(Constants.System.FAIL);
			resp.setMsg("根据渠道查询数据失败");
		}
		
		return resp;
	}
}
