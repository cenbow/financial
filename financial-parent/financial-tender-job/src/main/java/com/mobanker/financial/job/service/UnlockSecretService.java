package com.mobanker.financial.job.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mobanker.financial.entity.FinanceErrorPwd;
import com.mobanker.financial.service.FinanceErrorPwdService;



/**
 * 锁定的密码解锁
 * 
 * @author yinyafei
 *
 */
@Service
public class UnlockSecretService {

	@Resource
	private FinanceErrorPwdService financeErrorPwdService;
	
	public void resetPwdErrorCount() {

		FinanceErrorPwd findParams = new FinanceErrorPwd();
		findParams.setErrorNum(3);
		List<FinanceErrorPwd> errorPwdList = financeErrorPwdService.getByObj(findParams);

		for (FinanceErrorPwd errorPwd : errorPwdList) {

			Date date = errorPwd.getUpdateTime();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			long minutes = System.currentTimeMillis() - cal.getTimeInMillis();
			if (minutes / (1000 * 60) > 180) {
				errorPwd.setErrorNum(new Integer(0));
				errorPwd.setUpdateUser("admin");
				financeErrorPwdService.update(errorPwd);
			}
		}
	}
}
