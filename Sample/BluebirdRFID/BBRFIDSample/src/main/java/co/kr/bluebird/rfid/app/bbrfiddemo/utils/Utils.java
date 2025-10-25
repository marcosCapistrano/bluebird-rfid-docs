package co.kr.bluebird.rfid.app.bbrfiddemo.utils;

import android.app.AlertDialog;
import android.content.Context;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;

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


}
