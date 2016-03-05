package com.mobanker.financial.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.activemq.command.ActiveMQQueue;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.common.constants.MessageContants.SendMessageCode;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.common.message.MessageCenter;
import com.mobanker.financial.entity.ActivityConf;
import com.mobanker.financial.entity.ActivityIssue;
import com.mobanker.financial.entity.ActivityReward;
import com.mobanker.financial.entity.FinanceBankCard;
import com.mobanker.financial.entity.FinanceInvestUser;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.mq.ActiveMQProducer;

/**
 * Description: 理财活动奖励
 * 
 * Detail: 双赢理财1+1,被邀请人投标返现20% 双赢理财1+1,邀请人（内部员工和投资大于1W的普通用户）现金奖励 千分之五
 * 双赢理财1+1,邀请人（投资小于1W的普通用户）现金奖励千分之三
 * 
 * @author yinyafei
 * @date 2015.12.22
 */
@Service
@Aspect
public class ActivityRewardMoneyService {

	private static final Logger logger = LoggerFactory.getLogger(ActivityRewardMoneyService.class);
	private final String logPrefix = "[理财活动三奖励]------";
	
	@Resource
	private FinanceSubmitTenderService submitTenderService;
	@Resource
	private FinanceInvestUserService investUserService;
	@Resource
	private ActivityIssueService activityIssueService;
	@Resource
	private ActivityRewardService activityRewardService;
	@Resource
	private ActivityConfService activityConfService;
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private FinanceBankCardService bankCardService;
	@Resource
	private ActiveMQProducer activeMQProducer;
	@Resource
	private ActiveMQQueue rewarMoneyQueue;
	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private MessageCenter messageCenter;

	@Pointcut("execution(* com.mobanker.financial.job.service.TenderCompletionService.tenderComplete(..)) || execution(* com.mobanker.financial.job.service.TenderCompletionService.tenderCompteteById(..))")
	public void tenderComplete() {

	}

