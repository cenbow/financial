package com.mobanker.financial.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mobanker.financial.common.constants.MessageContants.SendMessageCode;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.common.message.MessageCenter;
import com.mobanker.financial.entity.FinanceCommonCfg;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceUidMappingService;
import com.mobanker.financial.service.FinanceWarningService;

/**
 * 发送预警消息
 * 
 * @author yinyafei
 *
 */
@Service
public class FinanceWarningServiceImpl implements FinanceWarningService {

	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private FinanceInvestUserService investUserService;
	@Resource
	private MessageCenter messageCenter;

	@Override
	public void sendWarning(String warningInfo) {

		String messageUrl = commonCfgService.getRequestUrl(RequestUrl.MESSAGE_URL, SendMessageCode.SEND_SYS_NOCODE);

		List<FinanceCommonCfg> commonCfgList = commonCfgService.getCommonCfgByType("WARNING_PEOPLE");
		for (FinanceCommonCfg commonCfg : commonCfgList) {

			String uid = commonCfg.getValue();
			uid = uidMappingService.getYYDUid(uid);
			String contents = "您有一条重要预警:" + warningInfo + ",请及时处理！";
			messageCenter.sendSysNoCode(messageUrl, uid, contents);
		}
	}
}
