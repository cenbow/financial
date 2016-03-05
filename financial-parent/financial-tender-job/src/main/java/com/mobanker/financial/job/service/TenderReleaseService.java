package com.mobanker.financial.job.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobanker.financial.common.enums.TenderStatus;
import com.mobanker.financial.common.enums.TenderSyncStatus;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.service.FinanceTenderCfgService;


/**
 * Description: 定时发布标的
 * Detail:半点、整点发布标的
 * 
 * @author yinyafei
 * @date 2015/7/14
 */
@Service
public class TenderReleaseService {

	private static final Logger logger = LoggerFactory.getLogger(TenderReleaseService.class);
	private final String logPrefix = "[标的发布]------";

	@Resource
	private FinanceTenderCfgService financeTenderCfgService;

	/**
	 * 看这里、看这里： 主入口
	 */
	public List<FinanceTenderCfg> tenderRelease() {

		// 根据发布时间获取待发布列表
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		FinanceTenderCfg findParams = new FinanceTenderCfg();
		findParams.setInviteTime(cal.getTime());
		findParams.setStatus(TenderStatus.UNTENDER.toString());
		List<FinanceTenderCfg> tenderList = financeTenderCfgService.getByObj(findParams);
		logger.debug("{}发布时间:{},需发布个数:{}", logPrefix, cal.getTime(), tenderList.size());

		for (FinanceTenderCfg tenderCfg : tenderList) {
			tenderReleaseOne(tenderCfg);
		}
		
		return tenderList;
	}

	/**
	 * 根据标的id发布标的
	 * 
	 * @param tenderId
	 */
	public FinanceTenderCfg tenderReleaseById(String tenderId) {

		FinanceTenderCfg tenderCfg = financeTenderCfgService.getFinanceTenderCfg(tenderId);
		if (tenderCfg != null) {
			tenderReleaseOne(tenderCfg);
		}
		return tenderCfg;
	}
	
	/**
	 * 发布标的,将标的状态更改为2
	 * 
	 * @param tenderId
	 */
	public void tenderReleaseOne(FinanceTenderCfg tenderCfg) {

		logger.debug("{}标的内容:{} {}", logPrefix, tenderCfg.getId(), tenderCfg.getTenderName());

		tenderCfg.setStatus(TenderStatus.TENDERING.toString());
		tenderCfg.setSyncStatus("1");
		financeTenderCfgService.update(tenderCfg);
	}
	
	/**
	 * 标的同步成功通知
	 * @param map
	 */
	public void receive(HashMap<String, String> map) {
		String tenderId = map.get("id");
		if (!StringUtils.isEmpty(tenderId)) {
			FinanceTenderCfg tenderCfg = financeTenderCfgService.getById(tenderId);
			if (tenderCfg != null) {
				tenderCfg.setSyncStatus(TenderSyncStatus.SUCCESS.toString());
				financeTenderCfgService.update(tenderCfg);
			} else {
				logger.error("{}标的同步失败,标的不存在 :{}", logPrefix, tenderId);
			}
		} else {
			logger.error("{}标的同步失败 :{}", logPrefix, "通知结果不正确!");
		}
	}
}
