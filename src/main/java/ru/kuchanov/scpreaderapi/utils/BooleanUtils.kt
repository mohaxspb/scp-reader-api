package ru.kuchanov.scpreaderapi.utils

fun String.isBoolean() =
        true.toString().equals(this, true) || false.toString().equals(this, true)

fun String.toBooleanOrNull() =
        if (this.isBoolean()) {
            this.toBoolean()
        } else {
            null
        }