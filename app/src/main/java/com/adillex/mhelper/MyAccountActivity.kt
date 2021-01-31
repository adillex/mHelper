package com.adillex.mhelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.adillex.mhelper.databinding.ActivityMyAccountBinding

class MyAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "профиль"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_my_account)
        val fragment = myAccountFragment.newInstance(intent.getStringExtra("userId"))
        supportFragmentManager.beginTransaction().add(binding.frame.id, fragment).commit()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
