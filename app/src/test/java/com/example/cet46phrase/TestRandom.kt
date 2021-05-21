package com.example.cet46phrase

import org.junit.Test
import kotlin.random.Random

class TestRandom {

    @Test
    fun a(){
        val r = Random
        for (i in 0..50) {
            println(r.nextInt(5))
        }
    }
}