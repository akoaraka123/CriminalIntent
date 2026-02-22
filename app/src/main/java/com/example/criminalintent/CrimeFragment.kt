package com.example.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import java.util.UUID

class CrimeFragment : Fragment() {

    private var _crime: Crime? = null
    private val crime get() = _crime!!

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as? UUID ?: return
        _crime = CrimeRepository.getCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_crime == null) return null

        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title)
        dateButton = view.findViewById(R.id.crime_date)
        solvedCheckBox = view.findViewById(R.id.crime_solved)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (_crime == null) return

        titleField.setText(crime.title)
        titleField.addTextChangedListener { editable ->
            crime.title = editable?.toString() ?: ""
        }

        dateButton.text = crime.date.toString()
        dateButton.isEnabled = false

        solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked
        }
    }

    companion object {
        private const val ARG_CRIME_ID = "crime_id"

        fun newInstance(crimeId: UUID): CrimeFragment {
            return CrimeFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CRIME_ID, crimeId)
                }
            }
        }
    }
}
