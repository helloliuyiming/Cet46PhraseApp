package com.example.cet46phrase.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class RawJsonGsonAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter?, value: String?) {
        out?.jsonValue(value)
    }

    override fun read(jsonReader: JsonReader): String {
        return ""
    }
}