/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import static co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils.createAlertDialog;

public class BatteryFragment extends Fragment {
    private static final String TAG = BatteryFragment.class.getSimpleName();

    private static final boolean D = Constants.BAT_D;

    private TextView mBatteryTv;

    private Button mGetChargeBt;

    private Button mGetBatBt;

    //+smart battery
    private Button mGetSmartBattStatus;
    private Button mGetSmartBattVol;
    private Button mGetSmartBattSerial;
    private Button mGetSmartBattPresent;
    private Button mGetSmartBattLevel;
    private Button mGetSmartBattGauge;
    private Button mGetSmartBattHealth;
    private Button mGetSmartBattTemper;
    private Button mGetSmartBattCycleCnt;
    private Button mGetSmartBattCap;
    //smart battery+

    private TextView mMessageTextView;

    private ProgressBar mBatteryProgress;

    private Reader mReader;

    private Context mContext;

    private Handler mOptionHandler;

    private final BatteryHandler mBatteryHandler = new BatteryHandler(this);

    public static BatteryFragment newInstance() {
        return new BatteryFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.battery_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        mBatteryTv = (TextView) v.findViewById(R.id.battery_state_textview);

        mMessageTextView = (TextView) v.findViewById(R.id.message_textview);

        mGetChargeBt = (Button) v.findViewById(R.id.bt_charge);
        mGetChargeBt.setOnClickListener(buttonListener);

        mGetBatBt = (Button) v.findViewById(R.id.bt_bat);
        mGetBatBt.setOnClickListener(buttonListener);

        mBatteryProgress = (ProgressBar) v.findViewById(R.id.batt_progress);

        //+smart battery
        mGetSmartBattStatus = (Button) v.findViewById(R.id.bt_smart_batt);
        mGetSmartBattStatus.setOnClickListener(buttonListener);

        mGetSmartBattVol = (Button) v.findViewById(R.id.bt_smart_voltage);
        mGetSmartBattVol.setOnClickListener(buttonListener);

        mGetSmartBattSerial = (Button) v.findViewById(R.id.bt_smart_serial);
        mGetSmartBattSerial.setOnClickListener(buttonListener);

        mGetSmartBattPresent = (Button) v.findViewById(R.id.bt_smart_present);
        mGetSmartBattPresent.setOnClickListener(buttonListener);

        mGetSmartBattLevel = (Button) v.findViewById(R.id.bt_smart_level);
        mGetSmartBattLevel.setOnClickListener(buttonListener);

        mGetSmartBattGauge = (Button) v.findViewById(R.id.bt_smart_life_time);
        mGetSmartBattGauge.setOnClickListener(buttonListener);

        mGetSmartBattHealth = (Button) v.findViewById(R.id.bt_smart_health);
        mGetSmartBattHealth.setOnClickListener(buttonListener);

        mGetSmartBattTemper = (Button) v.findViewById(R.id.bt_smart_tmeperature);
        mGetSmartBattTemper.setOnClickListener(buttonListener);

        mGetSmartBattCycleCnt = (Button) v.findViewById(R.id.bt_smart_cycle_cnt);
        mGetSmartBattCycleCnt.setOnClickListener(buttonListener);

        mGetSmartBattCap = (Button) v.findViewById(R.id.bt_smart_capacity);
        mGetSmartBattCap.setOnClickListener(buttonListener);
        //smart battery+
        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mBatteryHandler);

        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            int value = mReader.SD_GetBatteryStatus();
            Activity activity = getActivity();
            if (activity != null) {
                mBatteryTv.setText(activity.getString(R.string.battery_state_str) +
                        value + " " + activity.getString(R.string.percent_str));
                mBatteryProgress.setProgress(value);
            }
        }
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

    private OnClickListener buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int ret = -100;
            String retString = null;
            
            int id = v.getId();
            if (id == R.id.bt_charge) {
                retString = "SD_GetChargeState ";
                ret = mReader.SD_GetChargeState();
                if (D) Log.d(TAG, "get charge state = " + ret);
            }else if (id == R.id.bt_bat) {
                retString = "SD_GetBatteryStatus ";
                ret = mReader.SD_GetBatteryStatus();
                if (D) Log.d(TAG, "bat = " + ret);
                Activity activity = getActivity();
                if (activity != null) {
                    mBatteryTv.setText(activity.getString(R.string.battery_state_str) + ret +
                            " " + activity.getString(R.string.percent_str));
                    mBatteryProgress.setProgress(ret);
                }
            }else if (id == R.id.bt_smart_serial) {
                retString = "SD_GetSmartBatterySerial ";
                retString += mReader.SD_GetSmartBatterySerial();
            }else if (id == R.id.bt_smart_batt) {
                retString = "SD_GetSmartBatteryStatus ";
                ret = mReader.SD_GetSmartBatteryStatus();
                Toast.makeText(mContext, "SD_GetSmartBatteryStatus= " + ret, Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_voltage) {
                retString = "SD_GetSmartBatteryVoltage ";
                ret = mReader.SD_GetSmartBatteryVoltage();
                Toast.makeText(mContext, "SD_GetSmartBatteryVoltage= " + ret + " mV", Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_present) {
                retString = "SD_GetSmartBatteryPresentStatus ";
                ret = mReader.SD_GetSmartBatteryPresentStatus();
                Toast.makeText(mContext, "SD_GetSmartBatteryPresentStatus= " + ret, Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_level) {
                retString = "SD_GetSmartBatteryLevel ";
                ret = mReader.SD_GetSmartBatteryLevel();
                Toast.makeText(mContext, "SD_GetSmartBatteryLevel= " + ret, Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_life_time) {
                retString = "SD_GetSmartBatteryGaugeTime ";
                ret = mReader.SD_GetSmartBatteryLifeTime();
                int h = ret / 60;
                int m = ret % 60;
                Toast.makeText(mContext, "SD_GetSmartBatteryGaugeTime= " + h + " hour " + m + " min", Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_health) {
                retString = "SD_GetSmartBatteryHealth ";
                ret = mReader.SD_GetSmartBatteryHealth();
                Toast.makeText(mContext, "SD_GetSmartBatteryHealth = " + ret, Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_tmeperature) {
                retString = "SD_GetSmartBatteryTemperature ";
                ret = mReader.SD_GetSmartBatteryTemperature();
                float d = (float) (ret/10.0);
                Toast.makeText(mContext, "SD_GetSmartBatteryTemperature = " + d + " degree", Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_cycle_cnt) {
                retString = "SD_GetSmartBatteryCycleCnt ";
                ret = mReader.SD_GetSmartBatteryCycleCnt();
                Toast.makeText(mContext, "SD_GetSmartBatteryCycleCnt  = " + ret, Toast.LENGTH_SHORT).show();
            }else if (id == R.id.bt_smart_capacity) {
                retString = "SD_GetSmartBatteryCapacity ";
                ret = mReader.SD_GetSmartBatteryCapacity();
                Toast.makeText(mContext, "SD_GetSmartBatteryCapacity = " + ret + " mV", Toast.LENGTH_SHORT).show();
            }else {
                if (ret != -100)
                    retString += ret;
            }
            mMessageTextView.setText(" " + retString);
        }
    };

    private static class BatteryHandler extends Handler {
        private final WeakReference<BatteryFragment> mExecutor;

        public BatteryHandler(BatteryFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            BatteryFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mBatteryHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        mBatteryTv.setText(activity.getString(R.string.battery_state_str) + m.arg2 + " " + activity.getString(R.string.percent_str));
                        mBatteryProgress.setProgress(m.arg2);

                        //+smart batter -critical temper
                        if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                            Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                        //smart batter -critical temper+

                        //+Always be display Battery
                        if (mOptionHandler != null) {
                            Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                            mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                        }
                        //Always be display Battery+
                    }
                } else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                }
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