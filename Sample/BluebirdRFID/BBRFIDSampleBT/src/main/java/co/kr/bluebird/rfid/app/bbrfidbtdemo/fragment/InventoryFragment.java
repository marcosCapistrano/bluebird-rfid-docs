/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/

 */

package co.kr.bluebird.rfid.app.bbrfidbtdemo.fragment;

import co.kr.bluebird.rfid.app.bbrfidbtdemo.Constants;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.fileutil.FileManager;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.R;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.fileutil.MediaUriWrite;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.permission.PermissionHelper;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.control.ListItem;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.control.TagListAdapter;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.stopwatch.StopwatchService;
import co.kr.bluebird.rfid.app.bbrfidbtdemo.utils.Utils;
import co.kr.bluebird.sled.BTReader;
import co.kr.bluebird.sled.SDConsts;
import co.kr.bluebird.sled.SelectionCriterias;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class InventoryFragment extends Fragment {

    private static final String TAG = InventoryFragment.class.getSimpleName();

    private static final boolean D = Constants.INV_D;

    private StopwatchService mStopwatchSvc;

    private TagListAdapter mAdapter;

    private ListView mRfidList;

    private TextView mBatteryText;

    private TextView mTimerText;

    private TextView mCountText;

    private TextView mSpeedCountText;

    private TextView mAvrSpeedCountTest;

    private Button mClearButton;

    private Button mInvenButton;

    private Button mStopInvenButton;

    private Switch mTurboSwitch;

    private Switch mRssiSwitch;

    private Switch mFilterSwitch;

    private Switch mSoundSwitch;

    private Switch mMaskSwitch;

    private Switch mToggleSwitch;

    private Switch mPCSwitch;

    private Switch mFileSwitch;

    private ProgressBar mProgressBar;

    private BTReader mReader;

    private Context mContext;

    private boolean mTagFilter = false;

    private boolean mSoundPlay = true;

    private boolean mMask = false;

    private boolean mInventory = false;

    private boolean mIsTurbo = true;

    private boolean mToggle = false;

    private boolean mIgnorePC = false;

    private boolean mRssi = false;

    private boolean mFile = false;

    private Handler mOptionHandler;

    private SoundPool mSoundPool;

    private int mSoundId;

    private float mSoundVolume;

    private boolean mSoundFileLoadState;

    private SoundTask mSoundTask;

    private double mOldTotalCount = 0;

    private double mOldSec = 0;

    private FileManager mFileManager;

    private MediaUriWrite mUriWrite;

    private String mLocateTag;

    private String mLocateEPC;

    private int mLocateStartPos;

    private LinearLayout mLocateLayout;

    private LinearLayout mListLayout;

    private ProgressBar mTagLocateProgress;

    private int mLocateValue;

    private ImageButton mBackButton;

    private TextView mLocateTv;

    private boolean mLocate;

    private TimerTask mLocateTimerTask;

    private Timer mClearLocateTimer;

    private Spinner mSessionSpin;
    private ArrayAdapter<CharSequence> mSessionChar;

    private Spinner mSelFlagSpin;
    private ArrayAdapter<CharSequence> mSelFlagChar;
    //private int mCurrentPower;

    private int mTickCount = 0;

    private Fragment mFragment;

    private UpdateStopwatchHandler mUpdateStopwatchHandler = new UpdateStopwatchHandler(this);

    private InventoryHandler mInventoryHandler = new InventoryHandler(this);

    public static InventoryFragment newInstance() {
        return new InventoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.inventory_frag, container, false);
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mRfidList = (ListView)v.findViewById(R.id.rfid_list);

        mRfidList.setOnItemClickListener(listItemClickListener);

        mLocateLayout = (LinearLayout)v.findViewById(R.id.tag_locate_container);

        mListLayout = (LinearLayout)v.findViewById(R.id.tag_list_container);

        mLocateTv = (TextView)v.findViewById(R.id.tag_locate_text);

        mTagLocateProgress = (ProgressBar)v.findViewById(R.id.tag_locate_progress);

        mBackButton = (ImageButton)v.findViewById(R.id.back_button);
        mBackButton.setOnClickListener(sledListener);

        mTimerText = (TextView)v.findViewById(R.id.timer_text);

        mCountText = (TextView)v.findViewById(R.id.count_text);

        mSpeedCountText = (TextView)v.findViewById(R.id.speed_count_text);

        mAvrSpeedCountTest = (TextView)v.findViewById(R.id.speed_avr_count_text);

        Activity activity = getActivity();

        if (activity != null) {
            String speedCountStr = activity.getString(R.string.speed_count_str) + activity.getString(R.string.speed_postfix_str);
            mSpeedCountText.setText(speedCountStr);
            mAvrSpeedCountTest.setText(speedCountStr);
        }

        mBatteryText = (TextView)v.findViewById(R.id.battery_text);

        mTurboSwitch = (Switch)v.findViewById(R.id.turbo_switch);

        mRssiSwitch = (Switch)v.findViewById(R.id.rssi_switch);

        mFilterSwitch = (Switch)v.findViewById(R.id.filter_switch);

        mSoundSwitch = (Switch)v.findViewById(R.id.sound_switch);

        mMaskSwitch = (Switch)v.findViewById(R.id.mask_switch);

        mToggleSwitch = (Switch)v.findViewById(R.id.toggle_switch);

        mPCSwitch = (Switch)v.findViewById(R.id.pc_switch);

        mFileSwitch = (Switch)v.findViewById(R.id.file_switch);

        mClearButton = (Button)v.findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(clearButtonListener);

        mInvenButton = (Button)v.findViewById(R.id.inven_button);
        mInvenButton.setOnClickListener(sledListener);

        mStopInvenButton = (Button)v.findViewById(R.id.stop_inven_button);
        mStopInvenButton.setOnClickListener(sledListener);

        mProgressBar = (ProgressBar)v.findViewById(R.id.timer_progress);
        mProgressBar.setVisibility(View.INVISIBLE);

        mSessionSpin = (Spinner)v.findViewById(R.id.session_spin);
        mSessionChar = ArrayAdapter.createFromResource(mContext, R.array.session_array,
                android.R.layout.simple_spinner_dropdown_item);
        mSessionSpin.setAdapter(mSessionChar);

        mSelFlagSpin = (Spinner)v.findViewById(R.id.sel_flag_spin);
        mSelFlagChar = ArrayAdapter.createFromResource(mContext, R.array.sel_flag_array,
                android.R.layout.simple_spinner_dropdown_item);
        mSelFlagSpin.setAdapter(mSelFlagChar);

        mAdapter = new TagListAdapter(mContext);
        mRfidList.setAdapter(mAdapter);

        bindStopwatchSvc();

        return v;
    }

    private OnItemSelectedListener sessionListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position > 0)
                Toast.makeText(mContext, "If you want to use session 1 ~ 3 value, toggle off", Toast.LENGTH_SHORT).show();
            mReader.RF_SetSession(position);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private OnItemSelectedListener selFlagListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mReader.RF_SetSelectionFlag(position + 1);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListItem i = (ListItem)mRfidList.getItemAtPosition(position);
            mLocateTag = i.mUt;
            mLocateStartPos = (i.mHasPc ? 0 : 4);
            if (i.mHasPc)
                mLocateEPC = mLocateTag.substring(4, mLocateTag.length());
            else
                mLocateEPC = mLocateTag;

            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(getString(R.string.locating_str));
            alert.setMessage(getString(R.string.want_tracking_str));

            alert.setPositiveButton(getString(R.string.yes_str), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SelectionCriterias s = new SelectionCriterias();
                    s.makeCriteria(SelectionCriterias.SCMemType.EPC, mLocateTag,
                            mLocateStartPos, mLocateTag.length() * 4,
                            SelectionCriterias.SCActionType.ASLINVA_DSLINVB);
                    mReader.RF_SetSelection(s);
                    switchLayout(false);
                    mLocateTv.setText(mLocateTag);
                }
            });
            alert.setNegativeButton(getString(R.string.no_str) ,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        }
    };

    private void switchLayout(boolean showList) {
        mLocate = !showList;
//        if (mLocate)
//            mReader.RF_SetRssiTrackingState(SDConsts.RFRssi.ON);
        if (showList) {
            mListLayout.setVisibility(View.VISIBLE);
            mLocateLayout.setVisibility(View.GONE);
            mInvenButton.setText(R.string.inven_str);
            mStopInvenButton.setText(R.string.stop_inven_str);
        } else {
            mTagLocateProgress.setProgress(0);
            mListLayout.setVisibility(View.GONE);
            mLocateLayout.setVisibility(View.VISIBLE);
            mInvenButton.setText(R.string.track_str);
            mStopInvenButton.setText(R.string.stop_track_str);
        }
    }

    private void createSoundPool() {
        boolean b = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            b = createNewSoundPool();
        else
            b = createOldSoundPool();
        if (b) {
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mSoundVolume = actVolume / maxVolume;
            SoundLoadListener();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(5).build();
        if (mSoundPool != null)
            return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean createOldSoundPool(){
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        if (mSoundPool != null)
            return true;
        return false;
    }

    private void SoundLoadListener() {
        if (mSoundPool != null) {
            mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    // TODO Auto-generated method stub
                    mSoundFileLoadState = true;
                }
            });
            mSoundId = mSoundPool.load(mContext, R.raw.beep, 1);
        }
    }

    private class SoundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            if (mLocate)
                mTagLocateProgress.setProgress(mLocateValue);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (mSoundPlay) {
                try {
                    if (mSoundFileLoadState) {
                        if (mSoundPool != null) {
                            mSoundPool.play(mSoundId, mSoundVolume, mSoundVolume, 0, 0, (48000.0f / 44100.0f));
                            try {
                                Thread.sleep(25);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (java.lang.NullPointerException e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mSoundFileLoadState = false;

        createSoundPool();

        mOldTotalCount = 0;

        mOldSec = 0;

        mReader = BTReader.getReader(mContext, mInventoryHandler);
        if (mReader != null && mReader.BT_GetConnectState() == SDConsts.BTConnectState.CONNECTED) {
            enableControl(true);
            updateButtonState();
        } else
            enableControl(false);

        mLocate = false;

        mInventory = false;

        addCheckListener();
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
        mReader.RF_StopInventory();
        pauseStopwatch();
        mInventory = false;
        if (mSoundPool != null)
            mSoundPool.release();
        mSoundFileLoadState = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if (mFileManager != null) {
                mFileManager.closeFile();
                mFileManager = null;
            }
        }else{
            if (mUriWrite != null) {
                mUriWrite.closeFile();
                mUriWrite = null;
            }
        }

        stopStopwatch();

        unbindStopwatchSvc();
        super.onStop();
    }

    private OnClickListener clearButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "clearButtonListener");
            clearAll();
        }
    };

    private void clearAll() {
        if (!mInventory) {
            mAdapter.removeAllItem();

            updateCountText();

            stopStopwatch();

            mOldTotalCount = 0;

            mOldSec = 0;

            updateSpeedCountText();

            updateAvrSpeedCountText();

            Activity activity = getActivity();
            if (activity != null)
                mSpeedCountText.setText("0" + activity.getString(R.string.speed_postfix_str));
        }
    }

    private OnClickListener sledListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "stopwatchListener");

            int id = v.getId();
            int ret;

            if(id == R.id.back_button){
                ret = mReader.RF_StopInventory();
                if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
                    mInventory = false;
                    enableControl(!mInventory);
                    pauseStopwatch();
                } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
                    Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();

                switchLayout(true);
                mLocateTv.setText("");
                mLocateTag = null;
                clearAll();
            }else if(id == R.id.inven_button){
                startInventory(Constants.InventoryType.NORAML);//[20250424]Add other inventory api for test
            }else if(id == R.id.stop_inven_button){
                stopInventory();//[20250424]Add other inventory api for test
            }
        }
    };

    //+20231011 change save routine
    @SuppressLint("SuspiciousIndentation")
    private void saveFile() {
        if (mFile && !mInventory && !mLocate) {
            mFileSwitch.setChecked(mFile);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if (mFileManager == null)
                mFileManager = new FileManager(mContext);
            mFileManager.saveFile(mAdapter);
        }else{
            if(mUriWrite == null)
                mUriWrite = new MediaUriWrite(mContext);
            mUriWrite.saveFile(mAdapter);
        }
    }
    //20231011 change save routine+
    private void openFile() {
        if (mFile && !mInventory && !mLocate) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                if (mFileManager == null)
                    mFileManager = new FileManager(mContext);
                mFileManager.openFile();
            }else{
                if(mUriWrite == null)
                    mUriWrite = new MediaUriWrite(mContext);
                mUriWrite.openFile();
            }
            mFileSwitch.setChecked(mFile);
        }
    }

    private OnCheckedChangeListener sledcheckListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            int id = buttonView.getId();
            if(id == R.id.turbo_switch){
                if (isChecked)
                    mIsTurbo = true;
                else
                    mIsTurbo = false;
            }else if(id == R.id.file_switch){
                if (isChecked) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mStoragePerms[0]);   //file write
                        if (!b) {
                            PermissionHelper.requestPermission(getActivity(), PermissionHelper.mStoragePerms);
                        } else
                            mFile = true;
                    }else{
                        mFile = true;
                    }
                } else {
                    mFile = false;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        if (mFileManager != null) {
                            mFileManager.closeFile();
                            mFileManager = null;
                        }
                    }else{
                        if (mUriWrite != null) {
                            mUriWrite.closeFile();
                            mUriWrite = null;
                        }
                    }
                }
            }else if(id == R.id.rssi_switch){
                if (isChecked) {
                    if (mReader.RF_SetRssiTrackingState(SDConsts.RFRssi.ON) == SDConsts.RFResult.SUCCESS)
                        mRssi = true;
                } else {
                    if (mReader.RF_SetRssiTrackingState(SDConsts.RFRssi.OFF) == SDConsts.RFResult.SUCCESS)
                        mRssi = false;
                }
            }else if(id == R.id.filter_switch){
                clearAll();
                if (isChecked)
                    mTagFilter = true;
                else
                    mTagFilter = false;
            }else if(id == R.id.sound_switch){
                if (isChecked)
                    mSoundPlay = true;
                else
                    mSoundPlay = false;
            }else if(id == R.id.mask_switch){
                if (isChecked)
                    mMask = true;
                else
                    mMask = false;
            }else if(id == R.id.toggle_switch){
                if (isChecked) {
                    if (mReader.RF_SetToggle(SDConsts.RFToggle.ON) == SDConsts.RFResult.SUCCESS)
                        mToggle = true;
                } else {
                    if (mReader.RF_SetToggle(SDConsts.RFToggle.OFF) == SDConsts.RFResult.SUCCESS)
                        mToggle = false;
                }
            }else if(id == R.id.pc_switch){
                if (isChecked)
                    mIgnorePC = true;
                else
                    mIgnorePC = false;
            }
        }
    };

    private void startStopwatch() {
        if (D) Log.d(TAG, "startStopwatch");

        if (mStopwatchSvc != null && !mStopwatchSvc.isRunning())
            mStopwatchSvc.start();

        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void pauseStopwatch() {
        if (D) Log.d(TAG, "pauseStopwatch");

        if (mStopwatchSvc != null && mStopwatchSvc.isRunning())
            mStopwatchSvc.pause();

        updateCountText();

        updateTimerText();

        updateSpeedCountText();

        updateAvrSpeedCountText();

        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void stopStopwatch() {
        if (D) Log.d(TAG, "stopStopwatch");

        if (mStopwatchSvc != null && mStopwatchSvc.isRunning())
            mStopwatchSvc.pause();

        if (mStopwatchSvc != null)
            mStopwatchSvc.reset();

        updateTimerText();

        updateAvrSpeedCountText();

        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void updateCountText() {
        if (D) Log.d(TAG, "updateCountText");
        String text = Integer.toString(mAdapter.getCount());
        mCountText.setText(text);
    }

    private void updateTimerText() {
        if (D) Log.d(TAG, "updateTimerText");
        if (mStopwatchSvc != null)
            mTimerText.setText(mStopwatchSvc.getFormattedElapsedTime());
    }

    private void updateSpeedCountText() {
        if (D) Log.d(TAG, "updateSpeedCountText");
        String speedStr = "";
        double value = 0;
        double totalCount = 0;
        double sec = 0;
        if (mStopwatchSvc != null) {
            sec = ((double)((int)(mStopwatchSvc.getElapsedTime() / 100))) / 10;

            if (!mTagFilter)
                totalCount = mAdapter.getTotalCount();
            else {
                totalCount = mAdapter.getTotalCount();
                for (int i = 0 ; i < mAdapter.getCount(); i++)
                    totalCount += mAdapter.getItemDupCount(i);
            }
            if (totalCount > 0 && sec - mOldSec >= 1) {
                value = (double)((int)(((totalCount - mOldTotalCount) / (sec - mOldSec)) * 10)) / 10;

                mOldTotalCount = totalCount;

                mOldSec = sec;
                Activity activity = getActivity();
                if (activity != null)
                    speedStr = Double.toString(value) + activity.getString(R.string.speed_postfix_str);
                mSpeedCountText.setText(speedStr);
            }
        }
    }

    private void updateAvrSpeedCountText() {
        if (D) Log.d(TAG, "updateAvrSpeedCountText");
        String speedStr = "";
        double value = 0;
        int totalCount = 0;
        double sec = 0;
        if (mStopwatchSvc != null) {
            sec = ((double)((int)(mStopwatchSvc.getElapsedTime() / 100))) / 10;

            if (!mTagFilter)
                totalCount = mAdapter.getTotalCount();
            else {
                totalCount = mAdapter.getTotalCount();
                for (int i = 0 ; i < mAdapter.getCount(); i++)
                    totalCount += mAdapter.getItemDupCount(i);
            }
            if (totalCount > 0 && sec >= 1)
                value = (double)((int)(((double)totalCount / sec) * 10)) / 10;

            Activity activity = getActivity();
            if (activity != null)
                speedStr = Double.toString(value) + activity.getString(R.string.speed_postfix_str);
            mAvrSpeedCountTest.setText(speedStr);
        }
    }

    private void enableControl(boolean b) {
        if (b)
            mRfidList.setOnItemClickListener(listItemClickListener);
        else
            mRfidList.setOnItemClickListener(null);
        mTurboSwitch.setEnabled(b);
        mRssiSwitch.setEnabled(b);
        mFilterSwitch.setEnabled(b);
        mSoundSwitch.setEnabled(b);
        mMaskSwitch.setEnabled(b);
        mToggleSwitch.setEnabled(b);
        mPCSwitch.setEnabled(b);
        mFileSwitch.setEnabled(b);
        mSessionSpin.setEnabled(b);
        mSelFlagSpin.setEnabled(b);
        mBackButton.setVisibility(b ? View.VISIBLE :View.INVISIBLE);

    }

    private ServiceConnection mStopwatchSvcConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "onServiceConnected");

            mStopwatchSvc = ((StopwatchService.LocalBinder)arg1).getService(mUpdateStopwatchHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "onServiceDisconnected");

            mStopwatchSvc = null;
        }
    };

    private void bindStopwatchSvc() {
        if (D) Log.d(TAG, "bindStopwatchSvc");
        mContext.bindService(new Intent(mContext, StopwatchService.class), mStopwatchSvcConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindStopwatchSvc() {
        if (D) Log.d(TAG, "unbindStopwatchSvc");

        try {
            if (mStopwatchSvc != null)
                mContext.unbindService(mStopwatchSvcConnection);
        } catch (java.lang.IllegalArgumentException iae) {
            return;
        }
    }

    private static class UpdateStopwatchHandler extends Handler {
        private final WeakReference<InventoryFragment> mExecutor;
        public UpdateStopwatchHandler(InventoryFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            InventoryFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleUpdateStopwatchHandler(msg);
            }
        }
    }

    public void handleUpdateStopwatchHandler(Message m) {
        if (D) Log.d(TAG, "mUpdateStopwatchHandler");
        if (m.what == StopwatchService.TICK_WHAT) {
            if (D) Log.d(TAG, "received stopwatch message");

            mTickCount++;

            updateCountText();

            updateSpeedCountText();

            if (mTickCount == 10) {
                updateAvrSpeedCountText();
                mTickCount = 0;
            }
            updateTimerText();

            mStopwatchSvc.update();

            mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);

        }
    }

    private static class InventoryHandler extends Handler {
        private final WeakReference<InventoryFragment> mExecutor;
        public InventoryHandler(InventoryFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            InventoryFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleInventoryHandler(msg);
            }
        }
    }

    public void handleInventoryHandler(Message m) {
        if (D) Log.d(TAG, "mInventoryHandler");
        if (D) Log.d(TAG, "m arg1 = " + m.arg1 + " arg2 = " + m.arg2);
        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                switch(m.arg1) {
                    //+Hotswap feature
                    case SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED:
                        if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE)
                            Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                        else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE)
                            Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(mFragment).attach(mFragment).commit();
                        break;
                    //Hotswap feature+
                    case SDConsts.SDCmdMsg.TRIGGER_PRESSED:
                        startInventory(Constants.InventoryType.CUSTOM);//[20250424]Add other inventory api for test
                        break;

                    case SDConsts.SDCmdMsg.SLED_INVENTORY_STATE_CHANGED:
                        mInventory = false;
                        enableControl(!mInventory);
                        pauseStopwatch();
                        // In case of low battery on inventory, reason value is LOW_BATTERY
                        Toast.makeText(mContext, "Inventory Stopped reason : " + m.arg2, Toast.LENGTH_SHORT).show();

                        mAdapter.addItem(-1, "Inventory Stopped reason : " + m.arg2,  Integer.toString(m.arg2), !mIgnorePC, mTagFilter);
                        break;

                    case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                        stopInventory();//[20250424]Add other inventory api for test
                        break;
                    //You can receive this message every a minute. SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED
                    case SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED:
                        //Toast.makeText(mContext, "Battery state = " + m.arg2, Toast.LENGTH_SHORT).show();
                        if (D) Log.d(TAG, "Battery state = " + m.arg2);
                        mBatteryText.setText("" + m.arg2 + "%");

                        //+smart batter -critical temper

                        if (m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE) {
                            if (mInventory) {
                                pauseStopwatch();
                                mInventory = false;
                                enableControl(!mInventory);
                            }
                            Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                        }
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
                        break;
                }
                break;

            case SDConsts.Msg.RFMsg:
                switch(m.arg1) {
                    //+RF_PerformInventoryCustom
                    case SDConsts.RFCmdMsg.INVENTORY_CUSTOM_READ:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            String data = (String)m.obj;
                            if (data != null)
                                processReadDataCustom(data);
                        }
                        break;
                    //RF_PerformInventoryCustom+

                    case SDConsts.RFCmdMsg.INVENTORY:
                    case SDConsts.RFCmdMsg.READ:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null  && m.obj instanceof String) {
                                String data = (String) m.obj;
                                if (data != null)
                                    processReadData(data);
                            }
                        }
                        break;
                    case SDConsts.RFCmdMsg.LOCATE:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null  && m.obj instanceof Integer)
                                processLocateData((int) m.obj);
                        }
                        break;
                }
                break;
            case SDConsts.Msg.BTMsg:
                if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_CONNECTION_STATE_CHANGED) {
                    if (D) Log.d(TAG, "SLED_BT_CONNECTION_STATE_CHANGED = " + m.arg2);
                    if (mReader.BT_GetConnectState() != SDConsts.BTConnectState.CONNECTED) {
                        if (mInventory) {
                            pauseStopwatch();
                            mInventory = false;
                        }
                        enableControl(false);
                    }
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_CONNECT_STATE_CHANGED).sendToTarget();
                } else if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_DISCONNECTED || m.arg1 == SDConsts.BTCmdMsg.SLED_BT_CONNECTION_LOST) {
                    if (mInventory) {
                        pauseStopwatch();
                        mInventory = false;
                    }
                    enableControl(false);
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_CONNECT_STATE_CHANGED).sendToTarget();
                }
                break;
        }
    }

    private void processLocateData(int data) {
        startLocateTimer();
        mLocateValue = data;
        //mTagLocateProgress.setProgress(data);
        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }

    private void processReadData(String data) {
        //updateCountText();
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String info = "";
        String epcDecode = "";
        String originalData = data;
        if (data.contains(";")) {
            if (D) Log.d(TAG, "full tag = " + data);
            //full tag example(with optional value)
            //1) RF_PerformInventory => "3000123456783333444455556666;rssi:-54.8"
            //2) RF_PerformInventoryWithLocating => "3000123456783333444455556666;loc:64"
            //3) RF_PerformInventoryWithTagCorder => "3000123456783333444455556666;rssi:-54.8;tagc:1111111"
//            int infoTagPoint = data.indexOf(';');
//            info = data.substring(infoTagPoint, data.length());
//            int infoPoint = info.indexOf(':') + 1;
//            info = info.substring(infoPoint, info.length());
//            if (D) Log.d(TAG, "info tag = " + info);
//            data = data.substring(0, infoTagPoint);
//            if (D) Log.d(TAG, "data tag = " + data);

            data = "";
            String[] splitData = originalData.split(";");
            Activity activity = getActivity();
            String prefix = "";
            for(String dt : splitData) {
                if(dt.startsWith("rssi:")) {
                if (activity != null)
                    prefix = activity.getString(R.string.rssi_str);
                    info = prefix + dt.replace("rssi:", "");
                    if (D) Log.d(TAG, "rssi tag = " + info);
                } else if(dt.startsWith("loc:")) {
                if (activity != null)
                    prefix = activity.getString(R.string.loc_str);
                    info = prefix + dt.replace("loc:", "");
                    if (D) Log.d(TAG, "loc tag = " + info);
                } else if(dt.startsWith("epcdc:")) {
                    if (activity != null)
                        prefix = activity.getString(R.string.epc_decode_str);
                    epcDecode = prefix + dt.replace("epcdc:", "");
                    if (D) Log.d(TAG, "epc_decode tag = " + info);
                } else if(TextUtils.isEmpty(data)){
                    data = dt;
                    if (D) Log.d(TAG, "data tag = " + data);
                }
            }
        }
        mAdapter.addItem(-1, data, info, epcDecode, !mIgnorePC, mTagFilter);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if (mFileManager != null && mFile)
                mFileManager.writeToFile(data);
        }else{
            if (mUriWrite != null && mFile)
                mUriWrite.writeToFile(data);
        }

        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        if (!mInventory) {
            updateCountText();
            updateSpeedCountText();
            updateAvrSpeedCountText();
        }

        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }

    private void addCheckListener() {
        if (mTurboSwitch != null)
            mTurboSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mRssiSwitch != null)
            mRssiSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mFilterSwitch != null)
            mFilterSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mSoundSwitch != null)
            mSoundSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mMaskSwitch != null)
            mMaskSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mToggleSwitch != null)
            mToggleSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mPCSwitch != null)
            mPCSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mFileSwitch != null)
            mFileSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mSessionSpin != null)
            mSessionSpin.setOnItemSelectedListener(sessionListener);

        if (mSelFlagSpin != null)
            mSelFlagSpin.setOnItemSelectedListener(selFlagListener);
    }

    private void updateButtonState() {
        mPCSwitch.setChecked(mIgnorePC);

        mFileSwitch.setChecked(mFile);

        mFilterSwitch.setChecked(mTagFilter);

        mSoundSwitch.setChecked(mSoundPlay);

        mMaskSwitch.setChecked(mMask);

        mTurboSwitch.setChecked(mIsTurbo);


        if (mReader != null) {
            int toggle = mReader.RF_GetToggle();
            if (toggle == SDConsts.RFToggle.ON)
                mToggle = true;
            else
                mToggle = false;
            mToggleSwitch.setChecked(mToggle);

            int session = mReader.RF_GetSession();
            if (session != mSessionSpin.getSelectedItemPosition())
                mSessionSpin.setSelection(session);

            int flag = mReader.RF_GetSelectionFlag();
            if (flag != mSelFlagSpin.getSelectedItemPosition() + 1)
                mSelFlagSpin.setSelection(flag - 1);

            int rssi = mReader.RF_GetRssiTrackingState();
            if (rssi == SDConsts.RFRssi.ON)
                mRssi = true;
            else
                mRssi = false;
            mRssiSwitch.setChecked(mRssi);

            int battery = mReader.SD_GetBatteryStatus();
            if (!(battery < 0 || battery > 100))
                mBatteryText.setText("" + battery + "%");

        }
    }

    private void startLocateTimer() {
        stopLocateTimer();

        mLocateTimerTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                locateTimeout();
            }
        };
        mClearLocateTimer = new Timer();
        mClearLocateTimer.schedule(mLocateTimerTask, 500);
    }

    private void stopLocateTimer() {
        if (mClearLocateTimer != null ) {
            mClearLocateTimer.cancel();
            mClearLocateTimer = null;
        }
    }

    private void locateTimeout() {
        mTagLocateProgress.setProgress(0);
    }

    //+RF_PerformInventoryCustom
    private void processReadDataCustom(String data) {
        updateCountText();
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String rssi = "";
        String customData = "";
        if (data.contains(";")) {
            if (D) Log.d(TAG, "full tag = " + data);
            //full tag example = "3000123456783333444455556666;rssi:-54.8^custom=2348920384"
            int customdDataPoint = data.indexOf('^');
            customData = data.substring(customdDataPoint, data.length());
            int customPoint = customData.indexOf('=') + 1;
            customData = customData.substring(customPoint, customData.length());
            if (D) Log.d(TAG, "custom data = " + customData);
            data = data.substring(0, customdDataPoint);

            int rssiTagPoint = data.indexOf(';');
            rssi = data.substring(rssiTagPoint, data.length());
            int rssiPoint = rssi.indexOf(':') + 1;
            rssi = rssi.substring(rssiPoint, rssi.length());
            if (D) Log.d(TAG, "rssi tag = " + rssi);
            data = data.substring(0, rssiTagPoint);

            if (D) Log.d(TAG, "data tag = " + data);
            data = data + "\n" + customData;
        }
        if (rssi != "") {
            Activity activity = getActivity();
            if (activity != null)
                rssi = activity.getString(R.string.rssi_str) + rssi;
        }
        mAdapter.addItem(-1, data, rssi, false/*mTagFilter*/,false);

        if (mSoundPlay) {
            if (mSoundTask == null) {
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            } else {
                if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mSoundTask.cancel(true);
                    mSoundTask = null;
                    mSoundTask = new SoundTask();
                    mSoundTask.execute();
                }
            }
        }
        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        if (!mInventory) {
            updateCountText();
            updateSpeedCountText();
            updateAvrSpeedCountText();
        }
    }
    //RF_PerformInventoryCustom+

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            switch (requestCode) {
                case PermissionHelper.REQ_PERMISSION_CODE:
                    if (permissions != null) {
                        boolean hasResult = true;
                        for (int result : grantResults) {
                            if (result != PackageManager.PERMISSION_GRANTED) {
                                hasResult = false;
                                break;
                            }
                        }
                        if (hasResult) {
                            mFile = true;
                        }else {
                            mFile = false;
                        }
                    }
                    break;
            }
            mFileSwitch.setChecked(mFile);
        }else{
            mFile = true;
            mFileSwitch.setChecked(mFile);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_FILE_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(Environment.isExternalStorageManager()) {
                    mFile = true;
                } else {
                    Toast.makeText(mContext, getString(R.string.permission_not_allowed), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //<-[20250424]Add other inventory api for test
    private void startInventory(int type) {
        if (!mInventory) {
            clearAll();
            int ret = 0;
            if (mLocate) {
                ret = mReader.RF_PerformInventoryForLocating(mLocateEPC);
            } else {
                switch (type){
                    case Constants.InventoryType.NORAML:
                        ret = mReader.RF_PerformInventory(mIsTurbo, mMask, mIgnorePC);
                        break;
                    case Constants.InventoryType.RSSI_TO_LOCATE:
                        ret = mReader.RF_PerformInventoryWithLocating(mIsTurbo, mMask, mIgnorePC);
                        break;
                    case Constants.InventoryType.CUSTOM:
                        mReader.RF_PerformInventoryCustom(SelectionCriterias.SCMemType.TID, 0, 2, "00000000", mMask);
                        break;
                    case Constants.InventoryType.WITH_PAHSE_FREQ:
                        ret = mReader.RF_PerformInventoryWithPhaseFreq(mIsTurbo, mMask, mIgnorePC);
                        break;
                }
            }
            if (ret == SDConsts.RFResult.SUCCESS) {
                startStopwatch();
                mInventory = true;
                enableControl(!mInventory);
            } else if (ret == SDConsts.RFResult.MODE_ERROR)
                Toast.makeText(mContext, "Start Inventory failed, Please check RFR MODE", Toast.LENGTH_SHORT).show();
            else if (ret == SDConsts.RFResult.LOW_BATTERY)
                Toast.makeText(mContext, "Start Inventory failed, LOW_BATTERY", Toast.LENGTH_SHORT).show();
            else if (D) Log.d(TAG, "Start Inventory failed");
        }
    }

    private void stopInventory(){
        int ret = mReader.RF_StopInventory();
        if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
            mInventory = false;
            saveFile();//20231011 change save routine
            enableControl(!mInventory);
            pauseStopwatch();
        } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
            Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();

    }
    //[20250424]Add other inventory api for test->
}