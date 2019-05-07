package com.example.zbarbarcodescanner.util;
import android.content.Context;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.KeyStoreException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.bouncycastle.x509.X509CertificatePair;


public class SignVerifier {

    public Pair<String,Boolean> verifySignature(byte[] inputData, byte[] signature, PublicKey publicKey,int recur){
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            String shaDigest=getSHARec(new String(inputData),recur);
            publicSignature.update(getBytefromHex(shaDigest));
            return new Pair<>(shaDigest,publicSignature.verify(signature));
        }
        catch(InvalidKeyException e) {
            e.printStackTrace();
        }
        catch(SignatureException e) {
            e.printStackTrace();
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new Pair<>("",false);
    }
    public PublicKey getPublicKey(Context context) {
        try {
            String alias = "abhishek";
            String password="kumar123";
            InputStream is = context.getAssets().open("mykeystore.bks");

            KeyStore keystore = KeyStore.getInstance("BKS");
            keystore.load(is, password.toCharArray());

            Key key = keystore.getKey(alias, password.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keystore.getCertificate(alias);

                // Get public key
                return cert.getPublicKey();
            }
        }
        catch(KeyStoreException e) {
            e.printStackTrace();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch(CertificateException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
    public PublicKey getPublicKey(String filename, char[] password,Context context) {
        try {
            InputStream fis = context.getAssets().open(filename);
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)f.generateCertificate(fis);

            PublicKey pk = certificate.getPublicKey();
//            KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");
//            InputStream fis = context.getAssets().open(filename);
//            pkcs12KeyStore.load(fis, password);
//            fis.close();
//            KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);
//            KeyStore.Entry entry = pkcs12KeyStore.getEntry("owlstead", param);
//            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
//                throw new KeyStoreException("That's not a private key!");
//            }
//            KeyStore.PrivateKeyEntry privKeyEntry = (KeyStore.PrivateKeyEntry) entry;
//            PublicKey publicKey = privKeyEntry.getCertificate().getPublicKey();
            fis.close();
            return pk;
        }

        catch(IOException e) {
            e.printStackTrace();
        }
        catch(CertificateException e) {
            e.printStackTrace();
        }
//        catch(UnrecoverableEntryException e) {
//            e.printStackTrace();
//        }
//        catch(KeyStoreException e) {
//                e.printStackTrace();
//        }
//        catch(NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
        return null;
    }
    private byte[] getSHA(byte []input,int recur) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for(int i=0;i<=recur;++i) {
                input=md.digest(input);
            }
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return input;
    }

    private String getSHARec(String input,int recur) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for(int i=0;i<recur;++i) {
                input=getHexStringFromBytes(md.digest(input.getBytes()));
            }
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return input;
    }
    public static String getHexStringFromBytes(byte []data) {
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
    private byte []getBytefromHex(String hexString) {
        String hexCharset="0123456789abcdef";
        byte []data=new byte[hexString.length()/2];
        int j,k;
        for(int i=0;i<hexString.length();i+=2) {
           j= hexCharset.indexOf(hexString.charAt(i));
           k=hexCharset.indexOf(hexString.charAt(i+1));
           data[i/2]=(byte)((j<<4)^k);
        }
        return data;
    }
}
