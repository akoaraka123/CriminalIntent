package com.example.criminalintent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class CrimeListFragment : Fragment() {

    private var _recyclerView: RecyclerView? = null
    private val recyclerView get() = _recyclerView!!
    private var adapter: CrimeAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        _recyclerView = view.findViewById(R.id.crime_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.toolbar))
        (activity as AppCompatActivity).supportActionBar?.title = "CriminalIntent"
        setHasOptionsMenu(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CrimeAdapter(CrimeRepository.getCrimes()) { crime ->
            val intent = Intent(requireContext(), CrimeActivity::class.java).apply {
                putExtra(CrimeActivity.EXTRA_CRIME_ID, crime.id)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_new_crime -> {
                val crime = Crime(
                    id = UUID.randomUUID(),
                    title = "New Crime",
                    date = Date(),
                    isSolved = false
                )
                CrimeRepository.addCrime(crime)
                startActivity(CrimeActivity.newIntent(requireContext(), crime.id))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _recyclerView = null
        adapter = null
    }

    private class CrimeAdapter(
        private val crimes: List<Crime>,
        private val onCrimeClick: (Crime) -> Unit
    ) : RecyclerView.Adapter<CrimeAdapter.CrimeHolder>() {

        inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            private val titleTextView: TextView = view.findViewById(R.id.crime_title)
            private val dateTextView: TextView = view.findViewById(R.id.crime_date)
            private val solvedIcon: ImageView = view.findViewById(R.id.crime_solved_icon)

            private var crime: Crime? = null

            init {
                view.setOnClickListener(this)
            }

            fun bind(crime: Crime) {
                this.crime = crime
                titleTextView.text = crime.title
                val formatter = SimpleDateFormat("EEE MMM dd yyyy hh:mm a", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("Asia/Manila")
                dateTextView.text = formatter.format(crime.date)
                solvedIcon.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
            }

            override fun onClick(v: View) {
                crime?.let { onCrimeClick(it) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            holder.bind(crimes[position])
        }

        override fun getItemCount(): Int = crimes.size
    }
}
