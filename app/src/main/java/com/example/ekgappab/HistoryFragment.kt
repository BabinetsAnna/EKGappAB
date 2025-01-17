package com.example.ekgappab

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
class HistoryFragment : Fragment() {

    private val database = DataResultDB()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val bpTab = view.findViewById<TextView>(R.id.bloodPressureTab)
        val ecgTab = view.findViewById<TextView>(R.id.ecgTab)
        val bpScrollView = view.findViewById<ScrollView>(R.id.bpScrollView)
        val ecgScrollView = view.findViewById<ScrollView>(R.id.ecgScrollView)
        val bpListLayout = view.findViewById<LinearLayout>(R.id.bpListLayout)
        val ecgListLayout = view.findViewById<LinearLayout>(R.id.ecgListLayout)

        bpScrollView.visibility = View.VISIBLE
        ecgScrollView.visibility = View.GONE
        bpTab.setTextColor(resources.getColor(R.color.activeTabColor))
        bpTab.setTypeface(null, Typeface.BOLD)
        // Логіка перемикання вкладок
        bpTab.setOnClickListener {
            bpScrollView.visibility = View.VISIBLE
            ecgScrollView.visibility = View.GONE
            ecgTab.setTextColor(resources.getColor(R.color.black))
            bpTab.setTextColor(resources.getColor(R.color.activeTabColor))
            bpTab.setTypeface(null, Typeface.BOLD)
            ecgTab.setTypeface(null, Typeface.NORMAL)
        }

        ecgTab.setOnClickListener {
            ecgScrollView.visibility = View.VISIBLE
            bpScrollView.visibility = View.GONE
            ecgTab.setTextColor(resources.getColor(R.color.activeTabColor))
            bpTab.setTextColor(resources.getColor(R.color.black))
            ecgTab.setTypeface(null, Typeface.BOLD)
            bpTab.setTypeface(null, Typeface.NORMAL)
        }

        // Завантаження даних
        val sessionManager = UserSessionManager(requireContext())
        val userId = sessionManager.getUserId()

        if (userId != null) {
            database.getAllBPDataForUser(userId) { bpDataList ->
                bpListLayout.removeAllViews()
                bpDataList.sortedByDescending { it.date }.forEach { bpData ->
                    val listItem = LayoutInflater.from(requireContext())
                        .inflate(R.layout.list_item_bp, bpListLayout, false)

                    val dateView = listItem.findViewById<TextView>(R.id.bpDateText)
                    val bpView = listItem.findViewById<TextView>(R.id.bpDataText)
                    val hrView = listItem.findViewById<TextView>(R.id.bpHeartRateText)

                    dateView.text = formatTimestamp(bpData.date)
                    bpView.text = "SYS/DIA: ${bpData.bpsys}/${bpData.bpdia}"
                    hrView.text = "HR: ${bpData.heartrate} bpm"

                    bpListLayout.addView(listItem)
                }
            }

            database.getAllEcgDataForUser(userId) { ecgDataList ->
                ecgListLayout.removeAllViews()
                ecgDataList.sortedByDescending { it.date }.forEach { ecgData ->
                    val listItem = LayoutInflater.from(requireContext())
                        .inflate(R.layout.list_item_ecg, ecgListLayout, false)

                    val dateView = listItem.findViewById<TextView>(R.id.ecgDateText)
                    val hrView = listItem.findViewById<TextView>(R.id.ecgHeartRateText)
                    val detailButton = listItem.findViewById<ImageButton>(R.id.ecgDetailsButton)

                    dateView.text = formatTimestamp(ecgData.date)
                    hrView.text = "HR: ${ecgData.heartrate} bpm"

                    detailButton.setOnClickListener {
                        val ecgFragment = ShowEcgHistoryFragment.newInstance(ecgData.ecgId)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ecgFragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    ecgListLayout.addView(listItem)
                }
            }
        }

        return view
    }

    private fun formatTimestamp(timestamp: Long): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(timestamp))
    }
}
