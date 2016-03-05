package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.UnlockSecretService;

/**
 * Description:锁定密码解锁
 * 
 * @author yinyafei
 *
 */
@Component
public class UnlockSecretTask {

	@Resource
	private UnlockSecretService unlockSecretService;

	public void unlockSecret() {

		unlockSecretService.resetPwdErrorCount();
	}
}
