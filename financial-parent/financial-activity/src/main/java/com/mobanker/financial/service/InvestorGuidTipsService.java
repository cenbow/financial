package com.mobanker.financial.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceBankCard;
import com.mobanker.financial.entity.FinanceDepositWithdraw;
import com.mobanker.financial.entity.FinanceIncome;
import com.mobanker.financial.entity.FinanceMessage;
import com.mobanker.financial.entity.FinanceSubmitTender;

/**
 * 用户引导消息发送
 * 
 * @author yinyafei
 *
 */
@Service
public class InvestorGuidTipsService {

	@Resource
	private FinanceBankCardService financeBankCardService;
	@Resource
	private FinanceDepositWithdrawService financeDepositWithdrawService;
	@Resource
	private FinanceMessageService financeMessageService;
	@Resource
	private FinanceSubmitTenderService financeSubmitTenderService;
	@Resource
	private FinanceIncomeService financeIncomeService;

	/**
	 * 昨日绑卡未充值
	 */
	public void sendBindNoRechargeUserMsg() {

		List<FinanceMessage> messageList = new ArrayList<FinanceMessage>();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date date = DateUtils.getStartDatetime(cal.getTime());
		List<FinanceBankCard> bankCardList = financeBankCardService.getBankCardByDate(date);
		for (FinanceBankCard bankCard : bankCardList) {

			String uid = bankCard.getUid();
			FinanceDepositWithdraw findParams = new FinanceDepositWithdraw();
			findParams.setUid(uid);
			findParams.setRecordType("deposit");
			findParams.setStatus("SUCCESS");
			List<FinanceDepositWithdraw> depositList = financeDepositWithdrawService.getByObj(findParams);
			if (depositList.size() == 0) {

				FinanceMessage message = new FinanceMessage();
				message.setMessageType("all");
				message.setNid("lc_guide_yesterday_binding_not_deposit");
				message.setUid(uid);
				message.setSendStatus("0");
				message.setRealSendTime(new Date());
				message.setSettingSendTime(new Date());
				messageList.add(message);
			}
		}

		if (messageList.size() > 0) {
			financeMessageService.insertBatch(messageList);
		}
	}

	/**
	 * 历史绑卡未充值
	 */
	public void sendBindNoRechargeHistoryUserMsg() {

		List<FinanceMessage> messageList = new ArrayList<FinanceMessage>();

		FinanceBankCard findParams = new FinanceBankCard();
		findParams.setIsBinding("1");
		List<FinanceBankCard> bankCardList = financeBankCardService.getByObj(findParams);
		for (FinanceBankCard bankCard : bankCardList) {

			String uid = bankCard.getUid();
			FinanceMessage messageParams = new FinanceMessage();
			messageParams.setUid(uid);
			messageParams.setNid("lc_guide_binding_not_deposit");
			if (financeMessageService.getByObj(messageParams).size() == 2) {
				continue;
			}

			FinanceDepositWithdraw depositRecord = financeDepositWithdrawService.getLatestDepostRecord(uid);
			if (depositRecord == null) {

				long bindTime = bankCard.getCreateTime().getTime();
				long nowTime = new Date().getTime();
				if ((nowTime - bindTime) / 1000 > 60 * 60 * 24) {
					FinanceMessage message = new FinanceMessage();
					message.setMessageType("all");
					message.setNid("lc_guide_binding_not_deposit");
					message.setUid(uid);
					message.setSendStatus("0");
					message.setRealSendTime(new Date());
					message.setSettingSendTime(new Date());
					messageList.add(message);
				}
			}

			if (messageList.size() > 0 && messageList.size() % 1000 == 0) {
				financeMessageService.insertBatch(messageList);
				messageList.clear();
			}
		}
		if (messageList.size() > 0) {
			financeMessageService.insertBatch(messageList);
		}
	}

