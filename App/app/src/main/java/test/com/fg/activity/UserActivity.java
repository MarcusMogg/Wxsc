package test.com.fg.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fg.activity.R;

import java.util.ArrayList;
import java.util.HashMap;

import test.com.fg.Classes.Details;
import test.com.fg.Classes.User;
import test.com.fg.util.Configuration;
import test.com.fg.util.HttpUtils;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class UserActivity extends Activity implements HttpUtils.HttpListener , Details.getDetailsListener{

    private static final int LOGIN = 101;
    private static final int LOGIN_DONE = 1;
    private static final int GET_FAV = 102;
    private static final int GET_FAV_DONE = 2;
    private static final int GET_DETAILS_ERROR = 3;
    private static final int GET_DETAILS_DONE = 4;
    private static final int DELETE_FAV_DONE = 5;
    private TextView logout;
    private User mUser = new User();
    private ArrayList<Details> favfood = new ArrayList<>();
    private SharedPreferences user;
    private ProgressDialog progressDialog;
    private String token;
    private HttpUtils mHttpUtils;
    private JSONArray favfoodJson;
    private static final int DELETE_FAV = 2333;
    private TextView username;
    private double suggestCal;
    private double eatCal = 0;
    private HashMap<String,Integer> eat = new HashMap<>();
    private TextView sumCal,warning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user);
        initView();
    }


    private void initView() {
        username = findViewById(R.id.user_username);
        logout = findViewById(R.id.tv_right_text);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        mHttpUtils = HttpUtils.getInstance();
        mHttpUtils.setHttpListener(this);
        user = getSharedPreferences("user",0);
        progressDialog = new ProgressDialog(this);
        getToken();
    }

    private void getFav() {
        mHttpUtils.get(token,Configuration.HOST+Configuration.FAVFOOD,GET_FAV);
    }

    private void logOut(){
        SharedPreferences.Editor editor = user.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(UserActivity.this,Login.class);
        this.startActivity(intent);
        finish();
    }

    private void getToken() {
        progressDialog.setMessage("正在获取用户信息");
        progressDialog.show();
        mUser.setEmail(user.getString("email",""));
        mUser.setPwd(user.getString("pwd",""));
        mUser.to_login(this,LOGIN);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case LOGIN_DONE:
                            checkLogin((String) msg.obj);
                            break;
                case GET_FAV_DONE:
                    parserFav((String) msg.obj);
                    break;
                case GET_DETAILS_DONE:
                    if(msg.arg1==(favfoodJson.size())){
                        showFavFood();
                    }
                    break;
                case GET_DETAILS_ERROR:
                    Toast.makeText(UserActivity.this,"网络异常",Toast.LENGTH_SHORT).show();
                    break;
                case DELETE_FAV_DONE:
                    Toast.makeText(UserActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserActivity.this,UserActivity.class);
                    UserActivity.this.startActivity(intent);
                    UserActivity.this.finish();

                default:
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void showFavFood() {
        progressDialog.dismiss();
        for(Details dishNow : favfood){
            createLayout(dishNow.getEnglishName(),dishNow.getChineseName(),
                    dishNow.getCal(),user.getString(dishNow.getEnglishName(),""));
        }
        createAns();
    }

    private void createAns() {
        LinearLayout menuLinear = findViewById(R.id.mymenu_layout);
        LinearLayout anslayout = new LinearLayout(this);
        anslayout.setOrientation(LinearLayout.HORIZONTAL);
        anslayout.setPadding(0,10,0,0);

        sumCal = new TextView(this);
        warning = new TextView(this);
        sumCal.setPadding(0,15,20,0);
        sumCal.setTextSize(20);
        warning.setTextSize(20);
        anslayout.addView(sumCal);
        anslayout.addView(warning);
        menuLinear.addView(anslayout);
    }
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showAns(){
        sumCal.setText("今日摄入热量："+String.format("%.2f",eatCal)+"大卡");
        if(eatCal>1.25*suggestCal){
            warning.setTextColor(getResources().getColor(R.color.red));
            warning.setText("摄入热量过多");
        }else if(eatCal<0.75*suggestCal){
            warning.setTextColor(getResources().getColor(R.color.blue));
            warning.setText("摄入热量过少");
        }else {
            warning.setTextColor(getResources().getColor(R.color.green));
            warning.setText("摄入合适");
        }
    }

    private void createLayout(String Name, String ChineseName, double Cal, String picpath) {
        LinearLayout menuLinear = findViewById(R.id.mymenu_layout);
        LinearLayout dish = new LinearLayout(this);
        dish.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        dish.setPadding(0,0,0,10);

        createImage(dish, picpath);
        createText(info, ChineseName);
        createText(info, Cal);
        createEditText(info,Name);
        dish.addView(info);
        createButton(dish, "删除", Name);
        menuLinear.addView(dish);
    }

    private void createEditText(LinearLayout info, final String engName) {
        EditText cal = new EditText(this);
        cal.setHint("请输入今日摄入量(g)");
        cal.setTextSize(17);
        cal.setInputType(InputType.TYPE_CLASS_NUMBER);
        cal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!"".equals(s.toString())){
                    eat.put(engName,Integer.parseInt(s.toString()));
                    countCal();
                }else {
                    Toast.makeText(UserActivity.this,"请输入正确摄入量",Toast.LENGTH_SHORT).show();
                }
            }
        });
        info.addView(cal);
    }

    private void countCal() {
        eatCal=0;
        for (Details temp:favfood){
            if(null!=eat.get(temp.getEnglishName()))
                eatCal = eatCal + (temp.getCal()*eat.get(temp.getEnglishName()))/100;
        }
        showAns();
    }

    private void createImage(LinearLayout oneDish, String picpath) {
        ImageView dishImage = new ImageView(this);
        Bitmap foodBitmap = BitmapFactory.decodeFile(picpath);
        dishImage.setImageBitmap(foodBitmap);
        oneDish.addView(dishImage);
    }

    @SuppressLint("ResourceAsColor")
    private void createButton(LinearLayout oneDish, String delete, final String Name) {
        LinearLayout delete_layout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,80);
        delete_layout.setLayoutParams(lp);
        delete_layout.setGravity(Gravity.RIGHT);
        Button deleteItem = new Button(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(150,80);
        deleteItem.setLayoutParams(layoutParams);
        deleteItem.setText(delete);
        deleteItem.setPadding(0,-7,0,0);
        deleteItem.setTextSize(15);
        deleteItem.setBackgroundColor(getResources().getColor(R.color.red));
        deleteItem.setTextColor(getResources().getColor(R.color.black));
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,String> pp= new HashMap<>();
                pp.put("name",Name);
                SharedPreferences.Editor e = user.edit();
                e.remove(Name);
                e.apply();
                mHttpUtils.delete(token,Configuration.HOST+Configuration.FAVFOOD,pp,DELETE_FAV);
            }
        });
        delete_layout.addView(deleteItem);
        oneDish.addView(delete_layout);
    }

    private void createText(LinearLayout oneDish,String ChineseName) {
        TextView menuNowName=new TextView(this);
        menuNowName.setText(ChineseName);
        menuNowName.setPadding(20,0,0,0);
        menuNowName.setTextSize(17);
        menuNowName.setTextColor(getResources().getColor(R.color.black));
        oneDish.addView(menuNowName);
    }
    private void createText(LinearLayout oneDish,double nutrition) {
        TextView menuNowName=new TextView(this);
        menuNowName.setText("卡路里：" + String.valueOf(nutrition) + "大卡/100g");
        menuNowName.setPadding(20,0,0,0);
        menuNowName.setTextSize(17);
        menuNowName.setTextColor(getResources().getColor(R.color.black));
        oneDish.addView(menuNowName);
    }

    private void parserFav(String msg) {
        favfoodJson = JSONObject.parseArray(msg);
        Log.e("sdfds",msg);
        System.out.println(favfoodJson.size());
        if(favfoodJson.size() == 0){
            showFavFood();
        }
        for (int i = 0;i<favfoodJson.size();i++){
            JSONObject temp = favfoodJson.getJSONObject(i);
            Details te = new Details(temp.getString("details"),this);
            te.toGetDetails(i+1);
            favfood.add(te);
        }
    }

    @Override
    public void onConnDone(int responseCode, String message, int requestCode) {
        switch (requestCode) {
            case LOGIN:
                if(responseCode==200){
                    Message msg = Message.obtain();
                    msg.what = LOGIN_DONE;
                    msg.obj=message;
                    mHandler.sendMessage(msg);
                }
                break;
            case GET_FAV:
                if(responseCode==200){
                    Message msg = Message.obtain();
                    msg.what = GET_FAV_DONE;
                    msg.obj=message;
                    mHandler.sendMessage(msg);
                }
                break;
            case DELETE_FAV:
                if(responseCode==200){
                    Message msg = Message.obtain();
                    msg.what = DELETE_FAV_DONE;
                    msg.obj=message;
                    mHandler.sendMessage(msg);
                }
                break;
            default:
                break;
        }
    }

    private void checkLogin(String msg) {
        JSONObject res = JSONObject.parseObject(msg);
        if(null == res.getString("token")){
            Toast.makeText(UserActivity.this,"登录过期",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserActivity.this,Login.class);
            this.startActivity(intent);
            finish();
        }else {
            token = res.getString("token");
            initUser();
            getFav();
        }
    }

    private void initUser() {
        mUser.setName(user.getString("nickName",""));
        username.setText(mUser.getName());
        mUser.setSex(user.getInt("sex",0));
        mUser.setAge(user.getInt("age",18));
        mUser.setHigh(user.getFloat("high",175));
        mUser.setWeight(user.getFloat("weight",65));
        mUser.setIsHighFat(user.getInt("isHighFat",0));
        mUser.setIsHighSugar(user.getInt("isHighSugar",0));
        SharedPreferences.Editor editor = user.edit();
        editor.putString("token",token);
        editor.apply();
        if(mUser.getSex() == 0)
            suggestCal = (660 + 1.38 * mUser.getWeight() + 5 * mUser.getHigh() -
                    6.8 * 35) * 1.2;
        else if(mUser.getSex() == 1)
            suggestCal = (655 + 9.6 * mUser.getWeight() + 1.9 * mUser.getHigh() -
                    4.7 * 35) * 1.2;
    }

    @Override
    public void getDone(int flag) {
        Message msg = Message.obtain();
        msg.what = GET_DETAILS_DONE;
        msg.arg1 = flag;
        mHandler.sendMessage(msg);
    }

    @Override
    public void getErroer(String error) {
        Message msg = Message.obtain();
        msg.what = GET_DETAILS_ERROR;
        msg.obj = "网络异常";
        mHandler.sendMessage(msg);
    }
}
