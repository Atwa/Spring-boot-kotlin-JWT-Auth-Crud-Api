package com.atwa.remote_ps.util.error

import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult

@Service
class ValidationErrorProcessorImpl : ValidationErrorProcessor {
    override fun process(result: BindingResult): String {
        val sb = StringBuilder()
        result.fieldErrors.forEach { sb.append("${it.field} ${it.defaultMessage} ,") }
        return sb.removeSuffix(",").toString()
    }
}