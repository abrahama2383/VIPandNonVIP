package com.demo.bankdemo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthTest {
    @Test fun rules() {
        assertEquals(Category.REGULAR, authenticate("user1", "user1")?.category)
        assertEquals(Category.VIP, authenticate("vip42", "vip42")?.category)
        assertEquals(Category.REGULAR, authenticate(" user7 ", "user7")?.category)
        listOf("user1" to "user2", "user" to "user", "vip" to "vip", "userA" to "userA",
            "admin1" to "admin1", "vip1x" to "vip1x", "" to "").forEach { (u, p) ->
            assertNull(u, authenticate(u, p))
        }
    }
}
