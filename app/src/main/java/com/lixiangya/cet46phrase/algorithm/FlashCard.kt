package com.lixiangya.cet46phrase.algorithm

interface FlashCard {

    fun getRepetitions():Int

    fun getInterval():Int

    fun getEasinessFactor():Float{
        return 2.5f
    }

}