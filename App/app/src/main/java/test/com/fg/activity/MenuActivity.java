package test.com.fg.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.Toast;

import com.fg.activity.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import test.com.fg.Classes.Cookbook;
import test.com.fg.util.Configuration;

public class MenuActivity extends AppCompatActivity {

    private static final int GET_COOKBOOK_SUCCEED = 101;
    private static final int GET_COOKBOOK_ERROR = 102;

    private WebView steps;
    private Intent mIntent;
    private int step;
    private Cookbook mCookbook;
    private String[] Ingredient;
    private String Tips;
    private ProgressDialog mProgressDialog;
    private String ImagesUrlPath;
    private StringBuilder sb = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initView();
    }

    private void initView(){
        steps = findViewById(R.id.steps);
        mProgressDialog = new ProgressDialog(this);
        mIntent = this.getIntent();
        String cookBookUrl = mIntent.getStringExtra("CookBookUrl");
        step = mIntent.getIntExtra("step",0);
        Ingredient = mIntent.getStringArrayExtra("Ingredient");
        Tips = mIntent.getStringExtra("Tips");
        String[] strings = cookBookUrl.split("/");
        ImagesUrlPath = strings[0] + "/" + strings[1] + "/";
//        System.out.println(cookBookUrl + step + ImagesUrlPath);
        mCookbook = new Cookbook(cookBookUrl,step,
                new Cookbook.getCookBookListener() {
                    @Override
                    public void getDone() {
                        Message msg = Message.obtain();
                        msg.what = GET_COOKBOOK_SUCCEED;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void getErroer(String error) {
                        Message msg = Message.obtain();
                        msg.what = GET_COOKBOOK_ERROR;
                        msg.obj = "网络异常";
                        handler.sendMessage(msg);
                    }
        });
        getCookBook();
    }

    private void getCookBook() {
        mProgressDialog.setMessage("正在获取相关信息");
        mProgressDialog.show();
        mCookbook.toGetCookBook();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_COOKBOOK_SUCCEED:
                    showCookBook();
                    break;
                case GET_COOKBOOK_ERROR:
                    errorToast((String) msg.obj);
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void showCookBook() {
        mProgressDialog.dismiss();
        String[] textList = mCookbook.getTextList();
        boolean[] haveImg = mCookbook.getHaveImg();

        addText("食材\n",5,"black","黑体",true );

        for(String string : Ingredient){
            addText(string+"\n",3,"orange","黑体");
        }

        for (int i = 0; i <step; i++) {
            addText("步骤"+(i+1)+"\n",5,"black","黑体",true );
            addText(textList[i]+"\n");
            if(haveImg[i]){
                addImage(i);
            }else {
                addText("<br>");
            }
        }

        addText("Tips",5,"black","黑体",true );
        addText(Tips);
        Document parse = Jsoup.parse(sb.toString());
        Elements imgs = parse.getElementsByTag("img");
        if (!imgs.isEmpty()) {
            for (Element e : imgs) {
                imgs.attr("width", "100%");
                imgs.attr("height", "auto");
            }
        }

        String content = parse.toString();
        steps.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);


    }

    private void addText(String text){
        sb.append("<p>").append(text).append("</p>");
    }

    private void addText(String text ,int size){
        sb.append("<font size=").append("\"").append(size).append("\"").append("color=\"red\">").append(text).append("</font>");
    }

    private void addText(String text ,int size, String color){
        sb.append("<font size=").append("\"").append(size).append("\"");
        sb.append("color=").append("\"").append(color).append("\"");
        sb.append(">");
        sb.append("<p>").append(text).append("</p>").append("</font>");
    }

    private void addText(String text ,int size,String color ,String style,boolean cu){
        sb.append("<font size=").append("\"").append(size).append("\"");
        sb.append("color=").append("\"").append(color).append("\"");
        sb.append("face=").append("\"").append(style).append("\"");
        sb.append(">");
        if(cu){
            sb.append("<p>").append("<b>").append(text).append("</b>").append("</p>").append("</font>");
        }else {
            sb.append("<p>").append(text).append("</p>").append("</font>");
        }

    }

    private void addText(String text ,int size,String color ,String style){
        sb.append("<font size=").append("\"").append(size).append("\"");
        sb.append("color=").append("\"").append(color).append("\"");
        sb.append("face=").append("\"").append(style).append("\"");
        sb.append(">");
        sb.append(text).append("<br>").append("</font>");
    }

    private void addImage(int index){
        sb.append("<p><img src=\"").append(Configuration.HOST).append(ImagesUrlPath);
        sb.append(index+1).append(".jpg").append("?size=960x960\"\" class=\"mCS_img_loaded\"></p>");
    }

    private void errorToast(String error) {
        mProgressDialog.dismiss();
        Toast.makeText(this,error,Toast.LENGTH_LONG).show();
    }

}
