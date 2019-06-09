package test.com.fg.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.imageclassify.AipImageClassify;
import com.fg.activity.R;

import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import test.com.fg.util.Configuration;
import test.com.fg.util.UploadUtil;
import test.com.fg.util.UploadUtil.OnUploadProcessListener;

import static test.com.fg.util.Configuration.API_KEY;
import static test.com.fg.util.Configuration.APP_ID;
import static test.com.fg.util.Configuration.SECRET_KEY;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class MainActivity extends AppCompatActivity implements OnClickListener,OnUploadProcessListener{
	private static final String TAG = "uploadImage";
	
	/**
	 * 去上传文件
	 */
	protected static final int TO_UPLOAD_FILE = 1;  
	/**
	 * 上传文件响应
	 */
	protected static final int UPLOAD_FILE_DONE = 2;
	/**
	 * 选择文件
	 */
	public static final int TO_SELECT_PHOTO = 3;
	/**
	 * 上传初始化
	 */
	private static final int UPLOAD_INIT_PROCESS = 4;
	/**
	 * 上传中
	 */
	private static final int UPLOAD_IN_PROCESS = 5;

	/**
	 * 成功获取结果
	 */
	private static final int GET_RESULT_SUCCEED =6 ;

	private static final int CONNECT_FAILED =7 ;


	private static final int REQUEST_TAKE_PHOTO = 0;// 拍照
	private static final int REQUEST_CROP = 1;// 裁剪
	private static final int SCAN_OPEN_PHONE = 2;// 相册
	private Uri imgUri; // 拍照时返回的uri
	private Uri mCutUri;// 图片裁剪时返回的uri

	private static final int REQUEST_PERMISSION = 100;
	/***
	 * 这里的这个URL是我服务器的javaEE环境URL
	 */

	private static String requestURL = Configuration.HOST+"api/ident";
	private Button takeButton,uploadButton,pickButton;
	private Switch dishSwitch;
	private ImageView imageView;
    private TextView recText, swText;
    private FloatingActionButton faButton;

	private String picPath = null;
	private ProgressDialog progressDialog;
	//private Uri photoUri;
	private boolean time = true;
	private AipImageClassify client;
	private boolean hasPermission = false;
	private File imgFile;
	private long exitTime = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_RIGHT_ICON);
		getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.account);
        setContentView(R.layout.fab);
