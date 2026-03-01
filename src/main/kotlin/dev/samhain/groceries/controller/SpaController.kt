package dev.samhain.groceries.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SpaController {
    @GetMapping("/{path:[^\\.]*}")
    fun spa(): String = "forward:/index.html"
}
