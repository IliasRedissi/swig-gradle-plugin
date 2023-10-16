package com.redissi.swig.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.redissi.sample.Company
import com.redissi.sample.groovy.Employee
import com.redissi.swig.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("sample")
            System.loadLibrary("SampleWrapper")
            System.loadLibrary("SampleGroovyWrapper")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val google = Company().apply {
            name = "Google"
            foundationYear = 1998
        }
        val lockheimer = Employee("Hiroshi Lockheimer", google)

        binding.employeeName.text = lockheimer.name
        binding.companyName.text = "${lockheimer.company.name} (${lockheimer.company.foundationYear})"
    }
}