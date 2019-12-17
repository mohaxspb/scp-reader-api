package ru.kuchanov.scpreaderapi.utils


import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * used to mark classes for which default constructor must be created
 */
@Target(CLASS)
@Retention(RUNTIME)
annotation class NoArgConstructor