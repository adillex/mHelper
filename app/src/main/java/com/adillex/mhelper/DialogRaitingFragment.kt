package com.adillex.mhelper

import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import com.adillex.mhelper.databinding.FragmentDialogRaitingBinding
import kotlinx.android.synthetic.main.fragment_dialog_raiting.view.*

private var binding: FragmentDialogRaitingBinding? = null

class DialogRaitingFragment : DialogFragment(), View.OnClickListener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dialog_raiting, container, false)
        dialog.setTitle("Title!")
        val v: View = inflater.inflate(R.layout.fragment_dialog_raiting, null)
        v.findViewById<View>(R.id.getRaiting).setOnClickListener{
            var num :Int? = binding?.StarsForRaiting?.numStars
            activity?.let {it1 ->
                App.saveSharedPreferences(it1,"stars", num.toString())
            }

            dismiss()
        }
        return v
    }

    override fun onClick(v: View?) {
        onClick(v)
    }
}