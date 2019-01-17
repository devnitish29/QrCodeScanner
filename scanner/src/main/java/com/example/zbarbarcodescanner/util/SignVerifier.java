package com.example.zbarbarcodescanner.util;
import android.content.Context;

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

public class SignVerifier {

    public boolean verifySignature(byte[] inputData, byte[] signature, PublicKey publicKey){
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(getSHA(inputData));
            return publicSignature.verify(signature);
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
        return false;
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
            KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");
            InputStream fis = context.getAssets().open(filename);
            pkcs12KeyStore.load(fis, password);
            fis.close();
            KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);
            KeyStore.Entry entry = pkcs12KeyStore.getEntry("owlstead", param);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                throw new KeyStoreException("That's not a private key!");
            }
            KeyStore.PrivateKeyEntry privKeyEntry = (KeyStore.PrivateKeyEntry) entry;
            PublicKey publicKey = privKeyEntry.getCertificate().getPublicKey();
            fis.close();
            return publicKey;
        }
        catch(KeyStoreException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch(CertificateException e) {
            e.printStackTrace();
        }
        catch(UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return null;
    }
    private byte[] getSHA(byte []input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // digest() method called
            // to calculate message digest of an input
            // and return array of byte
            return md.digest(input);
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
