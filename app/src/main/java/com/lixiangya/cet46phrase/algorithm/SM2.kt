package com.lixiangya.cet46phrase.algorithm

import java.lang.IllegalArgumentException

class SM2 {

    companion object{
        /**
        5 - perfect response
        4 - correct response after a hesitation
        3 - correct response recalled with serious difficulty
        2 - incorrect response; where the correct one seemed easy to recall
        1 - incorrect response; the correct one remembered
        0 - complete blackout.
         */
        fun calculateSuperMemo2Algorithm(
            card: FlashCard,
            quality: Int
        ): Long {
            if (quality < 0 || quality > 5) {
                throw IllegalArgumentException("quality仅支持 [0，5]")
            }

            // retrieve the stored values (default values if new cards)
            var repetitions: Int = card.getRepetitions()
            var easiness: Float = card.getEasinessFactor()
            var interval: Int = card.getInterval()

            // easiness factor
            easiness =
                Math.max(
                    1.3,
                    easiness + 0.1 - (5.0 - quality) * (0.08 + (5.0 - quality) * 0.02)
                ).toFloat()

            // repetitions
            if (quality < 3) {
                repetitions = 0
            } else {
                repetitions += 1
            }
            //        card.setRepetitions(repetitions);
            // interval
            interval = if (repetitions <= 1) {
                1
            } else if (repetitions == 2) {
                6
            } else {
                Math.round(interval * easiness)
            }

            // next practice
            val millisecondsInDay = 60 * 60 * 24 * 1000
            val now = System.currentTimeMillis()
            return now + millisecondsInDay * interval
        }
    }


}