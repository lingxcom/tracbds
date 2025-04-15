package com.tracbds.server.msg.x0200;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracbds.core.IJT808MsgAttached;
import com.tracbds.core.bean.AttachedBean0x0200;
import com.tracbds.core.utils.Utils;
import com.tracbds.server.service.JT808DataService;

import io.netty.channel.ChannelHandlerContext;
@Component
public class A2B implements IJT808MsgAttached {
	@Autowired
	private JT808DataService jt808DataService;
	@Override
	public int getAttachedId() {
		return 0x2B;
	}

	@Override
	public Object getValue(byte[] bytes,String tid,ChannelHandlerContext ctx,Map<String,Object> map,List<AttachedBean0x0200> listAttachedBean0x0200,boolean isVersion) {
		//return Utils.byteArrayToInt(bytes);
		try {
			int num1=Utils.byteArrayToInt(new byte[] {bytes[0],bytes[1]});
			int num2=Utils.byteArrayToInt(new byte[] {bytes[2],bytes[3]});
			if(num1<=0)return null;
			String oil=this.jt808DataService.getOil(tid, String.valueOf(num1));
			map.put("A02", oil);
			String temp="油量:"+oil+"L";
			if(map.containsKey("status_str")) {
				map.put("status_str", map.get("status_str").toString()+","+temp);
			}else {
				map.put("status_str", temp);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(id);
		//System.out.println(num);
		return null;
	}

}