//		android.support.v7.app.ActionBar actionBar=getSupportActionBar();
//		if(actionBar!=null)
//		{
//			actionBar.hide();
//		}
        initView();
    }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "再按一次退出程序",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
		}
	}
    /**
     * 初始化数据
     */
	private void initView() {
        takeButton = this.findViewById(R.id.fab_take_photo);
		pickButton = this.findViewById(R.id.fab_pick_photo);
        uploadButton = this.findViewById(R.id.uploadImage);
        dishSwitch = this.findViewById(R.id.switchDish);
        recText = this.findViewById(R.id.title_text);
        swText = this.findViewById(R.id.textView);
        faButton = this.findViewById(R.id.FAButton);
		checkPermissions();
        takeButton.setOnClickListener(this);
        pickButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        dishSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@SuppressLint("ResourceAsColor")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ColorStateList color_china = recText.getContext()
						.getResources()
						.getColorStateList(R.color.fab_china);
				ColorStateList color_english = recText.getContext()
						.getResources()
						.getColorStateList(R.color.fab);
				if(isChecked){
					faButton.setImageResource(R.drawable.photo_c);
					faButton.setBackgroundTintList(color_china);
					recText.setTextColor(color_china);
					recText.setText("中餐识别");
					swText.setText("中餐         ");
					time = false;
				}else{
					faButton.setImageResource(R.drawable.photo_b);
					faButton.setBackgroundTintList(color_english);
					recText.setTextColor(color_english);
					recText.setText("西餐识别");
					swText.setText("         西餐");
					time = true;
				}
			}
		});
        imageView = this.findViewById(R.id.imageView);
        progressDialog = new ProgressDialog(this);
		client = new AipImageClassify(APP_ID, API_KEY, SECRET_KEY);
	}

	private void checkPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// 检查是否有存储和拍照权限
			if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
					&& checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
					) {
				hasPermission = true;
			} else {
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				hasPermission = true;
			} else {
				Toast.makeText(this, "权限授予失败！", Toast.LENGTH_SHORT).show();
				hasPermission = false;
			}
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fab_take_photo:
			if(hasPermission){
				takePhoto();
			}else {
				checkPermissions();
			}
			break;
		case R.id.fab_pick_photo:
			if(hasPermission){
				//pickPhoto();
				openGallery();
			}else {
				checkPermissions();
			}
			break;
		case R.id.uploadImage:
			if(picPath!=null)
			{
				if(time){
					handler.sendEmptyMessage(TO_UPLOAD_FILE);
				}else {
					Chinesefood();
				}
			}else{
				Toast.makeText(this, "上传的文件路径出错", Toast.LENGTH_LONG).show();
			}
			break;
		//case
		default:
			break;
		}
	}

	private void Chinesefood() {
		progressDialog.setMessage("正在识别...");
		progressDialog.show();
		final HashMap<String, String> options = new HashMap<String, String>();
		options.put("top_num", "1");
		options.put("filter_threshold", "0.7");
		options.put("baike_num", "1");
		new Thread(new Runnable() {
			@Override
			public void run() {
				org.json.JSONObject resu = client.dishDetect(picPath, options);
				JSONObject res = null;
				try {
					res = JSONObject.parseObject(resu.getJSONArray("result").get(0).toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Bundle bundle = new Bundle();
				bundle.putBoolean("isEng",time);
				assert res != null;
				bundle.putString("name",res.getString("name"));
				bundle.putString("baike_info",res.getString("baike_info"));
				bundle.putString("ImagePath",picPath);
				getResultSucceed(bundle);
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				// 拍照并进行裁剪
				case REQUEST_TAKE_PHOTO:
					Log.e(TAG, "onActivityResult: imgUri:REQUEST_TAKE_PHOTO:" + imgUri.toString());
					cropPhoto(imgUri, true);
					break;

				// 裁剪后设置图片
				case REQUEST_CROP:
					//imageView.setImageURI(mCutUri);
					picPath = getRealFilePath(this,mCutUri);
					Log.e(TAG, "onActivityResult: imgUri:REQUEST_CROP:" + mCutUri.toString());
					showPicPhoto();
					break;
				// 打开图库获取图片并进行裁剪
				case SCAN_OPEN_PHONE:
					Log.e(TAG, "onActivityResult: SCAN_OPEN_PHONE:" + data.getData().toString());
					cropPhoto(data.getData(), false);
					break;
			}
		}
	}


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
	/**
	 * 上传服务器响应回调
	 */
	@Override
	public void onUploadDone(int responseCode, String message) {
		progressDialog.dismiss();
		Message msg = Message.obtain();
		msg.what = UPLOAD_FILE_DONE;
		msg.arg1 = responseCode;
		msg.obj = message;
		handler.sendMessage(msg);
	}




	private void showPicPhoto()
    {
        Log.i(TAG, "最终选择的图片="+picPath);
        Bitmap bm = BitmapFactory.decodeFile(picPath);
        imageView.setImageBitmap(bm);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        imageView.setMaxWidth(bm.getWidth()*8);
//		imageView.setMaxHeight(bm.getHeight()*8);
    }
	private void toUploadFile()
	{
		progressDialog.setMessage("正在识别...");
		progressDialog.show();

		String fileKey = "file";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this);  //设置监听器监听上传状态
		uploadUtil.uploadFile( picPath,fileKey, requestURL,null);
	}

	private void getResult(final String result){

		progressDialog.setMessage("正在获取菜品信息...");
		progressDialog.show();
		JSONObject temp = JSONObject.parseObject(result);
		JSONObject res = JSONObject.parseObject(temp.getString("item"));
		Bundle bundle = new Bundle();
		bundle.putBoolean("isEng",time);
		bundle.putString("name",res.getString("name"));
		bundle.putString("cookBook",res.getString("cookBook"));
		bundle.putString("details",res.getString("details"));
		bundle.putString("ImagePath",picPath);
		getResultSucceed(bundle);
	}

	/**
	 * 成功获取识别结果
	 */
	public void getResultSucceed(Bundle b){
		progressDialog.dismiss();
		Message msg = Message.obtain();
		msg.what = GET_RESULT_SUCCEED;
		msg.obj = b;
		handler.sendMessage(msg);
	}


	@Override
	public void onUploadProcess(int uploadSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_IN_PROCESS;
		msg.arg1 = uploadSize;
		handler.sendMessage(msg);
	}

	@Override
	public void initUpload(int fileSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_INIT_PROCESS;
		msg.arg1 = fileSize;
		handler.sendMessage(msg );
	}



	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TO_UPLOAD_FILE:
				toUploadFile();
				break;

			case UPLOAD_INIT_PROCESS:
				break;

			case UPLOAD_IN_PROCESS:
				break;

			case CONNECT_FAILED:
				Toast.makeText(MainActivity.this,"连接失败",Toast.LENGTH_LONG).show();
				break;
			case UPLOAD_FILE_DONE:
				if(msg.arg1 == 200){
					getResult((String) msg.obj);
				}
				else{
					Toast.makeText(MainActivity.this,"错误代码：" + msg.arg1,Toast.LENGTH_LONG).show();
				}
				break;
			case GET_RESULT_SUCCEED:
				Bundle b = (Bundle) msg.obj;
				showResult(b);
				break;

			default:
				break;
		}
			super.handleMessage(msg);
	}
		
	};
	private void showResult(Bundle bundle){
		Intent intent = new Intent(this,ResultActivity.class);
		intent.putExtras(bundle);
		this.startActivity(intent);
	}
	private void takePhoto() {
		// 要保存的文件名
		String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
		String fileName = "photo_" + time;
		// 创建一个文件夹
		String path = Environment.getExternalStorageDirectory() + "/take_photo";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		// 要保存的图片文件
		imgFile = new File(file, fileName + ".jpg");
		// 将file转换成uri
		// 注意7.0及以上与之前获取的uri不一样了，返回的是provider路径
		imgUri = getUriForFile(this, imgFile);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// 添加Uri读取权限
		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		// 或者
		//        grantUriPermission("com.rain.takephotodemo", imgUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// 添加图片保存位置
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
		intent.putExtra("return-data", false);
		startActivityForResult(intent, REQUEST_TAKE_PHOTO);
	}

	private void openGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, SCAN_OPEN_PHONE);
	}

	public String getRealFilePath( Context context, Uri uri ) {
		if ( null == uri ) return null;
		final String scheme = uri.getScheme();
		String data = null;
		if ( scheme == null )
			data = uri.getPath();
		else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
			data = uri.getPath();
		} else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
			Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
			if ( null != cursor ) {
				if ( cursor.moveToFirst() ) {
					int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
					if ( index > -1 ) {
						data = cursor.getString( index );
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	// 从file中获取uri
	// 7.0及以上使用的uri是contentProvider content://com.rain.takephotodemo.FileProvider/images/photo_20180824173621.jpg
	// 6.0使用的uri为file:///storage/emulated/0/take_photo/photo_20180824171132.jpg
	private static Uri getUriForFile(Context context, File file) {
		if (context == null || file == null) {
			throw new NullPointerException();
		}
		Uri uri;
		if (Build.VERSION.SDK_INT >= 24) {
			uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.rain.takePhoto.FileProvider", file);
		} else {
			uri = Uri.fromFile(file);
		}
		return uri;
	}

	private void cropPhoto(Uri uri, boolean fromCapture) {
		Intent intent = new Intent("com.android.camera.action.CROP"); //打开系统自带的裁剪图片的intent
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");

		// 注意一定要添加该项权限，否则会提示无法裁剪
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

		intent.putExtra("scale", true);

		// 设置裁剪区域的宽高比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// 设置裁剪区域的宽度和高度
		intent.putExtra("outputX", 224);
		intent.putExtra("outputY", 224);

		// 取消人脸识别
		intent.putExtra("noFaceDetection", true);
		// 图片输出格式
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

		// 若为false则表示不返回数据
		intent.putExtra("return-data", false);

		// 指定裁剪完成以后的图片所保存的位置,pic info显示有延时
		if (fromCapture) {
			// 如果是使用拍照，那么原先的uri和最终目标的uri一致,注意这里的uri必须是Uri.fromFile生成的
			mCutUri = Uri.fromFile(imgFile);
		} else { // 从相册中选择，那么裁剪的图片保存在take_photo中
			String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
			String fileName = "photo_" + time;
			File mCutFile = new File(Environment.getExternalStorageDirectory() + "/take_photo/", fileName + ".jpeg");
			if (!mCutFile.getParentFile().exists()) {
				mCutFile.getParentFile().mkdirs();
			}
			mCutUri = Uri.fromFile(mCutFile);
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
		Toast.makeText(this, "剪裁图片", Toast.LENGTH_SHORT).show();
		// 以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
		Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intentBc.setData(uri);
		this.sendBroadcast(intentBc);

		startActivityForResult(intent, REQUEST_CROP); //设置裁剪参数显示图片至ImageVie
	}

}
