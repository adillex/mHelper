package com.adillex.mhelper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.adillex.mhelper.databinding.FragmentPasswordRecoveryStartBinding
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PasswordRecoveryStartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordRecoveryStartFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var mAuth: FirebaseAuth? = null

    private lateinit var binding: FragmentPasswordRecoveryStartBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_password_recovery_start, container, false)
        mAuth = FirebaseAuth.getInstance()

        binding.getNewPasswordButton.setOnClickListener{
            mAuth?.sendPasswordResetEmail(binding.eMailET.text.toString())?.addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(activity,"Письмо с ссылкой восстановления пароля было отправлено на почту", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(activity,"Произошла ошибка", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PasswordRecoveryStartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
