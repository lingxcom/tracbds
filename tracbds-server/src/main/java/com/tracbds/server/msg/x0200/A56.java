package com.tracbds.server.msg.x0200;import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tracbds.core.IJT808MsgAttached;
import com.tracbds.core.bean.AttachedBean0x0200;

import io.netty.channel.ChannelHandlerContext;
@Component
public class A56 implements IJT808MsgAttached {

	@Value("#{configs['jt808.server.0x0200.0x56.quantity']}")
	private String enabled="false";
	@Override
	public int getAttachedId() {
		return 0x56;//北京，陈工，电量
	}

	@Override
	public Object getValue(byte[] bytes,String tid,ChannelHandlerContext ctx,Map<String,Object> map,List<AttachedBean0x0200> listAttachedBean0x0200,boolean isVersion) {
		if(!"true".equals(enabled))return null;//因为是扩展功能，需要通过配置来开启此功能
		int t=bytes[0]*10;
		if(map.containsKey("status_str")) {
			map.put("status_str", map.get("status_str").toString()+","+"电量:"+t+"%");
		}else {
			map.put("status_str", "电量:"+t+"%");
		}
		return null;
	}
	
}
