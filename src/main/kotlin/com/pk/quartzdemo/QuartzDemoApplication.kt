package com.pk.quartzdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QuartzDemoApplication

fun main(args: Array<String>) {
	runApplication<QuartzDemoApplication>(*args)
}
