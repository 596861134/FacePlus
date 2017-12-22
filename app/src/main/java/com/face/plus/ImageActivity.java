package com.face.plus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.face.network.ApiStores;
import com.face.network.HttpControl;
import com.face.network.ResponseListener;
import com.face.network.UtilContext;
import com.facepp.library.util.Util;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class ImageActivity extends AppCompatActivity {
    //Detect API文档：https://console.faceplusplus.com.cn/documents/4888373
    ImageView id_photo;
    TextView face_info;
    Context mContext;
    String msg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mContext = ImageActivity.this;
        UtilContext.init(mContext,true);

        id_photo = (ImageView) findViewById(R.id.id_photo);
        face_info = (TextView) findViewById(R.id.face_info);

        Bitmap bitmap = null;
        Intent intent = getIntent();
        if (intent!=null){
            msg = intent.getStringExtra("msg");
            if (msg!=null){
                if (msg.equals("1")){
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.liuyifei1);
                }else {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.faceadd1);

                }
            }
        }
        id_photo.setImageBitmap(bitmap);
        getPost(bitmap);


        //多请求
//        List<Face> list = new ArrayList<>();
//        list.add(new Face("changfangxinglian_toubu",R.mipmap.changfangxinglian_toubu));
//        list.add(new Face("changyuanxinglian_toubu",R.mipmap.changyuanxinglian_toubu));
//        list.add(new Face("fangxinglian_toubu",R.mipmap.fangxinglian_toubu));
//        list.add(new Face("jichulian_toubu",R.mipmap.jichulian_toubu));
//        list.add(new Face("lingxinglian_toubu",R.mipmap.lingxinglian_toubu));
//        list.add(new Face("luanyuanxinglian_toubu",R.mipmap.luanyuanxinglian_toubu));
//        list.add(new Face("sanjiaoxinglian_toubu",R.mipmap.sanjiaoxinglian_toubu));
//        list.add(new Face("xingrenxinglian_toubu",R.mipmap.xingrenxinglian_toubu));
//        list.add(new Face("yuanxinglian_toubu",R.mipmap.yuanxinglian_toubu));
//        for (Face f: list) {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), f.getResId());
//            getPost(bitmap,f.getName());
//
//        }


        // 未封装的请求方式
