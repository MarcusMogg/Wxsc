package test.com.fg.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.fg.activity.R;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class DianpingActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar mLoadingProgress;
    private Intent mIntent;
    private String mstrLoginUrl = "https://www.dianping.com/search/keyword/2/0_咖喱鸡";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dianping);
        initView();
    }

    private void initView() {
        webView = this.findViewById(R.id.dianping);
        mIntent = this.getIntent();
        mLoadingProgress = findViewById(R.id.progressBarLoading);
        mLoadingProgress.setMax(100);
        System.out.println("############################################################################################");
        System.out.println(mIntent.getStringExtra("URL"));
        mstrLoginUrl = "https://www.dianping.com/search/keyword/2/0_" + mIntent.getStringExtra("URL")
                + "/r1488";

        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true); //设置可以支持缩放
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.loadUrl(mstrLoginUrl);

        // 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("http:")||url.startsWith("https:")){
                    view.loadUrl(url);
                    return false;
                }else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            }

        });
    }

    private class WebChromeClientProgress extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (mLoadingProgress != null) {
                mLoadingProgress.setProgress(progress);
                if (progress == 100) mLoadingProgress.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, progress);
        }
    }

    /**
     * 按键响应，在WebView中查看网页时，检查是否有可以前进的历史记录。
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack())
        {

            // 返回键退回
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up
        // to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }


}
