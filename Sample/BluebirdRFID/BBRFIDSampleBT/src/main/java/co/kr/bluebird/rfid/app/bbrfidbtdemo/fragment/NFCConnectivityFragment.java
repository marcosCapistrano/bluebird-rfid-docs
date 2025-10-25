/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfidbtdemo.fragment;

import java.lang.ref.WeakReference;
import java.util.Set;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.Constants;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.R;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.permission.PermissionHelper;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.control.ListItem;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.control.TagListAdapter;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.utils.Utils;
import co.kr.bluebird.sled.BTReader;
import co.kr.bluebird.sled.SDConsts;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class NFCConnectivityFragment extends Fragment {
    private static final String TAG = NFCConnectivityFragment.class.getSimpleName();

    private static final boolean D = Constants.CON_D;

    private TextView mActionTextView;

    private TextView mMessageTextView;

    private TextView mConnectedDeviceTextView;

    private Button mDisconnectBt;

    private Button mEnableBt;

    private Button mDisableBt;

    private Button mGetBtStateBt;

    private Button mScanBt;

    private Button mStopScanBt;

    private Button mRemoveAllPairedBt;

    private Button mNfcBt;

    private Button mQrBt;


    private BTReader mReader;

    private Context mContext;

    private Handler mOptionHandler;

    private TagListAdapter mAdapter;

    private ListView mDeviceList;

    private ProgressBar mProgressBar;

    private final ConnectivityHandler mConnectivityHandler = new ConnectivityHandler(this);

    public static NFCConnectivityFragment newInstance() {
        return new NFCConnectivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.nfc_connectivity_frag, container, false);
        mContext = inflater.getContext();

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mActionTextView = (TextView)v.findViewById(R.id.action_textview);

        mMessageTextView = (TextView)v.findViewById(R.id.message_textview);

        mConnectedDeviceTextView = (TextView)v.findViewById(R.id.connected_device_textview);

        mDisconnectBt = (Button)v.findViewById(R.id.bt_disconnect);
        mDisconnectBt.setOnClickListener(buttonListener);

        mEnableBt = (Button)v.findViewById(R.id.bt_enable);
        mEnableBt.setOnClickListener(buttonListener);
        mEnableBt.setVisibility(View.INVISIBLE);

        mDisableBt = (Button)v.findViewById(R.id.bt_disable);
        mDisableBt.setOnClickListener(buttonListener);
        mDisableBt.setVisibility(View.INVISIBLE);

        mGetBtStateBt = (Button)v.findViewById(R.id.bt_state);
        mGetBtStateBt.setOnClickListener(buttonListener);
        mGetBtStateBt.setVisibility(View.INVISIBLE);

        mScanBt = (Button)v.findViewById(R.id.bt_scan);
        mScanBt.setOnClickListener(buttonListener);
        mScanBt.setVisibility(View.INVISIBLE);

        mStopScanBt = (Button)v.findViewById(R.id.bt_stop_scan);
        mStopScanBt.setOnClickListener(buttonListener);
        mStopScanBt.setVisibility(View.INVISIBLE);

        mRemoveAllPairedBt = (Button)v.findViewById(R.id.bt_remove_pair);
        mRemoveAllPairedBt.setOnClickListener(buttonListener);
        mRemoveAllPairedBt.setVisibility(View.INVISIBLE);

        mNfcBt = (Button)v.findViewById(R.id.conn_nfc);
        mNfcBt.setOnClickListener(buttonListener);
        mNfcBt.setVisibility(View.INVISIBLE);

        mQrBt = (Button)v.findViewById(R.id.conn_qr);
        mQrBt.setOnClickListener(buttonListener);
        mQrBt.setVisibility(View.INVISIBLE);

        mProgressBar = (ProgressBar)v.findViewById(R.id.scan_progress);

        mDeviceList = (ListView)v.findViewById(R.id.device_list);
        mDeviceList.setOnItemClickListener(listListener);
        mAdapter = new TagListAdapter(mContext);
        mDeviceList.setAdapter(mAdapter);

        //+NFC
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            Toast.makeText(mContext, (String) "NFC is off. Please turn on the NFC", Toast.LENGTH_LONG).show();
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
            else
                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
        //NFC+
        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = BTReader.getReader(mContext, mConnectivityHandler);
        if (mReader != null) {
            if (mReader.BT_IsEnabled())
                addPairedDevices();
            else{
                if(mReader.BT_Enable())
                    addPairedDevices();
                else
                if (D) Log.d(TAG, "BT DISABLED");
            }

            if (mReader.BT_GetConnectState() == SDConsts.BTConnectState.CONNECTED)
                updateConnectedInfo(mReader.BT_GetConnectedDeviceName() + "\n" + mReader.BT_GetConnectedDeviceAddr());
            else
                updateConnectedInfo("");
        }
        updateConnectStateTextView();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private OnClickListener buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int ret = -100;
            String retString = null;
            Set<BluetoothDevice> pairedDevices = null;
            int id = v.getId();
            if(id == R.id.bt_disconnect){
                if (mReader.BT_Disconnect() == SDConsts.BTResult.SUCCESS)
                    retString = "Disconnect";
                else
                    retString = "Disconnect failed";
            }else if(id == R.id.bt_enable){
                if (mReader.BT_Enable())
                    retString = "Bluetooth Enable";
                else
                    retString = "Bluetooth Enable failed";
            }else if(id == R.id.bt_disable){
                mAdapter.removeAllItem();
                if (mReader.BT_Disable())
                    retString = "Bluetooth Disable";
                else
                    retString = "Bluetooth Disable failed";
            }else if(id == R.id.bt_state){
                if (mReader.BT_IsEnabled())
                retString += "Enabled";
                    else
                retString += "Disabled";
            }else if(id == R.id.bt_scan){
                addPairedDevices();
                boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mLocationPerms[0]);
                if (b) {
                    if (mReader.BT_StartScan()) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        retString = "Bluetooth Scan";
                    } else
                        retString = "Bluetooth Scan failed";
                }
                else
                    PermissionHelper.requestPermission(getActivity(), PermissionHelper.mLocationPerms);
            }else if(id == R.id.bt_stop_scan){
                if (mReader.BT_StopScan()) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    retString = "Bluetooth Stop Scan";
                }
                else
                    retString = "Bluetooth Stop Scan failed";
            }else if(id == R.id.bt_remove_pair){
                if (mReader.BT_StopScan())
                    mProgressBar.setVisibility(View.INVISIBLE);
                pairedDevices = mReader.BT_GetPairedDevices();
                if (pairedDevices != null && pairedDevices.size() > 0) {
                    for (BluetoothDevice d : pairedDevices)
                        mReader.BT_UnpairDevice(d.getAddress());
                    retString = "Bluetooth Remove All Paired";
                }
                mAdapter.removeAllItem();
            }

            if (ret != -100) {
                retString += ret;
            }
            mActionTextView.setText(" " + retString);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (requestCode) {
                case PermissionHelper.REQ_PERMISSION_CODE:
                    if (permissions != null) {
                        boolean hasResult = false;
                        for (String p : permissions) {
                            if (p.equals(PermissionHelper.mLocationPerms[0])) {
                                hasResult = true;
                                break;
                            }
                        }
                        if (hasResult) {
                            if (grantResults != null && grantResults.length != 0 &&
                                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                if (mReader.BT_StartScan())
                                    mProgressBar.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                    break;
            }
        }
    }

    private OnItemClickListener listListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // TODO Auto-generated method stub
            ListItem li = (ListItem) mAdapter.getItem(arg2);
            int result = -100;
            if (mReader.BT_GetConnectState() != SDConsts.BTConnectState.CONNECTED) {
                result = mReader.BT_Connect(li.mDt);
                mActionTextView.setText(" " + "Connect result = " + result);
            }
            else {
                result = mReader.BT_Disconnect();
                mActionTextView.setText(" " + "Disconnect result = " + result);
            }
            if (D) Log.d(TAG, "Click Result = " + result);
        }
    };

    private static class ConnectivityHandler extends Handler {
        private final WeakReference<NFCConnectivityFragment> mExecutor;
        public ConnectivityHandler(NFCConnectivityFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            NFCConnectivityFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mConnectivityHandler");
        if (true) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
        String receivedData = "";
        switch (m.what) {
            case SDConsts.Msg.BTMsg:
                if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_DEVICE_FOUND) {
                    receivedData = "SLED_BT_DEVICE_FOUND";
                    if (m.obj != null  && m.obj instanceof Bundle) {
                        Bundle b = (Bundle) m.obj;
                        String name = b.getString(SDConsts.BT_BUNDLE_NAME_KEY);
                        String addr = b.getString(SDConsts.BT_BUNDLE_ADDR_KEY);
                        int bondState = b.getInt(SDConsts.BT_BUNDLE_BOND_STATE_KEY);
                        if (D)
                            Log.d(TAG, "SLED_BT_DEVICE_FOUND " + name + " " + addr + " " + bondState);
                        mAdapter.addItem(-1, name, addr, false, false);
                    }
                }
                else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_BOND_STATE_CHAGNED) {
                    receivedData = "SLED_BT_BOND_STATE_CHAGNED";
                    if (m.obj != null  && m.obj instanceof String) {
                        Bundle b = (Bundle)m.obj;
                        String name = b.getString(SDConsts.BT_BUNDLE_NAME_KEY);
                        String addr = b.getString(SDConsts.BT_BUNDLE_ADDR_KEY);
                        int bondState = b.getInt(SDConsts.BT_BUNDLE_BOND_STATE_KEY);
                        int newBondState = b.getInt(SDConsts.BT_BUNDLE_BOND_NEW_STATE_KEY);
                        int prevBondState = b.getInt(SDConsts.BT_BUNDLE_BOND_PREV_STATE_KEY);
                        if (D) Log.d(TAG, "SLED_BT_BOND_STATE_CHAGNED " + name + " " + addr + " " + bondState + " " + newBondState + " " + prevBondState);
                    }
                }
                else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_PAIRING_REQUEST) {
                    receivedData = "SLED_BT_PAIRING_REQUEST";
                    updateConnectStateTextView();
                }
                else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_DISCOVERY_STARTED) {
                    receivedData = "SLED_BT_DISCOVERY_STARTED";
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_DISCOVERY_FINISHED) {
                    receivedData = "SLED_BT_DISCOVERY_FINISHED";
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_STATE_CHANGED) {
                    receivedData = "SLED_BT_STATE_CHANGED";
                    if (m.obj != null  && m.obj instanceof String) {
                        Bundle b = (Bundle)m.obj;
                        int newBondState = b.getInt(SDConsts.BT_BUNDLE_BOND_NEW_STATE_KEY);
                        int prevBondState = b.getInt(SDConsts.BT_BUNDLE_BOND_PREV_STATE_KEY);
                        if (D) Log.d(TAG, "SLED_BT_STATE_CHANGED " + newBondState + " " + prevBondState);
                    }
                    if (mReader.BT_IsEnabled())
                        addPairedDevices();
                    if (D) Log.d(TAG, "BT State changed = " + m.arg2);
                }
                else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_CONNECTION_STATE_CHANGED) {
                    receivedData = "SLED_BT_CONNECTION_STATE_CHANGED";
                    if (D) Log.d(TAG, "SLED_BT_CONNECTION_STATE_CHANGED = " + m.arg2);
                    updateConnectStateTextView();
                }
                else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_CONNECTION_ESTABLISHED) {
                    receivedData = "SLED_BT_CONNECTION_ESTABLISHED";
                    updateConnectedInfo(mReader.BT_GetConnectedDeviceName() + "\n" + mReader.BT_GetConnectedDeviceAddr());
                    addPairedDevices();
                }
                else if  (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_DISCONNECTED) {
                    receivedData = "SLED_BT_DISCONNECTED";
                    updateConnectedInfo("");
                }
                else if  (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_CONNECTION_LOST) {
                    receivedData = "SLED_BT_CONNECTION_LOST";
                    updateConnectedInfo("");
                }
                else if  (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_ACL_CONNECTED) {
                    receivedData = "SLED_BT_ACL_CONNECTED";
                    if (m.obj != null  && m.obj instanceof String) {
                        Bundle b = (Bundle)m.obj;
                        String name = b.getString(SDConsts.BT_BUNDLE_NAME_KEY);
                        String addr = b.getString(SDConsts.BT_BUNDLE_ADDR_KEY);
                        int bondState = b.getInt(SDConsts.BT_BUNDLE_BOND_STATE_KEY);
                        if (D) Log.d(TAG, "SLED_BT_ACL_CONNECTED " + name + " " + addr + " " + bondState);
                    }
                    //updateConnectedInfo("");
                }
                else if  (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_ACL_DISCONNECT_REQUESTED) {
                    receivedData = "SLED_BT_ACL_DISCONNECT_REQUESTED";
                    updateConnectedInfo("");
                }
                else if  (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_ACL_DISCONNECTED) {
                    receivedData = "SLED_BT_ACL_DISCONNECTED";
                    if (m.obj != null  && m.obj instanceof String) {
                        Bundle b = (Bundle)m.obj;
                        String name = b.getString(SDConsts.BT_BUNDLE_NAME_KEY);
                        String addr = b.getString(SDConsts.BT_BUNDLE_ADDR_KEY);
                        int bondState = b.getInt(SDConsts.BT_BUNDLE_BOND_STATE_KEY);
                        if (D) Log.d(TAG, "SLED_BT_ACL_DISCONNECTED " + name + " " + addr + " " + bondState);
                    }
                    updateConnectedInfo("");
                }
                else if  (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_ADAPTER_CONNECTION_STATE_CHANGED) {
                    receivedData = "SLED_BT_ADAPTER_CONNECTION_STATE_CHANGED";
                    if (m.obj != null  && m.obj instanceof String) {
                        Bundle b = (Bundle)m.obj;
                        String name = b.getString(SDConsts.BT_BUNDLE_NAME_KEY);
                        String addr = b.getString(SDConsts.BT_BUNDLE_ADDR_KEY);
                        int bondState = b.getInt(SDConsts.BT_BUNDLE_BOND_STATE_KEY);
                        int newConState = b.getInt(SDConsts.BT_BUNDLE_CON_NEW_STATE_KEY);
                        int prevConState = b.getInt(SDConsts.BT_BUNDLE_CON_PREV_STATE_KEY);
                        if (D) Log.d(TAG, "SLED_BT_CONNECTION_STATE_CHANGED " + name + " " + addr + " " + bondState + " " + newConState + " " +  prevConState);
                    }
                }
                break;
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    //+smart batter -critical temper
                    if (m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
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
                break;
        }
        mMessageTextView.setText(receivedData);
    }

    private void addPairedDevices() {
        if (mAdapter != null && mReader != null) {
            mAdapter.removeAllItem();
            Set<BluetoothDevice> pairedDevices = mReader.BT_GetPairedDevices();
            if (pairedDevices != null && pairedDevices.size() > 0) {
                for (BluetoothDevice d : pairedDevices)
                    mAdapter.addItem(-1, d.getName() + "\n" + "[paired device]", d.getAddress(), false, false);
            }
        }
    }

    private void updateConnectedInfo(String info) {
        if (mReader != null && mConnectedDeviceTextView != null) {
            mConnectedDeviceTextView.setText(info);
            mConnectedDeviceTextView.setTextColor(Color.BLUE);
            if (info != null && info != "")
                mDisconnectBt.setVisibility(View.VISIBLE);
            else
                mDisconnectBt.setVisibility(View.INVISIBLE);
        }
    }

    private void updateConnectStateTextView() {
        if (mOptionHandler != null)
            mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_CONNECT_STATE_CHANGED).sendToTarget();
    }
}