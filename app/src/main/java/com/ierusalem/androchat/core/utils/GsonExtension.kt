package com.ierusalem.androchat.core.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

@Suppress("unused")
object Json {
    val gson: Gson =
        GsonBuilder().registerTypeAdapterFactory(
            object : TypeAdapterFactory {
                override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
                    val kClass = Reflection.getOrCreateKotlinClass(type.rawType)
                    return if (kClass.sealedSubclasses.any()) {
                        SealedClassTypeAdapter(kClass, gson)
                    } else
                        gson.getDelegateAdapter(this, type)
                }
            }).create()

    inline fun <reified T> fromJson(x: String): T = this.gson.fromJson(x, T::class.java)
    fun <T> fromJsonWithClass(x: String, classObj: Class<T>): T =
        this.gson.fromJson(x, classObj)

    fun <T> toJson(item: T): String = this.gson.toJson(item)
}

class SealedClassTypeAdapter<T : Any>(private val kClass: KClass<Any>, private val gson: Gson) : TypeAdapter<T>() {
    override fun read(jsonReader: JsonReader): T? {
        jsonReader.beginObject() //start reading the object
        val nextName = jsonReader.nextName() //get the name on the object
        val innerClass = kClass.sealedSubclasses.firstOrNull {
            it.simpleName!!.contains(nextName)
        }
            ?: throw Exception("$nextName is not found to be a data class of the sealed class ${kClass.qualifiedName}")
        val x = gson.fromJson<T>(jsonReader, innerClass.javaObjectType)
        jsonReader.endObject()
        //if there a static object, actually return that back to ensure equality and such!
        return innerClass.objectInstance as T? ?: x
    }

    override fun write(out: JsonWriter, value: T) {
        val jsonString = gson.toJson(value)
        out.beginObject()
        out.name(value.javaClass.canonicalName.splitToSequence(".").last()).jsonValue(jsonString)
        out.endObject()
    }

}