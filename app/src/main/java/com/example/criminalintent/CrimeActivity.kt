package com.example.criminalintent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class CrimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crime)

        val crimeId = intent.getSerializableExtra(EXTRA_CRIME_ID) as? UUID ?: return

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CrimeFragment.newInstance(crimeId))
                .commit()
        }
    }

    companion object {
        const val EXTRA_CRIME_ID = "com.example.criminalintent.crime_id"

        fun newIntent(context: Context, crimeId: UUID): Intent {
            return Intent(context, CrimeActivity::class.java).apply {
                putExtra(EXTRA_CRIME_ID, crimeId)
            }
        }
    }
}
