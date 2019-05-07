package com.example.zbarbarcodescanner;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.zbarbarcodescanner.util.AadhaarQR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class QrModeAdapter extends FragmentPagerAdapter {
    private AadhaarQR aadhaarQR;
    private String xmlData;
    private boolean isVerified;

    public QrModeAdapter(FragmentManager fm,AadhaarQR aadhaarQR,String xml,boolean isVerified) {
        super(fm);
        this.aadhaarQR=aadhaarQR;
        this.xmlData=xml;
        this.isVerified=isVerified;
    }
    @Override
    public Fragment getItem(int i) {
        if(i==0) {
            try {
                DataFragment dataFragment=new DataFragment();
                Bundle bundle=new Bundle();
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                ObjectOutputStream o=new ObjectOutputStream(out);
                o.writeObject(aadhaarQR);
                bundle.putByteArray("Data",out.toByteArray());
                bundle.putBoolean("verify",isVerified);
                o.close();;
                out.close();
                dataFragment.setArguments(bundle);
                return dataFragment;
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        else {
            XmlFragment xmlFragment=new XmlFragment();
            Bundle bundle=new Bundle();
            bundle.putString("Xml",xmlData);
            bundle.putString("name",aadhaarQR.getName());
            xmlFragment.setArguments(bundle);
            return xmlFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "DATA";
        }
        else  {
            return "XML";
        }
    }

}
