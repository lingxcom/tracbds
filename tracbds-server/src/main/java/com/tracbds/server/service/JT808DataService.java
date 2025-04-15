package com.tracbds.server.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tracbds.core.service.JT808CommonService;
import com.tracbds.core.utils.Utils;

@Component
public class JT808DataService {
	
	public static ConcurrentLinkedQueue<Map<String,Object>> LocatoinData=new ConcurrentLinkedQueue<Map<String,Object>>();
	
	public static Cache<String, Integer> UPDATE_CAR_CACHE = CacheBuilder.newBuilder().maximumSize(1000000).expireAfterWrite(5, TimeUnit.MINUTES).build();
	
	public static Cache<String, List<Map<String,Object>>> OIL_CACHE=CacheBuilder.newBuilder().maximumSize(200000).expireAfterAccess(30, TimeUnit.MINUTES).build();

	@Value("#{configs['jt808.server.0x0200.autoadd']}")
	private String autoadd="true";
	@Autowired
	private JdbcTemplate jdbcTemplate;
	/**
	 * 允许上报的设备ID
	 */
	//private Set<String> tids=Collections.synchronizedSet(new HashSet<>());
	/**
	 * 车辆注册 
	 * {"p0":"2013","p1":0,"p2":0,"p3":"3031313131","p4":"4C494E4758313131313131313131313131313131","p5":"31313131313131","p6":1,"p7":"闽D12345","tid":"013800138999"}
	 * @param map
	 * @return 0：成功；1：车辆已被注册；2：数据库中无该车辆；
		3：终端已被注册；4：数据库中无该终端
	 */
	public int handle0100(Map<String,Object> map) {
		
		return 0;
	}
	/**
	 * 终端注销
	 * @param tid
	 */
	public void handle0003(String tid) {
		
	}
	/**
	 * 下发鉴权码
	 * @return
	 */
	public String get0100Code() {
		return "1234567890";
	}
	/**
	 * 检查设备上报的鉴权码
	 * @param code
	 * @return
	 */
	public boolean check0102Code(String code) {
		return true ;
	}
	
	public void saveEvent(String tid, String event) {
		this.jdbcTemplate.update("insert into tgps_event(car_id,event,ts) values(?,?,?)",tid,event,Utils.getTime());
	}
	
	public void handl0900(String tid,int type,byte[] data) {
			String hexstring=Utils.bytesToHex(data);
		
		this.jdbcTemplate.update("insert into tgps_jt808_touchuan(tid,type,data,ascii_code,uord,ts,remark) values(?,?,?,?,?,?,?)",
				tid,String.format("0x%02X", type),hexstring,new String(data),"上行",Utils.getTime(),"");
	}
	
