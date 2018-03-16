package com.course.hbutil;

import java.util.HashMap;
import java.util.Map;

public class MoneyTest {
	final static  String url = "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
	public static void main(String[] args) {
		String orderNNo =  MoneyUtils.getOrderNo() ; 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nonce_str", MoneyUtils.buildRandom());//随机字符串
		map.put("mch_billno", orderNNo);//商户订单
		map.put("mch_id", "1251687301");//商户号
		map.put("wxappid", "wxa3a96a975a3d7848");//公众号的appid例如：wx7e12sd4cdff   //
		//map.put("nick_name", "AGDSFGSGD");//提供方名称
		map.put("send_name", "网众验房");//用户名
		map.put("re_openid", "op5Xsw5gadTpTpTa6RmdPz-h5jtQ");//用户openid//杜秋ouGZUs4T5_6trWkvMw9_XC0RNe3c
		map.put("total_amount", 100);//付款金额按分来计算 100=1元 只能之int型 并且要大于1元小于等于200元
		map.put("min_value", 100);//最小红包		
		map.put("max_value", 100);//最大红包
		map.put("total_num", 1);//红包发送总人数  
		map.put("wishing", "新年快乐");//红包祝福语
		map.put("client_ip", "127.0.0.1");//ip地址
		map.put("act_name", "网众验房");//活动名称
		//map.put("remark", "AAAA");//备注
		map.put("sign", MoneyUtils.createSign(map));//签名
		String result = "";
		try {
			result = MoneyUtils.doSendMoney(url, MoneyUtils.createXML(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("result:"+result);
	}
}


