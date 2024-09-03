package com.cloudpos.convertdifffinger.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cloudpos.DeviceException;
import com.cloudpos.OperationResult;
import com.cloudpos.POSTerminal;
import com.cloudpos.TimeConstants;
import com.cloudpos.convertdifffinger.mgr.FPMgrImpl;
import com.cloudpos.convertdifffinger.mgr.IFPMgr;
import com.cloudpos.convertdifffinger.utils.HexString;
import com.cloudpos.fingerprint.Fingerprint;
import com.cloudpos.fingerprint.FingerprintDevice;
import com.cloudpos.fingerprint.FingerprintOperationResult;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Importer;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.upek.android.ptapi.PtConstants;
import com.upek.android.ptapi.PtException;
import com.cloudpos.convertdifffinger.R;
import com.cloudpos.convertdifffinger.utils.TextViewUtil;

import java.lang.reflect.Method;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FingerprintDevice device = null;
    private Button btn1;
    private Importer mImporter;
    private Engine mEngine;
    protected Handler mHandler;
    protected TextView log_text;
    private String deviceType;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(this);
        log_text = (TextView) this.findViewById(R.id.text_result);
        log_text.setMovementMethod(ScrollingMovementMethod.getInstance());

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String str = msg.obj + "\n";
                if (msg.what == 0) {
                    TextViewUtil.infoGRAYTextView(log_text, str);
                } else if (msg.what == 1) {
                    TextViewUtil.infoBlueTextView(log_text, str);
                } else if (msg.what == 2) {
                    TextViewUtil.infoRedTextView(log_text, str);
                }
            }
        };



        deviceType = checkDeviceType();
        Log.d("deviceType：", deviceType);
        if (deviceType.toLowerCase(Locale.ROOT).contains("tuzheng")) {
            final Context context = this;
            //ready FingerprintDevice
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (device == null) {
                        device = (FingerprintDevice) POSTerminal.getInstance(context).getDevice("cloudpos.device.fingerprint");
                    }
                    try {
                        device.open(1);
                        writerLogInTextview("device.open success!", 1);
                    } catch (DeviceException e) {
                        writerLogInTextview("device.open failed!", 2);
                        e.printStackTrace();
                    }
                }
            }).start();
        }else {
            //ready importer and engine
            try {
                mImporter = UareUGlobal.GetImporter();
                mEngine = UareUGlobal.GetEngine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log_text.setText("deviceType is " + deviceType + "\n");
    }

    private void mInvokeTuzheng() {
        writerLogInTextview("press your finger!", 0);

        //1,get Tuzheng fingerprint Data
        byte[] tuzhengBs = getTuzhengData();
        if (tuzhengBs == null) {
            Log.d("ConvertDiffFinger", "not get tuzheng fingerprint");
            writerLogInTextview("not get tuzheng fingerprint", 2);
            return;
        }
        try {
            //Convert the acquired data into an FMD object,format is ISO_19794_2_2005;
            Fmd fmTUZHENG_ISO = mImporter.ImportFmd(tuzhengBs, Fmd.Format.ISO_19794_2_2005, Fmd.Format.ISO_19794_2_2005);
            writerLogInTextview("fmTUZHENG_ISO finger import ok!", 1);

            //Convert the acquired data into an FMD object,format is ANSI_378_2004;
            Fmd fmTUZHENG_ISO_TO_ANSI378 = mImporter.ImportFmd(tuzhengBs, Fmd.Format.ISO_19794_2_2005, Fmd.Format.ANSI_378_2004);
            writerLogInTextview("fmTUZHENG_ISO_TO_ANSI378 finger ok!", 1);

            // use crossmatch sdk : "engine.Compare"
            /*
             * Compare
             * int Compare(Fmd fmd1,
             *           int view_index1,
             *           Fmd fmd2,
             *           int view_index2)
             *           throws UareUException
             * Compares two fingerprints.
             * Given two single views from two FMDs, this function returns a dissimilarity score indicating the quality of the match. The dissimilarity scores returned values are between: 0=match PROBABILITY_ONE=no match Values close to 0 indicate very close matches, values closer to PROBABILITY_ONE indicate very poor matches. For a discussion of how to evaluate dissimilarity scores, as well as the statistical validity of the dissimilarity score and error rates, consult the Developer Guide.
             *
             * Parameters:
             * fmd1 - First FMD.
             * view_index1 - Index of the view in the first FMD.
             * fmd2 - Second FMD.
             * view_index2 - Index of the view in the second FMD.
             * Returns:
             * Dissimilarity score.
             * Throws:
             * UareUException - if failed to perform comparison.
             * */

            //The parameter is the fmd object converted above.
            int score = mEngine.Compare(fmTUZHENG_ISO, 0, fmTUZHENG_ISO_TO_ANSI378, 0);

            //compare myself, score: 0=match
            if (score == 0) {
                writerLogInTextview("Compare fmTUZHENG_ISO and fmTUZHENG_ISO_TO_ANSI378 fingerprint success!", 1);
            } else {
                writerLogInTextview("Compare fmTUZHENG_ISO and fmTUZHENG_ISO_TO_ANSI378 fingerprint failed!", 2);
            }

            Log.i("ConvertDiffFinger", String.format("mEngine.Compare,score = %d", score));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mInvokeCrossmatch() {
        writerLogInTextview("press your finger!", 0);

        //1,get CROSSMATCH fingerprint Data
        byte[] crossmatchBs = getcrossmatchData();
        if (crossmatchBs == null) {
            Log.d("ConvertDiffFinger", "not get crossmatch fingerprint");
            writerLogInTextview("not get crossmatch fingerprint", 2);
            return;
        }
        try {
            //Convert the acquired data into an FMD object,format is ANSI_378_2004;
            Fmd fmCROSSMATCH_ANSI = mImporter.ImportFmd(crossmatchBs, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
            writerLogInTextview("fmCROSSMATCH_ANSI finger import ok!", 1);
            Log.e("fp", "" + HexString.bufferToHex(fmCROSSMATCH_ANSI.getData()));

            //Convert the acquired data into an FMD object,format is ISO_19794_2_2005;
            Fmd CROSSMATCH_ANSI_TO_ISO = mImporter.ImportFmd(crossmatchBs, Fmd.Format.ANSI_378_2004, Fmd.Format.ISO_19794_2_2005);
            Log.e("fp", "" + HexString.bufferToHex(CROSSMATCH_ANSI_TO_ISO.getData()));
            writerLogInTextview("fmCROSSMATCH_ANSI_TO_ISO finger ok!", 1);

            // use crossmatch sdk : "engine.Compare"
            /*
             * Compare
             * int Compare(Fmd fmd1,
             *           int view_index1,
             *           Fmd fmd2,
             *           int view_index2)
             *           throws UareUException
             * Compares two fingerprints.
             * Given two single views from two FMDs, this function returns a dissimilarity score indicating the quality of the match. The dissimilarity scores returned values are between: 0=match PROBABILITY_ONE=no match Values close to 0 indicate very close matches, values closer to PROBABILITY_ONE indicate very poor matches. For a discussion of how to evaluate dissimilarity scores, as well as the statistical validity of the dissimilarity score and error rates, consult the Developer Guide.
             *
             * Parameters:
             * fmd1 - First FMD.
             * view_index1 - Index of the view in the first FMD.
             * fmd2 - Second FMD.
             * view_index2 - Index of the view in the second FMD.
             * Returns:
             * Dissimilarity score.
             * Throws:
             * UareUException - if failed to perform comparison.
             * */

            //The parameter is the fmd object converted above.
            int score = mEngine.Compare(fmCROSSMATCH_ANSI, 0, CROSSMATCH_ANSI_TO_ISO, 0);

            //compare myself, score: 0=match
            if (score == 0) {
                writerLogInTextview("Compare fmCROSSMATCH_ANSI and CROSSMATCH_ANSI_TO_ISO fingerprint success!", 1);
            } else {
                writerLogInTextview("Compare fmCROSSMATCH_ANSI and CROSSMATCH_ANSI_TO_ISO fingerprint failed!", 2);
            }

            Log.i("ConvertDiffFinger", String.format("mEngine.Compare,score = %d", score));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public byte[] getcrossmatchData() {
        byte[] crossmatchBs = null;
        IFPMgr fpMgr = FPMgrImpl.getInstance();
        try {
            fpMgr.open(MainActivity.this);
            fpMgr.deleteAll(MainActivity.this);
            writerLogInTextview("press finger keep,enroll ", 1);
            try {
                byte[] image = fpMgr.GrabImage(PtConstants.PT_GRAB_TYPE_508_508_8_SCAN508_508_8);
                int iWidth = fpMgr.getImagewidth();
                //get fingerprint , format is ANSI_378
                Fmd fm = ConvertImgToIsoTemplate(image, iWidth);
                //Fmd fm1 = im.ImportFmd(readFingerprintData(), Fmd.Format.ISO_19794_2_2005, Fmd.Format.ISO_19794_2_2005);
                if (fm != null) {
                    byte[] bs = fm.getData();
                    crossmatchBs = bs;
                    //Log.e("fp", "" + HexString.bufferToHex(bs));
                    writerLogInTextview("crossmatch enroll success!", 1);
                } else {
                    Log.e("fm", "fm is null");
                    writerLogInTextview("crossmatch enroll failed!", 2);
                }
            } catch (PtException e) {
                writerLogInTextview("exception " + e.getMessage(), 2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            writerLogInTextview("exception occured !" + e.getMessage(), 2);
        } finally {
            fpMgr.close();
        }
        return crossmatchBs;
    }


    private Fmd ConvertImgToIsoTemplate(byte[] aImage, int iWidth) {
        if (aImage == null) {
            return null;
        }
        int iHeight = aImage.length / iWidth;
        try {
            Engine engine = UareUGlobal.GetEngine();
            Fmd fmd = engine.CreateFmd(aImage, iWidth, iHeight, 500, 0, 0, Fmd.Format.ANSI_378_2004);
            Log.i("BasicSample", "Import a Fmd from a raw image OK");
            return fmd;
        } catch (UareUException e) {
            Log.d("BasicSample", "Import Raw Image Fail", e);
            return null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (deviceType.toLowerCase(Locale.ROOT).contains("tuzheng")) {
                            mInvokeTuzheng();
                        } else if (deviceType.toLowerCase(Locale.ROOT).contains("crossmatch")) {
                            mInvokeCrossmatch();
                        }
                    }
                }).start();
                break;
        }
    }


    private byte[] getTuzhengData() {
        try {
            FingerprintOperationResult operationResult = device.waitForFingerprint(TimeConstants.FOREVER);
            if (operationResult.getResultCode() == OperationResult.SUCCESS) {
                Fingerprint fingerprint = ((FingerprintOperationResult) operationResult).getFingerprint(100, 100);
                return fingerprint.getFeature();
            } else {
                writerLogInTextview("scan fail", 2);
            }
            //Fingerprint fingerprint1 = device.getFingerprint(1);//1 = ISOFINGERPRINT_TYPE_ISO2005
            //return fingerprint1.getFeature();
        } catch (DeviceException e) {
            writerLogInTextview(e.getMessage(), 2);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (device != null) {
            try {
                device.close();
            } catch (DeviceException e) {
                e.printStackTrace();
                Log.d("ConvertDiffFinger", "onDestroy,device.close()");
            }
        }
    }

    private void writerLogInTextview(String log, int id) {
        Message msg = new Message();
        msg.what = id;
        msg.obj = log;
        mHandler.sendMessage(msg);
    }


    private String checkDeviceType() {
        return getProperty("wp.fingerprint.model", "not");
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
