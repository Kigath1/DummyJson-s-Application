package com.xmlapplication.features.dashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.xmlapplication.R

class ItemDashboardCard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        private lateinit var binding : ItemDashboardCard
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.item_dashboard_card)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}