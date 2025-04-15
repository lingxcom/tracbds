package com.tracbds.server.netty.websocket.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.tracbds.core.IJT808Cache;
import com.tracbds.core.service.JT808CommonService;
import com.lingx.web.api.IApi;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
@Component
public class WebSocketApi1005 extends AbstractApi {
	public static AttributeKey<String> channel_langugae_key= AttributeKey.valueOf("Language");
	@Autowired 
	private JT808CommonService jt808CommonService;
	public WebSocketApi1005(){
		this.setCmd("1005");
	}
	@Override
	public Map<String, Object> execute(Map<String, Object> param, ChannelHandlerContext ctx) {
		String car_id=param.get("car_id").toString();
		String language=IApi.getParamString(param, "language", "zh-CN");
		String array[]=car_id.split(",");
		Map<String, Object> ret=new HashMap<>();
		List<Map<String,Object>> list=new ArrayList<>();
		String cid=ctx.channel().id().asLongText();
		ctx.channel().attr(channel_langugae_key).set(language);
		for(String id:array) {
			list.add(this.getGpsData(id, cid, param,language));
		}
		ret.put("data", list);
		ret.put("websocketDataType", "list");
		return ret;
	}

	private Map<String,Object> getGpsData(String car_id,String cid,Map<String, Object> param,String language){
		Map<String, Object> map=jt808CommonService.getLast0x0200Data(car_id);
		String tid=map.get("tid").toString();
		if(IJT808Cache.REALTIME_TIDS.getIfPresent(cid)==null) {
			Set<String> sets=new HashSet<String>();
			sets.add(car_id);
			IJT808Cache.REALTIME_TIDS.put(cid, sets);
			
		}else {
			if(param.containsKey("single"))IJT808Cache.REALTIME_TIDS.getIfPresent(cid).clear();
			IJT808Cache.REALTIME_TIDS.getIfPresent(cid).add(car_id);
		}
		
		map.put("online", IJT808Cache.SESSIONS.getIfPresent(tid)!=null?"1":"0");
		this.jt808CommonService.addJT808Info(map,language);
		
		return map;
	}
}