	public String getOil(String tid,String oil) {
		
		float ret=0;
		if(OIL_CACHE.getIfPresent(tid)==null) {
			List<Map<String,Object>> list=this.jdbcTemplate.queryForList("select key1,value1 from tgps_oiltype_item where oiltype_id in (select bylc from tgps_car where id=? ) order by key1 asc",tid);
			OIL_CACHE.put(tid, list);
		}
		if(OIL_CACHE.getIfPresent(tid)!=null) {
			 List<Map<String,Object>> list=OIL_CACHE.getIfPresent(tid);
			 if(list.size()==0)return oil;
			 int index=-1;
			 for(int i=0;i<list.size();i++) {
				 Map<String,Object> m=list.get(i);
				 if(Float.parseFloat(oil)>=Float.parseFloat(m.get("key1").toString())) {
					 index=i;
				 }
			 }
			 if(index>=0) {
				 ret=Float.parseFloat(list.get(index).get("value1").toString());
				 if(index<list.size()-1) {
					 float temp=Float.parseFloat(oil);
					 float temp1=Float.parseFloat(list.get(index).get("key1").toString());
					 float temp2=Float.parseFloat(list.get(index+1).get("key1").toString());
					 float percentage=(temp-temp1)/(temp2-temp1);

					 temp1=Float.parseFloat(list.get(index).get("value1").toString());
					 temp2=Float.parseFloat(list.get(index+1).get("value1").toString());
					 ret=ret+(temp2-temp1)*percentage;
				 }
			 }
		}
		return String.format("%.2f", ret);
	}
	/**
	 * 多媒体事件信息上传
	 */
	public void save0800(Map<String, Object> map) {
		String sql="insert into tgps_0800(tid,mid,type,gs,event,tdh,systime) values(?,?,?,?,?,?,?)";
		this.jdbcTemplate.update(sql,map.get("tid"),map.get("p1"),map.get("p2"),map.get("p3"),map.get("p4"),map.get("p5"),Utils.getTime());
	}
	/**
	 * 媒体文件上报
	 */
	public void save0801(Map<String, Object> map) {
		String sql="insert into tgps_car_photo(tid,path,acc,lat,lng,speed,fx,gpstime,mid,type,gs,event,tdh,systime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		this.jdbcTemplate.update(sql,map.get("tid"),map.get("filepath"),map.get("acc"),map.get("lat"),map.get("lng"),map.get("speed"),map.get("direction"),map.get("gpstime"),
				map.get("p1"),map.get("p2"),map.get("p3"),map.get("p4"),map.get("p5"),Utils.getTime());
	}
	
	
	public void save0200(Map<String, Object> map) {
		if(map.get("tid")==null)return;
		
		if(map.get("speed")==null){
			map.put("speed", "0");
		}
		if(map.get("direction")==null){
			map.put("direction", "0");
		}
		if(map.get("alarm")==null){
			map.put("alarm", "0");
		}
		if(map.get("status")==null){
			map.put("status", "0");
		}
		if(map.get("height")==null){
			map.put("height", "0");
		}
		if(map.get("A01")==null){
			map.put("A01", "0");
		}
		if(map.get("A02")==null){
			map.put("A02", "0");
		}
		if(map.get("A30")==null){
			map.put("A30", "0");
		}
		if(map.get("A31")==null){
			map.put("A31", "0");
		}
		if(map.get("status_str")==null){
			map.put("status_str", "");
		}
		if(map.get("alarm_str")==null){
			map.put("alarm_str", "");
		}
		if(map.get("lat")==null){
			map.put("lat", "0");
		}
		if(map.get("lng")==null){
			map.put("lng", "0");
		}
		//long status=Long.parseLong(map.get("status").toString());
		if(UPDATE_CAR_CACHE.getIfPresent(map.get("tid").toString())==null) {
			UPDATE_CAR_CACHE.put(map.get("tid").toString(), 1);
			updateCar(map,"true".equals(this.autoadd));
			
		}
		LocatoinData.add(map);
		/*
		try {
			this.jdbcTemplate.update(String.format("insert into %s(car_id,acc,location,alarm,status,lat,lng,height,speed,direction,gpstime,systime,mileage,oil,txxh,wxxh,status_str,alarm_str) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", this.jt808CommonService.get0x0200TableName(map.get("gpstime").toString())),
					map.get("tid"),(status&0b01)>0?"1":"0",(status&0b10)>0?"1":"0",map.get("alarm"),map.get("status"),map.get("lat"),map.get("lng"),map.get("height"),map.get("speed"),map.get("direction"),map.get("gpstime"),Utils.getTime(),map.get("A01"),map.get("A02"),map.get("A30"),map.get("A31")
							,map.get("status_str"),map.get("alarm_str"));
		} catch (Exception e) {
			System.out.println(JSON.toJSONString(map));
			e.printStackTrace();
		}
		*/
	}
	
	public void updateCar(Map<String, Object> map, boolean isAutoAdd) {
		long status = Long.parseLong(map.get("status").toString());
		String sql = "update tgps_car set online='1', acc=?,location=?,alarm=?,status=?,lat=?,lng=?,height=?,speed=?,direction=?,mileage=?,gpstime=?,systime=? where id=?";
		int c = this.jdbcTemplate.update(sql, (status & 0b01) > 0 ? "1" : "0", (status & 0b10) > 0 ? "1" : "0",
				map.get("alarm"), map.get("status"), map.get("lat"), map.get("lng"), map.get("height"),
				map.get("speed"), map.get("direction"), map.get("A01"), map.get("gpstime"), Utils.getTime(),
				map.get("car_id"));
		if (c == 0 && isAutoAdd) {
			this.addCar(map.get("tid").toString());
		}
	}
	
	private void addCar(String tid) {
		if (!"true".equals(this.autoadd))
			return;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			Date sdate = new Date();
			Date edate = new Date();
			edate.setYear(edate.getYear() + 1);
			String time = Utils.getTime();
			this.jdbcTemplate.update(
					"insert into tgps_car(tid,carno,color,sim,stime,etime,create_time,modify_time,online) values(?,?,?,?,?,?,?,?,'1')",
					tid, tid, 0, tid, sdf.format(sdate), sdf.format(edate), time, time);
			int car_id=this.jdbcTemplate.queryForObject("select id from tgps_car where tid=?", Integer.class,tid);
			this.jdbcTemplate.update("insert into tgps_group_car(group_id,car_id) values(?,?)", 147, car_id);
			this.jdbcTemplate.update("delete from tgps_car_tdh where car_id=?",car_id);
			String channels[]=new String[] {"1","2","3","4"};
			for (String temp22 : channels) {
				this.jdbcTemplate.update("insert into tgps_car_tdh(car_id,tdh,name)values(?,?,?)", car_id,
						temp22.replace("CH-", ""), temp22);
			}
			tid = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
