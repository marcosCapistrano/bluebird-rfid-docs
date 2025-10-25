package co.kr.bluebird.rfid.app.bbrfidbtdemo.utils;

import android.app.AlertDialog;
import android.content.Context;

import co.kr.bluebird.rfid.app.bbrfidbtdemo.R;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    //+smart batter -critical temper
    public static void createAlertDialog(Context ctx, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getString(R.string.smart_critical_temper_title));
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton(ctx.getString(R.string.drawer_close), null);
        builder.show();
    }
    //smart batter -critical temper+

    public static final String getStringFromBytes(byte[] b, int length) {
        StringBuilder sb = new StringBuilder();
        if (b != null && b.length > 0 && b.length >= length) {
            for (int i = 0; i < length; i++) {
                sb.append(String.format("%02X", b[i] & 0xff));
            }
        }
        return sb.toString();
    }

}
