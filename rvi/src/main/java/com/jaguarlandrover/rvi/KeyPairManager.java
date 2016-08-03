package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    KeyManager.java
 * Project: HVACDemo
 *
 * Created by Lilli Szafranski on 10/1/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.*;

import android.util.Base64;

//import org.bouncycastle.util.encoders.Base64;
//import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


import static android.content.Context.MODE_PRIVATE;

/* Code from Stack-Overflow question/answer: http://stackoverflow.com/a/18115456 */

class KeyPairManager
{
    private final static String TAG = "HVACDemo:KeyManager";

    SharedPreferences SP;
    SharedPreferences.Editor SPE;
    PublicKey pubKey;
    PrivateKey privKey;
    Context context;

//    protected KeyManager(Context context) {
    protected KeyPairManager() {
//        this.context = context;
//        SP = context.getSharedPreferences("KeyPair", MODE_PRIVATE);
    }


    public static PrivateKey getPrivKeyA() {

        return getPrivateKey("MIICUjCCAbugAwIBAgIJAMI080XZPsPUMA0GCSqGSIb3DQEBCwUAMEIxCzAJBgNV\n" +
                            "BAYTAlVTMQ8wDQYDVQQIDAZPcmVnb24xETAPBgNVBAcMCFBvcnRsYW5kMQ8wDQYD\n" +
                            "VQQKDAZHRU5JVkkwHhcNMTUxMTI3MjMxMTQ0WhcNMTYxMTI2MjMxMTQ0WjBCMQsw\n" +
                            "CQYDVQQGEwJVUzEPMA0GA1UECAwGT3JlZ29uMREwDwYDVQQHDAhQb3J0bGFuZDEP\n" +
                            "MA0GA1UECgwGR0VOSVZJMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDg5A1u\n" +
                            "Z5F36vQEYbMWCV4wY4OVmicYWEjjl/8YPA01tsz4x68i/NnlMNalqpGCIZ0AwqGI\n" +
                            "5DZAWWoR400L3SAmYD6sWj2L9ViIAPk3ceDU8olYrf/Nwj78wVoG7qqNLgMoBNM5\n" +
                            "84nlY4jy8zJ0Ka9WFBS2aDtB3Aulc1Q8ZfhuewIDAQABo1AwTjAdBgNVHQ4EFgQU\n" +
                            "4Sz8rAMA+dHymJTlZSkap65qnfswHwYDVR0jBBgwFoAU4Sz8rAMA+dHymJTlZSka\n" +
                            "p65qnfswDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQDFOapf3DNEcXgp\n" +
                            "1u/g8YtBW24QsyB+RRavA9oKcFiIaHMkbJyUsOergwOXxBYhduuwVzQQo9P5nR0W\n" +
                            "RdUfwtE0GuaiC8WUmjR//vKwakj9Bjuu73ldYj9ji9+eXsL/gtpGWTIlHeGugpFs\n" +
                            "mVrUm0lY/n2ilJQ1hzBZ9lFLq0wfjw==");
    }