//        doPost(bitmap);
    }
    StringBuffer bu;
    private void getPost(final Bitmap bitmap){
        final int imgWidth = bitmap.getWidth();//图片宽度
        bu = new StringBuffer();

        Log.e("TAG","图片的宽度是："+imgWidth);
        bu.append("图片的宽度是："+imgWidth + "\n");
        ApiStores apiStores = HttpControl.retrofit();
        HttpControl.buildHttpRequest(apiStores.detect(Util.API_KEY, Util.API_SECRET, getPart(bitmap), 2), new ResponseListener() {
            @Override
            public void onSuccess(JSONObject object) {
                Log.e("TAG","onSuccess:"+object.toString());

                String[] keys = readAssetsTxt(mContext,"key.config").split("\n");
                ReadContext ctx = JsonPath.parse(object.toString());
                StringBuffer result = new StringBuffer();

                int length = ctx.read("$.faces.length()");
                Log.e("TAG","共检测到"+length+"张人脸");
                bu.append("共检测到"+length+"张人脸\n");
                int minFace = 0;
                int minWidth = imgWidth / 2;
                for (int i = 0; i < length; i++) {
                    // 取第i张人脸的中点离图片的中点的距离
                    int width = ctx.read("$.faces["+i+"].landmark.contour_chin.x", Integer.class);
                    Log.e("TAG","第"+i+"张人脸中点是:"+width);
                    bu.append("第"+i+"张人脸中点是:"+width+"\n");
                    // 选出离中点最近的人脸
                    int current = Math.abs(width - (imgWidth / 2));
                    Log.e("TAG", "离中点的距离:" + current);
                    bu.append("离中点的距离:" + current+"\n");
                    if (current <= minWidth) {
                        minWidth = current;
                        minFace = i;
                    }
                }
                Log.e("TAG", "离中点最近的脸是:" + minFace);
                bu.append("离中点最近的脸是:" + minFace +"\n");
                for (String key : keys) {
                    result.append(ctx.read("$.faces["+minFace+"].landmark." + key.trim() + ".x", String.class) + " ");
                    result.append(ctx.read("$.faces["+minFace+"].landmark." + key.trim() + ".y", String.class) + " ");
                }
                Log.e("TAG","关键点是："+result.toString());
                bu.append("离中点最近的脸的关键点是:\n" + result.toString());

                int left = ctx.read("$.faces["+minFace+"].face_rectangle.left", Integer.class);
                int top = ctx.read("$.faces["+minFace+"].face_rectangle.top", Integer.class);
                int right = left + ctx.read("$.faces["+minFace+"].face_rectangle.width", Integer.class);
                int bottom = top + ctx.read("$.faces["+minFace+"].face_rectangle.height", Integer.class);
                drawRectangles(bitmap,left,top,right,bottom,bu);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG","onError:"+e.getMessage());
            }

            @Override
            public void onFail(JSONObject object) {
                Log.e("TAG","onFail:"+object.toString());
            }

            @Override
            public void showProgress() {

            }

            @Override
            public void disMissProgress() {

            }
        });

    }

    private void drawRectangles(Bitmap imageBitmap, int left, int top, int right, int bottom, StringBuffer bu) {
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(3);  //线的宽度
        canvas.drawRect(left, top, right, bottom, paint);
        id_photo.setImageBitmap(mutableBitmap);
        face_info.setText(bu.toString());
    }

    @NonNull
    private MultipartBody.Part getPart(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        RequestBody body = RequestBody.create(MediaType.parse("image/png"), byteArray);
        return MultipartBody.Part.createFormData("image_file", "test.png", body);
    }

    /**
     * 将bitmap转换为byte[]
     *
     * @param bitmap 需要转换的bitmap
     * @return 返回byte[]
     */
    private byte[] getArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.toByteArray();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] array = baos.toByteArray();
        return array;
    }

    /**
     * 读取assets下的txt文件，返回utf-8 String
     * @param context
     * @param fileName
     * @return
     */
    public static String readAssetsTxt(Context context, String fileName){
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String text = new String(buffer, "utf-8");
            // Finally stick the string into the text view.
            return text;
        } catch (IOException e) {
            // Should never happen!
            //            throw new RuntimeException(e);
            e.printStackTrace();
        }
        return "读取错误，请检查文件名";
    }

    private void doPost(Bitmap bitmap) {
//        File file = new File("你的本地图片路径");
//        byte[] buff = getBytesFromFile(file);
        byte[] buff = getArray(bitmap);
        String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", Util.API_KEY);
        map.put("api_secret", Util.API_SECRET);
        map.put("return_landmark", 2);
        byteMap.put("image_file", buff);
        try {
            post(url, map, byteMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private final static int CONNECT_TIME_OUT = 30000;
    private final static int READ_OUT_TIME = 50000;
    private static String boundaryString = getBoundary();

    protected void post(final String url, final HashMap<String, Object> map, final HashMap<String, byte[]> fileMap) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conne;
                    URL url1 = new URL(url);
                    conne = (HttpURLConnection) url1.openConnection();
                    conne.setDoOutput(true);
                    conne.setUseCaches(false);
                    conne.setRequestMethod("POST");
                    conne.setConnectTimeout(CONNECT_TIME_OUT);
                    conne.setReadTimeout(READ_OUT_TIME);
                    conne.setRequestProperty("accept", "*/*");
                    conne.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
                    conne.setRequestProperty("connection", "Keep-Alive");
                    conne.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
                    DataOutputStream obos = new DataOutputStream(conne.getOutputStream());
                    Iterator iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> entry = (Map.Entry) iter.next();
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        obos.writeBytes("--" + boundaryString + "\r\n");
                        obos.writeBytes("Content-Disposition: form-data; name=\"" + key
                                + "\"\r\n");
                        obos.writeBytes("\r\n");
                        obos.writeBytes(value + "\r\n");
                    }
                    if (fileMap != null && fileMap.size() > 0) {
                        Iterator fileIter = fileMap.entrySet().iterator();
                        while (fileIter.hasNext()) {
                            Map.Entry<String, byte[]> fileEntry = (Map.Entry<String, byte[]>) fileIter.next();
                            obos.writeBytes("--" + boundaryString + "\r\n");
                            obos.writeBytes("Content-Disposition: form-data; name=\"" + fileEntry.getKey()
                                    + "\"; filename=\"" + encode(" ") + "\"\r\n");
                            obos.writeBytes("\r\n");
                            obos.write(fileEntry.getValue());
                            obos.writeBytes("\r\n");
                        }
                    }
                    obos.writeBytes("--" + boundaryString + "--" + "\r\n");
                    obos.writeBytes("\r\n");


                    BufferedReader reader = new BufferedReader(new InputStreamReader(conne.getInputStream(), "UTF-8"));
                    String line = "";
                    StringBuffer buffer = new StringBuffer();
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    reader.close();
                    Log.e("TAG","URL:"+buffer.toString());

                    obos.flush();
                    obos.close();
                    InputStream ins = null;
                    int code = conne.getResponseCode();
                    if (code == 200) {
                        ins = conne.getInputStream();
                    } else {
                        ins = conne.getErrorStream();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buff = new byte[4096];
                    int len;
                    while ((len = ins.read(buff)) != -1) {
                        baos.write(buff, 0, len);
                    }
                    byte[] bytes = baos.toByteArray();
                    ins.close();

                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("msg",bytes);
                    message.setData(bundle);
                    handler.sendMessage(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            byte[] msgs = data.getByteArray("msg");
            String val = new String(msgs);

            Log.i("mylog", "请求结果为-->" + val);

        }
    };


}
