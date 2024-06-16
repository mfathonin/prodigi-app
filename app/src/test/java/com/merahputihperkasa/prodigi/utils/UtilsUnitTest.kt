package com.merahputihperkasa.prodigi.utils

import org.junit.Assert.*
import org.junit.Test

class URiValidatorTest {

    @Test
    fun testValidUris() {
        assertTrue(isValidURi("http://www.example.com"))
        assertTrue(isValidURi("https://www.google.com"))
        assertTrue(isValidURi("https://subdomain.example.org/path"))
        assertTrue(isValidURi("https://mpp-hub.netlify.app/links/test-links-1231"))
    }

    @Test
    fun testInvalidUris() {
        assertFalse(isValidURi("ftp://invalid.com"))
        assertFalse(isValidURi("www.example.com")) // Missing protocol
        assertFalse(isValidURi("http:// space.com")) // Space in domain
        assertFalse(isValidURi("https://.invalid")) // Invalid domain
    }
}