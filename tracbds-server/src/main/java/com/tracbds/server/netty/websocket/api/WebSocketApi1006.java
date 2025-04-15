package com.tracbds.server.netty.websocket.api;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.tracbds.core.IJT808Cache;

import io.netty.channel.ChannelHandlerContext;
@Component
public class WebSocketApi1006 extends AbstractApi {
	
	public WebSocketApi1006(){
		this.setCmd("1006");
	}
	@Override
	public Map<String, Object> execute(Map<String, Object> param, ChannelHandlerContext ctx) {
		String car_id=param.get("car_id").toString();
		String array[]=car_id.split(",");
		String cid=ctx.channel().id().asLongText();
		if(IJT808Cache.REALTIME_TIDS.getIfPresent(cid)!=null){
			for(String id:array) {
				IJT808Cache.REALTIME_TIDS.getIfPresent(cid).remove(id);
			}
			
		}
		
		return this.getRetMap(1, "SUCCESS");
	}


}
