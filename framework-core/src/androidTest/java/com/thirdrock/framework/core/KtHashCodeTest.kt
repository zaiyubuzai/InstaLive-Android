package com.venus.framework.core

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kotlin 1.3.30之后 target JDK8的话，data class生成的hashcode代码，Int & Long会使用一个新方法，导致android 7以前的版本crash
 * https://stackoverflow.com/q/45935788/679563
 * https://fabric.io/wespoke/android/apps/com.fambase.fivemiles.stage/issues/5cb196dcf8b88c2963c43412
 */
class KtHashCodeTest {
    @Test
    fun testGeneratedHashcode() {
        assertTrue(M().hashCode() != 0)
    }
}

private data class M(
    val a: Int = 1,
    val b: Long = 2
)
