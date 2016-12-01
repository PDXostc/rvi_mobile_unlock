package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PrivilegeMatchingTest.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 11/30/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.test.AndroidTestCase;
import java.lang.reflect.Method;

public class PrivilegeMatchingTest extends AndroidTestCase
{
    private final static String TAG = "RVI/PrivMatchingTest___";

    private Class[] argTypes = new Class[] { String.class, String.class };
    private Method rightMatchesServiceIdentifierMethod = null;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        rightMatchesServiceIdentifierMethod = Privilege.class.getDeclaredMethod("rightMatchesServiceIdentifier", argTypes);
        rightMatchesServiceIdentifierMethod.setAccessible(true);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private String nm_message(String a, String b) {
        return "Strings \"" + a + "\" and \"" + b + "\" do match and shouldn't";
    }

    private String ym_message(String a, String b) {
        return "Strings \"" + a + "\" and \"" + b + "\" don't match and should";
    }

    private boolean rightMatchesServiceIdentifierMethod(String r, String s) {
        try {
            return (boolean) rightMatchesServiceIdentifierMethod.invoke(null, r, s);
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }

        return false;
    }

    public final void testPrivilegeMatching_AbsoluteStrings_NoWildcards_Matching() {
        String r1  = "foo";
        String r2  = "foo/bar";
        String r3  = "foo/bar/baz";
        String r4  = " / / ";
        String r5  = "foo/ /baz";
        String r6  = "foo/./baz";
        String r7  = "!/?/@";
        String r8  = " foo/ bar /baz ";
        String r9  = "fo$o/bar$/b$az";
        String r10 = "foo/foo/foo";
        String r11 = "foo ";
        String r12 = " foo";
        String r13 = ".foo";
        String r14 = "foo.";
        String r15 = " ";
        String r16 = "!@?";
        String r17 = "fo$o";

        String s1  = "foo";
        String s2  = "foo/bar";
        String s3  = "foo/bar/baz";
        String s4  = " / / ";
        String s5  = "foo/ /baz";
        String s6  = "foo/./baz";
        String s7  = "!/?/@";
        String s8  = " foo/ bar /baz ";
        String s9  = "fo$o/bar$/b$az";
        String s10 = "foo/foo/foo";
        String s11 = "foo ";
        String s12 = " foo";
        String s13 = ".foo";
        String s14 = "foo.";
        String s15 = " ";
        String s16 = "!@?";
        String s17 = "fo$o";

        assertTrue(ym_message(r1 , s1 ), rightMatchesServiceIdentifierMethod(r1 , s1 ));
        assertTrue(ym_message(r2 , s2 ), rightMatchesServiceIdentifierMethod(r2 , s2 ));
        assertTrue(ym_message(r3 , s3 ), rightMatchesServiceIdentifierMethod(r3 , s3 ));
        assertTrue(ym_message(r4 , s4 ), rightMatchesServiceIdentifierMethod(r4 , s4 ));
        assertTrue(ym_message(r5 , s5 ), rightMatchesServiceIdentifierMethod(r5 , s5 ));
        assertTrue(ym_message(r6 , s6 ), rightMatchesServiceIdentifierMethod(r6 , s6 ));
        assertTrue(ym_message(r7 , s7 ), rightMatchesServiceIdentifierMethod(r7 , s7 ));
        assertTrue(ym_message(r8 , s8 ), rightMatchesServiceIdentifierMethod(r8 , s8 ));
        assertTrue(ym_message(r9 , s9 ), rightMatchesServiceIdentifierMethod(r9 , s9 ));
        assertTrue(ym_message(r10, s10), rightMatchesServiceIdentifierMethod(r10, s10));
        assertTrue(ym_message(r11, s11), rightMatchesServiceIdentifierMethod(r11, s11));
        assertTrue(ym_message(r12, s12), rightMatchesServiceIdentifierMethod(r12, s12));
        assertTrue(ym_message(r13, s13), rightMatchesServiceIdentifierMethod(r13, s13));
        assertTrue(ym_message(r14, s14), rightMatchesServiceIdentifierMethod(r14, s14));
        assertTrue(ym_message(r15, s15), rightMatchesServiceIdentifierMethod(r15, s15));
        assertTrue(ym_message(r16, s16), rightMatchesServiceIdentifierMethod(r16, s16));
        assertTrue(ym_message(r17, s17), rightMatchesServiceIdentifierMethod(r17, s17));
    }

