package com.tracbds.server.msg.x0200;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.tracbds.core.IJT808MsgAttached;
import com.tracbds.core.bean.AttachedBean0x0200;
import com.tracbds.core.utils.Utils;

import io.netty.channel.ChannelHandlerContext;
@Component
public class A51 implements IJT808MsgAttached {

	@Value("#{configs['jt808.server.0x0200.0x51.temperature']}")
	private String enabled="false";
	@Override
	public int getAttachedId() {
		return 0x51;
	}

	@Override
	public Object getValue(byte[] bytes,String tid,ChannelHandlerContext ctx,Map<String,Object> map,List<AttachedBean0x0200> listAttachedBean0x0200,boolean isVersion) {
		if(!"true".equals(enabled))return null;//因为是扩展功能，需要通过配置来开启此功能
		int a=Utils.byteArrayToInt(new byte[] {bytes[0],bytes[1]});
		float t=0;
		//System.out.println(a);
		//System.out.println(Integer.parseInt("8081", 16));
		//System.out.println(String.format("%04X", (1<<15)));
		if((a&(1<<15))>0) {
			a=a-(1<<15);
			t=-(a/10f);
		}else {
			t=a/10f;
		}
		//System.out.println("温度:"+t);
		if(map.containsKey("status_str")) {
			map.put("status_str", map.get("status_str").toString()+","+"温度:"+t);
		}else {
			map.put("status_str", "温度:"+t);
		}
		return null;
	}
	
}
