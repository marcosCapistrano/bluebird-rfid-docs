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

public class Constants {

    public static final String VERSION = "20231205";

    public static final boolean RECV_D = false;

    public static final boolean MAIN_D = false;

    public static final boolean CON_D = false;

    public static final boolean SD_D = false;

    public static final boolean CFG_D = false;

    public static final boolean ACC_D = false;

    public static final boolean SEL_D = false;

    public static final boolean INV_D = false;

    public static final boolean BAR_D = false;

    public static final boolean SB_BAR_D = false;

    public static final boolean BAT_D = false;

    public static final boolean INFO_D = false;

    //+NFC/QR
    public static class Fragment {
        public static final int CONNECTIVITY = 0;
        public static final int NFC_CONNECT = 1;
        public static final int BARCODE_CONNECT = 2;
        public static final int SD_FUNCTION = 3;
        public static final int RF_CONFIG = 4;
        public static final int RF_ACCESS = 5;
        public static final int RF_SELECTION= 6;
        public static final int RAPID_READ = 7;
        public static final int INVENTORY = 8;
        public static final int BARCODE_BC = 9;
        public static final int BARCODE_SB = 10;
        public static final int BATTERY = 11;
        public static final int INFO = 12;
        public static final int TEST = 13;
    }
    //NFC/QR+
    //+CAMERA BARCODE
    public static final String MAC_ADDR = "";
    public static final int REQUEST_CODE = 1000;
    //CAMERA BARCODE+

    //<-[20250424]Add other inventory api for test
    public static class InventoryType {
        public static final int NORAML = 0;
        public static final int RSSI_TO_LOCATE = 1;
        public static final int FIND_LOCATE = 2;
        public static final int CUSTOM = 3;
        public static final int RSSI_LIMIT = 4;
        public static final int WITH_PAHSE_FREQ = 5;
    }
    //[20250424]Add other inventory api for test->

}