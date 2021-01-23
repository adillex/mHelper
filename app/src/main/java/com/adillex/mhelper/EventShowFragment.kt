package com.adillex.mhelper

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.adillex.mhelper.databinding.FragmentEventShowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventShowFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventShowFragment : Fragment() {
    private lateinit var binding: FragmentEventShowBinding
    private var eventId: String? = null
    private var myRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var event:Event? = null
    private var mAuth: FirebaseAuth? = null
    private var myRefUsers: DatabaseReference? = null
    private var responded: Array<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arguments?.let {
                eventId = it.getString("eventId",null)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.w("EventShowFragment","зашел в шоу")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_show, container, false)

        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        myRef = database!!.getReference("events")
        myRefUsers = database!!.getReference("users")

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            view?.findNavController()?.navigateUp()
        }

        myRef?.child(eventId!!)?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                }
            override fun onDataChange(p0: DataSnapshot) {
                val event = p0.getValue(Event::class.java)
                binding.headerEventNickName.text = event?.nickname
                if(event?.workingStatus == 1){
                    binding.headerWorkingStatus.text = "в поисках волотеров"
                }else if(event?.workingStatus == 2){
                    binding.headerWorkingStatus.text = "в процессе выполнения"
                    binding.klick.visibility = View.GONE
                }else{
                    binding.headerWorkingStatus.text = "завершен"
                    binding.klick.visibility = View.GONE
                }
                val content = SpannableString(event?.usermail)
                content.setSpan(UnderlineSpan(), 0, content.length, 0)

                binding.masterMail.text = content
                binding.masterMail.setOnClickListener{
                    val bundle = Bundle()
                    bundle.putString("userId", event?.userId.toString())
                    view?.findNavController()
                        ?.navigate(R.id.action_eventShowFragment_to_myAccountFragment, bundle)
                }
                binding.description.text = event?.description
                binding.klick.setOnClickListener{
                    myRef?.child(eventId!!)?.child("responds")?.setValue(mAuth?.uid.toString())
                    mAuth?.uid?.let { it1 ->
                        myRefUsers?.child(it1)?.child("respondedEvents")?.child(eventId!!)?.setValue("")
                    }
                    myRef?.child(eventId!!)?.child("workingStatus")?.setValue(2)
                    myRefUsers?.child(event?.userId!!)?.child("events")?.child(eventId!!)?.child("workingStatus")?.setValue(2)
                }
                binding.klickOut.setOnClickListener{
                    it.findNavController().navigate(R.id.action_eventShowFragment_to_mainMapFragment)
                }
            }
        })


        return binding.root
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventShowFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
