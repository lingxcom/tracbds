package com.tracbds.server.netty.websocket.api;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.tracbds.core.utils.Utils;
import com.tracbds.server.netty.websocket.WebsocketUtils;

import io.netty.channel.ChannelHandlerContext;
/**
 * 用户授权
 * @author lingx.com
 *
 */
@Component
public class WebSocketApi1004 extends AbstractApi {
	public static final int WAIT_TIME=10;
	public WebSocketApi1004(){
		this.setCmd("1004");
	}
	
	@Override
	public Map<String, Object> execute(Map<String, Object> param,ChannelHandlerContext ctx) {
		String text=param.get("text").toString();
		WebsocketUtils.setText(ctx.channel(),text);
		Map<String,Object> ret=this.getRetMap(1, "过滤条件设置成功");
		ret.put("time", Utils.getTime());
		ret.put("hexstring", "过滤条件设置成功");
		return ret;
	}
	
}