	/**
	 * 昨天充值未投标
	 */
	public void sendRechargeNoSubmitUserMsg() {

		List<FinanceMessage> messageList = new ArrayList<FinanceMessage>();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date date = DateUtils.getStartDatetime(cal.getTime());
		List<String> uidList = financeDepositWithdrawService.getDepositRecordByDate(date);
		for (String uid : uidList) {
			FinanceSubmitTender findParams = new FinanceSubmitTender();
			findParams.setUid(uid);
			List<FinanceSubmitTender> submitTenderList = financeSubmitTenderService.getByObj(findParams);
			if (submitTenderList.size() == 0) {

				FinanceMessage message = new FinanceMessage();
				message.setMessageType("all");
				message.setNid("lc_guide_yesterday_deposit_not_bid");
				message.setUid(uid);
				message.setSendStatus("0");
				message.setRealSendTime(new Date());
				message.setSettingSendTime(new Date());

				FinanceIncome income = financeIncomeService.getFinanceIncomeByUid(uid);
				if (income != null) {
					String amount = income.getSavingpotAvailable().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
					message.setReplaceContent(amount);
				}

				messageList.add(message);
			}
		}
		if (messageList.size() > 0) {
			financeMessageService.insertBatch(messageList);
		}
	}

	/**
	 * 历史充值未投标
	 */
	public void sendRechargeNoSubmitHistoryUserMsg() {

		List<FinanceMessage> messageList = new ArrayList<FinanceMessage>();

		List<String> uidList = financeDepositWithdrawService.getDepositRecordByDate(null);
		for (String uid : uidList) {

			FinanceMessage messageParams = new FinanceMessage();
			messageParams.setUid(uid);
			messageParams.setNid("lc_guide_deposit_not_bid");
			if (financeMessageService.getByObj(messageParams).size() == 2) {
				continue;
			}

			FinanceDepositWithdraw depositRecord = financeDepositWithdrawService.getLatestDepostRecord(uid);
			if (depositRecord != null) {

				long depositTime = depositRecord.getCreateTime().getTime();
				long nowTime = new Date().getTime();
				if ((nowTime - depositTime) / 1000 < 60 * 60 * 24) {
					continue;
				}
			}

			FinanceSubmitTender findParams = new FinanceSubmitTender();
			findParams.setUid(uid);
			List<FinanceSubmitTender> submitTenderList = financeSubmitTenderService.getByObj(findParams);
			if (submitTenderList.size() == 0) {

				FinanceMessage message = new FinanceMessage();
				message.setMessageType("all");
				message.setNid("lc_guide_deposit_not_bid");
				message.setUid(uid);
				message.setSendStatus("0");
				message.setRealSendTime(new Date());
				message.setSettingSendTime(new Date());
				messageList.add(message);
			}
			
			if (messageList.size() > 0 && messageList.size() % 1000 == 0) {
				financeMessageService.insertBatch(messageList);
				messageList.clear();
			}
		}
		if (messageList.size() > 0) {
			financeMessageService.insertBatch(messageList);
		}
	}

	/**
	 * 在投中的用户
	 * 
	 * @param nid
	 */
	public void sendSubmitedUserMsg(String nid) {

		List<FinanceMessage> messageList = new ArrayList<FinanceMessage>();

		List<FinanceIncome> incomeList = financeIncomeService.getEarnsGreaterThanZeroIncome();
		for (FinanceIncome income : incomeList) {

			FinanceMessage message = new FinanceMessage();
			message.setMessageType("message");
			message.setNid(nid);
			message.setUid(income.getUid());
			message.setSendStatus("0");
			message.setRealSendTime(new Date());
			message.setSettingSendTime(new Date());

			message.setReplaceContent(income.getAccumulatedIncome().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			messageList.add(message);

			if (messageList.size() > 0 && messageList.size() % 10 == 0) {
				financeMessageService.insertBatch(messageList);
				messageList.clear();
			}
		}
		if (messageList.size() > 0) {
			financeMessageService.insertBatch(messageList);
		}
	}
}
