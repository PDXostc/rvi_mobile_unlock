package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    DlinkPacketTest.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 7/6/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.test.AndroidTestCase;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class UtilTest extends AndroidTestCase
{
    private final static String TAG = "RVI/UtilTest___________";

    private Integer tidCounter = 0;

    private DlinkPacket mPacket;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testRFC1035DomainValidation_NormalDomains() {
        String d1 = "foo";
        String d2 = "foo.bar";
        String d3 = "foo.bar.baz";
        String d4 = "foo.bar.baz1";
        String d5 = "foo.bar2.baz1";
        String d6 = "foo3.bar2.baz1";
        String d7 = "foo3.bar2.baz1-gazook";

        try {
            assertEquals(d1, Util.rfc1035(d1));
            assertEquals(d2, Util.rfc1035(d2));
            assertEquals(d3, Util.rfc1035(d3));
            assertEquals(d4, Util.rfc1035(d4));
            assertEquals(d5, Util.rfc1035(d5));
            assertEquals(d6, Util.rfc1035(d6));
            assertEquals(d7, Util.rfc1035(d7));

        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public final void testRFC1035DomainValidation_NullString() {
        String d1 = null;

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_EmptyString() {
        String d1 = "";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_EmptyLabel1() {
        String d1 = "foo..bar";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_EmptyLabel2() {
        String d1 = "foo...bar";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_EmptyLabel3() {
        String d1 = "foo..bar..baz";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_IllegalPrecedingSingleDot() {
        String d1 = ".foo.bar.baz";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_IllegalPrecedingDoubleDot() {
        String d1 = "..foo.bar.baz";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_IllegalSucceedingSingleDot() {
        String d1 = "foo.bar.baz.";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_IllegalSucceedingDoubleDot() {
        String d1 = "foo.bar.baz..";

        try {
            d1 = Util.rfc1035(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_IllegalCharacters() {
        Boolean exceptionHit = false;

        String d1 = "foo/";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo+";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo#";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo bar";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = " ";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "%$#";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "/foo";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testRFC1035DomainValidation_IllegalStartToSubdomain() {
        Boolean exceptionHit = false;

        String d1 = "foo.bar.1baz";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo.2bar.baz";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "3foo.bar.baz";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "-foo.bar";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }
        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo.-bar";

        try {
            d1 = Util.rfc1035(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_ValidStrings() {
        String d1  = "foo";
        String d2  = "foo.bar";
        String d3  = "foo.bar.baz";
        String d4  = "foo/bar";
        String d5  = "foo/bar/baz";
        String d6  = " / / ";
        String d7  = "foo/ /baz";
        String d8  = "foo/./baz";
        String d9  = "!/?/@";
        String d10 = " foo/ bar /baz ";
        String d11 = "fo$o/bar$/b$az";
        String d12 = "foo/foo/foo";
        String d13 = "foo ";
        String d14 = " foo";
        String d15 = ".foo";
        String d16 = "foo.";
        String d17 = " ";
        String d18 = "!@?";
        String d19 = "fo$o";

        try {
            assertEquals(d1 , Util.validated(d1 ));
            assertEquals(d2 , Util.validated(d2 ));
            assertEquals(d3 , Util.validated(d3 ));
            assertEquals(d4 , Util.validated(d4 ));
            assertEquals(d5 , Util.validated(d5 ));
            assertEquals(d6 , Util.validated(d6 ));
            assertEquals(d7 , Util.validated(d7 ));
            assertEquals(d8 , Util.validated(d8 ));
            assertEquals(d9 , Util.validated(d9 ));
            assertEquals(d10, Util.validated(d10));
            assertEquals(d11, Util.validated(d11));
            assertEquals(d12, Util.validated(d12));
            assertEquals(d13, Util.validated(d13));
            assertEquals(d14, Util.validated(d14));
            assertEquals(d15, Util.validated(d15));
            assertEquals(d16, Util.validated(d16));
            assertEquals(d17, Util.validated(d17));
            assertEquals(d18, Util.validated(d18));
            assertEquals(d19, Util.validated(d19));

        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }
    }

    public final void testIdentifierComponentValidation_NullString() {
        String d1 = null;

        try {
            d1 = Util.validated(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_EmptyString() {
        String d1 = "";

        try {
            d1 = Util.validated(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_EmptyTopicLevelStart() {
        String d1 = "//foo/bar";

        try {
            d1 = Util.validated(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_EmptyTopicLevelMiddle() {
        String d1 = "foo//bar";

        try {
            d1 = Util.validated(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_EmptyTopicLevelEnd() {
        String d1 = "foo/bar//";

        try {
            d1 = Util.validated(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_IllegalPrecedingSlash() {
        String d1 = "/foo/bar/baz";

        try {
            d1 = Util.validated(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_IllegalSucceedingSlash() {
        String d1 = "foo/bar/baz/";

        try {
            d1 = Util.validated(d1);

        } catch (Exception e) {
            assertTrue(true);

            return;
        }

        assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_IllegalCharacters() {
        Boolean exceptionHit = false;

        String d1 = "foo/";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo+";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo#";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "/foo";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "+foo";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "f#oo";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "/foo/";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo///bar";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);
    }

    public final void testIdentifierComponentValidation_NullUTF8Characters() {
        Boolean exceptionHit = false;

        String d1 = "\u0000";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo\u0000";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "foo\u0000foo";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);

        exceptionHit = false;

        d1 = "\u0000foo";

        try {
            d1 = Util.validated(d1);
        } catch (Exception e) {
            exceptionHit = true;
        }

        if (!exceptionHit)
            assertTrue("Test string '" + d1 + "' failed", false);
    }
}