	@AfterReturning(value = "tenderComplete()", returning = "tenderList")
	public void rewardInviterMoney(List<FinanceTenderCfg> tenderList) {

		logger.debug("{}rewardInviterMoney开始处理活动奖励.....", logPrefix);

		for (FinanceTenderCfg tenderCfg : tenderList) {

			FinanceSubmitTender findParams = new FinanceSubmitTender();
			findParams.setSid(tenderCfg.getId());
			List<FinanceSubmitTender> submitTenderList = submitTenderService.getByObj(findParams);
			for (FinanceSubmitTender submitTender : submitTenderList) {

				String uid = submitTender.getUid();
				boolean isVip = investUserService.isVipInvestor(uid);
				if (isVip) {
					continue;
				}

				// 判断投资用户是否是属于被邀请过来的
				FinanceInvestUser newInvestor = investUserService.getInvestUserByUid(uid);
				if (newInvestor != null) {
					ActivityIssue issueParams = new ActivityIssue();
					issueParams.setActivity("L-003-201512001");
					issueParams.setStatus("0");
					issueParams.setTakerPhone(newInvestor.getPhone());
					issueParams.setElementEarningsAmount(BigDecimal.ZERO);
					issueParams.setElementInvestAmount(BigDecimal.ZERO);
					List<ActivityIssue> issueList = activityIssueService.getByObj(issueParams);
					for (ActivityIssue iterIssue : issueList) {

						String inviterId = iterIssue.getInviterId();
						String rewardId = iterIssue.getRewardId();
						ActivityReward reward = activityRewardService.getById(rewardId);
						
						if (reward != null) {

							BigDecimal rate = new BigDecimal(reward.getValue());
							BigDecimal submitAmount = submitTender.getAmount();

							// 生成邀请人记录
							if (reward.getType().equals("money") && reward.getSendObject().equals("inviter")) {
								ActivityIssue issue = new ActivityIssue();
								issue.setTakerId(inviterId);
								issue.setInviterPhone(newInvestor.getPhone());
								issue.setElementInvestAmount(submitAmount);
								BigDecimal rewardAmount = submitAmount.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
								issue.setElementEarningsAmount(rewardAmount);
								issue.setRewardId(reward.getId());
								issue.setActivity(reward.getActivity());
								issue.setSerialNum(BillNoUtils.GenerateBillNo());
								issue.setReason("L-003-201512001活动现金奖励");
								issue.setRuleId("1");
								issue.setStatus("2"); // 待处理
								issue.setCreateUser("admin");
								issue.setUpdateUser("admin");
								activityIssueService.insert(issue);
								
								rewardInvestorMoney(issue);
							}

							// 生成被邀请人记录
							if (reward.getType().equals("money") && reward.getSendObject().equals("taker")) {
								ActivityIssue issue = new ActivityIssue();
								issue = new ActivityIssue();
								issue.setTakerId(uid);
								issue.setTakerPhone(newInvestor.getPhone());
								issue.setInviterId(inviterId);
								issue.setElementInvestAmount(submitAmount);
								BigDecimal rewardAmount = submitTender.getExpectIncome().multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
								issue.setElementEarningsAmount(rewardAmount);
								issue.setRewardId(reward.getId());
								issue.setActivity(reward.getActivity());
								issue.setSerialNum(BillNoUtils.GenerateBillNo());
								issue.setReason("L-003-201512001活动现金奖励");
								issue.setRuleId("2");
								issue.setStatus("2"); // 待处理
								issue.setCreateUser("admin");
								issue.setUpdateUser("admin");
								activityIssueService.insert(issue);

								String messageUrl = commonCfgService.getRequestUrl(RequestUrl.MESSAGE_URL, SendMessageCode.SEND_REMIND);
								Map<String, Object> replaceParam = new HashMap<String, Object>();
								replaceParam.put("#amount#", rewardAmount);
								messageCenter.sendRemind(messageUrl, "lc_act_12001_take_tender_success", uidMappingService.getYYDUid(issue.getTakerId()), replaceParam);
								
								rewardInvestorMoney(issue);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 新用户投资奖励
	 */
	public void rewardInvestorMoney(ActivityIssue issue) {

		BigDecimal rewardAmount = issue.getElementEarningsAmount();
		FinanceInvestUser investor = investUserService.getInvestUserByUid(issue.getTakerId());
		ActivityReward reward = activityRewardService.getById(issue.getRewardId());

		if (investor != null && reward != null) {
			if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
				Map<String, String> messageMap = new HashMap<String, String>();
				messageMap.put("customerName", investor.getName());
				messageMap.put("phone", investor.getPhone());
				messageMap.put("userId", uidMappingService.getYYDUid(investor.getUid()));
				// 活动名称

				ActivityConf confParams = new ActivityConf();
				confParams.setCode(reward.getActivity());
				List<ActivityConf> confList = activityConfService.getByObj(confParams);
				if (confList.size() > 0) {
					messageMap.put("activityId", confList.get(0).getCode());
					messageMap.put("activityName", confList.get(0).getName());
				}

				messageMap.put("amount", rewardAmount.toString());
				messageMap.put("activityNid", issue.getSerialNum());
				messageMap.put("productType", "shoujidai");

				FinanceBankCard bankCard = bankCardService.getBankCardByUid(investor.getUid());
				if (bankCard != null) {
					messageMap.put("debitcardNum", bankCard.getBankCard());
					messageMap.put("bankName", bankCard.getBankName());
				} else {
					return; // 如果没有绑定银行卡直接跳过
				}
				messageMap.put("productChannel", "app");

				activeMQProducer.sendMapMsg(messageMap, rewarMoneyQueue);
			}
		}
	}

	/**
	 * 放款成功通知
	 * 
	 * @param message
	 */
	public void receive(HashMap<String, String> map) {

		logger.debug("{}rewardInviterMoney收款放款成功通知{}", logPrefix, map);

		String activityNid = map.get("activityNid");
		if (activityNid != null) {
			ActivityIssue findParams = new ActivityIssue();
			findParams.setSerialNum(activityNid);
			List<ActivityIssue> issueList = activityIssueService.getByObj(findParams);
			if (issueList.size() > 0) {
				ActivityIssue issue = issueList.get(0);
				issue.setStatus("1"); // 发放成功
				issue.setIssueTime(DateUtils.convert(map.get("activitySuccessTime")));
				issue.setUpdateTime(new Date());
				activityIssueService.update(issue);

				// 打款成功消息中心
				String messageUrl = commonCfgService.getRequestUrl(RequestUrl.MESSAGE_URL, SendMessageCode.SEND_REMIND);
				Map<String, Object> replaceParam = new HashMap<String, Object>();
				replaceParam.put("#amount#", map.get("amount"));
				if (issue.getRuleId().equals("1")) {
					messageCenter.sendRemind(messageUrl, "lc_act_12001_invite_lending", uidMappingService.getYYDUid(issue.getTakerId()), replaceParam);
				} else if (issue.getRuleId().equals("2")) {
					messageCenter.sendRemind(messageUrl, "lc_act_12001_take_lending", uidMappingService.getYYDUid(issue.getTakerId()), replaceParam);
				}
			}
		}
	}
}
