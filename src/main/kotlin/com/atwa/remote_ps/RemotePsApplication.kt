package com.atwa.remote_ps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PsManagerApplication

fun main(args: Array<String>) {
	runApplication<PsManagerApplication>(*args)
}