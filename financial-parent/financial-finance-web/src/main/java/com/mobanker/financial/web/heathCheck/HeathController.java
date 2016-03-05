package com.mobanker.financial.web.heathCheck;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mobanker.framework.constant.Constants;
import com.mobanker.framework.dto.ResponseEntity;

/**
 * Description：健康检查
 * 
 * @author yinyafei
 * 2015.11.19
 */
@Controller
public class HeathController {
	
	@RequestMapping("/heathCheck")
	@ResponseBody
	public ResponseEntity heathCheck() {
		
		ResponseEntity responseEntity = new ResponseEntity();
		responseEntity.setStatus(Constants.System.OK);
		responseEntity.setMsg("这家伙还活着!");
		return responseEntity;
	}
}
