package com.atwa.psManager.util.error

import java.lang.RuntimeException

class BadRequestException(override val message:String) : RuntimeException() {
}