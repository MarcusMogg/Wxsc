package test.com.fg.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtils {
    private static HttpUtils httpUtils;
    private HttpListener mHttpListener;
    private HttpUtils() {
    }

    private static final int UPLOAD_SERVER_ERROR_CODE = 3;


    private static final String TAG = "HttpUtils";
    private static final String CHARSET = "utf-8"; // 设置编码


    public static HttpUtils getInstance() {
        if (null == httpUtils) {
            httpUtils = new HttpUtils();
        }
        return httpUtils;
    }

    public void get(final String RequestURL,final int requestCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doConn(null,RequestURL,null,"GET", requestCode);
            }
        }).start();
    }

    public void get(final String token,final String RequestURL, final int requestCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doConn(token,RequestURL,null,"GET", requestCode);
            }
        }).start();
    }


    public void postParams(final String RequestURL,final Map<String, String> param,final int requestCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doConn(null,RequestURL,param,"POST", requestCode);
            }
        }).start();

    }

    public void postParams(final String token, final String RequestURL, final Map<String, String> param,final int requestCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doConn(token,RequestURL,param,"POST",requestCode);
            }
        }).start();

    }

    public void delete(final String token, final String RequestURL, final Map<String, String> param,final int requestCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doConn(token,RequestURL,param,"DELETE",requestCode);
            }
        }).start();

    }


    private void doConn(String token,String RequestURL, Map<String, String> param,String type,int requestCode){
        String result = null;
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 读取超时
            int readTimeOut = 50 * 1000;
            conn.setReadTimeout(readTimeOut);
            // 超时时间
            int connectTimeout = 50 * 1000;
            conn.setConnectTimeout(connectTimeout);
            conn.setDoInput(true); // 允许输入流
            if(!"GET".equals(type))
                conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod(type); // 请求方式
            conn.setRequestProperty("Charset", CHARSET); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            //conn.setRequestProperty("user-agent", "PostmanRuntime");
            if(token != null){
                conn.setRequestProperty("Authorization","Bearer "+token);
                //System.out.println(token);
            }


            if (param != null && param.size() > 0) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuilder sb1 = new StringBuilder();
                for (String key : param.keySet()) {
                    String value = param.get(key);
                    sb1.append(key).append("=");
                    sb1.append(value).append("&");
                }
                sb1.deleteCharAt(sb1.length() - 1);
                Log.e("参数：",sb1.toString());
                //System.out.println(sb1.toString());
                dos.write(sb1.toString().getBytes("utf-8"));
                dos.flush();
            }


            //获取响应码 200=成功 当响应成功，获取响应的流

            int res = conn.getResponseCode();
			Log.e(TAG, "response code:" + res);
            if (res == 200) {
                InputStream input = conn.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(input));
                StringBuilder sb1 = new StringBuilder();
                String str;
                while ((str = bf.readLine()) != null) {
                    sb1.append(str);
            }
                result = sb1.toString();
				Log.e(TAG, "result : " + result);
                sendMessage(res,result,requestCode);
            } else if(res == 400){
                sendMessage(400,"失败",requestCode);
            }else {
                sendMessage(res,"错误",requestCode);
            }
        } catch (IOException e) {
            sendMessage(UPLOAD_SERVER_ERROR_CODE,"连接失败：" + e.getMessage(),requestCode);
            e.printStackTrace();
        }
    }

    private void sendMessage(int responseCode,String message,int requestCode){
        System.out.println(responseCode+","+message+requestCode);
        mHttpListener.onConnDone(responseCode,message,requestCode);
    }

    public void setHttpListener(HttpListener httpListener) {
        mHttpListener = httpListener;
    }

    public interface HttpListener{
        void onConnDone(int responseCode, String message,int requestCode);
    }

}
