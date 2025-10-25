/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfidbtdemo.fragment;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.Constants;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.R;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.utils.Utils;
import co.kr.bluebird.sled.BCResumeListener;
import co.kr.bluebird.sled.BTReader;
import co.kr.bluebird.sled.SDConsts;

public class BarcodeFragment extends Fragment {
    private static final String TAG = BarcodeFragment.class.getSimpleName();

    private static final boolean D = Constants.BAR_D;

    private Button mSetRfidBt;
    private Button mSetBarcodeBt;
    private Button mGetTriggerBt;

    private Button mPressBarcode;
    private Button mReleaseBarcode;

    private Button mSetModeKeyEnable;
    private Button mSetModeKeyDisable;
    private Button mGetModeKeyEnableState;

    private Button mSetMultiScanTypeEnable;
    private Button mSetMultiScanTypeDisable;
    private Button mGetMultiScanTypeState;

    private Spinner mBarcodeMultiSpin;
    private ArrayAdapter<CharSequence> mBarcodeMultiChar;
    private Button mSetBarcodeMultiNumberBt;
    private Button mGetBarcodeMultiNumberBt;

    private Spinner mBarcodeTriggerSpin;
    private ArrayAdapter<CharSequence> mBarcodeTriggerChar;
    private Button mSetBarcodeTriggerBt;
    private Button mGetBarcodeTriggerBt;

    private Spinner mBarcodeHWSpin;
    private ArrayAdapter<CharSequence> mBarcodeHWChar;
    private Button mSetBarcodeHWBt;
    private Button mGetBarcodeHWBt;

    private Button mPauseBarcode;
    private Button mResumeBarcode;
    private Button mGetBarcodeState;

    private Button mEnableMulti;
    private Button mDisableMulti;
    private Button mGetMultiState;

    private Button mGetBarcodeCameraInfoBt;

    private TextView mMessageTextView;

    private TextView mDataTextView;

    private BTReader mReader;

    private Context mContext;

    private Handler mOptionHandler;

    private Fragment mFragment;

    private final BarcodeHandler mBarcodeHandler = new BarcodeHandler(this);

    public static BarcodeFragment newInstance() {
        return new BarcodeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.barcode_frag, container, false);
        if (D) Log.d(TAG, "onCreateView");
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mMessageTextView = (TextView)v.findViewById(R.id.message_textview);

        mDataTextView = (TextView)v.findViewById(R.id.data_textview);

        mSetRfidBt = (Button)v.findViewById(R.id.bt_settigger_rfid);
        mSetRfidBt.setOnClickListener(buttonListener);
        mSetBarcodeBt = (Button)v.findViewById(R.id.bt_settigger_barcode);
        mSetBarcodeBt.setOnClickListener(buttonListener);
        mGetTriggerBt = (Button)v.findViewById(R.id.bt_trigger);
        mGetTriggerBt.setOnClickListener(buttonListener);

        mSetModeKeyEnable = (Button)v.findViewById(R.id.bt_mode_key_enable);
        mSetModeKeyEnable.setOnClickListener(buttonListener);
        mSetModeKeyDisable = (Button)v.findViewById(R.id.bt_mode_key_disable);
        mSetModeKeyDisable.setOnClickListener(buttonListener);
        mGetModeKeyEnableState = (Button)v.findViewById(R.id.bt_get_mode_key_state);
        mGetModeKeyEnableState.setOnClickListener(buttonListener);

        mSetMultiScanTypeEnable = (Button)v.findViewById(R.id.bt_multi_scan_type_enable);
        mSetMultiScanTypeEnable.setOnClickListener(buttonListener);
        mSetMultiScanTypeDisable = (Button)v.findViewById(R.id.bt_multi_scan_type_disable);
        mSetMultiScanTypeDisable.setOnClickListener(buttonListener);
        mGetMultiScanTypeState = (Button)v.findViewById(R.id.bt_get_multi_scan_type_state);
        mGetMultiScanTypeState.setOnClickListener(buttonListener);

        mPressBarcode = (Button)v.findViewById(R.id.bt_barcode_press);
        mPressBarcode.setOnClickListener(buttonListener);
        mReleaseBarcode = (Button)v.findViewById(R.id.bt_barcode_release);
        mReleaseBarcode.setOnClickListener(buttonListener);

