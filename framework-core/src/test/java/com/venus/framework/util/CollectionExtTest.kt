package com.venus.framework.util

import com.venus.framework.util.eq
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionExtTest {

    @Test
    fun listsEquals() {
        var a = listOf(9, 2, 7)
        var b = listOf(9, 2, 7)
        assertTrue(a eq b)

        a = listOf(9, 2, 7)
        b = listOf(9, 7, 2)
        assertFalse(a eq b)
    }

    @Test
    fun emptyListsEquals() {
        assertTrue(emptyList<Any>() eq emptyList<Int?>())
    }

    @Test
    fun nullListsEqualsToNothingButNulls() {
        assertFalse(null as List<Any>? eq emptyList<Int?>())
        assertFalse(emptyList<Int?>() eq null)
        assertTrue(null as List<Any>? eq null)
    }
}
