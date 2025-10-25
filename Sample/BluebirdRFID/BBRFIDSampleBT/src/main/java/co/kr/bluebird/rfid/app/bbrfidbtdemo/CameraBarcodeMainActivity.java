package co.kr.bluebird.rfid.app.bbrfidbtdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CameraBarcodeMainActivity extends Activity{

    public static Activity mCameraBarcodeMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CameraScannerActivity.class);
        integrator.initiateScan();

        mCameraBarcodeMainActivity = CameraBarcodeMainActivity.this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraBarcodeMainActivity = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("onActivityResult", "onActivityResult: .");
        if (resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            String read = scanResult.getContents();
            Log.d("onActivityResult", "onActivityResult: ." + read);
            Toast.makeText(this, read, Toast.LENGTH_LONG).show();

            Intent i = new Intent();
            intent.putExtra(Constants.MAC_ADDR, read);
            setResult(RESULT_OK, intent);

            finish();
        }
    }
}
