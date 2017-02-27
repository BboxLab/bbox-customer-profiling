/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 BboxLab
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bouyguestelecom.tv.bridge.customer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import tv.bouyguestelecom.fr.bboxapilibrary.MyBbox;
import tv.bouyguestelecom.fr.bboxapilibrary.MyBboxManager;

/**
 * @author Bertrand Martel
 */
public class BboxHolder {

    private static final String TAG = BboxHolder.class.getCanonicalName();

    public static BboxHolder mInstance = new BboxHolder();
    private MyBbox mBbox;
    private MyBboxManager bboxManager = new MyBboxManager();
    private Handler handler = new Handler();

    /**
     * Singleton: private constructor. Instance must be retrieved with getInstance method
     */
    private BboxHolder() {}

    public MyBboxManager getBboxManager() {
        return bboxManager;
    }

    public void bboxSearch(final Context context){

        bboxManager.startLookingForBbox(context, new MyBboxManager.CallbackBboxFound() {
            @Override
            public void onResult(final MyBbox bboxFound) {

                // When we find our Bbox, we stopped looking for other Bbox.
                bboxManager.stopLookingForBbox();

                // We save our Bbox.
                mBbox = bboxFound;
                Log.i(TAG, "Bbox found: " + mBbox.getIp() + " macAdress: " + mBbox.getMacAddress());

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("bboxip", mBbox.getIp());
                editor.commit();
            }
        });

    }

    /**
     * set the current bbox
     *
     * @param ip bbox ip
     */
    public void setCustomBbox(String ip) {
        mBbox = new MyBbox(ip);
    }

    /**
     * Return the current bbox. null if not correctly initialized !
     *
     * @return the bbox.
     */
    public MyBbox getBbox() throws MyBboxNotFoundException {
        if (mBbox == null) {
            throw new MyBboxNotFoundException();
        }
        return mBbox;
    }

    public static BboxHolder getInstance() {
        return mInstance;
    }

}
