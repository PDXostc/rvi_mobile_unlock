package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Privilege.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 10/4/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.util.Base64;
import com.google.gson.annotations.SerializedName;
import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;

/**
 * The class that encapsulates an RVI Privilege object: a server-signed JWT that contains privileges such as the right_to_invoke and right_to_receive services,
 * when those rights are available, the server-signed device certificate of the presenting RVI node, and a few other things.
 */
class Privilege {
    private final static String TAG = "RVI/Privilege__________";

    @SerializedName("right_to_invoke")
    private ArrayList<String> mRightToInvoke = null;

    @SerializedName("right_to_receive")
    private ArrayList<String> mRightToReceive = null;

    @SerializedName("iss")
    private String mIssuer = null;

    @SerializedName("device_cert")
    private String mEncodedDeviceCertificate = null;

    @SerializedName("validity")
    private Validity mValidity = null;

    @SerializedName("id")
    private String mId = null;

    private Certificate mCertificate = null;

    private String mJwt = null;

    public Privilege() {
    }

    /**
     * Constructor for the Privilege, which takes a server-signed jwt as a parameter. JWT can't be validated until we have the server and device certificate
     *
     * @param jwt JWT containing the privilege, signed by the server
     */
    Privilege(String jwt) {
        this.mJwt = jwt;
    }

    /**
     * Validates the JWT, using the server certificate passed in to the function, and deserializes its contents into the rest of the Privilege class's properties.
     *
     * @param key The server key used to verify the signature of the JWT
     * @return True if validation succeeds and JWT was parsed successfully, false if there was a problem
     */
    Boolean parse(Key key) {
        try {
            this.mRightToInvoke            = null;
            this.mRightToReceive           = null;
            this.mIssuer                   = null;
            this.mEncodedDeviceCertificate = null;
            this.mValidity                 = null;
            this.mIssuer                   = null;

            DefaultClaims claims = (DefaultClaims) Jwts.parser().setSigningKey(key).parse(getJwt()).getBody();

            this.mRightToInvoke            = (ArrayList<String>) claims.get("right_to_invoke", ArrayList.class);
            this.mRightToReceive           = (ArrayList<String>) claims.get("right_to_receive", ArrayList.class);
            this.mIssuer                   = claims.get("iss", String.class);
            this.mEncodedDeviceCertificate = claims.get("device_cert", String.class);
            this.mId                       = claims.get("id", String.class);

            this.mValidity = new Validity(Long.valueOf((Integer)claims.get("validity", HashMap.class).get("start")),
                                                   Long.valueOf((Integer)claims.get("validity", HashMap.class).get("stop")));

            byte [] decodedDeviceCert = Base64.decode(mEncodedDeviceCertificate, Base64.DEFAULT);
            this.mCertificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedDeviceCert));

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    /**
     * Confirms that the embedded device certificate matches the one provided.
     *
     * @param matching The certificate to compare.
     * @return True if matching, false if not.
     */
    boolean deviceCertificateMatches(Certificate matching) {
        return mCertificate.equals(matching);
    }

    /**
     * Old comparator method before use of multi-topic-level tail matching ('#').
     * @param right The topic filter contained in the privilege, representing a pattern for a service identifier to match against to confirm if the node has the
     *              right to receive or right to invoke that service.
     * @param serviceIdentifier The service identifier the right to receive string or right to invoke string is being compared against.
     * @return True if matches, false if not.
     */
    private boolean rightMatchesServiceIdentifierNonTailHashMatching(String right, String serviceIdentifier) {
        String rightParts[] = right.split("/", -1);
        String serviceParts[] = serviceIdentifier.split("/", -1);

        if (rightParts.length > serviceParts.length)
            return false;

        for (int i = 0; i < rightParts.length; i++) {
            if (!rightParts[i].toLowerCase().equals(serviceParts[i].toLowerCase()) && !rightParts[i].equals("+"))
                return false;
        }

        return true;
    }

