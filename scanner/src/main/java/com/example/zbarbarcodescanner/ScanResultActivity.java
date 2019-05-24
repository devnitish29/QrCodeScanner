package com.example.zbarbarcodescanner;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;

import com.example.zbarbarcodescanner.util.AadhaarQR;
import com.example.zbarbarcodescanner.util.SignVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.PublicKey;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import static com.example.zbarbarcodescanner.util.SignVerifier.getHexStringFromBytes;


public class ScanResultActivity extends AppCompatActivity  {
    private final static String SIGN_KEY="sign_key";
    private final Charset CHARSET=Charset.forName("ISO-8859-1");
    private boolean isVerified;
    private String sha256;
    private AadhaarQR aadhaarQR=new AadhaarQR();
    private String address[]=new String[11];
    private boolean isXML;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        initViews();
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setDisplayHomeAsUpEnabled(true);
        tabLayout.setupWithViewPager(viewPager);


        if(savedInstanceState!=null && savedInstanceState.containsKey(SIGN_KEY)) {
            isVerified=savedInstanceState.getBoolean(SIGN_KEY);
        }
        else {
            isVerified=false;
        }

        Intent intent=getIntent();
        if(intent!=null) {
            isXML=intent.getBooleanExtra(BarcodeScanner.IS_XML,false);
            byte []uidBytes=intent.getByteArrayExtra(Intent.EXTRA_TEXT);
            if(isXML) {
                parseXMLToObject(uidBytes);
            }
            else {
                loadDataToObject(uidBytes);
            }
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
        viewPager=findViewById(R.id.pager);
        tabLayout =findViewById(R.id.modeTabs);
        toolbar=findViewById(R.id.toolbar);
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
            Log.e("NITISH", "parseXMLToObject: "+parser.toString() );
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
        aadhaarQR.setUid(temp.second);

        temp=getData(uidBytes,temp.first);
        aadhaarQR.setName(temp.second);

        temp=getData(uidBytes,temp.first);
        aadhaarQR.setDob(temp.second);

        temp=getData(uidBytes,temp.first);
        aadhaarQR.setGender(temp.second);

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

//        StringBuilder str=new StringBuilder();
//
//        for(String part:address) {
//            str.append(part+" ");
//        }
        String add=address[0]+" "+address[3]+" "+address[8]+" "+address[4]+" "+address[2]+" "+address[10]+" "+address[6]+" "+address[9]+" "+address[1]+" "+
                address[7]+"-"+address[5]+" ";

        aadhaarQR.setAddress(add);

        byte flag=uidBytes[0];

        switch (new String(new byte[] {flag},CHARSET).charAt(0)) {
            case '0':
                break;
            case '1':
                aadhaarQR.setEmail(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                break;
            case '2':
                aadhaarQR.setMobile(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                break;
            case '3':
                aadhaarQR.setMobile(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                aadhaarQR.setEmail(getHexStringFromBytes(getDataReverse(uidBytes,end,32)));
                end-=32;
                break;
        }
        aadhaarQR.setImage(Base64.encodeToString(getDataReverse(uidBytes,end,end-temp.first+1),Base64.DEFAULT));
    }

    private void verifySignature(byte []uidData) {
        byte []sign=Base64.decode(aadhaarQR.getDigSign(),Base64.DEFAULT);
        new VerifyAsyncTask().execute(uidData,sign);
    }
    private String getXmlOutput(String []address,AadhaarQR aadhaarQR) {
        StringWriter stringWriter=new StringWriter();
        XmlSerializer serializer= Xml.newSerializer();
        try {
            serializer.setOutput(stringWriter);
            serializer.startDocument("UTF-8",true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag("","OfflinePaperlessKyc");
            serializer.attribute("","referenceId",aadhaarQR.getUid());

            serializer.startTag("","UidData");

            serializer.startTag("","Poi");
            serializer.attribute("","dob",aadhaarQR.getDob());
            serializer.attribute("","e",aadhaarQR.getEmail());
            serializer.attribute("","gender",aadhaarQR.getGender());
            serializer.attribute("","m",aadhaarQR.getMobile());
            serializer.attribute("","name",aadhaarQR.getName());
            serializer.endTag("","Poi");

            serializer.startTag("","Poa");
            serializer.attribute("","country","India");
            serializer.attribute("","dist",address[1]);
            serializer.attribute("","house",address[3]);
            serializer.attribute("","loc",address[4]);
            serializer.attribute("","pc",address[5]);
            serializer.attribute("","po",address[6]);
            serializer.attribute("","state",address[7]);
            serializer.attribute("","street",address[8]);
            serializer.attribute("","subdist",address[9]);
            serializer.attribute("","vtc",address[10]);
            serializer.endTag("","Poa");

            serializer.startTag("","Pht");
            serializer.text(aadhaarQR.getImage());
            serializer.endTag("","Pht");
            serializer.endTag("","UidData");

            serializer.startTag("","Signature");
            serializer.startTag("","SignedInfo");
            serializer.startTag("","CanonicalizationMethod");
            serializer.attribute("","Algorithm","http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
            serializer.endTag("","CanonicalizationMethod");
            serializer.startTag("","SignatureMethod");
            serializer.attribute("","Algorithm","http://www.w3.org/2000/09/xmldsig#rsa-sha1");
            serializer.endTag("","SignatureMethod");
            serializer.startTag("","Reference");
            serializer.attribute("","URI","");
            serializer.startTag("","Transforms");
            serializer.startTag("","Transform");
            serializer.attribute("","Algorithm","http://www.w3.org/2000/09/xmldsig#enveloped-signature");
            serializer.endTag("","Transform");
            serializer.endTag("","Transforms");
            serializer.startTag("","DigestMethod");
            serializer.attribute("","Algorithm","http://www.w3.org/2001/04/xmlenc#sha256");
            serializer.endTag("","DigestMethod");
            serializer.startTag("","DigestValue");
            serializer.text(sha256);
            serializer.endTag("","DigestValue");
            serializer.endTag("","Reference");
            serializer.endTag("","SignedInfo");
            serializer.startTag("","SignatureValue");
            serializer.text(aadhaarQR.getDigSign());
            serializer.endTag("","SignatureValue");
            serializer.endTag("","Signature");
            serializer.endTag("","OfflinePaperlessKyc");
            serializer.endDocument();
            stringWriter.close();
        }
        catch(IOException e) {

        }
        return stringWriter.toString();

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



    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id)  {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    class VerifyAsyncTask extends AsyncTask<byte[],Void,Pair<String,Boolean>> {
        @Override
        protected Pair<String,Boolean> doInBackground(byte[]... uidDataBytes) {
            byte[] uidData=uidDataBytes[0];
            byte[] sign=uidDataBytes[1];
            try {
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                out.write(uidData,0,uidData.length-256);
                byte[] rawData=out.toByteArray();
                out.close();
                SignVerifier signVerifier=new SignVerifier();
                PublicKey publicKey=signVerifier.getPublicKey("okyc-publickey.cer","test".toCharArray(),getApplicationContext());
                return signVerifier.verifySignature(rawData,sign,publicKey,aadhaarQR.getUid().charAt(3)-'0');

            }
            catch(IOException e) {
                e.printStackTrace();
            }
            return new Pair<>("",false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Pair<String,Boolean> isVerified) {
            super.onPostExecute(isVerified);
            ScanResultActivity.this.isVerified=isVerified.second;
            ScanResultActivity.this.sha256=isVerified.first;
            String xmlContent="";
            if(!isXML) {
                xmlContent=getXmlOutput(address,aadhaarQR);
            }
            QrModeAdapter qrModeAdapter=new QrModeAdapter(getSupportFragmentManager(),aadhaarQR,xmlContent,ScanResultActivity.this.isVerified);
            viewPager.setAdapter(qrModeAdapter);
        }
    }



}
