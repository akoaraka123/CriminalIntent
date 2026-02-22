package com.example.criminalintent

import java.util.Date
import java.util.UUID

object CrimeRepository {
    private val crimes = mutableListOf<Crime>().apply {
        repeat(100) { i ->
            add(
                Crime(
                    id = UUID.randomUUID(),
                    title = "Crime #$i",
                    date = Date(),
                    isSolved = i % 2 == 0
                )
            )
        }
    }

    fun getCrimes(): List<Crime> = crimes

    fun getCrime(id: UUID): Crime? = crimes.find { it.id == id }

    fun addCrime(crime: Crime) {
        crimes.add(0, crime)
    }
}
