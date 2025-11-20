// src/main/kotlin/com/example/counter/entity/Group.kt
package com.example.counter.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "groups")
data class Group(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val groupId: Int = 0,

    @Column(name = "group_name", nullable = false)
    var groupName: String = "",

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL])
    @JsonIgnore
    var counters: MutableList<Counter> = mutableListOf()
)