package com.mobanker.financial.job.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mobanker.financial.entity.FinanceCommonCfg;
import com.mobanker.financial.service.FinanceCommonCfgService;

/**
 * 还款开关
 * 
 * @author yinyafei
 * 2015.12.20
 */
@Service
public class RepaySwitchService {

	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private RepaySwitchService repaySwitchService;
	
	/**
	 * 获取自动还款开关状态
	 */
	public boolean isAutoRepay() {

		String value = commonCfgService.getCommonCfgValueByCode("AUTO_REFUND_FLAG");
		if (!StringUtils.isEmpty(value)) {
			if (value.equals("ON")) {
				return true;
			}
		} else {
			FinanceCommonCfg commonCfg = new FinanceCommonCfg();
			commonCfg.setId("9001");
			commonCfg.setType("AUTO_REFUND_FLAG");
			commonCfg.setTypeDesc("自动还款配置");
			commonCfg.setCode("AUTO_REFUND_FLAG");
			commonCfg.setCodeDesc("ON:开启 OFF 关闭");
			commonCfg.setValue("OFF");
			commonCfg.setIsDeleted("1");
			commonCfg.setCreateUser("admin");
			commonCfg.setUpdateUser("admin");
			commonCfgService.insert(commonCfg);
		}
		return false;
	}
	
	/**
	 * 开关操作
	 * 
	 * @param flag
	 */
	public void turnOffAutoRepay(String flag) {

		FinanceCommonCfg commonCfg = commonCfgService.getCommonCfgByCode("AUTO_REFUND_FLAG");
		if (commonCfg != null) {
			commonCfg.setValue(flag);
			commonCfgService.update(commonCfg);
		}
	}
}
