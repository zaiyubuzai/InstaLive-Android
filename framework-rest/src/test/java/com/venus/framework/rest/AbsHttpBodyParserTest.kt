package com.venus.framework.rest

import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.lang.reflect.Type

@RunWith(MockitoJUnitRunner::class)
class AbsHttpBodyParserTest {

    @Mock private lateinit var parser: AbsHttpBodyParser<Any>

    @Before fun setup() {
        `when`(parser.ensureValidBody(anyString(), any(Type::class.java)))
            .thenCallRealMethod()
    }

    /**
     * 正确处理空白body, 避免解析结果为null
     */
    @Test fun ensureValidBody() {
        var resultBody = parser.ensureValidBody("", List::class.java)
        assertNotNull(resultBody)
        assertEquals("[]", resultBody)

        resultBody = parser.ensureValidBody("", Set::class.java)
        assertNotNull(resultBody)
        assertEquals("[]", resultBody)

        resultBody = parser.ensureValidBody("", Map::class.java)
        assertNotNull(resultBody)
        assertEquals("{}", resultBody)
    }

    @Test fun ensureValidBodyForObject() {
        val resultBody = parser.ensureValidBody("", AbsHttpBodyParser::class.java)
        assertNotNull(resultBody)
        assertEquals("{}", resultBody)
    }

    @Test fun ensureValidBodyForParameterizedType() {
        var resultBody = parser.ensureValidBody("", TypeToken.getParameterized(List::class.java, String::class.java).type)
        assertNotNull(resultBody)
        assertEquals("[]", resultBody)

        resultBody = parser.ensureValidBody("", TypeToken.getParameterized(Set::class.java, String::class.java).type)
        assertNotNull(resultBody)
        assertEquals("[]", resultBody)

        resultBody = parser.ensureValidBody("", TypeToken.getParameterized(Map::class.java, String::class.java, String::class.java).type)
        assertNotNull(resultBody)
        assertEquals("{}", resultBody)
    }
}
