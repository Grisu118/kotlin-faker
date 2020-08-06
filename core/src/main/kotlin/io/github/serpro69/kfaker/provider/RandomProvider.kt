package io.github.serpro69.kfaker.provider

import io.github.serpro69.kfaker.*
import java.lang.reflect.Array.newInstance
import java.util.*
import kotlin.Boolean
import kotlin.Char
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.NoSuchElementException
import kotlin.Short
import kotlin.String
import kotlin.reflect.*

/**
 * Provider for functionality not covered by the standard dictionary files.
 *
 * Inspired by [Creating a random instance of any class in Kotlin blog post](https://blog.kotlin-academy.com/creating-a-random-instance-of-any-class-in-kotlin-b6168655b64a).
 */
@Suppress("unused")
class RandomProvider internal constructor(random: Random) {
    private val randomService = RandomService(random)

    /**
     * Creates an instance of [T]. If [T] has a parameterless public constructor then it will be used to create an instance of this class,
     * otherwise a constructor with minimal number of parameters will be used with randomly-generated values.
     *
     * @throws NoSuchElementException if [T] has no public constructor.
     */
    inline fun <reified T : Any> randomClassInstance() = T::class.randomClassInstance()

    @JvmSynthetic
    @PublishedApi
    internal fun <T : Any> KClass<T>.randomClassInstance(): T {
        val instance = this.constructors.find { it.parameters.isEmpty() && it.visibility == KVisibility.PUBLIC }?.call()

        return if (instance != null) instance else {
            val constructor = this.constructors
                .filter { it.visibility == KVisibility.PUBLIC }
                .minBy { it.parameters.size }
                ?: throw NoSuchElementException("No suitable constructor found for $this")

            val params = constructor.parameters
                .map { it.type.classifier as KClass<*> }
                .map {
                    when {
                        it.isPrimitive() -> it.randomPrimitive()
                        it.java.isEnum -> it.randomEnum()
                        it.java.isArray -> it.randomArray()
                        // TODO: 16.06.19 Arrays, Lists, Maps, (other collections?)
                        else -> it.randomClassInstance()
                    }
                }
                .toTypedArray()

            constructor.call(*params)
        }
    }

    /**
     * @return true if this class represents a primitive type which can be handled in [randomPrimitive].
     */
    private fun KClass<*>.isPrimitive(): Boolean = when (this) {
        Double::class,
        Float::class,
        Long::class,
        Int::class,
        Short::class,
        Byte::class,
        String::class,
        Char::class,
        Boolean::class -> true
        else -> false
    }

    /**
     * Handles generation of primitive types since they do not have a public constructor.
     */
    private fun KClass<*>.randomPrimitive(): Any? {
        return when (this) {
            Double::class -> randomService.nextDouble()
            Float::class -> randomService.nextFloat()
            Long::class -> randomService.nextLong()
            Int::class -> randomService.nextInt()
            Short::class -> randomService.nextInt().toShort()
            Byte::class -> randomService.nextInt().toByte()
            String::class -> randomService.nextString()
            Char::class -> randomService.nextChar()
            Boolean::class -> randomService.nextBoolean()
            else -> null
        }
    }

    /**
     * Handles generation of enums types since they do not have a public constructor.
     */
    private fun KClass<*>.randomEnum(): Any? {
        return randomService.randomValue(this.java.enumConstants)
    }

    @Suppress("UNCHECKED_CAST")
    private fun KClass<*>.randomArray(): Any? {
        val componentType = java.componentType.kotlin
        if (componentType.java.isArray) {
            TODO("Support arrays with more than one dimension")
        }
        if (componentType != String::class && componentType.isPrimitive()) {
            return when (componentType) {
                Double::class -> DoubleArray(10) { randomService.nextDouble() }
                Float::class -> FloatArray(10) { randomService.nextFloat() }
                Long::class -> LongArray(10) { randomService.nextLong() }
                Int::class -> IntArray(10) { randomService.nextInt() }
                Short::class -> ShortArray(10) { randomService.nextInt().toShort() }
                Byte::class -> ByteArray(10) { randomService.nextInt().toByte() }
                Char::class -> CharArray(10) { randomService.nextChar() }
                Boolean::class -> BooleanArray(10) { randomService.nextBoolean() }
                else -> throw IllegalStateException("Should not happen")
            }
        }
        val array= newInstance(componentType.java, 10) as Array<Any?>
        for ( i in array.indices) {
            array[i] = componentType.randomClassInstance()
        }
        return array
    }
}
