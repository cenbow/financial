package com.mobanker.financial.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.mobanker.framework.constant.enums.PermissionResult;
import com.mobanker.framework.dto.LoginResponse;
import com.mobanker.framework.dto.LoginUserDto;
import com.mobanker.framework.exception.DataCommitException;
import com.mobanker.framework.service.LoginService;

@Service
public class LoginServiceImpl implements LoginService {

	@Override
	public LoginUserDto userLoginSuccess(LoginUserDto user, String token,
			String loginIp, HttpServletRequest request,
			HttpServletResponse response) throws DataCommitException {
		return null;
	}

	@Override
	public boolean userLoginSuccess(LoginUserDto user,
			HttpServletRequest request, HttpServletResponse response) {
		return false;
	}

	@Override
	public PermissionResult checkPermission(HttpServletRequest request) {
		return null;
	}

	@Override
	public LoginResponse login(String loginName, String password,
			String loginIp, HttpServletRequest request,
			HttpServletResponse response) throws DataCommitException {
		return null;
	}

	@Override
	public void logout(String userId, String loginIp)
			throws DataCommitException {
		
	}

}
