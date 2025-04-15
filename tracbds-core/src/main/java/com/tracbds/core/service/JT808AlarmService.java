package com.tracbds.core.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tracbds.core.bean.AlarmBean;
import com.tracbds.core.bean.StatusBean;
import com.tracbds.core.utils.Utils;
import com.lingx.service.LanguageService;

@Component
public class JT808AlarmService {
	private static Cache<String, Map<String,Object>> alarmCache = CacheBuilder.newBuilder().maximumSize(1000000)
			.expireAfterAccess(5, TimeUnit.MINUTES).build();
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private GroupService groupService;
	@Resource 
	private LanguageService languageService;
	
	@Value("#{configs['jt808.core.alarm.sync.address']}")
	private String enabledSyncAddress="true";
	private List<AlarmBean> list=new ArrayList<>();
	
	private List<StatusBean> listStatus=new ArrayList<>();
	
	private int alarmConfigValue=0,statusConfigValue=0;
	@PostConstruct
	public void init() {
		this.reloadData();
	}
	
	public void saveAlarm(String tid,String name,Map<String,Object> map) {
		 this.saveAlarm(tid, name, map,false);
	}
	public void saveAlarm(String tid,String name,Map<String,Object> map,boolean isPushMessage) {
		String key=tid+name;
		if(alarmCache.getIfPresent(key)!=null)return;//太过连续上报就不处理，当作同一报警
		alarmCache.put(key, map);
		if(!map.containsKey("A01"))map.put("A01", 0);
		String address="";//this.addressService.getAddressOffsetGCJ02(map.get("lat").toString(), map.get("lng").toString());
		this.jdbcTemplate.update(
				"insert into tgps_car_alarm(car_id,name,systime,speed,lat,lng,gpstime,mileage,group_name,address) values(?,?,?,?,?,?,?,?,?,?)",
				tid, name, Utils.getTime(),  map.get("speed"),
				map.get("lat"), map.get("lng"), map.get("gpstime"), map.get("A01"),this.groupService.getGroupNameBy(tid),address);
		//this.syncGroup();
		if(isPushMessage) {
			String time=Utils.getTime();
			String carno=this.getCarnoByTid(tid);
			Set<String> userids=this.getUserIdsByTid(tid);
			String alarmInfo=carno+" -> "+name;
			for(String userid:userids) {
				this.jdbcTemplate.update(
						"insert into tlingx_message(content,to_user_id,from_user_id,type,status,is_push,is_audio,route_path,create_time,read_time) values(?,?,?,?,?,?,?,?,?,?)",
						alarmInfo, userid, "", 1, 1, 0,0, "", time, time);
			}
			
		}
	}
	/**
	 * 围栏报警增加备注
	 * @param tid
	 * @param name
	 * @param map
	 * @param remark
	 */
	public void saveAlarm(String tid,String name,Map<String,Object> map,String remark) {
		if(!map.containsKey("A01"))map.put("A01", 0);
		String key=tid+"_"+name;
		if(alarmCache.getIfPresent(key)==null) {
			String address="";//this.addressService.getAddressOffsetGCJ02(map.get("lat").toString(), map.get("lng").toString());
		this.jdbcTemplate.update(
				"insert into tgps_car_alarm(car_id,name,systime,speed,lat,lng,gpstime,mileage,remark,group_name,address) values(?,?,?,?,?,?,?,?,?,?,?)",
				tid, name, Utils.getTime(),  map.get("speed"),
				map.get("lat"), map.get("lng"), map.get("gpstime"), map.get("A01"),remark,this.groupService.getGroupNameBy(tid),address);
		//this.syncGroup();
		}
	}
	public void saveData(String tid,long alarm,Map<String,Object> map) {
		if(!map.containsKey("A01"))map.put("A01", 0);
		//if(alarm==0)return;//为0时没有任何报警，这里开启就没办法计算报警时长
		for(AlarmBean bean:this.list) {
			String key=tid+"_"+bean.getName();
			if(((alarm&bean.getBit())>0)&&alarmCache.getIfPresent(key)==null) {
				map.put("stime", System.currentTimeMillis());
				String address="";//this.addressService.getAddressOffsetGCJ02(map.get("lat").toString(), map.get("lng").toString());
				this.jdbcTemplate.update(
						"insert into tgps_car_alarm(car_id,name,systime,speed,lat,lng,gpstime,mileage,group_name,address,remark) values(?,?,?,?,?,?,?,?,?,?,?)",
						tid,  bean.getName(), Utils.getTime(),  map.get("speed"),
						map.get("lat"), map.get("lng"), map.get("gpstime"), map.get("A01"),this.groupService.getGroupNameBy(tid),address,"");
				//this.syncGroup();
				alarmCache.put(key, map);
				if(bean.isPush()) {
					String time=Utils.getTime();
					String carno=this.getCarnoByTid(tid);
					Set<String> userids=this.getUserIdsByTid(tid);
					String alarmInfo=carno+" -> "+bean.getName();
					for(String userid:userids) {
						this.jdbcTemplate.update(
								"insert into tlingx_message(content,to_user_id,from_user_id,type,status,is_push,is_audio,route_path,create_time,read_time) values(?,?,?,?,?,?,?,?,?,?)",
								alarmInfo, userid, "", 1, 1, 0,bean.isAudio()?1:0, "", time, time);
					}
					
				}
			}else if(((alarm&bean.getBit())==0)&&alarmCache.getIfPresent(key)!=null&&alarmCache.getIfPresent(key).containsKey("gpstime")){
				Map<String,Object> smap=alarmCache.getIfPresent(key);
				Object id=null;
				List<Map<String,Object>> list=this.jdbcTemplate.queryForList("select id from tgps_car_alarm where car_id=? and name=? and gpstime=? order by id desc limit 1",
						tid,bean.getName(),smap.get("gpstime"));
				if(list.size()==0) {
					System.out.println("alarm end:"+tid+","+bean.getName()+","+JSON.toJSONString(smap));
					alarmCache.invalidate(key);
					return;
				}
				id=list.get(0).get("id");
				long stime=Long.parseLong(smap.get("stime").toString());
				long etime=System.currentTimeMillis();
				this.jdbcTemplate.update("update tgps_car_alarm set time=?,bjlc=?,end_lat=?,end_lng=?,end_speed=?,end_gpstime=?,end_mileage=? where id=?"
						,(etime-stime)/1000,(Float.parseFloat( map.get("A01").toString())-Float.parseFloat(smap.get("A01").toString()))
						,map.get("lat"), map.get("lng"),map.get("speed"), map.get("gpstime"), map.get("A01"),id);
				alarmCache.invalidate(key);
			}
		}
	}
	@Scheduled(cron="0 0/10 * * * ?")//10分钟处理一次
	public void reloadData() {
		list.clear();
		this.alarmConfigValue=0;
		this.statusConfigValue=0;
		List<Map<String,Object>> list1=this.jdbcTemplate.queryForList("select * from tgps_alarm_config where status='1'");
		for(Map<String,Object> map:list1) {
			AlarmBean bean=new AlarmBean();
			int bit=getBitNumber(map.get("bit").toString());
			this.alarmConfigValue+=bit;
			bean.setBit(bit);
			bean.setName(map.get("name").toString());
			bean.setAudio("1".equals(map.get("audio_alarm").toString()));
			bean.setPush("1".equals(map.get("push_alarm").toString()));
			this.list.add(bean);
		}
		
		this.listStatus.clear();
		 list1=this.jdbcTemplate.queryForList("select * from tgps_status_config where status='1'");
			for(Map<String,Object> map:list1) {
				StatusBean bean=new StatusBean();
				int bit=getBitNumber(map.get("bit").toString());
				this.statusConfigValue+=bit;
				bean.setBit(bit);
				bean.setName(map.get("name").toString());
				this.listStatus.add(bean);
			}
	}
	public String handleStatus_bak(long status) {
		StringBuilder sb=new StringBuilder();
		for(StatusBean bean:this.listStatus) {
			sb.append(bean.getName().split("[|]")[(status&bean.getBit())>0?1:0]).append(",");
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length()-1);
		}else {
			sb.append("-");
		}
		return sb.toString();
	}
	
	public String handleAlarm_bak(long alarm) {
		StringBuilder sb=new StringBuilder();
		for(AlarmBean bean:this.list) {
			if((alarm&bean.getBit())>0) {
				sb.append(bean.getName()).append(",");
			}
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length()-1);
		}else {
			sb.append("-");
		}
		return sb.toString();
	}
	
	
	public String handleStatus(long status,String language) {
		StringBuilder sb=new StringBuilder();
		for(StatusBean bean:this.listStatus) {
			sb.append(this.languageService.text(bean.getName().split("[|]")[(status&bean.getBit())>0?1:0], language)).append(",");
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length()-1);
		}else {
			sb.append("-");
		}
		return sb.toString();
	}
	
	public String handleAlarm(long alarm,String language) {
		StringBuilder sb=new StringBuilder();
		for(AlarmBean bean:this.list) {
			if((alarm&bean.getBit())>0) {
				sb.append(this.languageService.text(bean.getName(), language)).append(",");
			}
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length()-1);
		}else {
			sb.append("-");
		}
		return sb.toString();
	}
	private int getBitNumber(String bit) {
		int num=Integer.parseInt(bit);
		int ret=1;
		for(int i=0;i<num;i++) {
			ret=ret<< 1;
		}
		return ret;
	}
	public List<StatusBean> getStatusConfig(){
		return this.listStatus;
	}
	
	public List<AlarmBean> getAlarmConfig(){
		return this.list;
	}
	public int getAlarmConfigValue() {
		return alarmConfigValue;
	}
	public int getStatusConfigValue() {
		return statusConfigValue;
	}
	
	public String getCarnoByTid(String tid) {
		String carno="";
		List<Map<String,Object>> list=this.jdbcTemplate.queryForList("select carno from tgps_car where id=?",tid);
		if(list.size()>0)carno=list.get(0).get("carno").toString();
		return carno;
	}
	public Set<String> getUserIdsByTid(String tid) {
		Set<String> sets=new HashSet<>();
		List<Map<String,Object>> list=this.jdbcTemplate.queryForList("select user_id from tgps_group_user where group_id in (select group_id from tgps_group_car where car_id=?)",tid);
		for(Map<String,Object> map:list) {
			sets.add(map.get("user_id").toString());
		}
		return sets;
	}
}
