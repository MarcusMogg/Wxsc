package test.com.fg.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.fg.activity.R;

import java.util.HashMap;

import test.com.fg.Classes.User;
import test.com.fg.util.Configuration;
import test.com.fg.util.HttpUtils;

@SuppressLint("Registered")
public class RegisterActivity extends Activity implements View.OnClickListener ,HttpUtils.HttpListener {

    private final int R_SUCCESS = 1;
    private final int R_ERROR = 2;
    private final int SET_SUCCESS = 3;
    private final int SET_ERROR = 4;
    private final int LOGIN_SUCCESS = 5;
    private final int LOGIN_ERROR = 6;

    private final int LOGIN_RQS = 101;
    private final int R_RQS = 1001;
    private final int SET_RQS = 1002;

    private String[] sex = {"男","女"};
    private String[] sug = {"正常","高糖"};
    private String[] fat = {"正常","高脂"};
    private User mUser = new User();
    private String token;
    private HttpUtils mHttpUtils;
    private SharedPreferences mSharedPreferences;

    private EditText username,password,re_password,email,height,weight;
    private Button bt_sex,bt_sugar,bt_fat;
    private Button bt_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    @SuppressLint("WorldReadableFiles")
    private void initView() {
        mSharedPreferences = getSharedPreferences("user",Context.MODE_WORLD_READABLE);
        mHttpUtils = HttpUtils.getInstance();
        mHttpUtils.setHttpListener(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        re_password = findViewById(R.id.password_repeat);
        email = findViewById(R.id.email);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        bt_fat = findViewById(R.id.fat);
        bt_sex = findViewById(R.id.sex);
        bt_sugar = findViewById(R.id.sugar);
        bt_ok = findViewById(R.id.ok);
        bt_sugar.setOnClickListener(this);
        bt_sex.setOnClickListener(this);
        bt_fat.setOnClickListener(this);
        bt_ok.setOnClickListener(this);
    }


    public void dialog_show(String[] item,DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择");
        builder.setItems(item,listener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sex:
                dialog_show(sex,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bt_sex.setText(sex[which]);
                        mUser.setSex(which);
                    }
                });
                break;
            case R.id.sugar:
                dialog_show(sug,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bt_sugar.setText(sug[which]);
                        mUser.setIsHighSugar(which);
                    }
                });
                break;
            case R.id.fat:
                dialog_show(fat,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bt_fat.setText(fat[which]);
                        mUser.setIsHighFat(which);
                    }
                });
                break;
            case R.id.ok:
                startRegister();
            default:
                break;
        }
    }

    @Override
    public void onConnDone(int responseCode, String message, int requestCode) {
        System.out.println(message);
        switch (requestCode){
            case R_RQS:
                if(responseCode == 200){
                    Message msg = Message.obtain();
                    msg.what = R_SUCCESS;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }else {
                    Message msg = Message.obtain();
                    msg.what = R_ERROR;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
                break;
            case SET_RQS:
                if(responseCode == 200){
                    Message msg = Message.obtain();
                    msg.what = SET_SUCCESS;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }else {
                    Message msg = Message.obtain();
                    msg.what = SET_ERROR;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
                break;
            case LOGIN_RQS:
                if(responseCode == 200){
                    Message msg = Message.obtain();
                    msg.what = LOGIN_SUCCESS;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }else {
                    Message msg = Message.obtain();
                    msg.what = LOGIN_ERROR;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
                break;
            default:
                break;
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case R_SUCCESS:
                    checkSuccess((String)msg.obj);
                    break;
                case R_ERROR:
                    showR_error((String) msg.obj);
                    break;
                case LOGIN_SUCCESS:
                    setAll((String) msg.obj);
                    break;
                case LOGIN_ERROR:
                    Toast.makeText(RegisterActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
                    break;
                case SET_SUCCESS:
                    setSuccess();
                    break;
                case SET_ERROR:
                    Toast.makeText(RegisterActivity.this,"设置信息失败",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void checkSuccess(String obj) {
        //Log.e("obj",obj);
        JSONObject jsonObject = JSONObject.parseObject(obj);
        //Log.e("request",jsonObject.getJSONObject("request").toString());
        JSONObject temp = jsonObject.getJSONObject("request");
        //Log.e("####info:",temp.getString("info"));
        if("Success".equals(temp.getString("info"))){
            mUser.to_login(RegisterActivity.this,LOGIN_RQS);
        }else {
            Toast.makeText(RegisterActivity.this,temp.getString("info"),Toast.LENGTH_SHORT).show();
        }
    }

    private void showR_error(String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        JSONObject temp = jsonObject.getJSONObject("request");
        Toast.makeText(RegisterActivity.this,temp.getString("info"),Toast.LENGTH_SHORT).show();
    }

    private void setSuccess() {
        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("email",mUser.getEmail());
        editor.putString("pwd",mUser.getPwd());
        editor.putString("nickName",mUser.getName());
        editor.putInt("age",mUser.getAge());
        editor.putInt("sex",mUser.getSex());
        editor.putFloat("high", (float) mUser.getHigh());
        editor.putFloat("weight", (float) mUser.getWeight());
        editor.putInt("isHighSugar",mUser.getIsHighSugar());
        editor.putInt("isHighFat",mUser.getIsHighFat());
        editor.putString("token",token);
        editor.apply();
        //Intent intent = new Intent(RegisterActivity.this,UserActivity.class);
        finish();
    }

    private void setAll(String msg) {
        JSONObject res = JSONObject.parseObject(msg);
        token = res.getString("token");
        HashMap<String,String> params = mUser.getParams();
        mHttpUtils.postParams(token,Configuration.HOST+Configuration.SETALL,params,SET_RQS);
    }

    private void startRegister() {
        if(check()){
            HashMap<String,String> params = mUser.getParamsRegister();
            mHttpUtils.postParams(Configuration.HOST+Configuration.REGISTER,params,R_RQS);
        }
    }

    private boolean check(){
        String pwd = password.getText().toString();
        if("".equals(username.getText().toString())){
            Toast.makeText(RegisterActivity.this,"用户名不为空",Toast.LENGTH_SHORT).show();
            return false;
        }else if(!checkPwd(pwd)){
            Toast.makeText(RegisterActivity.this,"密码不少于6位，且同时包含数字和字母",Toast.LENGTH_SHORT).show();
            return false;
        }else  if(!pwd.equals(re_password.getText().toString())){
            Toast.makeText(RegisterActivity.this,"两次输入密码不一致",Toast.LENGTH_SHORT).show();
            return false;
        }else if(!checkEmail(email.getText().toString())) {
            Toast.makeText(RegisterActivity.this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            String hi = height.getText().toString();
            String we = weight.getText().toString();
            double h,w;
            if(!"".equals(hi) && !"".equals(we)) {
                h = Double.parseDouble(hi);
                w = Double.parseDouble(we);
                if(h<0||h>250||w<0||w>500){
                    Toast.makeText(RegisterActivity.this, "请输入正确身高体重", Toast.LENGTH_SHORT).show();
                    return false;
                }else {
                    mUser.setName(username.getText().toString());
                    mUser.setPwd(pwd);
                    mUser.setEmail(email.getText().toString());
                    mUser.setHigh(h);
                    mUser.setWeight(w);
                }
            }else {
                Toast.makeText(RegisterActivity.this, "请输入正确身高体重", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private boolean checkPwd(String str) {
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
        return str.matches(regex);
    }

    private boolean checkEmail(String str) {
        String regex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return str.matches(regex);
    }



}