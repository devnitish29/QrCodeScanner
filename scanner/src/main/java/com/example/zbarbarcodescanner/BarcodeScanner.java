package com.example.zbarbarcodescanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

public class BarcodeScanner extends AppCompatActivity {
    private Camera mCamera;
    private Handler autoFocusHandler;

    private Button scanButton;
    private ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;
    private Camera.Parameters params;
    private boolean isFlashOn=false;
    private boolean isDataValid=true;
    public static final int REQUEST_CODE=25;
    private FrameLayout preview;
    private Camera.AutoFocusCallback autoFocusCB;
    private Camera.PreviewCallback previewCb;
    private boolean isInitialised=false;
    public static final String IS_XML="is_xml";

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initControls();
    }

    @Override
    protected void onStart() {
        initialise();
        super.onStart();
    }


    private void initControls() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        autoFocusHandler = new Handler();
        initPreview();
        initAutoFocus();
        initialise();
        params = mCamera.getParameters();

        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        scanButton = findViewById(R.id.ScanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isFlashOn) {
                    turnFlashOn();
                }
                else {
                    turnFlashOff();
                }
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            releaseCamera();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void turnFlashOn() {
        if(mCamera!=null) {
            params =mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
            mCamera.startPreview();
            isFlashOn=true;
            scanButton.setText(getString(R.string.flashOffMessage));
            scanButton.setBackgroundColor(getResources().getColor(R.color.btnColor));
        }
        else {
            Toast.makeText(this,"Can't turn on Flash",Toast.LENGTH_LONG).show();
        }
    }

    private void turnFlashOff() {
        if(mCamera!=null) {
            params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            isFlashOn=false;
            scanButton.setText(getString(R.string.flashOnMessage));
            scanButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    public Camera getCameraInstance() {
        Camera camera=null;
        try {
            camera = Camera.open();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return camera;

    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            int numChild=preview.getChildCount();
            for(int i=0;i<numChild;++i) {
                preview.getChildAt(i).setVisibility(View.GONE);
            }
            isInitialised=false;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    void initPreview() {
        previewCb = new Camera.PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);
//                Log.e("NITISH", "onPreviewFrame: "+result);
                if (result != 0) {
                    previewing=false;
                    SymbolSet syms = scanner.getResults();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    if(v!=null) {
                        v.vibrate(100);
                    }
                    for (Symbol sym : syms) {
                        Log.e("NITISH", "SymbolSet: "+sym.getData());
                        isDataValid=true;
                        boolean isXML=false;
                        byte []message=sym.getDataBytes();

                        String res=new String(message,Charset.forName("UTF-8"));
                        String xmlHeader=res.substring(0,4);
                        Log.e("NITISH", "onPreviewFrame: res "+res);
                        Log.e("NITISH", "onPreviewFrame: xmlHeader "+xmlHeader);

                        byte []uiData=null;
                        if(xmlHeader.equals("<QPD") || xmlHeader.equals("<QDB")) {
                            uiData=message;
                            isXML=true;
                        }
                        else {
                            try {
                                uiData=new BigInteger(res).toByteArray();
                                String data_=new BigInteger(res).toString();
                                Log.d("DATA",data_+" "+data_.length());
                                Log.d("Size",uiData.length+" "+res.length());
                                uiData=decompress(uiData);
                                //showAlertDialog(new BigInteger(res).toString());
                                if(uiData==null) {
                                    isDataValid=false;
                                }
                            }
                            catch(NumberFormatException e) {

                                isDataValid=false;
                            }
                        }
                        if (!barcodeScanned && isDataValid) {
                            barcodeScanned = true;
                            turnFlashOff();
                            releaseCamera();
                            showScannedResult(uiData,isXML);
                        }
                        break;
                    }
                }
            }
        };
    }

    private void initAutoFocus() {
        autoFocusCB = new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                autoFocusHandler.postDelayed(doAutoFocus, 500);
            }
        };
    }



    private void showScannedResult(byte []uidData,boolean isXML) {
        Intent intent =new Intent(this,ScanResultActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT,uidData);
        intent.putExtra(IS_XML,isXML);
        startActivityForResult(intent,REQUEST_CODE);
    }

    private void showAlertDialog(String alertMessage) {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.app_name))
                .setCancelable(false)
                .setMessage(alertMessage+getString(R.string.readingErrorMessage))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private byte[] decompress(byte[] data) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            GZIPInputStream gis = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
            os.close();
            gis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return os.toByteArray();
    }

    @Override
    protected void onStop() {
        releaseCamera();
        turnFlashOff();
        super.onStop();
    }

    private void initialise() {
        if(!isInitialised) {
            barcodeScanned=false;
            mCamera=getCameraInstance();
            previewing = true;
            CameraPreview mPreview = new CameraPreview(BarcodeScanner.this, mCamera, previewCb,
                    autoFocusCB);
            preview = findViewById(R.id.cameraPreview);
            preview.addView(mPreview);
            mCamera.startPreview();
            isInitialised=true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CODE  && resultCode  == RESULT_OK) {
                initialise();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