    public static PublicKey getPubKeyA() {
        return getPublicKey("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEApkRFT75OhNvLGZixkslW\n" +
                            "k6bVXWwMRXx5rj46HqOOp9AW3rGtEqwbXHGPDONY9ur7RBIRHi9lFTjG/V4Ycvdu\n" +
                            "zwt3cr7YqgrGW7bIYitgXYrv8ymveX0ZVeBRwv2Ij96Ybh1P7BReq0oiAJNYsXpL\n" +
                            "wrPX24Bxz27I2oL6ZWLu79EtNXnGdkDiWbWXdjwxN5A8MgBkn+qzBDvEMpvClNV5\n" +
                            "s3dOtBrFUiFHY99jwznCN8tpuMReASHjcM46lmFHDEUyUmiBby4pSGpQVTg/QhO0\n" +
                            "8RrTNDHytvH/xMdrD99I4HrBHgM6eGJYSzfuoyI3lyKZlkOxFlYt88znCYVz2ulu\n" +
                            "urjwDqEGzuqZ3TC+WbBnmdQrYuvgh0xyFXZa3DHS8dCorUMt0W9vWsB7maC+KZgB\n" +
                            "40P/I/jsFC1DlYNIzRYJ3Ua4nnj8IbaXvbyMdDoQ5tQPmbnoKxo6ZIM3hv+K196O\n" +
                            "G3iP8c1TdzdAmRhjFEoXLShp1Y3Ek5O/ifPc6nY+IDgbyOFCo0MpFWCjRODKgh51\n" +
                            "aY1nfJ00sFc86IAmtgQYyjdFUbBx/Tpdhz2h7E7s9wm48I2WQ8Klv1KSEW/nuIdr\n" +
                            "H+nmab0V5CSYFsUs0ZzI3iTRbodT53vyJ+SbB4xK3vhXxp7mJaBm4xHjc0cnypUa\n" +
                            "AUMXTi0cduJ6Zt68ieVRc3MCAwEAAQ==");
    }






//    public void generateKeys() {
//        try {
//            KeyPairGenerator generator;
//            generator = KeyPairGenerator.getInstance("RSA", "BC");
//            generator.initialize(4096, new SecureRandom());
//
//            KeyPair pair = generator.generateKeyPair();
//
//            pubKey = pair.getPublic();
//            privKey = pair.getPrivate();
//
//            byte[] publicKeyBytes = pubKey.getEncoded();
//            String pubKeyStr = new String(Base64.encode(publicKeyBytes, Base64.DEFAULT));
//
//            byte[] privKeyBytes = privKey.getEncoded();
//            String privKeyStr = new String(Base64.encode(privKeyBytes, Base64.DEFAULT));
//
//            SPE = SP.edit();
//            SPE.putString("PublicKey", pubKeyStr);
//            SPE.putString("PrivateKey", privKeyStr);
//            SPE.commit();
//
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        }
//    }


    public static PublicKey getPublicKey(String pubKeyStr) {
        //if (!SP.contains("PublicKey")) generateKeys();

        //String pubKeyStr = SP.getString("PublicKey", "");
//        String pubKeyStr = //"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";
//                "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEApkRFT75OhNvLGZixkslW\n" +
//                "k6bVXWwMRXx5rj46HqOOp9AW3rGtEqwbXHGPDONY9ur7RBIRHi9lFTjG/V4Ycvdu\n" +
//                "zwt3cr7YqgrGW7bIYitgXYrv8ymveX0ZVeBRwv2Ij96Ybh1P7BReq0oiAJNYsXpL\n" +
//                "wrPX24Bxz27I2oL6ZWLu79EtNXnGdkDiWbWXdjwxN5A8MgBkn+qzBDvEMpvClNV5\n" +
//                "s3dOtBrFUiFHY99jwznCN8tpuMReASHjcM46lmFHDEUyUmiBby4pSGpQVTg/QhO0\n" +
//                "8RrTNDHytvH/xMdrD99I4HrBHgM6eGJYSzfuoyI3lyKZlkOxFlYt88znCYVz2ulu\n" +
//                "urjwDqEGzuqZ3TC+WbBnmdQrYuvgh0xyFXZa3DHS8dCorUMt0W9vWsB7maC+KZgB\n" +
//                "40P/I/jsFC1DlYNIzRYJ3Ua4nnj8IbaXvbyMdDoQ5tQPmbnoKxo6ZIM3hv+K196O\n" +
//                "G3iP8c1TdzdAmRhjFEoXLShp1Y3Ek5O/ifPc6nY+IDgbyOFCo0MpFWCjRODKgh51\n" +
//                "aY1nfJ00sFc86IAmtgQYyjdFUbBx/Tpdhz2h7E7s9wm48I2WQ8Klv1KSEW/nuIdr\n" +
//                "H+nmab0V5CSYFsUs0ZzI3iTRbodT53vyJ+SbB4xK3vhXxp7mJaBm4xHjc0cnypUa\n" +
//                "AUMXTi0cduJ6Zt68ieVRc3MCAwEAAQ==";

        byte[] sigBytes = Base64.decode(pubKeyStr, Base64.DEFAULT);

        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sigBytes);
        KeyFactory keyFact = null;

        try {
            keyFact = KeyFactory.getInstance("RSA");//, "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
//        catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        }

        try {
            return keyFact.generatePublic(x509KeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

//    public String getPublicKeyAsString() {
//        return SP.getString("PublicKey", "");
//    }

    public static PrivateKey getPrivateKey(String privKeyStr) {
        //if (!SP.contains("PrivateKey")) generateKeys();

        //String privKeyStr = SP.getString("PrivateKey", "");
//        String privKeyStr =
////                "MIIJJwIBAAKCAgEAqA2MbZ+mPUo68SB2Hp4AFgMHua74QJRlhQyFcDQQSs5umvDJ\n" +
////                "i9QMM+k1V7WHlPKM5nOXGVhpFAjeHlxKwM9ezz42yHsbG20YOf3DT0HlJ2blkLCm\n" +
////                "JrWLb9POeiJARumQ92NbPX57SJjlPmHtzadVGk/xS58qURNl1qK9a56bOrZNNtXg\n" +
////                "OW6uAQkaVDctIzIcHH+Mv3a72ZcaTWX6UuMt1QK6meQ1rdqGKblFpgOfFzp/jRwx\n" +
////                "8qo5bG9D4Vk0umao0o5PjHVssscqEaS3YLQD+HmzTV9R4OgauGFogHQIAjtgAchH\n" +
////                "sHJ/w3mb/j3AKsMvyBkPk3J/DG1gVNK+8+Iu2AYWwY2iZnYB8nGQipO6WUsohpMO\n" +
////                "sQ1ayu5bwS7Mpu+BFaOdc4UUUU3XU+YNyhyYnv9kCZJThulYLnU7gYaVPJL3rJBj\n" +
////                "yiEfWe3X033k9R4+HCGpEh6RksqHAytw+v0xLtDKr0IYPLjMXYXAHU0abnCRdx8Z\n" +
////                "Qa64TsKEoFicVUo8AdgR5boAclKaVWADab/WUEVJL8uIvutEvisNNqMv57C95f/0\n" +
////                "sBG+0rJ2W+ix3+DIwkSnZl+7FwXSMtCbV33la2Rz+Jv/G9OtRYUXyXo3Wtrshnti\n" +
////                "Od3f6KVd18uEE/dpkmWoCnM3AJObp+LH0Qtyyj9u7yiE/oH0TGxYaxN0AFUCAwEA\n" +
////                "AQKCAgAJdPm6EYnhWRQIDbOX9Xw7wN/maxo9SByOaC36KnPgQc2nmnU9ESm+ohkG\n" +
////                "XojKsPuM3GRcYMVJA1a8jqUGRKUpNG8QyRy1ZmY+nTEofKTQmtHAWrB+7Shzmo+7\n" +
////                "/8FuPCaURVsVEtENPM1pvUkMkaYQXP9PKh3Pgi/w7CeMrtg9bZTayR5dHQVduY1p\n" +
////                "v9EicedzExpwi9X2YLJyOJLlpzMlEYo+rsD2j52NK4fn17xUKk6TOkr1RO8GUxyW\n" +
////                "ydZVxwEpGCkR1mnPhbWwWHyZTIgMq4yThhGtfmJCjfi7gcwRlBXLA+JE46yc63MD\n" +
////                "06XEwkcmE4L04TSVFnKdU167irboCdXQ7wZCA6Hfk6nvqUtnfTu42atoJ0MIe/vE\n" +
////                "ZLvi0Kzgr8gD6wiCSEDnjJYeuSJ1HPnEONocx2aDklqXVqPnUcBCt8bj8/1e0uO/\n" +
////                "oKrx017Y8T1ccjU9nAdvdjpe6sTpVLEgkOlC4FQCHwdOBi0ncXInuNEsC6/EX/ZK\n" +
////                "ifwe7Txb006dgSTLpNNmupPGqjd3Z2TAkv7ptHUDRULLYM5Wwyh7gNGMkrmxRmHI\n" +
////                "YkwcVB+KVAl8eXVzeT683mZYPk8jczvjSiFXJkCkl+v3kO+qecPDrj/MKe5QF5SY\n" +
////                "RzNZkIzAK8cyY5WY80kqUkLujQXHHM+P+Y0BXXCmPdHCrkyT4QKCAQEA1GR0bXgW\n" +
////                "6F/UqCB4AWjry+JcqNlAJGc7JHL0CaQktaRe6HEecjY+1qBYsij2bd7kkqgvhsCH\n" +
////                "Ro4e8M+eOYPaZHAsAxehVMsUNEJ5Kx3Isu0hcT27uWqhkf2fqyU+PUG5gAmnZwgJ\n" +
////                "JN/PhuLoTRWTnhIQhEuh0QrlnhOy2Iprx5eSCCcmsh4bLfHiXGiFbuOlk8jTTnQW\n" +
////                "0PLMyr1n4SHXFA4XRLAN1FLuvPGdy9wgybAqHwl4UX0b3ifZBIjrSXbpNhhdoJJ/\n" +
////                "JDtMfi2Udlpu9tbkmgbloTQq5a/c7Ozd9/ZzGKTOa9ZlUtZtWTMkhVG6aoUTRw5X\n" +
////                "Uw25kPXqWBBb7wKCAQEAyo6Rpg1cv+WkWAxwwDJGlLVcEwGrlosuHyH4+u9XtJul\n" +
////                "v5EkjLq35YBL5cGcwhRdugASgWqgBEvMQ5PAyTu6o7rWEVnIh2DZa6krXtD/ABgJ\n" +
////                "rTSFoHQkxub+mJo8HYSNo1NEzH+OigF8kexntWLeQFbIIjznuDTnN7ErrSS6xQ1R\n" +
////                "SwcCy8AiRpk1g7V18qyVq6jmx4dHzKpBvS7/7lxk6nuzbkiztpm0MmQQkj/oKtGf\n" +
////                "HiIPK4pFqFhLjsOphYztv1dD73D3Otfz/W+Eo9jMI4M05hz3YSZTRkuy+HBJKcVC\n" +
////                "P7qdkjT6JoYWwmnbupVkD1BCiWDLG+FR/dBroDrz+wKCAQBrKrTEd4+3eIMowhlL\n" +
////                "PNGdcn4mhFglBiGFZqtUIgqTzHhG+KzQHCbgzM90Nc8B6TGT/mDcsVCQfS2HfHrt\n" +
////                "smYDv8W1WbsBueZb6B6TbtfR0GzLYsbR68fSjhJ+nLAm4H7k5/obtqZGgNeJy/xC\n" +
////                "yNxPKfZDipX/kGwDsxTOxilT8Fqy2m5f+WjlaeM5lVOefCcTxoU4VMMecyR6N8+u\n" +
////                "fRC1AdcjxWF0/NORaqigMFYq7kSSnIilNWdWVOzfqOZvjxxlOy4i00hr06/pCr6a\n" +
////                "aU8MJan7Pfenu1bZxj7HHU1yzFuO17mAGBHQFnneRDvtOSvCi5sKg2X0djxntG2m\n" +
////                "ObrpAoIBAFXaq/1X+JIVwdzYGQo9xRDO9iHQOWxrvbbQbXne7KvOtShHWtiTY9LQ\n" +
////                "2Fqf/9N7Uma+FRnpmb14azHRdItW17sCPC3KgmZvNHifkZR0vUDK738Mnf7ue5Aa\n" +
////                "5JrF9OkGAArUBnsvLbls++HV8/c72rwmjFmGuyims8I1RC/1O3y+MlC8vgCihWSr\n" +
////                "F7WlukKLyDykMH0t587l80s+mpfrbZK9r8X05sdkyq35H9T9ZWiQxQK+qPNpfjDm\n" +
////                "i9ANOtMycqsBT/gzR3cXhYFMPUo9X/lFaayHZos09WL8PvVyknAz/eqJLLSF4+lk\n" +
////                "OE1bFRk2rNyYxjr4Xd6JWHleV9/oXPUCggEAOHCvPonfaZNOilKGzRYd967JxH5V\n" +
////                "n/gLxdYrOJx6+Vv8qzhNq+sozOBEgLxJ7xUNHo0r9TqB8Mpv6Q9hMYvXgqnf+j58\n" +
////                "lcie9vDwaTa0yPFymvD/47C5nc6w2Hd8nRc0D3wzOPSdIu7tExxv44911OlQuH/z\n" +
////                "01L8WARunxjyzQkbe4DL+L/5A6DgYWrNiWV/SqMv0j/UaaGVNlm3uTsh77EqxXwL\n" +
////                "4XSDE1NztW/9k+7SdtQv9hxPR1uM4qoE4qW0LXo/IlCSTf1BvGXk5cvH/3DINaQI\n" +
////                "Ik5mQP9/uCfnnniqLrCyF/c5zY+nUAjVbiIW8RF7kd3h5QGzCzGI1PCA1A==";
//
//
//                /* Old, but working key */
//                //"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";
//
//                "MIICUjCCAbugAwIBAgIJAMI080XZPsPUMA0GCSqGSIb3DQEBCwUAMEIxCzAJBgNV\n" +
//                "BAYTAlVTMQ8wDQYDVQQIDAZPcmVnb24xETAPBgNVBAcMCFBvcnRsYW5kMQ8wDQYD\n" +
//                "VQQKDAZHRU5JVkkwHhcNMTUxMTI3MjMxMTQ0WhcNMTYxMTI2MjMxMTQ0WjBCMQsw\n" +
//                "CQYDVQQGEwJVUzEPMA0GA1UECAwGT3JlZ29uMREwDwYDVQQHDAhQb3J0bGFuZDEP\n" +
//                "MA0GA1UECgwGR0VOSVZJMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDg5A1u\n" +
//                "Z5F36vQEYbMWCV4wY4OVmicYWEjjl/8YPA01tsz4x68i/NnlMNalqpGCIZ0AwqGI\n" +
//                "5DZAWWoR400L3SAmYD6sWj2L9ViIAPk3ceDU8olYrf/Nwj78wVoG7qqNLgMoBNM5\n" +
//                "84nlY4jy8zJ0Ka9WFBS2aDtB3Aulc1Q8ZfhuewIDAQABo1AwTjAdBgNVHQ4EFgQU\n" +
//                "4Sz8rAMA+dHymJTlZSkap65qnfswHwYDVR0jBBgwFoAU4Sz8rAMA+dHymJTlZSka\n" +
//                "p65qnfswDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQDFOapf3DNEcXgp\n" +
//                "1u/g8YtBW24QsyB+RRavA9oKcFiIaHMkbJyUsOergwOXxBYhduuwVzQQo9P5nR0W\n" +
//                "RdUfwtE0GuaiC8WUmjR//vKwakj9Bjuu73ldYj9ji9+eXsL/gtpGWTIlHeGugpFs\n" +
//                "mVrUm0lY/n2ilJQ1hzBZ9lFLq0wfjw==";


        byte[] sigBytes = Base64.decode(privKeyStr, Base64.DEFAULT);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(sigBytes);

        KeyFactory keyFact = null;

        try {
            keyFact = KeyFactory.getInstance("RSA", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        try {
            return keyFact.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getPrivateKeyAsString() {
        return SP.getString("PrivateKey", "");
    }
}
