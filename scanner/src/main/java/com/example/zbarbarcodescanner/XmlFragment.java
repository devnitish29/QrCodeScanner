package com.example.zbarbarcodescanner;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zbarbarcodescanner.retro.RetroAPI;
import com.example.zbarbarcodescanner.retro.RetroControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class XmlFragment extends Fragment {
    private String xmlData;
    private TextView xmlView;
    private String name;


    public XmlFragment() {
        // Required empty public constructor
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_xml, container, false);
        xmlView=view.findViewById(R.id.xmlTextView);
        xmlView.setText(xmlData);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xmlData=getArguments().getString("Xml");
        name=getArguments().getString("name");
        handleXmlString(xmlData);

    }

    public void handleXmlString(String xmlContent) {
        if(xmlContent.length()>0) {

            RetroAPI retroAPI= RetroControl.getRetroInstance().create(RetroAPI.class);
            RetroControl.sendXmlResponse(retroAPI,xmlContent,new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.isSuccessful()) {
                        Toast.makeText(getActivity(),response.body(),Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getActivity(),"Failed2",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(getActivity(),"Failed",Toast.LENGTH_LONG).show();
                }
            });

            makeXmlFile(xmlContent);
        }
    }


    private void makeXmlFile(String xmlString) {
        boolean isFileMade=false;
        if(isExternalStorageWritable()) {
            File file=new File(getPublicAlbumStorageDir("QrXml").getAbsolutePath()+"/"+name+".xml");
            try {
                FileOutputStream fo=new FileOutputStream(file);
                fo.write(xmlString.getBytes());
                fo.close();
                isFileMade=true;
            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        if(!isFileMade)
            Toast.makeText(getActivity(),"Unable to Create XML file",Toast.LENGTH_LONG).show();
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public File getPublicAlbumStorageDir(String fileName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), fileName);
        if (!file.mkdirs()) {
            Log.e("FILE_TAG", "Directory not created");
        }
        return file;
    }
}
