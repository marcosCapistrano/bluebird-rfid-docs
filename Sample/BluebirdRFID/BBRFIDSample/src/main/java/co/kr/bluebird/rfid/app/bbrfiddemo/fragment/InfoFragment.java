/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 * 
 * Author : Bogon Jun
 *
 * Date : 2016.01.18
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class InfoFragment extends Fragment {
    
    private static final String TAG = InfoFragment.class.getSimpleName();

    private static final boolean D = Constants.INFO_D;

    private TextView mOSVersionTv;
    
    private TextView mRFIDLibVersionTv;
    
    private TextView mRFIDModuleVersion;

    private TextView mSDFirmwareVersion;
    
    private TextView mSDBTFirmwareVersion;

    private TextView mSDSerialNumber;
    
    private TextView mSDBootloaderBersion;
    
    private TextView mAppVersion;

    private TextView mSleType;

    //+Set Model String
    private TextView mFirmwareVerStr;

    private TextView mBTVerStr;

    private TextView mSerialStr;

    private TextView mBootloaderStr;

    private String modelIDStr;
    //Set Model String+

    private Reader mReader;

    private Context mContext;
    
    private Handler mOptionHandler;
    
    private InfoHandler mInfoHandler = new InfoHandler(this);
    
    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    private Fragment mFragment;

    private TextView tv_serial_num;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.info_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mOSVersionTv = (TextView)v.findViewById(R.id.tv_os_version);
        mRFIDLibVersionTv = (TextView)v.findViewById(R.id.tv_rf_lib_version);
        mRFIDModuleVersion = (TextView)v.findViewById(R.id.tv_rfid_version);
        mSDFirmwareVersion = (TextView)v.findViewById(R.id.tv_sd_firm_version);
        mSDBTFirmwareVersion = (TextView)v.findViewById(R.id.tv_sd_bt_firm_version);
        mSDSerialNumber = (TextView)v.findViewById(R.id.tv_sd_serial_number);
        mSDBootloaderBersion = (TextView)v.findViewById(R.id.tv_bootloader_version);
        mAppVersion = (TextView)v.findViewById(R.id.tv_app_version);
        mSleType = (TextView)v.findViewById(R.id.tv_sled_type);
        tv_serial_num = (TextView)v.findViewById(R.id.tv_serial_num);

        mFirmwareVerStr = (TextView)v.findViewById(R.id.tv_firmVer);
        mBTVerStr = (TextView)v.findViewById(R.id.tv_btVer);
        mSerialStr = (TextView)v.findViewById(R.id.tv_serial);
        mBootloaderStr = (TextView)v.findViewById(R.id.tv_bootloaderVer);
        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mInfoHandler);
        mFirmwareVerStr.setText(getString(R.string.sd_lib_version_str));
        mBTVerStr.setText(getString(R.string.sd_bt_version_str));
        mSerialStr.setText(getString(R.string.sd_serial_str));
        mBootloaderStr.setText(getString(R.string.bootloader_version_str));
        mOSVersionTv.setText(Build.DISPLAY);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            mRFIDLibVersionTv.setText(mReader.RF_GetLibVersion());
            mRFIDModuleVersion.setText(mReader.RF_GetRFIDVersion());
            mSDFirmwareVersion.setText(mReader.SD_GetVersion());
            mSDSerialNumber.setText(mReader.SD_GetSerialNumber());
            mSDBootloaderBersion.setText(mReader.SD_GetBootLoaderVersion());
            mSDBTFirmwareVersion.setText(mReader.SD_GetBTVersion());
            try{
                tv_serial_num.setText(mReader.SD_GetHostSerialNumber());
            }catch (NoSuchMethodError e1){
                e1.printStackTrace();
				 tv_serial_num.setText("unknown0");
            }catch (Exception e){
                e.printStackTrace();
				 tv_serial_num.setText("unknown1");
            }
            int sledType = mReader.SD_GetType();
            if(sledType == SDConsts.SLED_TYPE.INTERNAL_SLED){
                mSleType.setText("INTERNAL_SLED");
            }else if(sledType == SDConsts.SLED_TYPE.RFR900_EXTERNAL_SLED){
                mSleType.setText("EXTERNAL_SLED");
            }else if(sledType == SDConsts.SLED_TYPE.RFR901_EXTERNAL_SLED){
                mSleType.setText("EXTERNAL_SLED");
            }
        }
        mAppVersion.setText(Constants.VERSION);
        super.onStart();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        if (D) Log.d(TAG, "onStop");
        super.onStop();
    }
    
    private static class InfoHandler extends Handler {
        private final WeakReference<InfoFragment> mExecutor;
        public InfoHandler(InfoFragment f) {
            mExecutor = new WeakReference<>(f);
        }
        
        @Override
        public void handleMessage(Message msg) {
            InfoFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mInfoHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
        
        switch (m.what) {
        case SDConsts.Msg.SDMsg:
            if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                if (mOptionHandler != null)
                    mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
            }
            //+Always be display Battery
            else if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                //+smart batter -critical temper
                if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                    Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                //smart batter -critical temper+

                Activity activity = getActivity();
                if (activity != null) {
                    if (mOptionHandler != null) {
                        Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                        mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                    }
                }
            }
            //Always be display Battery+
            //+Hotswap feature
            else if (m.arg1 == SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED) {
                if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE)
                    Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE)
                    Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(mFragment).attach(mFragment).commit();
            }
            //Hotswap feature+
            break;
        }
    }
}