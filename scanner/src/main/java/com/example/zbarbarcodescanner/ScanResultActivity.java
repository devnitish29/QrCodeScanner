package com.example.zbarbarcodescanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zbarbarcodescanner.util.SignVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;

import org.openJpeg.OpenJPEGJavaDecoder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ScanResultActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String SIGN_KEY="sign_key";

    private TextView nameView;
    private TextView dobView;
    private TextView genderView;
    private TextView addressView;
    private TextView uidView;
    private TextView verifyStatusView;
    private Button scanRepeatButton;
    private Button showExtraButton;
    private ImageView profieImageView;
    private ImageView digSignImageView;
    private ProgressBar progressBar;
    private final Charset CHARSET=Charset.forName("ISO-8859-1");
    private boolean isVerified;
    private AadhaarQR aadhaarQR=new AadhaarQR();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        initViews();

        if(savedInstanceState!=null && savedInstanceState.containsKey(SIGN_KEY)) {
            isVerified=savedInstanceState.getBoolean(SIGN_KEY);
            showVerificationResult(isVerified);
        }
        else {
            isVerified=false;
        }

        Intent intent=getIntent();
        if(intent!=null) {
            boolean isXML=intent.getBooleanExtra(BarcodeScanner.IS_XML,false);
            byte []uidBytes=intent.getByteArrayExtra(Intent.EXTRA_TEXT);
            if(isXML) {
                parseXMLToObject(uidBytes);
            }
            else {
                loadDataToObject(uidBytes);
            }
            displayData();
            if(!isVerified) {
                verifySignature(uidBytes);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SIGN_KEY,isVerified);
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        nameView=findViewById(R.id.textViewName);
        dobView=findViewById(R.id.textViewDob);
        genderView= findViewById(R.id.textViewGender);
        addressView= findViewById(R.id.textViewAddress);
        uidView= findViewById(R.id.textViewUid);
        verifyStatusView=findViewById(R.id.verifyProgressView);
        progressBar=findViewById(R.id.verifyProgress);
        scanRepeatButton= findViewById(R.id.scanRepeatButton);
        showExtraButton=findViewById(R.id.showExtraButton);
        profieImageView=findViewById(R.id.profileImageView);
        digSignImageView=findViewById(R.id.digSignImage);
        scanRepeatButton.setOnClickListener(this);
        showExtraButton.setOnClickListener(this);
    }

    private void parseXMLToObject(byte []uidBytes) {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new ByteArrayInputStream(uidBytes);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String eltName = null;

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        eltName = parser.getName();
                        if ("QPDB".equals(eltName) || "QDB".equals(eltName)) {
                            aadhaarQR.setName(parser.getAttributeValue(null,"n"));
                            aadhaarQR.setUid(parser.getAttributeValue(null,"u"));
                            aadhaarQR.setGender(parser.getAttributeValue(null,"g"));
                            aadhaarQR.setDob(parser.getAttributeValue(null,"d"));
                            aadhaarQR.setAddress(parser.getAttributeValue(null,"a"));
                            aadhaarQR.setDigSign(parser.getAttributeValue(null,"s"));
                            aadhaarQR.setMobile(parser.getAttributeValue(null,"m"));
                            if("QPDB".equals(eltName))
                                aadhaarQR.setImage(parser.getAttributeValue(null,"i"));
                        }
                        break;
                }
                eventType = parser.next();
            }
        }
        catch(XmlPullParserException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDataToObject(byte[] uidBytes) {
        int end=uidBytes.length-1;
        aadhaarQR.setDigSign(Base64.encodeToString(getDataReverse(uidBytes,end,256),Base64.DEFAULT));
        end-=256;

        Pair<Integer,String> temp=getData(uidBytes,2);
        aadhaarQR.setUid("XXXX XXXX "+temp.second.substring(0,4));

        temp=getData(uidBytes,temp.first);
        aadhaarQR.setName(temp.second);

        temp=getData(uidBytes,temp.first);
        aadhaarQR.setDob(temp.second);

        temp=getData(uidBytes,temp.first);
        aadhaarQR.setGender(temp.second);

        String address[]=new String[11];

        temp=getData(uidBytes,temp.first);
        address[0]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[1]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[2]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[3]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[4]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[5]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[6]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[7]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[8]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[9]=temp.second;

        temp=getData(uidBytes,temp.first);
        address[10]=temp.second;

        StringBuilder str=new StringBuilder();
        for(String part:address) {
            str.append(part+" ");
        }

        aadhaarQR.setAddress(str.toString());

        byte flag=uidBytes[0];

        switch (new String(new byte[] {flag},CHARSET).charAt(0)) {
            case '0':
                break;
            case '1':
                aadhaarQR.setMobile(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                break;
            case '2':
                aadhaarQR.setEmail(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                break;
            case '3':
                aadhaarQR.setEmail(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                aadhaarQR.setMobile(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                break;
        }
        aadhaarQR.setImage(Base64.encodeToString(getDataReverse(uidBytes,end,end-temp.first+1),Base64.DEFAULT));
    }

    private void verifySignature(byte []uidData) {
        byte []sign=Base64.decode(aadhaarQR.getDigSign(),Base64.DEFAULT);
        new VerifyAsyncTask().execute(uidData,sign);
    }

    private Pair<Integer,String> getData(byte []uidBytes, int start) {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        int i=start;
        while(uidBytes[i]!=-1) {
            out.write(uidBytes[i++]);
        }
        try {
            out.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(i+1,new String(out.toByteArray(),CHARSET));
    }

    private byte[] getDataReverse(byte []uidBytes, int end,int length) {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        int offSet=end-length+1;
        try {
            out.write(uidBytes,offSet,length);
            out.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private void decodeJp2PNG(String imageString) {
        byte []imageBytes=Base64.decode(imageString,Base64.DEFAULT);
        try {
            FileOutputStream fo=this.openFileOutput("temp.jp2", Context.MODE_PRIVATE);
            fo.write(imageBytes);
            fo.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        OpenJPEGJavaDecoder decoder = new OpenJPEGJavaDecoder();
        String[] params2 = new String[4];
        params2[0] = "-i";
        String fileDir=this.getFilesDir().getPath();
        params2[1] = fileDir+"/temp.jp2";// path to jp2
        params2[2] = "-o";
        params2[3] = fileDir+"/temp.png"; // path to png
        decoder.decodeJ2KtoImage(params2);
        try {
            FileInputStream fin=this.openFileInput("temp.png");
            byte []buffer=new byte[fin.available()];
            fin.read(buffer);
            Bitmap bitmap= BitmapFactory.decodeByteArray(buffer,0,buffer.length);
            profieImageView.setImageBitmap(bitmap);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void displayData() {
        nameView.setText(aadhaarQR.getName());
        dobView.setText(aadhaarQR.getDob());
        addressView.setText(aadhaarQR.getAddress());
        switch (aadhaarQR.getGender()) {
            case "M":
                genderView.setText("Male");
                break;
            case "F":
                genderView.setText("Female");
                break;
        }
        uidView.setText(aadhaarQR.getUid());
        if(aadhaarQR.getImage()!=null && aadhaarQR.getImage().length()!=0) {
            decodeJp2PNG(aadhaarQR.getImage());
        }
        else {
            TextView nameTitleView =findViewById(R.id.textViewNameTag);
            nameTitleView.setPadding(0,80,0,0);
            nameView.setPadding(0,80,0,0);
            profieImageView.setVisibility(View.GONE);
        }
    }


    private void showAlertDialog(String title,String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private String getHexStringFromBytes(byte []data) {
        char []hexCharset="0123456789abcdef".toCharArray();
        char []hexString=new char[2*data.length];
        int temp;
        for(int i=0;i<data.length;++i) {
            temp=data[i] & 0xff;
            hexString[2*i]=hexCharset[temp>>4];
            hexString[2*i+1]=hexCharset[temp & 0x0f];
        }
        return new String(hexString);
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        if(id==R.id.scanRepeatButton) {
            onBackPressed();

        }
        else if(id==R.id.showExtraButton) {
            showAlertDialog("Extra data",
                    "Version: "+aadhaarQR.getVersion()+"\n\n"+
                            "Mobile: "+aadhaarQR.getMobile()+"\n\n"+
                            "Email: "+aadhaarQR.getEmail()+"\n\n"+
                            "DigSign: "+aadhaarQR.getDigSign());
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    class VerifyAsyncTask extends AsyncTask<byte[],Void,Boolean> {
        @Override
        protected Boolean doInBackground(byte[]... uidDataBytes) {
            byte[] uidData=uidDataBytes[0];
            byte[] sign=uidDataBytes[1];
            try {
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                out.write(uidData,0,uidData.length-256);
                byte[] rawData=out.toByteArray();
                out.close();
                SignVerifier signVerifier=new SignVerifier();
                PublicKey publicKey=signVerifier.getPublicKey("test_gen_self_signed.pkcs12","test".toCharArray(),getApplicationContext());
                return signVerifier.verifySignature(rawData,sign,publicKey);

            }
            catch(IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            startSignatureVerification();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean isVerified) {
            super.onPostExecute(isVerified);
            ScanResultActivity.this.isVerified=isVerified;
            showVerificationResult(isVerified);
        }
    }

    private void showVerificationResult(Boolean isVerified) {
        progressBar.setVisibility(View.GONE);
        digSignImageView.setVisibility(View.VISIBLE);
        if(isVerified) {
            digSignImageView.setImageResource(R.drawable.valid);
            verifyStatusView.setText(getString(R.string.verifyResultPositive));
        }
        else {
            digSignImageView.setImageResource(R.drawable.invalid);
            verifyStatusView.setText(getString(R.string.verifyResultNegative));
        }
    }
    private void startSignatureVerification() {
        verifyStatusView.setText(getString(R.string.verifyProgress));
        progressBar.setVisibility(View.VISIBLE);
        digSignImageView.setVisibility(View.GONE);
    }
}
