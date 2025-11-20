// src/main/kotlin/com/example/counter/controller/CounterController.kt
package com.example.counter.controller

import com.example.counter.entity.Counter
import com.example.counter.repository.CounterRepository
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/api")
class CounterController(private val repo: CounterRepository) {

    @GetMapping("/counters")
    fun getAll(): List<Counter> = repo.findAll()

    @PostMapping("/counters/{id}/inc")
    fun increment(@PathVariable id: Int, @RequestParam value: Int = 1): Counter {
        val counter = repo.findById(id).orElseThrow()
        counter.value += value
        return repo.save(counter)
    }
}