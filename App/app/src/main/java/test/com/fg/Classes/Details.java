package test.com.fg.Classes;

import android.os.Parcel;
import android.os.Parcelable;

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
public class Details implements Parcelable {
    private String url;
    private String ChineseName;
    private String EnglishName;
    private String Tag;
    private String[] Profile;
    private String[] Ingredient;
    private int step;
    private String Tips;
    private String[] tagArray;
    private String cookBookUrl;
    private String imagesUrlPath;
    private double cal;
    private double sugar;
    private double fat;
    private getDetailsListener listener;



    public Details(String url, getDetailsListener listener){
        this.url = url;
        this.listener = listener;
    }

    public void toGetDetails(final int flag){
        if(url != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doGetDetails(flag);
                }
            }).start();
        }else {
            listener.getErroer("上传链接错误");
        }
    }

    private void doGetDetails(int flag){
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
            listener.getDone(flag);
        }catch (IOException e) {
            listener.getErroer("网络异常");
        }
    }

    private void parserJSON(String jsonString){
        JSONArray temp = JSONObject.parseArray(jsonString);
        System.out.println(jsonString);
        JSONObject data = temp.getJSONObject(0);
        this.ChineseName = data.getString("Chinese Name");
        this.EnglishName = data.getString("English Name");
        this.Tag =data.getString("Tag");
        this.cal = data.getDouble("Cal");
        this.sugar = data.getDouble("Sugar");
        this.fat = data.getDouble("Fat");
        this.step = data.getInteger("Step");
        this.Ingredient = data.getString("Ingredient").split("<br>");
        this.Tips = data.getString("Tips");
        if("0".equals(Tag)){
            tagArray = null;
        }else {
            this.tagArray = Tag.split("##");
        }
        String[] profile = data.getString("Profile").split("<br>");
        int i = 0;
        for(String para:profile) {
            this.Profile = profile.clone();
            this.Profile[i]= "\u3000\u3000" + para;
            i++;
        }
        temp.clear();
    }

    public String getChineseName() {
        return ChineseName;
    }

    public void setChineseName(String chineseName) {
        ChineseName = chineseName;
    }

    public String getEnglishName() {
        return EnglishName;
    }

    public void setEnglishName(String englishName) {
        EnglishName = englishName;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }

    public double getCal() {
        return cal;
    }

    public void setCal(double cal) {
        this.cal = cal;
    }

    public double getSugar() {
        return sugar;
    }

    public void setSugar(double sugar) {
        this.sugar = sugar;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getImagesUrlPath() {
        return imagesUrlPath;
    }

    public void setImagesUrlPath(String imagesUrlPath) {
        this.imagesUrlPath = imagesUrlPath;
    }

    public String[] getProfile() {
        return Profile;
    }

    public void setProfile(String[] profile) {
        Profile = profile;
    }

    public String[] getIngredient() {
        return Ingredient;
    }

    public void setIngredient(String[] ingredient) {
        Ingredient = ingredient;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String[] getTagArray() {
        return tagArray;
    }

    public void setTagArray(String[] tagArray) {
        this.tagArray = tagArray;
    }

    public String getCookBookUrl() {
        return cookBookUrl;
    }

    public void setCookBookUrl(String cookBookUrl) {
        this.cookBookUrl = cookBookUrl;
    }

    public String getTips() {
        return Tips;
    }

    public void setTips(String tips) {
        Tips = tips;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(ChineseName);
        dest.writeString(EnglishName);
        dest.writeString(Tag);
        dest.writeStringArray(Profile);
        dest.writeStringArray(Ingredient);
        dest.writeInt(step);
        dest.writeString(Tips);
        dest.writeString(cookBookUrl);
        dest.writeString(imagesUrlPath);
    }

    public static final Creator<Details> CREATOR = new Creator<Details>() {
        @Override
        public Details createFromParcel(Parcel in) {
            return new Details(in);
        }

        @Override
        public Details[] newArray(int size) {
            return new Details[size];
        }
    };

    protected Details(Parcel in) {
        url = in.readString();
        ChineseName = in.readString();
        EnglishName = in.readString();
        Tag = in.readString();
        Profile = in.createStringArray();
        Ingredient = in.createStringArray();
        step = in.readInt();
        Tips = in.readString();
        cookBookUrl = in.readString();
        imagesUrlPath = in.readString();
    }


    public  interface getDetailsListener{

        void getDone(int flag);

        void getErroer(String error);

    }

}