        mSetBarcodeMultiNumberBt = (Button)v.findViewById(R.id.bt_set_multi_number);
        mSetBarcodeMultiNumberBt.setOnClickListener(buttonListener);
        mGetBarcodeMultiNumberBt = (Button)v.findViewById(R.id.bt_get_multi_number);
        mGetBarcodeMultiNumberBt.setOnClickListener(buttonListener);
        mBarcodeMultiSpin = (Spinner)v.findViewById(R.id.barcode_multi_number_spin);
        mBarcodeMultiChar = ArrayAdapter.createFromResource(mContext, R.array.multi_number_array,
                android.R.layout.simple_spinner_dropdown_item);
        mBarcodeMultiSpin.setAdapter(mBarcodeMultiChar);

        mSetBarcodeTriggerBt = (Button)v.findViewById(R.id.bt_set_trigger);
        mSetBarcodeTriggerBt.setOnClickListener(buttonListener);
        mGetBarcodeTriggerBt = (Button)v.findViewById(R.id.bt_get_trigger);
        mGetBarcodeTriggerBt.setOnClickListener(buttonListener);
        mBarcodeTriggerSpin = (Spinner)v.findViewById(R.id.barcode_trigger_spin);
        mBarcodeTriggerChar = ArrayAdapter.createFromResource(mContext, R.array.trigger_array,
                android.R.layout.simple_spinner_dropdown_item);
        mBarcodeTriggerSpin.setAdapter(mBarcodeTriggerChar);

        mSetBarcodeHWBt = (Button)v.findViewById(R.id.bt_set_hw_key);
        mSetBarcodeHWBt.setOnClickListener(buttonListener);
        mGetBarcodeHWBt = (Button)v.findViewById(R.id.bt_get_hw_key);
        mGetBarcodeHWBt.setOnClickListener(buttonListener);
        mBarcodeHWSpin = (Spinner)v.findViewById(R.id.barcode_hw_key_spin);
        mBarcodeHWChar = ArrayAdapter.createFromResource(mContext, R.array.barcodehw_array,
                android.R.layout.simple_spinner_dropdown_item);
        mBarcodeHWSpin.setAdapter(mBarcodeHWChar);

        mPauseBarcode = (Button)v.findViewById(R.id.bt_barcode_pause);
        mPauseBarcode.setOnClickListener(buttonListener);
        mResumeBarcode = (Button)v.findViewById(R.id.bt_barcode_resume);
        mResumeBarcode.setOnClickListener(buttonListener);
        mGetBarcodeState = (Button)v.findViewById(R.id.bt_get_barcode_state);
        mGetBarcodeState.setOnClickListener(buttonListener);

        mEnableMulti = (Button)v.findViewById(R.id.bt_enable_multi);
        mEnableMulti.setOnClickListener(buttonListener);
        mDisableMulti = (Button)v.findViewById(R.id.bt_disable_multi);
        mDisableMulti.setOnClickListener(buttonListener);
        mGetMultiState = (Button)v.findViewById(R.id.bt_get_multi_state);
        mGetMultiState.setOnClickListener(buttonListener);

        //<-[20250825]Add barcode/camera info
        mGetBarcodeCameraInfoBt = (Button) v.findViewById(R.id.bt_barcode_get_info);
        mGetBarcodeCameraInfoBt.setOnClickListener(buttonListener);
        //[20250825]Add barcode/camera info->

        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = BTReader.getReader(mContext, mBarcodeHandler);

