package dev.samhain.groceries

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GroceriesApplication

fun main(args: Array<String>) {
    runApplication<GroceriesApplication>(*args)
}
