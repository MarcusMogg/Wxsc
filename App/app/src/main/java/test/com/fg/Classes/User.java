package test.com.fg.Classes;

import com.alibaba.fastjson.JSONObject;

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
public class User {
    private String name;
    private String email;
    private String pwd;
    private int age = 18;
    private double high;
    private double weight;
    private int sex = 0; //0：男 1：女
    private int isHighSugar = 0;//0：不是 1：是
    private int isHighFat = 0;//0：不是 1：是

    public User(){}

    public User(String name, String email, String pwd) {
        this.name = name;
        this.email = email;
        this.pwd = pwd;
    }

    public User(String name, String email, String pwd, int age, double high, double weight, int sex, int isHighSugar, int isHighFat) {
        this.name = name;
        this.email = email;
        this.pwd = pwd;
        this.age = age;
        this.high = high;
        this.weight = weight;
        this.sex = sex;
        this.isHighSugar = isHighSugar;
        this.isHighFat = isHighFat;
    }

    public HashMap<String,String> getParams(){
        HashMap<String,String> pramas = new HashMap<>();
        pramas.put("NickName",name);
        pramas.put("Age",age+"");
        pramas.put("High",high+"");
        pramas.put("Weight",weight+"");
        pramas.put("Sex",sex+"");
        pramas.put("IsHighSugar",isHighSugar+"");
        pramas.put("IsHighFat",isHighFat+"");
        return pramas;
    }

    public void setAll(String jsonString){
        JSONObject jsob = JSONObject.parseObject(jsonString);
        this.name = jsob.getString("name");
        this.email = jsob.getString("email");
    }

    public HashMap<String,String> getParamsRegister(){
        HashMap<String,String> pramas = new HashMap<>();
        pramas.put("UserName",name);
        pramas.put("Email",email);
        pramas.put("Password",pwd);
        pramas.put("ConfirmPassword",pwd);
        return pramas;
    }

    public void to_login(HttpUtils.HttpListener listener,int requestCode){
        HttpUtils httpUtils = HttpUtils.getInstance();
        httpUtils.setHttpListener(listener);
        HashMap<String,String> loginInfo = new HashMap<>();
        loginInfo.put("Email",email);
        loginInfo.put("Password",pwd);
        loginInfo.put("RememberMe","true");
        httpUtils.postParams(Configuration.HOST+Configuration.LOGIN,loginInfo,requestCode);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getIsHighSugar() {
        return isHighSugar;
    }

    public void setIsHighSugar(int isHighSugar) {
        this.isHighSugar = isHighSugar;
    }

    public int getIsHighFat() {
        return isHighFat;
    }

    public void setIsHighFat(int isHighFat) {
        this.isHighFat = isHighFat;
    }
}
