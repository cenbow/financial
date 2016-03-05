package com.mobanker.financial.web.v1_0_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobanker.financial.dao.FinanceDailyDao;
import com.mobanker.financial.dao2.YydUsersInfoDao;
import com.mobanker.financial.job.service.BatchPayToCardTradeService;
import com.mobanker.financial.job.service.IncomeCalculationService;
import com.mobanker.financial.job.service.RepairBatchPayToCardService;
import com.mobanker.financial.job.service.RepairFailedTradeService;
import com.mobanker.financial.job.service.RepaySwitchService;
import com.mobanker.financial.job.service.RepaymentService;
import com.mobanker.financial.job.service.ReportCollectionDetailService;
import com.mobanker.financial.job.service.ReportPaymentDetailService;
import com.mobanker.financial.job.service.ReportRefoundStatisticsServcie;
import com.mobanker.financial.job.service.TendeStatusChangeService;
import com.mobanker.financial.job.service.TenderCompletionService;
import com.mobanker.financial.job.service.TenderReleaseService;
import com.mobanker.financial.job.service.UnlockSecretService;
import com.mobanker.financial.service.ActivityRewardMoneyService;
import com.mobanker.financial.service.FinanceDailyService;
import com.mobanker.financial.service.FinanceReportChannelService;
import com.mobanker.financial.service.InvestorGuidTipsService;
import com.mobanker.financial.task.MessageScanTask;
import com.mobanker.framework.constant.Constants;
import com.mobanker.framework.dto.ResponseEntity;

/**
 * Description：任务控制器
 * 
 * @author yinyafei 2015.11.19
 */
@Controller
@RequestMapping("/financialJob")
public class FinanceJobController {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private TenderReleaseService tenderReleaseService;
	@Resource
	private TenderCompletionService tenderCompletionService;
	@Resource
	private BatchPayToCardTradeService batchPayToCardTradeService;
	@Resource
	private IncomeCalculationService calculationService;
	@Resource
	private TendeStatusChangeService tendeStatusChangeService;
	@Resource
	private UnlockSecretService unlockSecretService;
	@Resource
	private ReportRefoundStatisticsServcie refoundStatisticsServcie;
	@Resource
	private ReportCollectionDetailService collectionDetailService;
	@Resource
	private ReportPaymentDetailService paymentDetailService;
	@Resource
	private RepaySwitchService repaySwitchService;
	@Resource
	private RepaymentService repaymentService;
	@Resource
	private InvestorGuidTipsService investorGuidTipsService;
	@Resource
	private MessageScanTask messageScanTask;
	@Resource
	private FinanceDailyService financeDailyService;
	@Resource
	private FinanceDailyDao financeDailyDao;
	@Resource
	private FinanceReportChannelService financeReportChannelService;
	@Resource
	private YydUsersInfoDao yydUsersInfoDao;
	@Resource
	private RepairFailedTradeService repairFailedTradeService;
	@Resource
	private RepairBatchPayToCardService repairBatchPayToCardService;
	@Resource
	private ActivityRewardMoneyService rewardMoneyService;


	/**
	 * 获取自动还款状态
	 */
	@RequestMapping(value = "/getAutoRepayStatus")
	@ResponseBody
	public ResponseEntity getAutoRepayStatus() {

		ResponseEntity responseEntity = new ResponseEntity();

		boolean switchFlag = repaySwitchService.isAutoRepay();
		Map<String, String> map = new HashMap<String, String>();
		if (switchFlag) {
			map.put("switchFlag", "ON");
			map.put("switchMsg", "自动还款开启中");
		} else {
			map.put("switchFlag", "OFF");
			map.put("switchMsg", "自动还款已关闭");
		}
		responseEntity.setData(map);
		responseEntity.setStatus(Constants.System.OK);
		return responseEntity;
	}
	
	@ResponseBody
	@RequestMapping(value = "tenderRefund", method = RequestMethod.POST)
	public ResponseEntity tenderRefund(String optType, String tenderNos, String orderNo) {

		ResponseEntity responseEntity = new ResponseEntity();

		try {
			// 批量还款
			if (optType.equals("0")) {
				repaymentService.repayByBatch(tenderNos);
			}
			// 按标的号还款
			else if (optType.equals("1")) {
				repaymentService.repayByBatch(tenderNos);
			}
			// 按订单号还款
			else if (optType.equals("2")) {
				repaymentService.repayByOrderNo(orderNo);
			}
			responseEntity.setStatus(Constants.System.OK);
			responseEntity.setMsg("付款成功");
		} catch (Exception e) {
			responseEntity.setStatus(Constants.System.FAIL);
			responseEntity.setMsg("付款失败:" + e.getMessage());
		}
		return responseEntity;
	}

