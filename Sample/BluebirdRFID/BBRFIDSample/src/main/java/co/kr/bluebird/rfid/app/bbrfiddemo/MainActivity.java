/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.legacy.app.ActionBarDrawerToggle;

import java.lang.ref.WeakReference;
import co.kr.bluebird.rfid.app.bbrfiddemo.fragment.*;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final boolean D = Constants.MAIN_D;

    public static final int MSG_OPTION_CONNECT_STATE_CHANGED = 0;

    public static final int MSG_OPTION_DISCONNECTED = 0;

    public static final int MSG_OPTION_CONNECTED = 1;

    public static final int MSG_BACK_PRESSED = 2;

    public static final int MSG_BATT_NOTI = 3;//Always be display Battery

    private String[] mFunctionsString;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private Reader mReader;

    private Context mContext;

    private FragmentManager mFragmentManager;

    private boolean mIsConnected;

    public boolean mSledUpdate = false;

    private ConnectivityFragment mConnectivityFragment;
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
    private TestFragment mTestFragment;

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

    Menu mMenu;//Always be display Battery

    private String modelIDStr;

    private final MainHandler mMainHandler = new MainHandler(this);

    public final UpdateConnectHandler mUpdateConnectHandler = new UpdateConnectHandler(this);

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

        mUILayout = (LinearLayout) findViewById(R.id.ui_layout);

        mCurrentFragment = null;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int buttonHeight = size.x / 3;

        mConnectButton = (ImageButton) findViewById(R.id.connect_bt);
        mConnectButton.setMinimumHeight(buttonHeight);

        mSDFunctionButton = (ImageButton) findViewById(R.id.sdfunc_bt);
        mSDFunctionButton.setMinimumHeight(buttonHeight);

        mRFConfigButton = (ImageButton) findViewById(R.id.rfconf_bt);
        mRFConfigButton.setMinimumHeight(buttonHeight);

        mRFAccessButton = (ImageButton) findViewById(R.id.rfacc_bt);
        mRFAccessButton.setMinimumHeight(buttonHeight);

        mRFSelectButton = (ImageButton) findViewById(R.id.rfsel_bt);
        mRFSelectButton.setMinimumHeight(buttonHeight);

        mRapidButton = (ImageButton) findViewById(R.id.rapid_bt);
        mRapidButton.setMinimumHeight(buttonHeight);

        mInventoryButton = (ImageButton) findViewById(R.id.inv_bt);
        mInventoryButton.setMinimumHeight(buttonHeight);

        //+add bc
        mBarcodeButton = (ImageButton) findViewById(R.id.bar_bt);
        mBarcodeButton.setMinimumHeight(buttonHeight);
//        mBarcodeButton.setVisibility(View.GONE);
        //add bc+

        mSBBarcodeButton = (ImageButton) findViewById(R.id.bar_sb_bt);
        mSBBarcodeButton.setMinimumHeight(buttonHeight);

        mBatteryButton = (ImageButton) findViewById(R.id.bat_bt);
        mBatteryButton.setMinimumHeight(buttonHeight);

        mInformationButton = (ImageButton) findViewById(R.id.info_bt);
        mInformationButton.setMinimumHeight(buttonHeight);

        ImageButton testButton = (ImageButton) findViewById(R.id.test_bt);
        testButton.setMinimumHeight(buttonHeight);
//        mCILogoImage = (ImageView) findViewById(R.id.ci_logo);
//        mCILogoImage.setMinimumHeight(buttonHeight);

