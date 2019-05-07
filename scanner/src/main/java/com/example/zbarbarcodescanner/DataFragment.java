package com.example.zbarbarcodescanner;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.zbarbarcodescanner.util.AadhaarQR;

import org.openJpeg.OpenJPEGJavaDecoder;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;


/**
 * A simple {@link Fragment} subclass.
 */
public class DataFragment extends Fragment implements View.OnClickListener {
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
    private String sha256;
    private AadhaarQR aadhaarQR=new AadhaarQR();
    private String address[]=new String[11];
    byte []qrData;
    private Activity mActivity;

    public DataFragment() {
        // Required empty public constructor
    }




    private void initViews(View view) {
        nameView=view.findViewById(R.id.textViewName);
        nameView.setText("");
        dobView=view.findViewById(R.id.textViewDob);
        genderView= view.findViewById(R.id.textViewGender);
        addressView= view.findViewById(R.id.textViewAddress);
        uidView= view.findViewById(R.id.textViewUid);
        verifyStatusView=view.findViewById(R.id.verifyProgressView);
        scanRepeatButton= view.findViewById(R.id.scanRepeatButton);
        profieImageView=view.findViewById(R.id.profileImageView);
        digSignImageView=view.findViewById(R.id.digSignImage);
        scanRepeatButton.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_data, container, false);
        initViews(view);
        displayData();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args=getArguments();
        if(args!=null) {
            try {
                qrData=args.getByteArray("Data");
                ByteArrayInputStream in=new ByteArrayInputStream(qrData);
                ObjectInputStream i=new ObjectInputStream(in);
                aadhaarQR=(AadhaarQR)i.readObject();
                isVerified=args.getBoolean("verify");
                in.close();
                i.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            catch(ClassNotFoundException e) {

            }
        }
        mActivity=getActivity();
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
        uidView.setText("XXXX XXXX "+aadhaarQR.getUid().substring(0,4));
        if(aadhaarQR.getImage()!=null && aadhaarQR.getImage().length()!=0) {
            byte []image=decodeJp2PNG(aadhaarQR.getImage());
            if(image!=null) {
                aadhaarQR.setImage(Base64.encodeToString(image,Base64.DEFAULT));
                Bitmap bitmap= BitmapFactory.decodeByteArray(image,0,image.length);
                profieImageView.setImageBitmap(bitmap);
            }

        }
        else {
            TextView nameTitleView =mActivity.findViewById(R.id.textViewNameTag);
            nameTitleView.setPadding(0,80,0,0);
            nameView.setPadding(0,80,0,0);
            profieImageView.setVisibility(View.GONE);
        }
        if(!isVerified) {
            digSignImageView.setImageResource(R.drawable.valid);
            verifyStatusView.setText(getString(R.string.verifyResultPositive));
        }
        else {
            digSignImageView.setImageResource(R.drawable.invalid);
            verifyStatusView.setText(getString(R.string.verifyResultNegative));
        }
    }

    private byte[] decodeJp2PNG(String imageString) {
        byte []imageBytes=Base64.decode(imageString,Base64.DEFAULT);
        try {
            FileOutputStream fo=mActivity.openFileOutput("temp.jp2", Context.MODE_PRIVATE);
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
        String fileDir=mActivity.getFilesDir().getPath();
        params2[1] = fileDir+"/temp.jp2";// path to jp2
        params2[2] = "-o";
        params2[3] = fileDir+"/temp.png"; // path to png
        decoder.decodeJ2KtoImage(params2);
        try {
            FileInputStream fin=mActivity.openFileInput("temp.png");
            byte []buffer=new byte[fin.available()];
            fin.read(buffer);
            return buffer;
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public void onClick(View view) {
        int id=view.getId();
        if(id==R.id.scanRepeatButton) {
            mActivity.onBackPressed();

        }
//        else if(id==R.id.showExtraButton) {
//            showAlertDialog("Extra data",
//                    "Version: "+aadhaarQR.getVersion()+"\n\n"+
//                            "Mobile: "+aadhaarQR.getMobile()+"\n\n"+
//                            "Email: "+aadhaarQR.getEmail()+"\n\n"+
//                            "DigSign: "+aadhaarQR.getDigSign());
//        }
    }

    private void showAlertDialog(String title,String message) {
        new AlertDialog.Builder(mActivity)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

}
