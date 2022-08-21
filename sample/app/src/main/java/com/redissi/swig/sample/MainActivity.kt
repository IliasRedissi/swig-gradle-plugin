package com.redissi.swig.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.redissi.sample.Company
import com.redissi.swig.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("sample")
            System.loadLibrary("SampleWrapper")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val google = Company("Google")

        binding.companyName.text = google.name
    }
}