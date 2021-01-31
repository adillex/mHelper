package com.adillex.mhelper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.adillex.mhelper.databinding.FragmentMyAccountBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.bottom_sheet_loyaut.*
import kotlinx.android.synthetic.main.bottom_sheet_loyaut.view.*
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_new_creater.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "userId"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [myAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val INTENT_PERMISSION_REQUEST_CAMERA_FOR_PHOTO = 100
private const val INTENT_PERMISSION_REQUEST_CAMERA = 120
private const val INTENT_TAKE_PHOTO_RESULT = 150
private const val INTENT_CHOOSE_PHOTO_FROM_GALLERY = 170

class myAccountFragment : Fragment() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var imageFilePath: String? = null
    private var actualProImagePath: String? = null
    private var mStorageRef: StorageReference? = null

    private var mAuth: FirebaseAuth? = null
    private var myRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var user: User? = null
    private var uid: String? = null
    private var isNotMyAccount: Boolean? = null


    private lateinit var binding: FragmentMyAccountBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_PARAM1,null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_my_account,container,false)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("users")

        mStorageRef = FirebaseStorage.getInstance().getReference();
        if(uid == null){
            uid = mAuth?.uid!!
            isNotMyAccount = false
        }else{
            isNotMyAccount = true
        }
        myRef?.child(uid!!)?.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.d("myAccountFragment", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                user = p0.getValue(User::class.java)
                binding.myRatingBar.rating = user?.stars!!
                binding.userName.setText(user?.name)
            }

        })
        Log.d("myAccountFragment", "$uid.jpg")
        mStorageRef?.child("$uid.jpg")?.downloadUrl?.addOnSuccessListener {
            val  downloadUri = it
            Log.d("myAccountFragment", it.path)
            Glide.with(this).load(downloadUri).apply(RequestOptions.circleCropTransform())
                .thumbnail(0.1f).into(binding.profileImage)
        }
        binding.usersEvents.setOnClickListener{
            val intent = Intent(activity, recyclerViewActivity::class.java)
            intent.putExtra("code",1)
            intent.putExtra("uid",uid)
            startActivity(intent)
            //view?.findNavController()?.navigate(R.id.action_myAccountFragment_to_usersEventsFragment)
        }
        binding.doneUsersEvents.setOnClickListener{
            val intent = Intent(activity, recyclerViewActivity::class.java)
            intent.putExtra("code",2)
            intent.putExtra("uid",uid)
            startActivity(intent)
        }

        binding.doneEvents.setOnClickListener{
            val intent = Intent(activity, recyclerViewActivity::class.java)
            intent.putExtra("code",3)
            intent.putExtra("uid",uid)
            startActivity(intent)
        }



        binding.profileImage.setOnClickListener{
            takePhotoOrChooseCamera()

        }

        binding.signOut.setOnClickListener{
            mAuth?.signOut()
            activity?.let{it->
                App.saveSharedPreferences(it, "isinSystem", "false")
                it.finish()
                startActivity(Intent(it, LogInActivity::class.java))
            }

        }
//        binding.myRatingBar.numStars = user?.stars


        bottomSheetBehavior = BottomSheetBehavior.from<View>(binding.bottomSheetLoyaut)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {}
            override fun onSlide(view: View, v: Float) {}
        })
        binding.bottomSheetLoyaut.chooseFromGalleryLl.setOnClickListener{
            requestCamera(2);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        binding.bottomSheetLoyaut.takePhotoLl.setOnClickListener{
            requestCamera(1);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        if(isNotMyAccount!!){
            binding.doneEvents.text = "выполненые пользователем события"
            binding.signOut.visibility = View.GONE
        }
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(uid:String) =
            myAccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, uid)

                }
            }
    }
    private fun takePhotoOrChooseCamera(){
        activity?.let{context->
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestCameraPermission(INTENT_PERMISSION_REQUEST_CAMERA_FOR_PHOTO);

            }else {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        }

    }

    private fun requestCameraPermission(request: Int) {
        activity?.let{context->
            if (request == INTENT_PERMISSION_REQUEST_CAMERA) { // Permission has not been granted and must be requested.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.CAMERA
                    )
                ) {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.CAMERA),
                        request
                    )
                } else {
                    // Request the permission. The result will be received in onRequestPermissionResult().
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.CAMERA),
                        request
                    )
                }
            }
            if (request == INTENT_PERMISSION_REQUEST_CAMERA_FOR_PHOTO) { // Permission has not been granted and must be requested.
// Provide an additional rationale to the user if the permission was not granted
// and the user would benefit from additional context for the use of the permission.
// Display a SnackBar with cda button to request the missing permission.
                // Request the permission
                ActivityCompat.requestPermissions(
                    context, arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    request
                )
            } else {
                // Request the permission. The result will be received in onRequestPermissionResult().
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    request
                )
            }
        }
    }
    private fun requestCamera(type: Int) {
        activity?.let{context->
            val pictureIntent: Intent
            if (type == 1) {
                pictureIntent = Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
                )
                if (pictureIntent.resolveActivity(context.getPackageManager()) != null) { //Create a file to store the image
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                    } catch (ex: IOException) { // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            context,
                            "com.adillex.mhelper.provider",
                            photoFile
                        )
                        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(
                            pictureIntent,
                            INTENT_TAKE_PHOTO_RESULT
                        )
                    }
                }
            } else {
                pictureIntent = Intent(Intent.ACTION_PICK)
                pictureIntent.type = "image/*"
                startActivityForResult(
                    pictureIntent,
                    INTENT_CHOOSE_PHOTO_FROM_GALLERY
                )
            }
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        var image: File? = null
        activity?.let{context->
            val imageFileName: String = App.generateId()
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
            )
            imageFilePath = image?.absolutePath

        }
        return image
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INTENT_TAKE_PHOTO_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                actualProImagePath = imageFilePath
                Glide.with(this).load(imageFilePath).apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.1f).into(profileImage)
                user?.let{user ->
                    uploadPhoto(user, uid!!)
                }
            }

        }

        if (requestCode == INTENT_CHOOSE_PHOTO_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    activity?.let{context->
                        val imageUri = data!!.data
                        val imageStream: InputStream? = context.getContentResolver().openInputStream(imageUri!!)
                        val selectedImage = BitmapFactory.decodeStream(imageStream)
                        actualProImagePath = getPathFromURI(imageUri)

                       Glide.with(this).asBitmap().load(selectedImage).apply(RequestOptions.circleCropTransform()).thumbnail(0.1f).into(profileImage)

                    }

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }
    fun getPathFromURI(contentUri: Uri): String? {
        var res: String? = null
        activity?.let { context->
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? = context.getContentResolver().query(contentUri, proj, null, null, null)
            cursor?.let {
                if (Objects.requireNonNull(cursor).moveToFirst()) {
                    val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    res = cursor.getString(columnIndex)
                }
            }
            cursor?.close()
        }


        return res
    }
    private fun uploadPhoto(user:User, uid:String){
        val  mStorageRef = FirebaseStorage.getInstance().getReference();
        val profilRef = mStorageRef.child(uid.plus(".jpg"))
        //mStorageRef.getFile()
        actualProImagePath?.let {
            val file = Uri.fromFile(File(it))
            profilRef.putFile(file).addOnSuccessListener {object : OnSuccessListener<UploadTask.TaskSnapshot>{
                override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                    myRef?.child(uid)?.child("profile")?.setValue(p0?.metadata?.path)
                }

            }
            }.addOnFailureListener {
                Log.d("myAccountFragment.kt", it.toString())
            }
        }
    }
}