    public final void testPrivilegeMatching_AbsoluteStrings_NoWildcards_NotMatching() {
        String r1  = "foo";
        String s1  = "bar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo";
        s1 = "foobar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo";
        s1 = "foofoo";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo";
        s1 = "barfoobar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foobar";
        s1 = "foo";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar";
        s1 = "bar/bar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar";
        s1 = "foo/foo";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        /* * * * * * */

        r1 = "foo/bar/baz";
        s1 = "foo/baz/bar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "foo/foo/foo";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "bar/bar/bar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "baz/baz/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "baz/bar/foo";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        /* * * * * * */

        r1 = "foo/foo";
        s1 = "foo/bar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "bar/bar";
        s1 = "foo/bar";

        /* * * * * * */

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo";
        s1 = "foo/bar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar";
        s1 = "foo/bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar";
        s1 = "foo";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "foo/bar";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        /* * * * * * */

        r1 = "foo/bar/baz";
        s1 = "foo/ /baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "foo/ bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "foo/bar /baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "foo/ bar /baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/ /baz";
        s1 = "foo/bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/ bar/baz";
        s1 = "foo/bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar /baz";
        s1 = "foo/bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/ bar /baz";
        s1 = "foo/bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        /* * * * * * */

        r1 = "foo/./baz";
        s1 = "foo/bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "foo/./baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/.!?@$%/baz";
        s1 = "foo/bar/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));

        r1 = "foo/bar/baz";
        s1 = "foo/.!?@$%/baz";

        assertFalse(nm_message(r1, s1), rightMatchesServiceIdentifierMethod(r1, s1));
    }

