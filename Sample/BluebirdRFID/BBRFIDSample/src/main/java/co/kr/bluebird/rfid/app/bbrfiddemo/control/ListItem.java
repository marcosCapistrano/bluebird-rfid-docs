/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 * 
 * Author : Bogon Jun
 *
 * Date : 2016.01.18
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.control;

public class ListItem {
    
    public int mIv;
    
    public String mUt;
    
    public String mDt;
    
    public int mDupCount;

    public boolean mHasPc;

    public String mPha;

    public String mFrequency;

    public String mEpcDecode;

    public String mAccessPW;//[20250402]Add Bulk encoding

    public ListItem() {
        mIv = -1;
        mUt = null;
        mDt = null;
        mDupCount = 0;
        mHasPc = true;
        mPha = null;
        mFrequency = null;
        mEpcDecode = null;
        mAccessPW = null; //[20250402]Add Bulk encoding
    }
}
