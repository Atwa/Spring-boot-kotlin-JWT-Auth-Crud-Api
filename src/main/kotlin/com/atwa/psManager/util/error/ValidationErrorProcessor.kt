package com.atwa.psManager.util.error

import org.springframework.context.annotation.Bean
import org.springframework.validation.BindingResult

interface ValidationErrorProcessor {
    fun process(result:BindingResult) : String
}