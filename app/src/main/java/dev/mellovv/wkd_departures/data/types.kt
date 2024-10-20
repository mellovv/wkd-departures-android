package dev.mellovv.wkd_departures.data

data class Vehicle (
    val number: Int,
    val direction: Int,
    val departure: Int,
    val delay: Int
)

data class Station (
    val id: Int,
    val departure: Int,
    val delay: Int
)