package com.wyyf.action.subscribercallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.InputSource;

import com.lys.mvc.action.BaseAjaxAction;
import com.lys.utils.StringUtils;
import com.wyyf.action.redbag.MoneySend;


@Controller
@RequestMapping("wyyf/gzh")
public class Subscribe extends BaseAjaxAction{
	@RequestMapping("eventService")
	@ResponseBody
	public String subscribe(ModelMap model,HttpServletRequest request){
	  // 微信加密签名
        String signature = request.getParameter("signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 随机字符串
        String echostr = request.getParameter("echostr");

        String str = getStringFromRequest(request);
        
        try {
        	 Map<?, ?>  xmlData = parseXMl(str);
        	 System.out.println("==="+xmlData.toString());
        	 //关注公众号事件
        	 if("subscribe".equals(xmlData.get("Event"))){
        		 insertSubscribeData(xmlData);
        	 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return echostr;
	}

	@RequestMapping("sendRedPack")
	public void sendRedPack(ModelMap model,HttpServletRequest request){
		try {
			String opendId = request.getParameter("openid");
			MoneySend.sendRedPack(opendId);
			response.sendRedirect("/wap/index.jsp");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void insertSubscribeData(Map parMap) throws Exception{
		StringBuffer sql = new StringBuffer();
		
		sql.append(" insert into subscribe(su_id,su_openid,su_create_time,su_event,su_add_time,su_to_user_name,is_delete) ");
		sql.append(" values(?,?,?,?,?,?,?) ");
		List dataList = new ArrayList<>();
		dataList.add(StringUtils.getUUID32());
		dataList.add(parMap.get("FromUserName"));
		dataList.add(parMap.get("CreateTime"));
		dataList.add(parMap.get("Event"));
		dataList.add(new Date());
		dataList.add(parMap.get("ToUserName"));
		dataList.add(0);
		baseBiz.executeTRANS(sql.toString(), dataList.toArray());
	}
	
	

	/**
	 * 需要引用jdom-1.0.jar
	 * @param xmlDoc
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> parseXMl(String xmlDoc) throws Exception {  
	    /*if (StringUtils.isBlank(xmlDoc)) {  
	        return null;  
	    } */ 
	    StringReader xmlString = new StringReader(xmlDoc);  
	    // 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入  
	    InputSource source = new InputSource(xmlString);  
	    // 创建一个新的SAXBuilder  
	    SAXBuilder saxb = new SAXBuilder();  
	    // 通过输入源构造一个Document  
	    Document doc = saxb.build(source);  
	    Element root = doc.getRootElement();  
	    // 得到根元素所有子元素的集合  
	    List<?> node = root.getChildren();  
	    Map<String, String> m = new HashMap<String, String>();  
	    if (node.size() > 0) {  
	        for (int i = 0; i < node.size(); i++) {  
	            Element el = (Element) node.get(i);  
	            m.put(el.getName(), el.getText());  
	        }  
	    } else {  
	        m.put(root.getName(), root.getText());  
	    }  
	    return m;  
	} 

	/**
	 * 通过request得到请求流中的微信报文str
	 * @param request
	 * @return
	 */
	public static String getStringFromRequest(HttpServletRequest request){
		
		StringBuffer strb = new StringBuffer();
		
		try {
		
			ServletInputStream in = request.getInputStream();

			BufferedReader breader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			
			String str = null;
			
			while(null != (str = breader.readLine())){
				
				strb.append(str);
				
			}
			
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
		return strb.toString();
		
	}
	
	/**
	 * 发送https请求
	 * 
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static JSONObject httpsRequest(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		try {
			
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//connection.connect(); //到此步只是建立与服务器的tcp连接，并未发送http请求
			connection.setDoInput(true);
			//直到getInputStream()方法调用请求才真正发送出去
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line=br.readLine()) != null){
				sb.append(line);
				sb.append("\n");
			}
			System.out.println(sb.toString());
			br.close();
			connection.disconnect();
			jsonObject = (JSONObject) JSONObject.stringToValue(sb.toString());
			//jsonObject = new JSONObject(sb.toString());
			//JSONObject.fromObject(sb.toString());
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	public static void main(String[] args) {
		String APPID  = "wxa3a96a975a3d7848";
		String SECRET = "29d457540f7076594afebe14faa65fb3";
		String requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+APPID+"&secret="+SECRET+"&code="+""+"&grant_type=authorization_code";
		
		JSONObject jsonObject = null;
		try {
			
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//connection.connect(); //到此步只是建立与服务器的tcp连接，并未发送http请求
			connection.setDoInput(true);
			//直到getInputStream()方法调用请求才真正发送出去
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while((line=br.readLine()) != null){
				sb.append(line);
				sb.append("\n");
			}
			System.out.println(sb.toString());
			br.close();
			connection.disconnect();
			jsonObject = (JSONObject) JSONObject.stringToValue(sb.toString());
			//jsonObject = new JSONObject(sb.toString());
			//JSONObject.fromObject(sb.toString());
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(jsonObject);
	}
}