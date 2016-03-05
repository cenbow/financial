package com.mobanker.financial.common.exceptions;

/**
 * Description:业务异常
 * 
 * @author yinyafei
 * 
 * 2015.11.17
 */
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = -6522339966752738250L;

	private String errorCode;

	public BusinessException() {
		super();
	}

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(Throwable cause) {
		super(cause);
	}

	public BusinessException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
}
