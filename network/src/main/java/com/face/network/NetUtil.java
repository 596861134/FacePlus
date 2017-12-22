package com.face.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * /**
 *
 * @author LV
 * @ClassName: NetMisc
 * @Description: 处理与网络相关的工具
 * @date 2014-12-1 下午5:35:22
 */
public class NetUtil {

    /**
     * 没有连接网络
     */
    private static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    private static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    private static final int NETWORK_WIFI = 1;

    public static int getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    /**
     * 是否连接WIFI
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();

    }

    /**
     * Check network connection which is connected.
     * <p/>
     * <p/>
     * Usually, we need to check the network connection is connected, currently
     * the given type is either Wifi or Mobile. Network state is available does
     * not mean that the network connection is connected. So just check the
     * network state available is the wrong way.
     * <p/>
     * <p/>
     * <Font color=red>Note</Font>:This method can only be run on a real device.
     *
     * @return if return true, the network connection has connected; else return
     * false.
     * @throws Exception Any exceptions should ensure that this method will always
     *                   return a value before the exception thrown. But in this
     *                   method, it just catch, does not throw.
     * @author Damet Liu
     * @see {@link ConnectivityManager#getAllNetworkInfo()}
     * @see {@link NetworkInfo#getState()}
     * @see {@link State}
     * @since 2013-12-1 17:00:00 V1.0.0
     */
    public static boolean isNetworkOnline() {
        boolean status = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) UtilContext.getContext().getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            for (int index = 0; index < networkInfos.length; index++) {
                NetworkInfo netInfo = networkInfos[index];
                if ((netInfo != null) && netInfo.isConnected()) {
                    status = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * 检查是否连接网络
     *
     * @param
     * @return
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) UtilContext.getContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    /**
     * @return 导出数据流
     */
    public static BufferedInputStream exportData(String dbName) {
        try {
            // 当前程序路径
            String path = UtilContext.getContext().getApplicationContext().getFilesDir().getAbsolutePath();
            path = path + "/../databases/" + dbName;
            File file = new File(path);
            FileInputStream inStream = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(inStream);
            return in;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * @param in 导入数据流
     */
    public static void importData(BufferedInputStream in, String dbName) {
        try {
            // 当前程序路径
            String path = UtilContext.getContext().getFilesDir().getAbsolutePath();
            path = path + "/../databases/";
            File file = new File(path);
            file.mkdirs();
            path += dbName;
            file = new File(path);
            file.createNewFile();
            FileOutputStream outStream = new FileOutputStream(file);
            BufferedOutputStream out = new BufferedOutputStream(outStream);
            int c;
            while ((c = in.read()) >= 0) {
                out.write(c);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分享文本信息
     *
     * @param
     * @param content 分享内容
     */
    public static void share(String content) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        // intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        UtilContext.getContext().startActivity(Intent.createChooser(intent, "分享到"));
    }

    /**
     * SharedPreferences 文件名
     */
    private static final String SETTINGS = "settings";

    /**
     * @param
     * @param key
     * @param value
     * @return 是否保存成功
     */
    public static boolean setSharedPreferences(String key, boolean value) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.edit().putBoolean(key, value).commit();
    }

    /**
     * @param
     * @param key
     * @param defaultValue
     * @return value
     */
    public static boolean getSharedPreferences(String key, boolean defaultValue) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    /**
     * @param
     * @param key
     * @param value
     * @return 是否保存成功
     */
    public static boolean setSharedPreferences(String key, float value) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.edit().putFloat(key, value).commit();
    }

    /**
     * @param
     * @param key
     * @param value
     * @return 是否保存成功
     */
    public static boolean setSharedPreferences(String key, long value) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.edit().putLong(key, value).commit();
    }

    /**
     * @param
     * @param key
     * @param defaultValue
     * @return value
     */
    public static float getSharedPreferences(String key, float defaultValue) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getFloat(key, defaultValue);
    }

    /**
     * @param
     * @param key
     * @param value
     * @return 是否保存成功
     */
    public static boolean setSharedPreferences(String key, int value) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.edit().putInt(key, value).commit();
    }

