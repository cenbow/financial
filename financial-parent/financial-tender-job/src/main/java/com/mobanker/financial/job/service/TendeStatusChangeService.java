package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.common.enums.TenderStatus;
import com.mobanker.financial.common.message.MessageCenter;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceCouponService;
import com.mobanker.financial.service.FinanceIncomeService;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTenderCfgService;
import com.mobanker.financial.service.FinanceUidMappingService;


@Service
public class TendeStatusChangeService {

	@Resource
	private FinanceTenderCfgService financeTenderCfgService;
	@Resource
	private FinanceCouponService financeCouponService;
	@Resource
	private FinanceSubmitTenderService financeSubmitTenderService;
	@Resource
	private FinanceIncomeService financeIncomeService;
	@Resource
	private FinanceInvestUserService financeInvestUserService;
	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private MessageCenter messageCenter;

	private final static Logger logger = LoggerFactory.getLogger(TendeStatusChangeService.class);
	private final String logPrefix = "[收益、结束状态修改]------";

	/**
	 * 更改标的状态，开始收益
	 */
	public void tenderStatusChange() {

		// 定时任务0点以后跑，去更改昨日的标的状态
		String dateStr = DateUtils.convert(new Date(), DateUtils.DATE_FORMAT).concat(" 00:00:00");
		Date date = DateUtils.convert(dateStr);
		// 待收益的标的
		FinanceTenderCfg findParams = new FinanceTenderCfg();
		findParams.setBeginTime(date);
		List<FinanceTenderCfg> tenderList = financeTenderCfgService.getByObj(findParams);
		logger.debug("{}标的开始收益个数:{}", logPrefix, tenderList.size());
		for (FinanceTenderCfg tenderCfg : tenderList) {

			// 如果标的没有人投，不再收益，直接改为结束
			if (tenderCfg.getInputAmount().compareTo(BigDecimal.ZERO) == 0) {
				logger.debug("{}无人投标，标的结束:{}", logPrefix, tenderCfg.getTenderName());
				tenderCfg.setStatus(TenderStatus.TENDER_END.toString());
			} else {
				logger.debug("{}标的开始收益:{}", logPrefix, tenderCfg.getTenderName());
				tenderCfg.setStatus(TenderStatus.TENDER_INCOMING.toString());
			}

			tenderCfg.setUpdateTime(new Date());
			financeTenderCfgService.update(tenderCfg);
		}

		// 待结束的标的
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		dateStr = DateUtils.convert(cal.getTime(), DateUtils.DATE_FORMAT).concat(" 23:59:59");
		date = DateUtils.convert(dateStr);
		findParams = new FinanceTenderCfg();
		findParams.setEndTime(date);
		List<FinanceTenderCfg> tenderFinishList = financeTenderCfgService.getByObj(findParams);
		logger.debug("{}标的结束个数:{}", logPrefix, tenderFinishList.size());
		for (FinanceTenderCfg tenderCfg : tenderFinishList) {
			logger.debug("{}标的结束:{}", logPrefix, tenderCfg.getTenderNo());

			tenderCfg.setStatus(TenderStatus.TENDER_END.toString());
			tenderCfg.setUpdateTime(new Date());
			financeTenderCfgService.update(tenderCfg);

			// 根据标的ID获取UID
			FinanceSubmitTender submitParams = new FinanceSubmitTender();
			submitParams.setSid(tenderCfg.getId());
			List<FinanceSubmitTender> entityList = financeSubmitTenderService.getByObj(submitParams);
			for (FinanceSubmitTender ft : entityList) {

				boolean isVip = financeInvestUserService.isVipInvestor(ft.getUid());
				if (!isVip) {
					logger.debug("{}发送标的到期消息uid:{}", logPrefix, ft.getUid());
					// sendMsg(ft);  // 根据财务还款清空发送消息
				}
			}
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param tenderCfg
	 * @param submitTender
	 * @param uid
	 */
	/*private void sendMsg(FinanceSubmitTender submitTender) {
		// 理财到期短信
		Map<String, Object> replaceParam = new HashMap<String, Object>();
		replaceParam.put("#amount#", submitTender.getAmount().add(submitTender.getExpectIncome()).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		String messageUrl = commonCfgService.getRequestUrl(RequestUrl.MESSAGE_URL, SendMessageCode.SEND_REMIND);
		String uid = uidMappingService.getYYDUid(submitTender.getUid());
		messageCenter.sendRemind(messageUrl, TepmlateNid.TENDER_EXPIRED_SMS, uid, replaceParam);
	}*/

	/**
	 * 单个标的状态修改(手工方式)
	 * 
	 * @param tenderId
	 */
	public void tenderStatusChangeById(String tenderId) {

		FinanceTenderCfg tenderCfg = financeTenderCfgService.getById(tenderId);
		if (tenderCfg != null) {

			// 如果标的没有人投，不再收益，直接改为结束
			if (tenderCfg.getInputAmount().compareTo(BigDecimal.ZERO) == 0) {
				logger.debug("{}无人投标，标的结束:{}", logPrefix, tenderCfg.getTenderName());
				tenderCfg.setStatus(TenderStatus.TENDER_END.toString());
			} else {
				logger.debug("{}标的开始收益:{}", logPrefix, tenderCfg.getTenderName());
				tenderCfg.setStatus(TenderStatus.TENDER_INCOMING.toString());
			}

			tenderCfg.setUpdateTime(new Date());
			financeTenderCfgService.update(tenderCfg);
		}
	}
}