    public final void testPrivilegeMatching_SingleLevelWildcards_Test1() {
        String r1  = "+";
        String s1  = "foo";         /* Matching     */
        String s2  = "foo/";        /* Not matching */
        String s3  = " ";           /* Matching     */
        String s4  = "/foo";        /* Not matching */
        String s5  = "+";           /* Matching (but illegal) */
        String s6  = "!@%";         /* Matching     */
        String s7  = "foo/bar/";    /* Not matching */
        String s8  = "foo/bar/baz"; /* Not matching */
        String s9  = "/foo/bar";    /* Not matching */
        String s10 = "foo//";       /* Not matching */
        String s11 = "foo///";       /* Not matching */
        String s12 = "foo//bar";    /* Not matching */
        String s13 = "foo/ /";      /* Not matching */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertFalse(nm_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertFalse(nm_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertTrue (ym_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertTrue (ym_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertFalse(nm_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertFalse(nm_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
    }


    public final void testPrivilegeMatching_SingleLevelWildcards_Test2() {
        String r1  = "foo/+";
        String s1  = "foo";         /* Not matching */
        String s2  = "foo/";        /* Matching     */
        String s3  = "foo/ ";       /* Matching     */
        String s4  = "foo/bar";     /* Matching     */
        String s5  = "foo/+";       /* Matching (but illegal) */
        String s6  = "foo/!@%";     /* Matching     */
        String s7  = "foo/bar/";    /* Not matching */
        String s8  = "foo/bar/baz"; /* Not matching */
        String s9  = "/foo/bar";    /* Not matching */
        String s10 = "foo//";       /* Not matching */
        String s11 = "foo///";       /* Not matching */
        String s12 = "foo//bar";    /* Not matching */
        String s13 = "foo/ /";      /* Not matching */

        assertFalse(nm_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertTrue (ym_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertTrue (ym_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertFalse(nm_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertFalse(nm_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
    }

    public final void testPrivilegeMatching_SingleLevelWildcards_Test3() {
        String r1  = "+/bar";
        String s1  = "bar";         /* Not matching */
        String s2  = "/bar";        /* Matching     */
        String s3  = " /bar";       /* Matching     */
        String s4  = "foo/bar";     /* Matching     */
        String s5  = "/bar";        /* Matching (but illegal) */
        String s6  = "+/bar";       /* Matching (but illegal) */
        String s7  = "!@%/bar";     /* Matching     */
        String s8  = "foo/bar/";    /* Not matching */
        String s9  = "foo/bar/baz"; /* Not matching */
        String s10 = "/foo/bar";    /* Not matching */
        String s11 = "//bar";       /* Not matching */
        String s12 = "///bar";       /* Not matching */
        String s13 = "foo//bar";    /* Not matching */
        String s14 = "/ /bar";      /* Not matching */

        assertFalse(nm_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertTrue (ym_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertTrue (ym_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertTrue (ym_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertFalse(nm_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertFalse(nm_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
        assertFalse(nm_message(r1, s14), rightMatchesServiceIdentifierMethod(r1, s14));
    }

    public final void testPrivilegeMatching_SingleLevelWildcards_Test4() {
        String r1  = "foo/+/baz";
        String s1  = "foo/bar/baz";     /* Matching     */
        String s2  = "foo/ /baz";       /* Matching     */
        String s3  = "foo/+/baz";       /* Matching (but illegal) */
        String s4  = "foo/!@%/baz";     /* Matching     */
        String s5  = "foo/baz";         /* Not matching */
        String s6  = "foo/bar";         /* Not matching */
        String s7  = "foo/baz/";        /* Not matching */
        String s8  = "foo/bar/bar";     /* Not matching */
        String s9  = "/foo/bar/baz";    /* Not matching */
        String s10 = "foo//baz";        /* Not matching */
        String s11 = "foo///baz";       /* Not matching */
        String s12 = "foo/bar/baz/";    /* Not matching */
        String s13 = "foo/bar/bar/baz"; /* Not matching */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertFalse(nm_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertFalse(nm_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertFalse(nm_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertFalse(nm_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
    }

    public final void testPrivilegeMatching_SingleLevelWildcards_Test5() {
        String r1  = "foo/+/+/gazook";
        String s1  = "foo/bar/baz/gazook";  /* Matching     */
        String s2  = "foo/ / /gazook";      /* Matching     */
        String s3  = "foo/+/+/gazook";      /* Matching (but illegal) */
        String s4  = "foo/!@%/!@%/gazook";  /* Matching     */
        String s5  = "foo/gazook";          /* Not matching */
        String s6  = "foo/bar/gazook";      /* Not matching */
        String s7  = "foo//gazook";         /* Not matching */
        String s8  = "foo///gazook";        /* Not matching */
        String s9  = "/foo/bar/baz/gazook"; /* Not matching */
        String s10 = "foo/bar/baz/baz";     /* Not matching */
        String s11 = "foo/bar/baz/gazook/"; /* Not matching */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertFalse(nm_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertFalse(nm_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
    }

    public final void testPrivilegeMatching_SingleLevelWildcards_Test6() {
        String r1  = "foo/+/bar/+/baz";
        String s1  = "foo/!@%/bar/!@%/baz"; /* Matching     */
        String s2  = "foo/ /bar/ /baz";     /* Matching     */
        String s3  = "foo/+/bar/+/baz";     /* Matching (but illegal) */
        String s4  = "foo/+/+/+/baz";       /* Not matching */
        String s5  = "foo/baz";             /* Not matching */
        String s6  = "foo/bar/baz";         /* Not matching */
        String s7  = "foobar/!/bar/!/baz";  /* Not matching */
        String s8  = "foo////baz";          /* Not matching */
        String s9  = "/foo/bar/baz/gazook"; /* Not matching */
        String s10 = "foo/bar/baz/baz";     /* Not matching */
        String s11 = "foo/bar/baz/gazook/"; /* Not matching */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertFalse(nm_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertFalse(nm_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertFalse(nm_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
    }

    public final void testPrivilegeMatching_SingleLevelWildcards_Test7() {
        String s1  = "/foo";
        String r1  = "+/+";  /* Matching     */
        String r2  = "/+";   /* Matching     */
        String r3  = "+";    /* Not matching */

        String s2  = "foo/";
        String r4  = "+/+";  /* Matching     */
        String r5  = "+/";   /* Matching     */
        String r6  = "+";    /* Not matching */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r2, s1 ), rightMatchesServiceIdentifierMethod(r2, s1 ));
        assertFalse(nm_message(r3, s1 ), rightMatchesServiceIdentifierMethod(r3, s1 ));

        assertTrue (ym_message(r4, s2 ), rightMatchesServiceIdentifierMethod(r4, s2 ));
        assertTrue (ym_message(r5, s2 ), rightMatchesServiceIdentifierMethod(r5, s2 ));
        assertFalse(nm_message(r6, s2 ), rightMatchesServiceIdentifierMethod(r6, s2 ));
    }

    public final void testPrivilegeMatching_MultiLevelWildcards_Test1() {
        String r1  = "#";
        String s1  = "foo";         /* Matching     */
        String s2  = "foo/";        /* Matching     */
        String s3  = "foo/ ";       /* Matching     */
        String s4  = "foo/bar";     /* Matching     */
        String s5  = "foo/+";       /* Matching (but illegal) */
        String s6  = "foo/!@%";     /* Matching     */
        String s7  = "foo/bar/";    /* Matching     */
        String s8  = "foo/bar/baz"; /* Matching     */
        String s9  = "/foo/bar";    /* Matching     */
        String s10 = "foo//";       /* Not matching */
        String s11 = "foo///";      /* Not matching */
        String s12 = "foo//bar";    /* Not matching */
        String s13 = "foo/ /";      /* Matching     */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertTrue (ym_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertTrue (ym_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertTrue (ym_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertTrue (ym_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertTrue (ym_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertFalse(nm_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertTrue (ym_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
    }

    public final void testPrivilegeMatching_MultiLevelWildcards_Test2() {
        String r1  = "/#";
        String s1  = "foo";         /* Not matching */
        String s2  = "foo/";        /* Not matching */
        String s3  = "foo/ ";       /* Not matching */
        String s4  = "foo/bar";     /* Not matching */
        String s5  = "foo/+";       /* Not matching */
        String s6  = "foo/!@%";     /* Not matching */
        String s7  = "foo/bar/";    /* Not matching */
        String s8  = "foo/bar/baz"; /* Not matching */
        String s9  = "/";           /* Matching     */
        String s10 = "/foo";        /* Matching     */
        String s11 = "/foo/bar";    /* Matching     */

        assertFalse(nm_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertFalse(nm_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertFalse(nm_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertFalse(nm_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertFalse(nm_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertFalse(nm_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertTrue (ym_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertTrue (ym_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertTrue (ym_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
    }

    public final void testPrivilegeMatching_MultiLevelWildcards_Test3() {
        String r1  = "foo/#";
        String s1  = "foo";         /* Matching */
        String s2  = "foo/";        /* Matching     */
        String s3  = "foo/ ";       /* Matching     */
        String s4  = "foo/bar";     /* Matching     */
        String s5  = "foo/+";       /* Matching (but illegal) */
        String s6  = "foo/!@%";     /* Matching     */
        String s7  = "foo/bar/";    /* Matching     */
        String s8  = "foo/bar/baz"; /* Matching     */
        String s9  = "/foo/bar";    /* Not matching */
        String s10 = "foo//";       /* Not matching */
        String s11 = "foo///";      /* Not matching */
        String s12 = "foo//bar";    /* Not matching */
        String s13 = "foo/ /";      /* Matching     */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertTrue (ym_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertTrue (ym_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertTrue (ym_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertTrue (ym_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertFalse(nm_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertTrue (ym_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
    }

    public final void testPrivilegeMatching_MultiLevelWildcards_Test4() {
        String r1  = "#/bar";       /* Not even legal */
        String s1  = "bar";         /* Not matching */
        String s2  = "/bar";        /* Not matching */
        String s3  = " /bar";       /* Not matching */
        String s4  = "foo/bar";     /* Not matching */
        String s5  = "/bar";        /* Not matching */
        String s6  = "#/bar";       /* Matching (but illegal) */
        String s7  = "!@%/bar";     /* Not matching */
        String s8  = "foo/bar/";    /* Not matching */
        String s9  = "foo/bar/baz"; /* Not matching */
        String s10 = "/foo/bar";    /* Not matching */
        String s11 = "//bar";       /* Not matching */
        String s12 = "///bar";      /* Not matching */
        String s13 = "foo//bar";    /* Not matching */
        String s14 = "/ /bar";      /* Not matching */

        assertFalse(nm_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertFalse(nm_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertFalse(nm_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertFalse(nm_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertFalse(nm_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertTrue (ym_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertFalse(nm_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertFalse(nm_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
        assertFalse(nm_message(r1, s14), rightMatchesServiceIdentifierMethod(r1, s14));
    }

    public final void testPrivilegeMatching_MultiLevelWildcards_Test5() {
        String r1  = "foo/+/baz/#";
        String s1  = "foo/bar/baz";          /* Matching     */
        String s2  = "foo/ /baz";            /* Matching     */
        String s3  = "foo/+/baz";            /* Matching (but illegal) */
        String s4  = "foo/!@%/baz";          /* Matching     */
        String s5  = "foo/baz";              /* Not matching */
        String s6  = "foo/bar";              /* Not matching */
        String s7  = "foo/baz/";             /* Not matching */
        String s8  = "foo/bar/bar";          /* Not matching */
        String s9  = "/foo/bar/baz";         /* Not matching */
        String s10 = "foo//baz";             /* Not matching */
        String s11 = "foo///baz";            /* Not matching */
        String s12 = "foo/bar/baz/";         /* Matching     */
        String s13 = "foo/bar/bar/baz";      /* Not matching */
        String s14 = "foo/bar/baz/baz";      /* Matching     */
        String s15 = "foo/bar/baz/";         /* Matching     */
        String s16 = "foo/bar/baz/baz/baz";  /* Matching     */
        String s17 = "foo/bar/baz/baz/baz/"; /* Matching     */

        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertFalse(nm_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertFalse(nm_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertFalse(nm_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertTrue (ym_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertFalse(nm_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
        assertTrue (ym_message(r1, s14), rightMatchesServiceIdentifierMethod(r1, s14));
        assertTrue (ym_message(r1, s15), rightMatchesServiceIdentifierMethod(r1, s15));
        assertTrue (ym_message(r1, s16), rightMatchesServiceIdentifierMethod(r1, s16));
        assertTrue (ym_message(r1, s17), rightMatchesServiceIdentifierMethod(r1, s17));
    }

    public final void testPrivilegeMatching_MultiLevelWildcards_Test6() {
        String r1  = "foo/+/+/gazook/#";
        String s1  = "foo/bar/baz/gazook";         /* Matching     */
        String s2  = "foo/ / /gazook";             /* Matching     */
        String s3  = "foo/+/+/gazook";             /* Matching (but illegal) */
        String s4  = "foo/!@%/!@%/gazook";         /* Matching     */
        String s5  = "foo/gazook";                 /* Not matching */
        String s6  = "foo/bar/gazook";             /* Not matching */
        String s7  = "foo//gazook";                /* Not matching */
        String s8  = "foo///gazook";               /* Not matching */
        String s9  = "/foo/bar/baz/gazook";        /* Not matching */
        String s10 = "foo/bar/baz/baz";            /* Not matching */
        String s11 = "foo/bar/baz/gazook/";        /* Matching     */
        String s12 = "foo/bar/baz/gazook/baz";     /* Matching     */
        String s13 = "foo/bar/baz/gazook/baz/";    /* Matching     */
        String s14 = "foo/bar/baz/gazook/baz/baz"; /* Matching     */


        assertTrue (ym_message(r1, s1 ), rightMatchesServiceIdentifierMethod(r1, s1 ));
        assertTrue (ym_message(r1, s2 ), rightMatchesServiceIdentifierMethod(r1, s2 ));
        assertTrue (ym_message(r1, s3 ), rightMatchesServiceIdentifierMethod(r1, s3 ));
        assertTrue (ym_message(r1, s4 ), rightMatchesServiceIdentifierMethod(r1, s4 ));
        assertFalse(nm_message(r1, s5 ), rightMatchesServiceIdentifierMethod(r1, s5 ));
        assertFalse(nm_message(r1, s6 ), rightMatchesServiceIdentifierMethod(r1, s6 ));
        assertFalse(nm_message(r1, s7 ), rightMatchesServiceIdentifierMethod(r1, s7 ));
        assertFalse(nm_message(r1, s8 ), rightMatchesServiceIdentifierMethod(r1, s8 ));
        assertFalse(nm_message(r1, s9 ), rightMatchesServiceIdentifierMethod(r1, s9 ));
        assertFalse(nm_message(r1, s10), rightMatchesServiceIdentifierMethod(r1, s10));
        assertTrue (ym_message(r1, s11), rightMatchesServiceIdentifierMethod(r1, s11));
        assertTrue (ym_message(r1, s12), rightMatchesServiceIdentifierMethod(r1, s12));
        assertTrue (ym_message(r1, s13), rightMatchesServiceIdentifierMethod(r1, s13));
        assertTrue (ym_message(r1, s14), rightMatchesServiceIdentifierMethod(r1, s14));
    }
}
