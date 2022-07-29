package com.atwa.remote_ps.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

object JsonParser {
    @Throws(JsonProcessingException::class)
    fun <T> toJson(objectMapper: ObjectMapper, `object`: T): String {
        return objectMapper.writeValueAsString(`object`)
    }

    @Throws(JsonProcessingException::class)
    fun <T> fromJson(objectMapper: ObjectMapper, payload: String?, clazz: Class<T>?): T {
        return objectMapper.readValue(payload, clazz)
    }

    @Throws(JsonProcessingException::class)
    fun <T> fromJson(objectMapper: ObjectMapper, payload: String?, clazz: TypeReference<T>?): T {
        return objectMapper.readValue(payload, clazz)
    }
}