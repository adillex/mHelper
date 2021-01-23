package com.adillex.mhelper

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.adillex.mhelper.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [registrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class registrationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mAuth: FirebaseAuth? = null
    private var myRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null


    private lateinit var binding: FragmentLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("users")




        binding.forgotPassword.setOnClickListener {
            it.findNavController().navigate(R.id.action_LogInFragment_to_passwordRecoveryStartFragment)
        }

        binding.logInButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            var allow = true
            if(binding.PasswordET.text.trim().length == 0 || binding.nameET.text.trim().length == 0){
                allow = false
            }
            if(allow){
                activity?.let { it1 ->
                    // get id
                    mAuth?.signInWithEmailAndPassword(binding.nameET.text.toString().trim(), binding.PasswordET.text.toString().trim())?.addOnCompleteListener {
                        if (it.isSuccessful){
                            binding.progressBar.visibility = View.INVISIBLE
                            context?.let { it2 -> App.saveSharedPreferences(it2,"mail",binding.nameET.text.toString()) }
                            startActivity(Intent(activity, MainActivity::class.java))
                        }else{
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(activity, "вы указали не правильный пароль или адрес, попробуйте еще раз",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                binding.progressBar.visibility = View.INVISIBLE
                Toast.makeText(activity,"не правильно введен логин или пароль",Toast.LENGTH_SHORT).show()
            }

        }
        binding.registrationButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_LogInFragment_to_newCreaterFragment)
        }
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment registrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                registrationFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
