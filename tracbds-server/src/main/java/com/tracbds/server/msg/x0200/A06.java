package com.tracbds.server.msg.x0200;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.tracbds.core.IJT808MsgAttached;
import com.tracbds.core.bean.AttachedBean0x0200;
import com.tracbds.core.utils.Utils;

import io.netty.channel.ChannelHandlerContext;
@Component
public class A06 implements IJT808MsgAttached {

	@Override
	public int getAttachedId() {
		return 0x06;
	}

	@Override
	public Object getValue(byte[] bytes,String tid,ChannelHandlerContext ctx,Map<String,Object> map,List<AttachedBean0x0200> listAttachedBean0x0200,boolean isVersion) {
		int cxwd=new Integer(Utils.byteArrayToInt(bytes)).shortValue();//车厢温度
		if(map.containsKey("status_str")) {
			map.put("status_str", map.get("status_str").toString()+","+"车厢温度:"+cxwd+"°C");
		}else {
			map.put("status_str", "车厢温度:"+cxwd+"°C");
		}
		return cxwd;
	}

	
}
