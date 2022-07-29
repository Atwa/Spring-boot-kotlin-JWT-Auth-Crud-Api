package com.atwa.remote_ps.util.error

import org.springframework.validation.BindingResult

interface ValidationErrorProcessor {
    fun process(result:BindingResult) : String
}