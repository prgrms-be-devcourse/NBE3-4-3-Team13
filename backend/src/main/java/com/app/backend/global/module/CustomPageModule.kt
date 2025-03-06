package com.app.backend.global.module

import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.data.domain.Page

class CustomPageModule(annotation: CustomPageJsonSerializer) : SimpleModule() {
    init {
        addSerializer(Page::class.java, PageSerializer(annotation))
    }

    private class PageSerializer(private val annotation: CustomPageJsonSerializer) :
        StdSerializer<Page<*>>(Page::class.java) {
        override fun serialize(value: Page<*>, gen: JsonGenerator, provider: SerializerProvider) {
            gen.run {
                writeStartObject()
                annotation.content.takeIf { it }?.let { writeObjectField("content", value.content) }
                annotation.hasContent.takeIf { it }?.let { writeBooleanField("hasContent", value.hasContent()) }
                annotation.totalPages.takeIf { it }?.let { writeNumberField("totalPages", value.totalPages) }
                annotation.totalElements.takeIf { it }?.let { writeNumberField("totalElements", value.totalElements) }
                annotation.numberOfElements.takeIf { it }
                    ?.let { writeNumberField("numberOfElements", value.numberOfElements) }
                annotation.size.takeIf { it }?.let { writeNumberField("size", value.size) }
                annotation.number.takeIf { it }?.let { writeNumberField("number", value.number) }
                annotation.hasPrevious.takeIf { it }?.let { writeBooleanField("hasPrevious", value.hasPrevious()) }
                annotation.hasNext.takeIf { it }?.let { writeBooleanField("hasNext", value.hasNext()) }
                annotation.isFirst.takeIf { it }?.let { writeBooleanField("isFirst", value.isFirst) }
                annotation.isLast.takeIf { it }?.let { writeBooleanField("isLast", value.isLast) }
                annotation.sort.takeIf { it }?.let { writeObjectField("sort", value.sort) }
                annotation.empty.takeIf { it }?.let { writeBooleanField("empty", value.isEmpty) }
                writeEndObject()
            }
        }
    }
}