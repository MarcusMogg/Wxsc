package test.com.fg.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.fg.activity.R;

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
public class ResultActivity extends AppCompatActivity implements View.OnClickListener, HttpUtils.HttpListener {

    private static final int GET_DETAILS_SUCCEED = 101;
    private static final int GET_DETAILS_ERROR = 102;
    private static final int ADD_FAV_SUCCEED = 1;
    private static final int ADD_FAV_ERROR = 2;
    private static final int LOGIN_USER = 10001;
    private static final int ADD_LOGIN_SUCCEED = 3;

    private ImageView foodImage;
    private ImageView star;
    private TextView TVfoodName;
    private TextView EngName;
    private TextView briefIntroduction;
    private TextView T;
    private Button cookBook;
    private Button dianP;
    private Intent mIntent;
    private ProgressDialog mProgressDialog;
    private Details mDetails;
    private boolean isEng;
    private boolean isFood = true;
    private JSONObject baike_info;
    private String[] TagsArray;
    private User mUser = new User();
    private boolean isLogin = false;
    private SharedPreferences user;
    private HttpUtils mHttpUtils;
    private String token;
    private String picPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_result);
//        if(getSupportActionBar()!=null)
//        {
//            getSupportActionBar().hide();;
//            getWindow().setFlags(
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN
//            );
//        }
        super.onCreate(savedInstanceState);
        initView();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cookBook:
                if(isEng)
                    showCookBook();
                else{
                    if(isFood)
                        showBaike();
                    else
                        Toast.makeText(this,"没有识别出菜品",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.dianP:
                if(isFood)
                    showDianPing();
                else
                    Toast.makeText(this,"没有识别出菜品",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        initUser();
        super.onResume();
    }

    private void showBaike() {
        Intent intent = new Intent(this,BaikeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("URL",baike_info.getString("baike_url"));
        intent.putExtras(bundle);
        this.startActivity(intent);
    }

    @SuppressLint({"ResourceAsColor", "NewApi"})
    private void creatText(String str, int color) {
        LinearLayout tipLinear=(LinearLayout) findViewById(R.id.Tips);
        TextView tipNow=new TextView(this);
        tipNow.setPadding(10,2,10,2);
        tipNow.setText(str);
        tipNow.setTextSize(15);
        tipNow.setBackgroundColor(getResources().getColor(color));
        tipNow.setTextColor(getResources().getColor(R.color.black));
        tipLinear.addView(tipNow);
    }

    private void initView(){
        mHttpUtils = HttpUtils.getInstance();
        mHttpUtils.setHttpListener(this);
        star = this.findViewById(R.id.iv_right_image_two);
        mIntent = this.getIntent();
        isEng = mIntent.getBooleanExtra("isEng",true);
        cookBook = this.findViewById(R.id.cookBook);
        dianP = this.findViewById(R.id.dianP);
        cookBook.setOnClickListener(this);
        if(!isEng)
            star.setImageDrawable(null);
        star.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isEng)
                    mUser.to_login(ResultActivity.this,LOGIN_USER);
            }
        });

        dianP.setOnClickListener(this);
        foodImage = this.findViewById(R.id.foodImage);
        TVfoodName = this.findViewById(R.id.result);
        EngName = this.findViewById(R.id.EngName);
        briefIntroduction = this.findViewById(R.id.briefIntroduction);
        mProgressDialog = new ProgressDialog(this);
        picPath = mIntent.getStringExtra("ImagePath");
        Bitmap foodBitmap = BitmapFactory.decodeFile(picPath);
        foodImage.setImageBitmap(foodBitmap);
        if(isEng){
            mDetails = new Details(mIntent.getStringExtra("details"),
                    new Details.getDetailsListener() {
                        @Override
                        public void getDone(int flag) {
                            Message msg = Message.obtain();
                            msg.what = GET_DETAILS_SUCCEED;
                            handler.sendMessage(msg);
                        }

                        @Override
                        public void getErroer(String error) {
                            Message msg = Message.obtain();
                            msg.what = GET_DETAILS_ERROR;
                            msg.obj = "网络异常";
                            handler.sendMessage(msg);
                        }
                    });
            mDetails.setImagesUrlPath("Image/" + mIntent.getStringExtra("name") + "/");
            mDetails.setCookBookUrl(mIntent.getStringExtra("cookBook"));
            toGetDetails();
        }else {
            showCh();
        }
        initUser();
    }

    private void initUser(){
        user = getSharedPreferences("user",0);
        String email = user.getString("email","");
        if(!"".equals(email)){
            isLogin = true;
            System.out.println(email);
            mUser.setEmail(email);
            mUser.setPwd(user.getString("pwd",""));
            mUser.setName(user.getString("nickName",""));
            mUser.setSex(user.getInt("sex",0));
            mUser.setAge(user.getInt("age",18));
            mUser.setHigh(user.getFloat("high",175));
            mUser.setWeight(user.getFloat("weight",65));
            mUser.setIsHighFat(user.getInt("isHighFat",0));
            mUser.setIsHighSugar(user.getInt("isHighSugar",0));
            token = user.getString("token","");
        }
    }

    private void showCh() {
        baike_info = JSONObject.parseObject(mIntent.getStringExtra("baike_info"));
        briefIntroduction.setText(baike_info.getString("description"));
        if(mIntent.getStringExtra("name").equals("非菜"))
        {
            isFood = false;
            TVfoodName.setText("没有识别出菜品");
        }else {
            TVfoodName.setText(mIntent.getStringExtra("name"));
        }
        cookBook.setText("百度百科");
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_DETAILS_SUCCEED:
                    showDetails();
                    break;
                case GET_DETAILS_ERROR:
                    errorToast((String) msg.obj);
                    break;
                case ADD_FAV_SUCCEED:
                    tianjiaCG();
                    break;
                case ADD_FAV_ERROR:
                    Toast.makeText(ResultActivity.this,"添加失败",Toast.LENGTH_SHORT).show();
                    break;
                case ADD_LOGIN_SUCCEED:
                    loginEnd((String) msg.obj);
                    break;
                default:
                        break;
            }
            super.handleMessage(msg);
        }
    };

    private void tianjiaCG() {
        Toast.makeText(ResultActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = user.edit();
        editor.putString(mDetails.getEnglishName(),picPath);
        editor.apply();
    }

    private void loginEnd(String msg) {
        JSONObject res = JSONObject.parseObject(msg);
        if(null == res.getString("token")){
            Toast.makeText(ResultActivity.this,"登录过期",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ResultActivity.this,Login.class);
            this.startActivity(intent);
        }else {
            token = res.getString("token");
            HashMap<String, String> p = new HashMap<>();
            p.put("name",mDetails.getEnglishName());
            mHttpUtils.postParams(token,Configuration.HOST+Configuration.FAVFOOD,p,0);
        }
    }

    private void errorToast(String error) {
        mProgressDialog.dismiss();
        Toast.makeText(this,error,Toast.LENGTH_LONG).show();
        cookBook.setText("百度百科");
    }

    private void showDetails() {
        int suggestFlag = 0;
        mProgressDialog.dismiss();
        TVfoodName.setText(mDetails.getChineseName());
        EngName.setText(mDetails.getEnglishName());
        TagsArray = mDetails.getTagArray();
        for(String temp:TagsArray){
            creatText(temp,R.color.fab);
        }
        if(mDetails.getSugar() < 5) {
            creatText("低糖", R.color.green);
            if(isLogin){
                if(mUser.getIsHighSugar() == 1){
                    suggestFlag = 1;
                }
            }
        }
        if(mDetails.getSugar() > 30) {
            creatText("高糖", R.color.red);
            if(isLogin){
                if(mUser.getIsHighSugar() == 1){
                    suggestFlag = 2;
                }
            }
        }
        if(mDetails.getFat() < 10) {
            creatText("低脂", R.color.green);
            if(isLogin){
                if(mUser.getIsHighFat() == 1){
                    suggestFlag = 1;
                }
            }
        }
        if(mDetails.getSugar() > 30) {
            creatText("高脂", R.color.red);
            if(isLogin){
                if(mUser.getIsHighFat() == 1){
                    suggestFlag = 2;
                }
            }
        }
        if(suggestFlag == 1)
            creatText("建议食用",R.color.green);
        else if(suggestFlag == 2)
            creatText("不建议食用",R.color.red);
        StringBuilder sb = new StringBuilder();
        for(String para:mDetails.getProfile()){
            sb.append(para).append("\n");
        }
        briefIntroduction.setText(sb.toString());
    }

    private void toGetDetails() {
        mProgressDialog.setMessage("正在获取相关信息");
        mProgressDialog.show();
        mDetails.toGetDetails(0);
    }

    private void showCookBook(){
        Intent intent = new Intent(this,MenuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("CookBookUrl",mIntent.getStringExtra("cookBook"));
        bundle.putInt("step",mDetails.getStep());
        bundle.putStringArray("Ingredient",mDetails.getIngredient());
        bundle.putString("Tips",mDetails.getTips());
        intent.putExtras(bundle);
        this.startActivity(intent);
    }
    private void showDianPing() {
        Intent intent = new Intent(this,DianpingActivity.class);
        Bundle bundle = new Bundle();
        if(isEng){
            bundle.putString("URL",mDetails.getChineseName());
        }else {
            bundle.putString("URL",mIntent.getStringExtra("name"));
        }
        intent.putExtras(bundle);
        this.startActivity(intent);
    }

    @Override
    public void onConnDone(int responseCode, String message, int requestCode) {
        switch (requestCode) {
            case 0:
                if(responseCode==200){
                    Message msg = Message.obtain();
                    msg.what = ADD_FAV_SUCCEED;
                    msg.obj = message;
                    handler.sendMessage(msg);
                }else {
                    Message msg = Message.obtain();
                    msg.what = ADD_FAV_ERROR;
                    msg.obj = message;
                    handler.sendMessage(msg);
                }
                break;
            case LOGIN_USER:
                if(responseCode == 200){
                    Message msg = Message.obtain();
                    msg.what = ADD_LOGIN_SUCCEED;
                    msg.obj = message;
                    handler.sendMessage(msg);
                }else {
                    Message msg = Message.obtain();
                    msg.what = ADD_FAV_ERROR;
                    msg.obj = message;
                    handler.sendMessage(msg);
                }
                break;
            default:
                break;
        }
    }
}
