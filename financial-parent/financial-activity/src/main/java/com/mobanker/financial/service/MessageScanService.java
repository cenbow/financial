package com.mobanker.financial.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobanker.financial.common.constants.MessageContants.SendMessageCode;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.common.message.MessageCenter;
import com.mobanker.financial.entity.FinanceMessage;
import com.mobanker.framework.mybatis.SqlInterceptor;

/**
 * 扫描信息发送表发送
 * 
 * @author yinyafei
 *
 */
@Service
public class MessageScanService {

	@Resource
	private FinanceMessageService FinanceMessageService;
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private MessageCenter messageCenter;

	public void scan() {

		SqlInterceptor.setRowBounds(new RowBounds(0, 1000));
		List<FinanceMessage> messageList = FinanceMessageService.getAllUnSendMessage();

		String messageUrl = commonCfgService.getRequestUrl(RequestUrl.MESSAGE_URL, SendMessageCode.SEND_REMIND);

		for (FinanceMessage message : messageList) {

			String nid = message.getNid();
			String userId = message.getUid();
			userId = uidMappingService.getYYDUid(userId);

			Map<String, Object> replaceParam = null;
			if (!StringUtils.isEmpty(message.getReplaceContent())) {
				replaceParam = new HashMap<String, Object>();
				replaceParam.put("#amount#", message.getReplaceContent());
			}
			String status = "1"; // 发送成功
			// 发送消息
			boolean flag = messageCenter.sendMarketingMessage(messageUrl, nid, userId, replaceParam);
			if (!flag) {
				status = "2"; // 消息发送失败
			}
			if (message.getMessageType().equals("all")) {
				// 发送消息
				flag = messageCenter.sendMarketingMessage(messageUrl, nid + "_sms", userId, replaceParam);
				if (!flag) {
					status = "3"; // 短信发送失败
				}
			}

			message.setSendStatus(status);
			FinanceMessageService.update(message);
		}
	}
}
