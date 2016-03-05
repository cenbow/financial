package com.mobanker.financial.dao2;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mobanker.financial.entity.YydUsersInfo;
import com.mobanker.financial.vo.DownloadChannelUserCountVo;
import com.mobanker.financial.vo.YydDataVo;
import com.mobanker.financial.vo.YydRegisterVo;




public interface YydUsersInfoDao {

	public YydUsersInfo getUserInfoByUid(String uid);
	
	public void updateUserInfo(YydUsersInfo yydUsersInfo);
	
	
	/**
	 * 获取某日注册用户数
	 * @param map
	 * @return
	 */
	List<YydRegisterVo> getRegUserNum(Map<String, Object> map);
	
	/**
	 * 获取某日理财注册用户
	 * @param map
	 * @return
	 */
	List<YydRegisterVo> getFinanceRegUserNum(Map<String, Object> map);
	
	/**
	 * 获取某日激活用户数
	 * @param map
	 * @return
	 */
	Integer getActivUserNum(Map<String, Object> map);
	
	/**
	 * 获取某日IOS注册用户数
	 * @param map
	 * @return
	 */
	List<YydRegisterVo> getIOSRegUserNum(Map<String, Object> map);
	
	List<YydRegisterVo> getIosFinanceRegUserNum(Map<String, Object> map);
	
	List<YydRegisterVo> getAndroidFinanceRegUserNum(Map<String, Object> map);
	
	/**
	 * 获取某日IOS激活用户数
	 * @param map
	 * @return
	 */
	Integer getIOSActivUserNum(Map<String, Object> map);
	
	/**
	 * 获取某日Android注册用户数
	 * @param map
	 * @return
	 */
	List<YydRegisterVo> getAndroidRegUserNum(Map<String, Object> map);
	
	/**
	 * 获取某日Android激活用户数
	 * @param map
	 * @return
	 */
	Integer getAndroidActivUserNum(Map<String, Object> map);
	
	/**
	 * 获取所有IOSuid
	 * @return
	 */
	List<String> getIOSUid();
	
	/**
	 * 获取所有Androiduid
	 * @return
	 */
	List<String> getAndroidUid();
	
	
	/**
	 * <!-- 查询某日每个渠道的注册用户数量-->
	 * @return
	 */
	List<DownloadChannelUserCountVo> queryDownloadChannelUserNum(Map<String, Object> map);
	
	List<DownloadChannelUserCountVo> queryFinanceDownloadChannelUserNum(Map<String, Object> map);
	
	/**
	 * 查询某日每个渠道的激活用户数量
	 * @param map
	 * @return
	 */
	List<DownloadChannelUserCountVo> queryDownloadChannelActivNum(Map<String, Object> map);
	
	/**
	 * 根据渠道获取uid
	 * @param channel
	 * @return
	 */
	List<String> getUidByChannel(String channel);
	
	String getChannelByUid(String uid);
	
	YydDataVo getDataByUid(String userId);
	
	Set<String> getAllChannel();
}