        if (mReader != null) {
            mBarcodeHWSpin.setSelection(mReader.BC_GetBarcodeKeyFormat());
            mBarcodeTriggerSpin.setSelection(mReader.BC_GetBarcodeTriggerMode());
            mBarcodeMultiSpin.setSelection(mReader.BC_GetBarcodeMultiScanNumber() - 1);
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
            int ret = -100;
            String retString = null;
            int mode;
            int id = v.getId();
            if(id == R.id.bt_settigger_rfid){
                retString = "SD_RFID_Mode ";
                mode = mReader.SD_GetTriggerMode();
                if (mode == SDConsts.SDTriggerMode.BARCODE)
                    ret = mReader.SD_SetTriggerMode(SDConsts.SDTriggerMode.RFID);
                if (D) Log.d(TAG, "set rfid cur mode = " + mode + " ret = " + ret);
            }
            //<-[20250825]Add barcode/camera info
            else if(id == R.id.bt_barcode_get_info){
                retString = "SD_BarcodeCamera_Info ";
                int info  = mReader.BC_GetSupportedDevicesInfo();
                retString +=" : " + info;
                if (D) Log.d(TAG, "get barcode/camera cur info = " + info);
            }
            //[20250825]Add barcode/camera info->
            else if(id == R.id.bt_settigger_barcode){
                retString = "SD_BARCODE_Mode ";
                mode = mReader.SD_GetTriggerMode();
                if (mode == SDConsts.SDTriggerMode.RFID)
                    ret = mReader.SD_SetTriggerMode(SDConsts.SDTriggerMode.BARCODE);
                if (D) Log.d(TAG, "set barcode cur mode = " + mode + " ret = " + ret);
            }else if(id == R.id.bt_trigger){
                retString = "SD_GetTriggerMode ";
                ret = mReader.SD_GetTriggerMode();
                if (ret == SDConsts.SDTriggerMode.BARCODE)
                    if (D) Log.d(TAG, "barcode");
                    else if (ret == SDConsts.SDTriggerMode.RFID)
                        if (D) Log.d(TAG, "rfid");
                        else
                        if (D) Log.d(TAG, "other state");
                if (D) Log.d(TAG, "get trigger mode = " + ret);
            }else if(id == R.id.bt_barcode_press){
                retString = "BC_SetTriggerState ";
                ret = mReader.BC_SetTriggerState(true);
            }else if(id == R.id.bt_barcode_release){
                retString = "BC_SetTriggerState ";
                ret = mReader.BC_SetTriggerState(false);
            }else if(id == R.id.bt_mode_key_enable){
                retString = "SD_SetModeKeyEnable ";
                ret = mReader.SD_SetModeKeyEnable(SDConsts.SDModeKeyState.ENABLE);
                if (D) Log.d(TAG, "set modekey enable = " + ret);
            }else if(id == R.id.bt_mode_key_disable){
                retString = "SD_SetModeKeyEnable ";
                ret = mReader.SD_SetModeKeyEnable(SDConsts.SDModeKeyState.DISABLE);
                if (D) Log.d(TAG, "set modekey disable = " + ret);
            }else if(id == R.id.bt_get_mode_key_state){
                retString = "SD_GetModeKeyEnableState ";
                ret = mReader.SD_GetModeKeyEnableState();
                if (D) Log.d(TAG, "get modekey enable state = " + ret);
            }else if(id == R.id.bt_multi_scan_type_enable){
                retString = "BC_SetBarcodeMultiScanType Enable";
                ret = mReader.BC_SetBarcodeMultiScanType(SDConsts.BCMultiScanType.ENABLE);
                if (D) Log.d(TAG, "set multi scan type enable = " + ret);
            }else if(id == R.id.bt_multi_scan_type_disable){
                retString = "BC_SetBarcodeMultiScanType Disable";
                ret = mReader.BC_SetBarcodeMultiScanType(SDConsts.BCMultiScanType.DISABLE);
                if (D) Log.d(TAG, "set multi scan type disable = " + ret);
            }else if(id == R.id.bt_get_multi_scan_type_state){
                retString = "BC_GetBarcodeMultiScanType ";
                ret = mReader.BC_GetBarcodeMultiScanType();
                if (D) Log.d(TAG, "get multi scan type = " + ret);
            }else if(id == R.id.bt_get_multi_number){
                retString = "BC_GetBarcodeMultiScanNumber ";
                ret = mReader.BC_GetBarcodeMultiScanNumber();
                if (D) Log.d(TAG, "get barcode multi number = " + ret);
            }else if(id == R.id.bt_set_multi_number){
                retString = "BC_SetBarcodeMultiScanNumber ";
                ret = mReader.BC_SetBarcodeMultiScanNumber(
                        mBarcodeMultiSpin.getSelectedItemPosition() + 1);
                if (D) Log.d(TAG, "set barcode multi number = " + ret);
            }else if(id == R.id.bt_get_trigger){
                retString = "BC_GetBarcodeTriggerMode ";
                ret = mReader.BC_GetBarcodeTriggerMode();
                if (D) Log.d(TAG, "get barcode trigger state = " + ret);
            }else if(id == R.id.bt_set_trigger){
                retString = "BC_SetBarcodeTriggerMode ";
                ret = mReader.BC_SetBarcodeTriggerMode(mBarcodeTriggerSpin.getSelectedItemPosition());
                if (D) Log.d(TAG, "set barcode trigger state = " + ret);
            }else if(id == R.id.bt_get_hw_key){
                retString = "BC_GetBarcodeKeyFormat ";
                ret = mReader.BC_GetBarcodeKeyFormat();
                if (D) Log.d(TAG, "get barcode hw state = " + ret);
            }else if(id == R.id.bt_set_hw_key){
                retString = "BC_SetBarcodeKeyFormat ";
                ret = mReader.BC_SetBarcodeKeyFormat(mBarcodeHWSpin.getSelectedItemPosition());
                if (D) Log.d(TAG, "set barcode hw state = " + ret);
            }else if(id == R.id.bt_barcode_pause){
                retString = "BC_PauseBarcode ";
                if (mReader.BC_GetBarcodeState() == SDConsts.BCState.ACTIVE) {
                    ret = mReader.BC_PauseBarcode();
                    if (D) Log.d(TAG, "pause barcode result = " + ret);
                }
            }else if(id == R.id.bt_barcode_resume){
                retString = "BC_ResumeBarcode ";
//                    if (mReader.BC_GetBarcodeState() == SDConsts.BCState.PAUSED) {
//                        ret = mReader.BC_ResumeBarcode();
//                        if (D) Log.d(TAG, "resume barcode result = " + ret);
//                    }
                if (mReader.BC_GetBarcodeState() == SDConsts.BCState.PAUSED) {
                    mReader.BC_ResumeBarcode(new BCResumeListener() {
                        @Override
                        public void onCompleted(int resumeResult) {
                            if (D) Log.d(TAG, "onCompleted = " + resumeResult);
                            mMessageTextView.setText("BC_ResumeBarcode " + resumeResult);
                        }
                    });
                }
            }else if(id == R.id.bt_get_barcode_state){
                retString = "BC_GetBarcodeState ";
                ret = mReader.BC_GetBarcodeState();
                if (D) Log.d(TAG, "get barcode state = " + ret);
            }else if(id == R.id.bt_enable_multi){
                retString = "BC_SetBarcodeMultiScan ENABLE";
                ret = mReader.BC_SetBarcodeMultiScan(SDConsts.BCMultiScanState.ENABLE);
                if (D) Log.d(TAG, "enable multi result = " + ret);
            }else if(id == R.id.bt_disable_multi){
                retString = "BC_SetBarcodeMultiScan DISABLE ";
                ret = mReader.BC_SetBarcodeMultiScan(SDConsts.BCMultiScanState.DISABLE);
                if (D) Log.d(TAG, "disable multi result = " + ret);
            }else if(id == R.id.bt_get_multi_state){
                retString = "BC_GetBarcodeMultiScanState ";
                ret = mReader.BC_GetBarcodeMultiScanState();
                if (D) Log.d(TAG, "get multi state = " + ret);
            }
            if (ret != -100)
                retString += ret;
            mMessageTextView.setText(" " + retString);
        }
    };

    private static class BarcodeHandler extends Handler {
        private final WeakReference<BarcodeFragment> mExecutor;
        public BarcodeHandler(BarcodeFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            BarcodeFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mBarcodeHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_PRESSED)
                    mMessageTextView.setText(" " + "TRIGGER_PRESSED");
                else if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_RELEASED)
                    mMessageTextView.setText(" " + "TRIGGER_RELEASED");
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_MODE_CHANGED)
                    mMessageTextView.setText(" " + "SLED_MODE_CHANGED " + m.arg2);
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                }
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    //+smart batter -critical temper
                    if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                        Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                    //smart batter -critical temper+

                    //+Always be display Battery
                    Activity activity = getActivity();
                    if (activity != null) {
                        if (mOptionHandler != null) {
                            Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                            mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                        }
                    }
                    //Always be display Battery+
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

            case SDConsts.Msg.BCMsg:
                StringBuilder readData = new StringBuilder();
                if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED)
                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_PRESSED");
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED)
                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_RELEASED");
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_READ) {
                    if (D) Log.d(TAG, "BC_MSG_BARCODE_READ");
                    if (m.arg2 == SDConsts.BCResult.SUCCESS)
                        readData.append(" " + "BC_MSG_BARCODE_READ");
                    else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT)
                        readData.append(" " + "BC_MSG_BARCODE_ACCESS_TIMEOUT");
                    if (m.obj != null && m.obj instanceof String) {
                        readData.append("\n" + (String)m.obj);
                        mDataTextView.setText(" " + (String)m.obj);
                    }
                    mMessageTextView.setText(" " + readData.toString());
                }
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_ERROR) {
                    if (D) Log.d(TAG, "BARCODE_ERROR");
                    if (m.arg2 == SDConsts.BCResult.LOW_BATTERY)
                        readData.append(" " + "BARCODE_ERROR Low battery");
                    else
                        readData.append(" " + "BARCODE_ERROR ");
                    readData.append("barcode pasue");
                }
                if (D) Log.d(TAG, "data = " + readData.toString());
                break;

            case SDConsts.Msg.BTMsg:
                if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_CONNECTION_STATE_CHANGED) {
                    if (D) Log.d(TAG, "SLED_BT_CONNECTION_STATE_CHANGED = " + m.arg2);
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_CONNECT_STATE_CHANGED).sendToTarget();
                }
                break;
        }
    }
}