	/**
	 * 手工触发定时任务
	 * 
	 * @param type
	 * @param param
	 */
	@RequestMapping(value = "/executeTask")
	@ResponseBody
	public ResponseEntity executeTask(String type, String param, String bakParam) {

		ResponseEntity responseEntity = new ResponseEntity();

		if (type.equals("1")) {
			// 标的发布
			tenderReleaseService.tenderReleaseById(param);

		} else if (type.equals("2")) {
			// 招标完成
			tenderCompletionService.tenderCompteteById(param);

		} else if (type.equals("3")) {
			// 批量代付到卡
			batchPayToCardTradeService.batchPayToCardTradeById(param);

		} else if (type.equals("4")) {
			// 收益计算
			if (StringUtils.isEmpty(param)) {
				calculationService.incomeCalculate();
			} else {
				calculationService.incomeCalculateById(param);
			}

		} else if (type.equals("5")) {
			// 标的状态修改(开始收益、标的到期)
			tendeStatusChangeService.tenderStatusChangeById(param);

		} else if (type.equals("6")) {
			// 密码解锁
			unlockSecretService.resetPwdErrorCount();

		} else if (type.equals("7")) {
			// 还款统计
			refoundStatisticsServcie.refoundStatisticsByDate(param);

		} else if (type.equals("8")) {
			// 实收明细报表
			collectionDetailService.actualCollectionDetail(param);

		} else if (type.equals("9")) {
			// 实付明细报表
			paymentDetailService.actualPayDetail(param);
		} else if (type.equals("10")) {
			// 引导消息
			executeGuidTips(param,bakParam);
		} else if (type.equals("11")) {
			// 消息扫描
			messageScanTask.controlMessageScan(param);
		} else if (type.equals("12")) {
			// 交易修复
			repairFailedTradeService.doRepair(param);
		} else if (type.equals("13")) {
			// 批量代付到卡修复
			repairBatchPayToCardService.doRepair();
		} else if (type.equals("14")) {
			// 手工还款
			repaymentByParam(param,bakParam);
		} else if ("100".equals(type)) {
			List<String> list = new ArrayList<String>();
			list.add(param);
			financeDailyService.generatorDaily(list);
		} else if ("101".equals(type)) {
			List<String> list = new ArrayList<String>();
			list.add(param);
			financeReportChannelService.generatorChannel(list);
		}
		responseEntity.setMsg("执行完毕");
		responseEntity.setStatus(Constants.System.OK);
		return responseEntity;
	}
	
	/**
	 * 引导消息细分
	 * @param type
	 */
	private void executeGuidTips(String param, String bakParam) {

		if (param.equals("1")) {
			// 昨日绑卡未充值
			investorGuidTipsService.sendBindNoRechargeUserMsg();
		} else if (param.equals("2")) {
			// 历史绑卡未充值
			investorGuidTipsService.sendBindNoRechargeHistoryUserMsg();
		} else if (param.equals("3")) {
			// 昨日充值未投标
			investorGuidTipsService.sendRechargeNoSubmitUserMsg();
		} else if (param.equals("4")) {
			// 历史充值未投标
			investorGuidTipsService.sendRechargeNoSubmitHistoryUserMsg();
		} else if (param.equals("5")) {
			// 关系维护
			investorGuidTipsService.sendSubmitedUserMsg(bakParam);
		}
	}
	
	/**
	 * 还款细分
	 * @param param
	 */
	public void repaymentByParam(String param, String bakParam) {
		if (param.equals("1")) {
			// 批量还款
			repaymentService.repayByBatch(bakParam);
		} else if (param.equals("2")) {
			// 按标的号还款
			repaymentService.repayByTender(bakParam);
		} else if (param.equals("3")) {
			// 按订单号还款
			repaymentService.repayByOrderNo(bakParam);
		}
	}
	
	/**
	 * 日报表插入
	 * @param date
	 * @return
	*/
	@RequestMapping(value="/insertDaily", method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity insertDaily(String date) {
		logger.debug("-------------into insertDaily"+date);
		ResponseEntity resp = new ResponseEntity();
		try {
			if (StringUtils.isEmpty(date)) {
				throw new IllegalArgumentException("参数不能为空");
			}
			List<String> list = new ArrayList<String>();
			list.add(date);
			@SuppressWarnings("unused")
			Integer res = financeDailyService.generatorDaily(list);
		} catch (Exception e) {
			logger.debug("插入财务日报失败");
			e.printStackTrace();
			resp.setStatus(Constants.System.FAIL);
			resp.setError(e.getMessage());
			resp.setMsg("插入日报失败");
		}
		return resp;
	}
	
	/**
	 * 渠道插入
	 * @param date
	 * @return
	*/
	@RequestMapping(value="/insertChannel", method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity insertChannel(String date) {
		logger.debug("---------into insertChannel"+date);
		ResponseEntity resp = new ResponseEntity();
		try {
			if (StringUtils.isEmpty(date)) {
				throw new IllegalArgumentException("参数不能为空");
			}
			List<String> list = new ArrayList<String>();
			list.add(date);
			financeReportChannelService.generatorChannel(list);
		} catch (Exception e) {
			logger.debug("插入渠道失败");
			e.printStackTrace();
			resp.setStatus(Constants.System.FAIL);
			resp.setError(e.getMessage());
			resp.setMsg("插入渠道失败");
		}
		return resp;
	}
}
