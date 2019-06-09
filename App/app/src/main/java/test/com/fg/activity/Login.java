package test.com.fg.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.fg.activity.R;

import java.util.HashMap;

import test.com.fg.util.Configuration;
import test.com.fg.util.HttpUtils;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class Login extends Activity implements View.OnClickListener,HttpUtils.HttpListener {

    private final int LOGIN_SUCCESS = 1;
    private final int LOGIN_ERROR = 2;
    private final int GETALL_SUCCESS = 3;
    private final int GETALL_ERROR = 4;

    private final int LOGIN_RQS = 101;
    private final int GETALL_RQS = 102;

    private EditText username, password;
    private Button login, register;
    private SharedPreferences mSharedPreferences;
    private String useremail;
    private String pwd;
    private HttpUtils httpUtils;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initView();
    }

    @SuppressLint("WorldReadableFiles")
    private void initView(){
        httpUtils = HttpUtils.getInstance();
        httpUtils.setHttpListener(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        mSharedPreferences = getSharedPreferences("user",Context.MODE_WORLD_READABLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login:
                login();
                break;
            case R.id.register:
                toRegister();
                break;
            default:
                break;
        }
    }

    private void toRegister() {
        Intent intent = new Intent(Login.this,RegisterActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    private void login() {
        useremail = username.getText().toString();
        pwd = password.getText().toString();
        if(check(useremail,pwd)){
            HashMap<String,String> loginInfo = new HashMap<>();
            loginInfo.put("Email",useremail);
            loginInfo.put("Password",pwd);
            loginInfo.put("RememberMe","true");
            httpUtils.postParams(Configuration.HOST+Configuration.LOGIN,loginInfo,LOGIN_RQS);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    Toast.makeText(Login.this,"正在获取信息",Toast.LENGTH_SHORT).show();
                    getAll((String)msg.obj);
                    break;
                case LOGIN_ERROR:
                    Toast.makeText(Login.this,"登录失败",Toast.LENGTH_SHORT).show();
                    break;
                case GETALL_SUCCESS:
                    Toast.makeText(Login.this,"登录成功",Toast.LENGTH_SHORT).show();
                    saveUser((String) msg.obj);
                    break;
                case GETALL_ERROR:
                    Toast.makeText(Login.this,"获取信息错误",Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    private void getAll(String obj) {
        JSONObject res = JSONObject.parseObject(obj);
        token = res.getString("token");
        httpUtils.get(token,Configuration.HOST+Configuration.GETALL,GETALL_RQS);
    }




    /**
     * 检查信息
     */
    private boolean check(String email,String pwd){
        boolean res = true;
        if(!checkEmail(email)){
            Toast.makeText(this,"邮箱格式错误",Toast.LENGTH_SHORT).show();
            res = false;
        }
        if("".equals(pwd)||null == pwd){
            Toast.makeText(this,"密码不能为空",Toast.LENGTH_SHORT).show();
            res = false;
        }
        return res;
    }

    @Override
    public void onConnDone(int responseCode, String message, int requestCode) {
        switch (requestCode) {
            case LOGIN_RQS:
                if (responseCode == 200){
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
            case GETALL_RQS:
                if (responseCode == 200){
                    Message msg = Message.obtain();
                    msg.what = GETALL_SUCCESS;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }else {
                    Message msg = Message.obtain();
                    msg.what = GETALL_ERROR;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
                break;
            default:
                break;
        }
    }

    private void saveUser(String message) {
        JSONObject temp = JSONObject.parseObject(message);
        JSONObject jsonObject = temp.getJSONObject("user");
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("email",useremail);
        editor.putString("pwd",pwd);
        editor.putString("nickName",jsonObject.getString("nickName"));
        editor.putInt("age",jsonObject.getInteger("age"));
        editor.putInt("sex",jsonObject.getInteger("sex"));
        editor.putFloat("high",jsonObject.getFloat("high"));
        editor.putFloat("weight",jsonObject.getFloat("weight"));
        editor.putInt("isHighSugar",jsonObject.getInteger("isHighSugar"));
        editor.putInt("isHighFat",jsonObject.getInteger("isHighFat"));
        editor.putString("token",token);
        editor.apply();
        loginSuccess();
    }

    private void loginSuccess() {
        this.finish();
    }
    private boolean checkEmail(String str) {
        String regex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return str.matches(regex);
    }
}

