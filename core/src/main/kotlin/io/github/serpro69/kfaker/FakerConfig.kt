@file:JvmName("FakerConfigBuilder")

package io.github.serpro69.kfaker

import java.util.*

class FakerConfig private constructor(
    val locale: String,
    val random: Random,
    val uniqueGeneratorRetryLimit: Int,
    val arraySize: ArraySize
) {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder internal constructor() {
        var locale = "en"
        var random = Random()
        var uniqueGeneratorRetryLimit = 100
        var arraySize = ArraySize(0, 10)

        internal fun build() = FakerConfig(
            locale,
            random,
            uniqueGeneratorRetryLimit,
            arraySize
        )
    }
}

fun FakerConfig.Builder.create(block: FakerConfig.Builder.() -> Unit) = this.apply(block).build()

data class ArraySize(val minSize: Int, val maxSize: Int)