package test.com.fg.Classes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import test.com.fg.util.Configuration;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class Cookbook {
    private String url;
    private int step;
    private String[] textList;
    private boolean[] haveImg;
    private getCookBookListener listener;

    public Cookbook(String url, int step, getCookBookListener listener){
        this.url = url;
        this.listener = listener;
        this.step = step;
        textList = new String[step];
        haveImg = new boolean[step];
    }


    public void toGetCookBook(){
        if(url != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doGetCookBook();
                }
            }).start();
        }else {
            listener.getErroer("上传链接错误");
        }
    }

    private void doGetCookBook(){
        try {
            URL detialsURL = new URL(Configuration.HOST + url);
            URLConnection conn = detialsURL.openConnection();
            InputStream is = conn.getInputStream();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            parserJSON(sb.toString());
            listener.getDone();
        }catch (IOException e) {
            listener.getErroer("网络异常");
        }
    }

    private void parserJSON(String jsonString){
        JSONArray temp = JSONObject.parseArray(jsonString);
        JSONObject data = temp.getJSONObject(0);
        for (int i = 1; i <= step; i++) {
            String[] text = data.getString("Step " + i).split("##");
            textList[i - 1] = text[0];
            haveImg[i - 1] = text[1].endsWith("jpg");
        }
    }

    public String[] getTextList() {
        return textList;
    }

    public void setTextList(String[] textList) {
        this.textList = textList;
    }

    public boolean[] getHaveImg() {
        return haveImg;
    }

    public void setHaveImg(boolean[] haveImg) {
        this.haveImg = haveImg;
    }

    public interface getCookBookListener{

        void getDone();

        void getErroer(String error);

    }

}
