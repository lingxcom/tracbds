package com.tracbds.api.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tracbds.core.service.JT808CommonService;
import com.lingx.service.LingxService;
import com.lingx.web.api.IApi;
import com.lingx.web.api.impl.AbstractAuthApi;
@Component
public class Api1102 extends AbstractAuthApi{

	@Value("#{configs['gps.map.default.url']}")
	private String mapURL="";
	@Autowired
	private LingxService lingxService;
	@Override
	public int getApiCode() {
		return 1102;
	}

	public boolean isLog() {
		return false;
	}
	@Override
	public String getApiName() {
		return "获取配置信息";
	}
	@Override
	public String getGroupName() {
		return "车载监控";
	}
	@Override
	public Map<String, Object> api(Map<String, Object> params) {
		Map<String,Object> ret=IApi.getRetMap(1, "SUCCESS");
		Map<String,Object> data=new HashMap<>();
		data.put("centerLatlng", this.lingxService.getConfigValue("jt808.map.latlng", "39.916385,116.396621"));
		//data.put("websocket0x0200URL", this.lingxService.getConfigValue("jt808.websocket0x0200URL", "ws://127.0.0.1:8803"));
	//	data.put("mapType", commonService.getMapType());
		data.put("mapURL", mapURL);//自定义地图
		ret.put("data", data);
		return ret;
	}
}
