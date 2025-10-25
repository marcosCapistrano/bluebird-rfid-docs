/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 *
 * Author : Bogon Jun
 *
 * Date : 2016.04.04
 */

package co.kr.bluebird.rfid.app.bbrfidbtdemo;

import co.kr.bluebird.rfid.app.bbrfidbtdemo.fragment.*;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.permission.PermissionHelper;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.utils.Utils;
import co.kr.bluebird.sled.BTReader;
import co.kr.bluebird.sled.SDConsts;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.legacy.app.ActionBarDrawerToggle;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final boolean D = Constants.MAIN_D;

    //+Barcode BT paring
    private static final String ACTION_BARCODE_CALLBACK_DECODING_DATA = "kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_DECODING_DATA";
    private static final String EXTRA_BARCODE_DECODING_DATA = "EXTRA_BARCODE_DECODING_DATA";
    private String MAC = "";
    //Barcode BT paring+
    public static final int MSG_OPTION_DISCONNECTED = 0;

    public static final int MSG_OPTION_CONNECT_STATE_CHANGED = 0;

    public static final int MSG_BACK_PRESSED = 2;

    public static final int MSG_BATT_NOTI = 3;//Always be display Battery

    private String[] mFunctionsString;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private BTReader mReader;

    private Context mContext;

    private FragmentManager mFragmentManager;

    private boolean mIsConnected;

    public boolean mSledUpdate = false;

    private BTConnectivityFragment mBTConnectivityFragment;
    private SDFragment mSDFragment;
    private RFAccessFragment mRFAccessFragment;
    private RFConfigFragment mRFConfigFragment;
    private RFSelectionFragment mRFSelectionFragment;
    private RapidFragment mRapidFragment;
    private InventoryFragment mInventoryFragment;
    private BarcodeFragment mBarcodeFragment;
    private SBBarcodeFragment mSBBarcodeFragment;
    private InfoFragment mInfoFragment;
    private BatteryFragment mBatteryFragment;
    //+NFC/QR Fragment
    public NFCConnectivityFragment mNFCConnectivityFragment;
    public BarCodeConnectivityFragment mQRConnectivityFragment;
    //+NFC/QR Fragment+

    private LinearLayout mUILayout;

    private Fragment mCurrentFragment;

    private ImageButton mConnectButton;
    private ImageButton mSDFunctionButton;
    private ImageButton mRFConfigButton;
    private ImageButton mRFAccessButton;
    private ImageButton mRFSelectButton;
    private ImageButton mRapidButton;
    private ImageButton mInventoryButton;
    private ImageButton mBarcodeButton;
    private ImageButton mSBBarcodeButton;
    private ImageButton mBatteryButton;
    private ImageButton mInformationButton;
    private ImageView mCILogoImage;
    private ImageView mCILogoImage2;

    private TestFragment mTestFragment;

    Menu mMenu;//Always be display Battery

    private String modelIDStr;

    private final MainHandler mMainHandler = new MainHandler(this);

    public final UpdateConnectHandler mUpdateConnectHandler = new UpdateConnectHandler(this);

    //+NFC BT paring
    private BluetoothAdapter mBluetoothAdapter;
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mNfcTechLists;
    private static final int MSG_SHOW_TOAST = 1000;
    private static final int MSG_SET_TEXT = 1001;
    //NFC BT paring+

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mUILayout = (LinearLayout)findViewById(R.id.ui_layout);

        mCurrentFragment = null;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int buttonHeight = size.x / 3;

        mConnectButton = (ImageButton)findViewById(R.id.connect_bt);
        mConnectButton.setMinimumHeight(buttonHeight);

        mSDFunctionButton = (ImageButton)findViewById(R.id.sdfunc_bt);
        mSDFunctionButton.setMinimumHeight(buttonHeight);

        mRFConfigButton = (ImageButton)findViewById(R.id.rfconf_bt);
        mRFConfigButton.setMinimumHeight(buttonHeight);

        mRFAccessButton = (ImageButton)findViewById(R.id.rfacc_bt);
        mRFAccessButton.setMinimumHeight(buttonHeight);

        mRFSelectButton = (ImageButton)findViewById(R.id.rfsel_bt);
        mRFSelectButton.setMinimumHeight(buttonHeight);

        mRapidButton = (ImageButton)findViewById(R.id.rapid_bt);
        mRapidButton.setMinimumHeight(buttonHeight);

        mInventoryButton = (ImageButton)findViewById(R.id.inv_bt);
        mInventoryButton.setMinimumHeight(buttonHeight);

        mBarcodeButton = (ImageButton)findViewById(R.id.bar_bt);
        mBarcodeButton.setMinimumHeight(buttonHeight);

        mSBBarcodeButton = (ImageButton)findViewById(R.id.bar_sb_bt);
        mSBBarcodeButton.setMinimumHeight(buttonHeight);

        mBatteryButton = (ImageButton) findViewById(R.id.bat_bt);
        mBatteryButton.setMinimumHeight(buttonHeight);

        mInformationButton = (ImageButton)findViewById(R.id.info_bt);
        mInformationButton.setMinimumHeight(buttonHeight);

