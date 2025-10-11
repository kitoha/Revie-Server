package revie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RevieApiApplication

fun main(args: Array<String>) {
    runApplication<RevieApiApplication>(*args)
}