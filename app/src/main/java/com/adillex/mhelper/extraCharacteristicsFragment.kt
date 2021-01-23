package com.adillex.mhelper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.adillex.mhelper.databinding.FragmentExtraCharacteristicsBinding
import kotlinx.android.synthetic.main.fragment_extra_characteristics.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [extraCharacteristicsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class extraCharacteristicsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentExtraCharacteristicsBinding


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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_extra_characteristics, container, false)

        val exCharecterestics = ExCharecteristecs()
        //object

        binding.backButtonImg.setOnClickListener{
            it.findNavController().navigate(R.id.action_extraCharacteristicsFragment_to_newEventCreaterFragment)
        }

        binding.saveButton.setOnClickListener{
            val bundle = Bundle()
            bundle.putSerializable("exChar",exCharecterestics)
            it.findNavController().navigate(R.id.action_extraCharacteristicsFragment_to_newEventCreaterFragment, bundle)
        }
        binding.ageRB.setOnCheckedChangeListener{_, isChecked->
            if(isChecked){
                binding.ageET.visibility = View.VISIBLE
            }else{
                exCharecterestics.age = null
                binding.ageET.visibility = View.INVISIBLE
            }
        }
        binding.genderRB.setOnCheckedChangeListener{_, isChecked->
            if(isChecked){
                binding.genderRestrictionLinearLayout.visibility = View.VISIBLE
            }else{
                exCharecterestics.isWoman = null
                binding.genderRestrictionLinearLayout.visibility = View.INVISIBLE
            }
        }
        binding.starRB.setOnCheckedChangeListener{_, isChecked->
            if(isChecked){
                binding.starsLinearLayout.visibility = View.VISIBLE
            }else{
                exCharecterestics.trust = null
                binding.starsLinearLayout.visibility = View.INVISIBLE
            }
        }

        binding.star1.setOnClickListener{
            exCharecterestics.trust = 1
            activity?.let { context -> ContextCompat.getColor(context, R.color.colorBlack) }?.let { black ->
                binding.star1.setBackgroundColor(black)
            }
        }
        binding.star2.setOnClickListener{
            exCharecterestics.trust = 2
            activity?.let { context -> ContextCompat.getColor(context, R.color.colorBlack) }?.let { black ->
                binding.star1.setBackgroundColor(black)
                binding.star2.setBackgroundColor(black)
            }
        }
        binding.star3.setOnClickListener{
            exCharecterestics.trust = 3
            activity?.let { context -> ContextCompat.getColor(context, R.color.colorBlack) }?.let { black ->
                binding.star1.setBackgroundColor(black)
                binding.star2.setBackgroundColor(black)
                binding.star3.setBackgroundColor(black)
            }
        }
        binding.star4.setOnClickListener{
            exCharecterestics.trust = 4
            activity?.let { context -> ContextCompat.getColor(context, R.color.colorBlack) }?.let { black ->
                binding.star1.setBackgroundColor(black)
                binding.star2.setBackgroundColor(black)
                binding.star3.setBackgroundColor(black)
                binding.star4.setBackgroundColor(black)
            }
        }
        binding.star5.setOnClickListener{
            exCharecterestics.trust = 5
            activity?.let { context -> ContextCompat.getColor(context, R.color.colorBlack) }?.let { black ->
                binding.star1.setBackgroundColor(black)
                binding.star2.setBackgroundColor(black)
                binding.star3.setBackgroundColor(black)
                binding.star4.setBackgroundColor(black)
                binding.star5.setBackgroundColor(black)
            }
        }

        binding.femaleRB.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                exCharecterestics.isWoman = true
                maleRB.isChecked = false
            }
        }

        binding.maleRB.setOnCheckedChangeListener{_, isChecked->
            if(isChecked){
                exCharecterestics.isWoman = false
                femaleRB.isChecked = false
            }
        }


        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            extraCharacteristicsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
