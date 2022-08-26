package com.redissi.swig.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.redissi.sample.Company
import com.redissi.sample.Employee
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
        val lockheimer = Employee("Hiroshi Lockheimer", google)

        binding.companyName.text = lockheimer.company.name
    }
}