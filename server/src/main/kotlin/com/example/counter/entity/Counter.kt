
package com.example.counter.entity

import jakarta.persistence.*

@Entity
@Table(name = "counters")
data class Counter(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val counterId: Int = 0,

    @Column(name = "counter_name", nullable = false)
    var counterName: String = "",

    @Column(name = "counter_value")
    var value: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null
)