    /**
     * @param
     * @param key
     * @param defaultValue
     * @return value
     */
    public static int getSharedPreferences(String key, int defaultValue) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getInt(key, defaultValue);
    }

    /**
     * @param context
     * @param key
     * @param value
     * @return 是否保存成功
     */
    public static boolean setSharedPreferences(Context context, String key, long value) {
        SharedPreferences sp = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.edit().putLong(key, value).commit();
    }

    /**
     * @param
     * @param key
     * @param defaultValue
     * @return value
     */
    public static long getSharedPreferences(String key, long defaultValue) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getLong(key, defaultValue);
    }

    /**
     * @param
     * @param key
     * @param value
     * @return 是否保存成功
     */
    public static boolean setSharedPreferences(String key, String value) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.edit().putString(key, value).commit();
    }

    /**
     * @param
     * @param key
     * @param defaultValue
     * @return value
     */
    public static String getSharedPreferences(String key, String defaultValue) {
        SharedPreferences sp = UtilContext.getContext().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }

    private static final int NETWORK_TYPE_UNAVAILABLE = -1;
    // private static final int NETWORK_TYPE_MOBILE = -100;
    private static final int NETWORK_TYPE_WIFI = -101;

    private static final int NETWORK_CLASS_WIFI = -101;
    private static final int NETWORK_CLASS_UNAVAILABLE = -1;
    /**
     * Unknown network class.
     */
    private static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "2G" networks.
     */
    private static final int NETWORK_CLASS_2_G = 1;
    /**
     * Class of broadly defined "3G" networks.
     */
    private static final int NETWORK_CLASS_3_G = 2;
    /**
     * Class of broadly defined "4G" networks.
     */
    private static final int NETWORK_CLASS_4_G = 3;

    private static DecimalFormat df = new DecimalFormat("#.##");

    // 适配低版本手机
    /**
     * Network type is unknown
     */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    public static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    public static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    public static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    public static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    public static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    public static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    public static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    public static final int NETWORK_TYPE_HSPAP = 15;

    /**
     * 格式化大小
     *
     * @param size
     * @return
     */
    public static String formatSize(long size) {
        String unit = "B";
        float len = size;
        if (len > 900) {
            len /= 1024f;
            unit = "KB";
        }
        if (len > 900) {
            len /= 1024f;
            unit = "MB";
        }
        if (len > 900) {
            len /= 1024f;
            unit = "GB";
        }
        if (len > 900) {
            len /= 1024f;
            unit = "TB";
        }
        return df.format(len) + unit;
    }

    public static String formatSizeBySecond(long size) {
        String unit = "B";
        float len = size;
        if (len > 900) {
            len /= 1024f;
            unit = "KB";
        }
        if (len > 900) {
            len /= 1024f;
            unit = "MB";
        }
        if (len > 900) {
            len /= 1024f;
            unit = "GB";
        }
        if (len > 900) {
            len /= 1024f;
            unit = "TB";
        }
        return df.format(len) + unit + "/s";
    }

    public static String format(long size) {
        String unit = "B";
        float len = size;
        if (len > 1000) {
            len /= 1024f;
            unit = "KB";
            if (len > 1000) {
                len /= 1024f;
                unit = "MB";
                if (len > 1000) {
                    len /= 1024f;
                    unit = "GB";
                }
            }
        }
        return df.format(len) + "\n" + unit + "/s";
    }

    /**
     * 获取运营商
     *
     * @return
     */
    public static String getProvider() {
        String provider = "未知";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) UtilContext.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            String IMSI = telephonyManager.getSubscriberId();
            if (IMSI == null) {
                if (TelephonyManager.SIM_STATE_READY == telephonyManager
                        .getSimState()) {
                    String operator = telephonyManager.getSimOperator();
                    if (operator != null) {
                        if (operator.equals("46000")
                                || operator.equals("46002")
                                || operator.equals("46007")) {
                            provider = "中国移动";
                        } else if (operator.equals("46001")) {
                            provider = "中国联通";
                        } else if (operator.equals("46003")) {
                            provider = "中国电信";
                        }
                    }
                }
            } else {
                if (IMSI.startsWith("46000") || IMSI.startsWith("46002")
                        || IMSI.startsWith("46007")) {
                    provider = "中国移动";
                } else if (IMSI.startsWith("46001")) {
                    provider = "中国联通";
                } else if (IMSI.startsWith("46003")) {
                    provider = "中国电信";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return provider;
    }

    /**
     * 获取网络类型
     *
     * @return
     */
    public static int getCurrentNetworkType() {
        int networkClass = getNetworkClass();
        int type = 1;
        switch (networkClass) {
            case NETWORK_CLASS_UNAVAILABLE:
                type = -1;
                break;
            case NETWORK_CLASS_WIFI:
                type = 0;
                break;
            case NETWORK_CLASS_2_G:
                type = 2;
                break;
            case NETWORK_CLASS_3_G:
                type = 3;
                break;
            case NETWORK_CLASS_4_G:
                type = 4;
                break;
            case NETWORK_CLASS_UNKNOWN:
                type = 1;
                break;
        }
        return type;
    }

    private static int getNetworkClassByType(int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_UNAVAILABLE:
                return NETWORK_CLASS_UNAVAILABLE;
            case NETWORK_TYPE_WIFI:
                return NETWORK_CLASS_WIFI;
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    private static int getNetworkClass() {
        int networkType = NETWORK_TYPE_UNKNOWN;
        try {
            final NetworkInfo network = ((ConnectivityManager) UtilContext.getContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            if (network != null && network.isAvailable()
                    && network.isConnected()) {
                int type = network.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    networkType = NETWORK_TYPE_WIFI;
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager telephonyManager = (TelephonyManager) UtilContext.getContext().getSystemService(
                                    Context.TELEPHONY_SERVICE);
                    networkType = telephonyManager.getNetworkType();
                }
            } else {
                networkType = NETWORK_TYPE_UNAVAILABLE;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return getNetworkClassByType(networkType);

    }

}
