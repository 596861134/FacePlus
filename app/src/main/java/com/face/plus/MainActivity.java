package com.face.plus;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facepp.library.FaceppActionActivity;
import com.facepp.library.util.ConUtil;
import com.facepp.library.util.SharedUtil;
import com.facepp.library.util.Util;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView WarrantyText;
    private ProgressBar WarrantyBar;
    private Button againWarrantyBtn;
    private SharedUtil mSharedUtil;


    String key = "UszzUSsXQRiTkAMH99XxQSmPGL9A2_6H";//api_key
    String secret = "QA3K3I9LZYtnxTViEESdkxd7mHXEp5fB";//api_secret
    String imageUrl = "http://pic1.hebei.com.cn/003/005/869/00300586905_449eedbb.jpg";
    StringBuffer sb = new StringBuffer();

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init() {
        mSharedUtil = new SharedUtil(this);
        WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
        WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
        againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
        String authTime0 = ConUtil.getFormatterDate(Facepp.getApiExpirationMillis(this, ConUtil.getFileContent(this, R.raw.megviifacepp_0_4_7_model)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String language_save = mSharedUtil.getStringValueByKey("language");
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (!language.equals(language_save))
            showLanguage(language);

        initData();
        network();
        onClickListener();
    }

    private void initData() {
        WarrantyText.setText(getResources().getString(R.string.auth_progress));
        againWarrantyBtn.setText(getResources().getString(R.string.auth_again));

    }

    private void network() {
        if (Facepp.getSDKAuthType(ConUtil.getFileContent(this, R.raw
                .megviifacepp_0_4_7_model)) == 2) {// 非联网授权
            authState(true,0);
            return;
        }

        againWarrantyBtn.setVisibility(View.GONE);
        WarrantyText.setText(getResources().getString(R.string.auth_progress));
        WarrantyBar.setVisibility(View.VISIBLE);
        final LicenseManager licenseManager = new LicenseManager(this);
        licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(this, ConUtil.getFileContent(this, R.raw
                .megviifacepp_0_4_7_model)));

        String uuid = ConUtil.getUUIDString(MainActivity.this);
        long apiName = Facepp.getApiName();

        licenseManager.setAuthTimeBufferMillis(0);

        licenseManager.takeLicenseFromNetwork(uuid, Util.API_KEY, Util.API_SECRET, apiName,
                LicenseManager.DURATION_30DAYS, "Landmark", "1", true, new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        authState(true,0);
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        if (Util.API_KEY == null || Util.API_SECRET == null) {
                            if (!ConUtil.isReadKey(MainActivity.this)) {
                                authState(false,1001);
                            }else{
                                authState(false,1001);
                            }
                        }else{
                            authState(false,i);
                        }

                    }
                });
    }

    private void freshView() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected void showLanguage(String language) {
        //设置应用语言类
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (language.equals("zh")) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            config.locale = Locale.ENGLISH;
        }
        resources.updateConfiguration(config, dm);
        mSharedUtil.saveStringValue("language", language);
        freshView();
    }


    private void authState(boolean isSuccess,int code) {
        if (isSuccess) {

            Intent intent = new Intent();
            intent.setClass(this, FaceppActionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity,all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
            startActivity(intent);

            finish();
        } else {
            WarrantyBar.setVisibility(View.GONE);
            againWarrantyBtn.setVisibility(View.VISIBLE);
            //更详细的错误码请以官网的文档为主https://console.faceplusplus.com.cn/documents/8458445
            if (code==403){
                WarrantyText.setText(Html.fromHtml("<u>"+getResources().getString(R.string.auth_bundle)+"</u>"));
                WarrantyText.setOnClickListener(onlineClick);
            }else if (code==1001){
                WarrantyText.setText(Html.fromHtml("<u>"+getResources().getString(R.string.key_secret)+"</u>"));
                WarrantyText.setOnClickListener(onlineClick);
            }else {
                WarrantyText.setText(Html.fromHtml("<u>"+getResources().getString(R.string.auth_fail)+"</u>"));
            }
        }
    }

    private View.OnClickListener onlineClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent= new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("https://console.faceplusplus.com.cn/service/face/intro");
            intent.setData(content_url);
            startActivity(intent);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    private void onClickListener() {
        againWarrantyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                network();
            }
        });
        findViewById(R.id.loading_layout_rootRel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConUtil.isGoneKeyBoard(MainActivity.this);
            }
        });
    }
}