//        mCILogoImage = (ImageView)findViewById(R.id.ci_logo);
//        mCILogoImage.setMinimumHeight(buttonHeight);

//        mCILogoImage2 = (ImageView)findViewById(R.id.ci_logo2);
//        mCILogoImage2.setMinimumHeight(buttonHeight);

        mConnectButton.setOnClickListener(buttonListener);
        mSDFunctionButton.setOnClickListener(buttonListener);
        mRFConfigButton.setOnClickListener(buttonListener);
        mRFAccessButton.setOnClickListener(buttonListener);
        mRFSelectButton.setOnClickListener(buttonListener);
        mRapidButton.setOnClickListener(buttonListener);
        mInventoryButton.setOnClickListener(buttonListener);
        mBarcodeButton.setOnClickListener(buttonListener);
        mSBBarcodeButton.setOnClickListener(buttonListener);
        mBatteryButton.setOnClickListener(buttonListener);
        mInformationButton.setOnClickListener(buttonListener);

        ImageButton testButton = (ImageButton) findViewById(R.id.test_bt);
        testButton.setMinimumHeight(buttonHeight);
        testButton.setOnClickListener(buttonListener);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.drawable.ic_launcher, R.string.drawer_open, R.string.drawer_close) {
            String mDrawerTitle = "Functions";

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        mFunctionsString = getResources().getStringArray(R.array.functions_array_new);//NFC/QR Fragment
//        mFunctionsString = getResources().getStringArray(R.array.functions_array);//NFC/QR Fragment
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFunctionsString));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mFragmentManager = getFragmentManager();

        mIsConnected = false;

        //+NFC BT paring
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);

        Intent intent = new Intent(mContext, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_MUTABLE);
        IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefIntent.addDataType("*/*");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        mIntentFilters = new IntentFilter[]{ndefIntent,};
        mNfcTechLists = new String[][]{new String[]{NfcF.class.getName()}};
        //NFC BT paring+
    }

    //+CAMERA BARCODE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "1. onActivityResult::requestCode = "+requestCode+" ,resultCode = "+resultCode+" ,data = "+data);

        if(resultCode != RESULT_OK){
            Log.d(TAG, "Request Fail! Result = " + resultCode);
            return;
        }

        if(requestCode == Constants.REQUEST_CODE)
            mQRConnectivityFragment.onActivityResult(requestCode, resultCode, data);
        else
            Log.d(TAG, "Other Request Code! requestCode = " + resultCode);
    }
    //CAMERA BARCODE+

    public View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = 0;
            int resId = v.getId();
            //
            if(resId ==  R.id.connect_bt){
                id = Constants.Fragment.CONNECTIVITY;
            }else if(resId ==  R.id.nfc_connect_bt){
                id = Constants.Fragment.NFC_CONNECT;
            }else if(resId ==  R.id.qr_connect_bt){
                id = Constants.Fragment.BARCODE_CONNECT;
            }else if(resId ==  R.id.sdfunc_bt){
                id = Constants.Fragment.SD_FUNCTION;
            }else if(resId ==  R.id.rfconf_bt){
                id = Constants.Fragment.RF_CONFIG;
            }else if(resId ==  R.id.rfacc_bt){
                id = Constants.Fragment.RF_ACCESS;
            }else if(resId ==  R.id.rfsel_bt){
                id = Constants.Fragment.RF_SELECTION;
            }else if(resId ==  R.id.rapid_bt){
                id = Constants.Fragment.RAPID_READ;
            }else if(resId ==  R.id.inv_bt){
                id = Constants.Fragment.INVENTORY;
            }else if(resId ==  R.id.bar_bt){
                id = Constants.Fragment.BARCODE_BC;
            }else if(resId ==  R.id.bar_sb_bt){
                id = Constants.Fragment.BARCODE_SB;
            }else if(resId ==  R.id.bat_bt){
                id = Constants.Fragment.BATTERY;
            }else if(resId ==  R.id.info_bt){
                id = Constants.Fragment.INFO;
            }else if(resId ==  R.id.test_bt){
                id = Constants.Fragment.TEST;
            }
            selectItem(id);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //+Always be display Battery
        mMenu = menu;
        if (mReader != null && mReader.BT_GetConnectState() == SDConsts.BTConnectState.CONNECTED) {
            modelIDStr = (mReader.SD_GetModel() == SDConsts.MODEL.RFR900) ? getString(R.string.rfr900) : getString(R.string.rfr901);
            int value = mReader.SD_GetBatteryStatus();
            menu.getItem(2).setVisible(true);
            menu.getItem(2).setTitle(Integer.toString(value) + "%");
        }
        //Always be display Battery+

        if (mIsConnected)
            menu.getItem(0).setVisible(true);
        else
            menu.getItem(0).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connected) {
            Toast.makeText(this, modelIDStr + " " + getString(R.string.sled_connected_str), Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.action_home) {
            switchToHome();
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        switch (position) {
            case Constants.Fragment.TEST:
                if (mTestFragment == null)
                    mTestFragment = mTestFragment.newInstance();
                mCurrentFragment = mTestFragment;
                break;
            case Constants.Fragment.CONNECTIVITY:
                //+A12 BT permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mBTPerms[0]);
                    if(!b) {
                        PermissionHelper.requestPermission(this, PermissionHelper.mBTPerms);
                        Toast.makeText(mContext, getString(R.string.permission_not_allowed), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //A12 BT permission+
                if (mBTConnectivityFragment == null)
                    mBTConnectivityFragment = BTConnectivityFragment.newInstance();
                mCurrentFragment = mBTConnectivityFragment;
                break;
            //+NFC/QR Fragment
            case Constants.Fragment.NFC_CONNECT:
                if (mNFCConnectivityFragment == null)
                    mNFCConnectivityFragment = NFCConnectivityFragment.newInstance();
                mCurrentFragment = mNFCConnectivityFragment;
                break;
            case Constants.Fragment.BARCODE_CONNECT:
                if (mQRConnectivityFragment == null)
                    mQRConnectivityFragment = BarCodeConnectivityFragment.newInstance();
                mCurrentFragment = mQRConnectivityFragment;
                break;
            //NFC/QR Fragment+
            case Constants.Fragment.SD_FUNCTION:
                if (mSDFragment == null)
                    mSDFragment = SDFragment.newInstance();
                mCurrentFragment = mSDFragment;
                break;
            case Constants.Fragment.RF_CONFIG:
                if (mRFConfigFragment == null)
                    mRFConfigFragment = RFConfigFragment.newInstance();
                mCurrentFragment = mRFConfigFragment;
                break;
            case Constants.Fragment.RF_ACCESS:
                if (mRFAccessFragment == null)
                    mRFAccessFragment = RFAccessFragment.newInstance();
                mCurrentFragment = mRFAccessFragment;
                break;
            case Constants.Fragment.RF_SELECTION:
                if (mRFSelectionFragment == null)
                    mRFSelectionFragment = RFSelectionFragment.newInstance();
                mCurrentFragment = mRFSelectionFragment;
                break;
            case Constants.Fragment.RAPID_READ:
                if (mRapidFragment == null)
                    mRapidFragment = RapidFragment.newInstance();
                mCurrentFragment = mRapidFragment;
                break;
            case Constants.Fragment.INVENTORY:
                if (mInventoryFragment == null)
                    mInventoryFragment = InventoryFragment.newInstance();
                mCurrentFragment = mInventoryFragment;
                break;
            case Constants.Fragment.BARCODE_BC:
                if (mBarcodeFragment == null)
                    mBarcodeFragment = BarcodeFragment.newInstance();
                mCurrentFragment = mBarcodeFragment;
                break;
            case Constants.Fragment.BARCODE_SB:
                if (mSBBarcodeFragment == null)
                    mSBBarcodeFragment = SBBarcodeFragment.newInstance();
                mCurrentFragment = mSBBarcodeFragment;
                break;
            case Constants.Fragment.BATTERY:
                if (mBatteryFragment == null)
                    mBatteryFragment = BatteryFragment.newInstance();
                mCurrentFragment = mBatteryFragment;
                break;
            case Constants.Fragment.INFO:
                if (mInfoFragment == null)
                    mInfoFragment = InfoFragment.newInstance();
                mCurrentFragment = mInfoFragment;
                break;
            default:
                return;
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content, mCurrentFragment);
        ft.commit();
        mDrawerList.setItemChecked(position, true);
        setTitle(mFunctionsString[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        mUILayout.setVisibility(View.GONE);
    }

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, " onStart");
        if(!mSledUpdate) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            boolean openResult = false;
            mReader = BTReader.getReader(mContext, mMainHandler);
            if (mReader != null)
                openResult = mReader.SD_Open();
            if (openResult == SDConsts.RF_OPEN_SUCCESS) {
                Log.i(TAG, "Reader opened");
            } else if (openResult == SDConsts.RF_OPEN_FAIL)
                if (D) Log.e(TAG, "Reader open failed");

            updateConnectState();
        }
        super.onStart();
    }

    //+NFC BT paring
    @Override
    protected void onNewIntent(Intent intent) {
        if (D) Log.d(TAG, " onNewIntent");
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndefTag = null;

        if (tag != null) {
            if (D) Log.d(TAG, " onNewIntent::get tag");

            ndefTag = Ndef.get(tag);
            if (ndefTag == null) {
                if (D) Log.d(TAG, " onNewIntent::ndefTag is null");
                return;
            }
        } else {
            if (D) Log.d(TAG, " onNewIntent::tag is null");
            return;
        }

        int size = ndefTag.getMaxSize();
        String type = ndefTag.getType();
        String id = getStringFromBytes(tag.getId(), tag.getId().length);

        Log.d(TAG, "[size]" + String.valueOf(size) + " /[type]" + type + "/[id]" + id);

        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (messages == null) return;
        for (int i = 0; i < messages.length; i++) {
            setReadTagData((NdefMessage) messages[0]);
        }
    }
    //NFC BT paring+

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "onResume");
        registerReceiver();//Barcode BT paring
        //+NFC BT paring
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        //NFC BT paring+
        super.onResume();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, " onPause");
        unregisterReceiver();//Barcode BT paring
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onStop() {
        if (D) Log.d(TAG, " onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (D) Log.d(TAG, " onDestroy");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mReader = BTReader.getReader(mContext, mMainHandler);
        if (mReader != null && mReader.BT_GetConnectState() == SDConsts.BTConnectState.CONNECTED) {
            mReader.BT_Disconnect();
        }
        mReader.SD_Close();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mCurrentFragment != null)
                mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);

            switch (requestCode) {
                case PermissionHelper.REQ_PERMISSION_CODE:
                    if (permissions != null) {
                        boolean hasResult = false;
                        for (String p : permissions) {
                            if (p.equals(PermissionHelper.mBTPerms[0])) {
                                hasResult = true;
                                break;
                            }
                        }
                        if (hasResult) {
                            if (grantResults != null && grantResults.length != 0 &&
                                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                selectItem(Constants.Fragment.CONNECTIVITY);
                            }

                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment != null)
            switchToHome();
        else
            super.onBackPressed();
    }

    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mExecutor;
        public MainHandler(MainActivity ac) {
            mExecutor = new WeakReference<>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mMainHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                //+Always be display Battery
                if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    //+smart batter -critical temper
                    if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                        Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                    //smart batter -critical temper+

                    Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                    mMenu.getItem(2).setTitle(Integer.toString(m.arg2) + "%");
                }
                //Always be display Battery+

                //+Hotswap feature
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED) {
                    if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE)
                        Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                    else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE)
                        Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                }
                //Hotswap feature+
                break;
            case SDConsts.Msg.RFMsg:
                break;
            case SDConsts.Msg.BCMsg:
                break;
        }
    }

    private void switchToHome() {
        try {
            mDrawerLayout.closeDrawer(mDrawerList);
            if (mCurrentFragment != null) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.remove(mCurrentFragment);
                ft.commit();
                mCurrentFragment = null;
                mReader = BTReader.getReader(mContext, mMainHandler);
            }
            setTitle(getString(R.string.app_name));
            if (mUILayout.getVisibility() != View.VISIBLE)
                mUILayout.setVisibility(View.VISIBLE);
        }
        catch (java.lang.IllegalStateException e) {
        }
        return;
    }

    private void updateConnectState() {
        if (mReader.BT_GetConnectState() == SDConsts.BTConnectState.CONNECTED) {
            mIsConnected = true;
        }
        else
            mIsConnected = false;
        invalidateOptionsMenu();
    }

    private static class UpdateConnectHandler extends Handler {
        private final WeakReference<MainActivity> mExecutor;
        public UpdateConnectHandler(MainActivity ac) {
            mExecutor = new WeakReference<>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity executor = mExecutor.get();
            if (executor != null) {
                executor.handleUpdateConnectHandler(msg);
            }
        }
    }

    public void handleUpdateConnectHandler(Message m) {
        if (m.what == MSG_OPTION_CONNECT_STATE_CHANGED) {
            updateConnectState();
        }
        else if (m.what == MSG_BACK_PRESSED) {
            switchToHome();
        }
        //+Always be display Battery
        else if (m.what == MSG_BATT_NOTI){
            mMenu.getItem(2).setTitle(Integer.toString(m.arg2) + "%");
        }
        //Always be display Battery+
    }

    //+Barcode BT paring
    private void registerReceiver() {
        if (D) Log.d(TAG, "registerReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BARCODE_CALLBACK_DECODING_DATA);
        registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        if (D) Log.d(TAG, "unregisterReceiver");
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) Log.d(TAG, "BroadcastReceiver");
            String action = intent.getAction();
            if(action.equals(ACTION_BARCODE_CALLBACK_DECODING_DATA)) {
                byte[] data = intent.getByteArrayExtra(EXTRA_BARCODE_DECODING_DATA);
                String dataResult;

                if(data != null) {
                    dataResult = new String(data);
                    try {
                        dataResult = new String(data, "Shift-JIS");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,  "[Data] = " + dataResult);
                    MAC = dataResult;
                    connectBT(MAC);
                }
            }
        }
    };

    public void connectBT(String mac) {
        Log.i(TAG, "[BT_Connect] :: mac = " + mac);
        int result = -100;
        if (mReader.BT_GetConnectState() != SDConsts.BTConnectState.CONNECTED) {
            if(!checkBTAddress(mac)){
                // throw new IllegalArgumentException(mac + " is not a valid Bluetooth address");
                Log.i(TAG, "this is barcode val = " + mac + " ,is not a valid Bluetooth address ");
            }else {
                result = mReader.BT_Connect(mac);
                Log.i(TAG, "[BT_Connect]  = " + result);
            }
        }
    }
    private static final int ADDRESS_LENGTH = 17;
    public static boolean checkBTAddress(String address) {
        if (address == null || address.length() != ADDRESS_LENGTH) {
            return false;
        }
        for (int i = 0; i < ADDRESS_LENGTH; i++) {
            char c = address.charAt(i);
            switch (i % 3) {
                case 0:
                case 1:
                    if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                        // hex character, OK
                        break;
                    }
                    return false;
                case 2:
                    if (c == ':') {
                        break;  // OK
                    }
                    return false;
            }
        }
        return true;
    }
    //Barcode BT paring+

    //+NFC BT paring
    public void setReadTagData(NdefMessage ndefmsg) {
        Log.i(TAG, "setReadTagData");
        if (ndefmsg == null) {
            return;
        }
        String msgs = "";
        msgs += ndefmsg.toString() + "\n";
        NdefRecord[] records = ndefmsg.getRecords();

        for (NdefRecord rec : records) {
            byte[] payload = rec.getPayload();
            String textEncoding = "UTF-8";
            if (payload.length > 0)
                textEncoding = (payload[0] & 0200) == 0 ? "UTF-8" : "UTF-16";

            Short tnf = rec.getTnf();
            String type = String.valueOf(rec.getType());
            String payloadStr = new String(rec.getPayload(), Charset.forName(textEncoding));

            Log.d(TAG, "[NDEF RECORD tnf]" + tnf + " / [NDEF RECORD TYPE]" + rec.getType()
                    + "/ [NDEF RECORD PAYLOAD]" + getStringFromBytes(rec.getPayload(), rec.getPayload().length));

            byte[] payloadData = rec.getPayload();
            byte[] mac = new byte[6];
            System.arraycopy(payloadData, 2, mac, 0, 6);
            ByteBuffer PAYLOAD = ByteBuffer.wrap(mac);
            mac = parseMacFromBluetoothRecord(PAYLOAD);

            String macStr = "";
            for (int i = 0; i < mac.length; i++) {
                if (i != mac.length - 1)
                    macStr = macStr + String.format("%02X", mac[i] & 0xff) + ":";
                else
                    macStr = macStr + String.format("%02X", mac[i] & 0xff);
            }
            Log.i(TAG, "mac = " + macStr);

            //get new format separator
            byte[] separator = new byte[1];
            System.arraycopy(payloadData, payloadData.length - 3, separator, 0, 1);
            String separator_str = Utils.getStringFromBytes(separator, separator.length);
            Log.d(TAG, "##[NFC/BT_paring] SEPARATOR = " + separator_str);

            if(separator[0] == (byte)0x2F) { //new
                Log.v(TAG, "[NEW FORMAT]");
                //get dev_type_len
                byte[] dev_type_len = new byte[1];
                System.arraycopy(payloadData, payloadData.length - 2, dev_type_len, 0, 1);
                String dev_type_len_str = Utils.getStringFromBytes(dev_type_len, dev_type_len.length);
                Log.d(TAG, "##[NFC/BT_paring] DEVICE TYPE LEN = " + dev_type_len_str);

                //get dev_type
                byte[] dev_type = new byte[1];
                System.arraycopy(payloadData, payloadData.length - 1, dev_type, 0, 1);
                String dev_type_str = Utils.getStringFromBytes(dev_type, dev_type.length);
                Log.d(TAG, "##[NFC/BT_paring] DEVICE TYPE = " + dev_type_str);

                if(dev_type_len[0] == (byte)0x01
                        && (dev_type[0] == 0x01 || dev_type[0] == 0x02 || dev_type[0] == 0x03)) {
                    Log.v(TAG, "Connect BT with DEV_TYPE");
                    connectNFCBT(macStr, dev_type_str);
                    return;
                }
            }

            connectNFCBT(macStr, null);
        }
    }

    public static byte[] parseMacFromBluetoothRecord(ByteBuffer payload) {
        byte[] address = new byte[6];
        payload.get(address);
        // ByteBuffer.order(LITTLE_ENDIAN) doesn't work for
        // ByteBuffer.get(byte[]), so manually swap order
        for (int i = 0; i < 3; i++) {
            byte temp = address[i];
            address[i] = address[5 - i];
            address[5 - i] = temp;
        }
        return address;
    }

    private void connectNFCBT(String mac) {
        Log.i(TAG, "[connectNFCBT] :: mac = " + mac);
        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(mac);

        int result = -100;
        if (mReader.BT_GetConnectState() != SDConsts.BTConnectState.CONNECTED) {
            result = mReader.BT_Connect(mac);
        } else {
            result = mReader.BT_Disconnect();
        }
    }

    private void connectNFCBT(String mac,  String type) {
        Log.i(TAG, "[connectNFCBT] :: mac = " + mac);
        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(mac);

        if (mReader.BT_GetConnectState() != SDConsts.BTConnectState.CONNECTED) {
            mReader.BT_Connect(mac, type);
        }
        else {
            mReader.BT_Disconnect();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_TOAST:
                    if (msg.obj != null)
                        Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SET_TEXT:
                    if (msg.obj != null)
                        break;
            }
        }
    };

    //util
    private String getStringFromBytes(byte[] b, int length) {
        StringBuilder sb = new StringBuilder();
        if (b != null && b.length > 0 && b.length >= length) {
            for (int i = 0; i < length; i++) {
                sb.append(String.format("%02X", b[i] & 0xff));
            }
        }
        return sb.toString();
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public void callNFCFrag() {
        selectItem(Constants.Fragment.NFC_CONNECT);
    }

    public void callQRFrag() {
        selectItem(Constants.Fragment.BARCODE_CONNECT);
    }

}