//        mCILogoImage2 = (ImageView) findViewById(R.id.ci_logo2);
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

        mFunctionsString = getResources().getStringArray(R.array.functions_array);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFunctionsString));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mFragmentManager = getFragmentManager();

        mIsConnected = false;
    }

    public View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = 0;
            int resId = v.getId();
            if(resId == R.id.test_bt){
                id = 11;
            }else if(resId == R.id.connect_bt){
                id = 0;
            }else if(resId == R.id.sdfunc_bt){
                id = 1;
            }else if(resId == R.id.rfconf_bt){
                id = 2;
            }else if(resId == R.id.rfacc_bt){
                id = 3;
            }else if(resId == R.id.rfsel_bt){
                id = 4;
            }else if(resId == R.id.rapid_bt){
                id = 5;
            }else if(resId == R.id.inv_bt){
                id = 6;
            }else if(resId == R.id.bar_bt){
                id = 7;
            }else if(resId == R.id.bar_sb_bt){
                id = 8;
            }else if(resId == R.id.bat_bt){
                id = 9;
            }else if(resId == R.id.info_bt){
                id = 10;
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
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
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
        } else if (id == R.id.action_home) {
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

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        switch (position) {
            case 0:
                if (mConnectivityFragment == null)
                    mConnectivityFragment = ConnectivityFragment.newInstance();
                mCurrentFragment = mConnectivityFragment;
                break;
            case 1:
                if (mSDFragment == null)
                    mSDFragment = SDFragment.newInstance();
                mCurrentFragment = mSDFragment;
                break;
            case 2:
                if (mRFConfigFragment == null)
                    mRFConfigFragment = RFConfigFragment.newInstance();
                mCurrentFragment = mRFConfigFragment;
                break;
            case 3:
                if (mRFAccessFragment == null)
                    mRFAccessFragment = RFAccessFragment.newInstance();
                mCurrentFragment = mRFAccessFragment;
                break;
            case 4:
                if (mRFSelectionFragment == null)
                    mRFSelectionFragment = RFSelectionFragment.newInstance();
                mCurrentFragment = mRFSelectionFragment;
                break;
            case 5:
                if (mRapidFragment == null)
                    mRapidFragment = RapidFragment.newInstance();
                mCurrentFragment = mRapidFragment;
                break;
            case 6:
                if (mInventoryFragment == null)
                    mInventoryFragment = InventoryFragment.newInstance();
                mCurrentFragment = mInventoryFragment;
                break;
            case 7:
                //+add bc
                if (mBarcodeFragment == null)
                    mBarcodeFragment = BarcodeFragment.newInstance();
                mCurrentFragment = mBarcodeFragment;
                break;
                //add bc+
            case 8:
                if (mSBBarcodeFragment == null)
                    mSBBarcodeFragment = mSBBarcodeFragment.newInstance();
                mCurrentFragment = mSBBarcodeFragment;
                break;
            case 9://8:
                if (mBatteryFragment == null)
                    mBatteryFragment = BatteryFragment.newInstance();
                mCurrentFragment = mBatteryFragment;
                break;
            case 10://9:
                if (mInfoFragment == null)
                    mInfoFragment = InfoFragment.newInstance();
                mCurrentFragment = mInfoFragment;
                break;
            case 11://9:
                if (mTestFragment == null)
                    mTestFragment = mTestFragment.newInstance();
                mCurrentFragment = mTestFragment;
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
            boolean isConnected = false;
            mReader = Reader.getReader(mContext, mMainHandler);
            if (mReader != null)
                openResult = mReader.SD_Open();
            if (openResult == SDConsts.SD_OPEN_SUCCESS) {
                Log.i(TAG, "Reader opened");
                modelIDStr = (mReader.SD_GetModel() == SDConsts.MODEL.RFR900) ? getString(R.string.rfr900) : getString(R.string.rfr901);
                if (mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED)
                    isConnected = true;
            } else if (openResult == SDConsts.RF_OPEN_FAIL)
                if (D) Log.e(TAG, "Reader open failed");

            updateConnectState(isConnected);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
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
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onStop() {
        if (D) Log.d(TAG, " onStop");
        if(!mSledUpdate) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mReader = Reader.getReader(mContext, mMainHandler);
            if (mReader != null)
                if (mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
                    mReader.SD_Disconnect();
                }
            mReader.SD_Close();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mCurrentFragment != null)
                mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    private void handleMessage(Message m) {
        if (D) Log.d(TAG, "handleMessage");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                //+Always be display Battery
                try {
                    if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                        //+smart batter -critical temper
                        if (m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
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
                } catch(NullPointerException e){

                }
                break;
            case SDConsts.Msg.RFMsg:
                break;
            case SDConsts.Msg.BCMsg:
                break;
        }
    }

    private void switchToHome() {
        if (D) Log.d(TAG, "switchToHome");
        try {
            mDrawerLayout.closeDrawer(mDrawerList);
            if (mCurrentFragment != null) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.remove(mCurrentFragment);
                ft.commit();
                mCurrentFragment = null;
                mReader = Reader.getReader(mContext, mMainHandler);
            }
            setTitle(getString(R.string.app_name));
            if (mUILayout.getVisibility() != View.VISIBLE) {
                mUILayout.setVisibility(View.VISIBLE);
            }
        } catch (java.lang.IllegalStateException e) {
        }
        return;
    }

    private void updateConnectState(boolean b) {
        mIsConnected = b;
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
        if (m.what == MSG_OPTION_DISCONNECTED) {
            Log.d(TAG, "MSG_OPTION_DISCONNECTED");
            updateConnectState(false);
        } else if (m.what == MSG_OPTION_CONNECTED) {
            Log.d(TAG, "MSG_OPTION_CONNECTED");
            updateConnectState(true);
        } else if (m.what == MSG_BACK_PRESSED)
            switchToHome();
            //+Always be display Battery
        else if (m.what == MSG_BATT_NOTI) {
            Log.d(TAG, "MSG_BATT_NOTI : " + m.arg2);
            mMenu.getItem(2).setTitle(Integer.toString(m.arg2) + "%");
        }
        //Always be display Battery+
    }
}