    /**
     * This is the method that takes a string representing a service identifier and one of the string contained in the privilege's right_to_receive/right_to_invoke
     * arrays and matches that string against the service identifier.
     *
     * @param right The topic filter contained in the privilege, representing a pattern for a service identifier to match against to confirm if the node has the
     *              right to receive or right to invoke that service.
     * @param serviceIdentifier The service identifier the right to receive string or right to invoke string is being compared against.
     * @return True if matches, false if not.
     */
    private static boolean rightMatchesServiceIdentifier(String right, String serviceIdentifier) {
        /* If for whatever reason, the service identifier or topic filter (rights string) contains 2+ '/'s in a row,
           then this is considered an empty topic level, which is not allowed, so return 'false' */
        if (right.contains("//") || serviceIdentifier.contains("//"))
            return false;

        String rightParts[] = right.split("/", -1);
        String serviceParts[] = serviceIdentifier.split("/", -1);

        if (rightParts.length == 0)
            return false;

        for (int i = 0; i < rightParts.length; i++) {
            if (i == (rightParts.length - 1) && rightParts[i].equals("#"))
                return true;

            if (i >= serviceParts.length)
                return false;

            if (!rightParts[i].toLowerCase().equals(serviceParts[i].toLowerCase()) && !rightParts[i].equals("+"))
                return false;
        }

        if (rightParts.length < serviceParts.length)
            return false;

        return true;
    }

    /**
     * Checks the service identifier against the privilege's right_to_receive list.
     *
     * @param fullyQualifiedServiceIdentifier The service identifier to check.
     * @return True if matches, false if not.
     */
    boolean grantsRightToReceive(String fullyQualifiedServiceIdentifier) {
        if (fullyQualifiedServiceIdentifier == null || mRightToReceive == null)
            return false;

        for (String right : mRightToReceive) {
            if (rightMatchesServiceIdentifier(right, fullyQualifiedServiceIdentifier))
                return true;
        }

        return false;
    }

    /**
     * Checks the service identifier against the privilege's right_to_invoke list.
     *
     * @param fullyQualifiedServiceIdentifier The service identifier to check.
     * @return True if matches, false if not.
     */
    boolean grantsRightToInvoke(String fullyQualifiedServiceIdentifier) {
        if (fullyQualifiedServiceIdentifier == null || mRightToInvoke == null)
            return false;

        for (String right : mRightToInvoke) {
            if (rightMatchesServiceIdentifier(right, fullyQualifiedServiceIdentifier))
                return true;
        }

        return false;
    }

    String getJwt() {
        return mJwt;
    }

    ArrayList<String> getRightToInvoke() {
        return mRightToInvoke;
    }

    ArrayList<String> getRightToReceive() {
        return mRightToReceive;
    }

    String getIssuer() {
        return mIssuer;
    }

    String getEncodedDeviceCertificate() {
        return mEncodedDeviceCertificate;
    }

    Validity getValidity() {
        return mValidity;
    }

    String getId() {
        return mId;
    }
}

enum ValidityStatus
{
    PENDING,
    VALID,
    EXPIRED,
    INVALID
}

/**
 * Class to encapsulate the validity object
 */
class Validity {
    private final static String PRETTY_DATE_TIME_FORMATTER = "MM/dd/yyyy h:mm a z";

    private Long mStart;

    private Long mStop;

    Validity(Long start, Long stop) {
        mStart = start;
        mStop = stop;
    }

    ValidityStatus getStatus() {
        Long currentTime = System.currentTimeMillis() / 1000;

        if (mStart > mStop) return ValidityStatus.INVALID;
        if (currentTime < mStart) return ValidityStatus.PENDING;
        if (currentTime > mStop) return ValidityStatus.EXPIRED;

        return ValidityStatus.VALID;
    }

    public Long getStart() {
        return mStart;
    }

    public Long getStop() {
        return mStop;
    }
}
