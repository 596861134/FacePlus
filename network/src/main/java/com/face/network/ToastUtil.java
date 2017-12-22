/**
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.face.network;

import android.widget.Toast;

/**
 * Toast工具
 */
public class ToastUtil {

    private static String oldMsg;
    protected static Toast toast = null;
    private static long oneTime=0;
    private static long twoTime=0;

    /**
     * String
     * @param s
     */
    public static void showToast(String s){
        if(toast==null){
            toast = Toast.makeText(UtilContext.getContext(), s, Toast.LENGTH_SHORT);
            toast.show();
            oneTime= System.currentTimeMillis();
        }else{
            twoTime= System.currentTimeMillis();
            if(s.equals(oldMsg)){
                if(twoTime-oneTime> Toast.LENGTH_SHORT){
                    toast.show();
                }
            }else{
                oldMsg = s;
                toast.setText(s);
                toast.show();
            }
        }
        oneTime=twoTime;
    }


    /**
     * int
     * @param resId
     */
    public static void showToast(int resId){
        showToast(UtilContext.getContext().getString(resId));
    }

}
