package com.atwa.remote_ps.util.error

import java.lang.RuntimeException

class BadRequestException(override val message:String) : RuntimeException() {
}