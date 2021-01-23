package com.adillex.mhelper

import android.Manifest
import android.app.Activity.RESULT_OK
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
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.adillex.mhelper.databinding.FragmentNewCreaterBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.bottom_sheet_loyaut.view.*
import kotlinx.android.synthetic.main.fragment_new_creater.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val INTENT_PERMISSION_REQUEST_CAMERA_FOR_PHOTO = 100
private const val INTENT_PERMISSION_REQUEST_CAMERA = 120
private const val INTENT_TAKE_PHOTO_RESULT = 150
private const val INTENT_CHOOSE_PHOTO_FROM_GALLERY = 170

class NewCreaterFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var mAuth: FirebaseAuth? = null
    private var myRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var imageFilePath: String? = null
    private var actualProImagePath: String? = null

    private lateinit var binding: FragmentNewCreaterBinding

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
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_new_creater,container,false)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database!!.getReference("users")

        binding.registerButton.setOnClickListener{
            if(binding.PasswordET.text.toString().trim() == binding.PasswordCheakET.text.toString().trim()){
                createAccount(binding.EmailET.text.toString().trim(), binding.PasswordET.text.toString().trim())
            }else{
                Toast.makeText(activity, "Пароль не был подтвержден",Toast.LENGTH_SHORT).show()
            }

        }
        binding.addPhoto.setOnClickListener{
            takePhotoOrChooseCamera()
        }

        bottomSheetBehavior = BottomSheetBehavior.from<View>(binding.bottomSheetLoyaut)

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)

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
        return binding.root
    }

    private fun createAccount(email:String, password: String){
        binding.progressBar.visibility = View.VISIBLE

        val user = User()
        user.mail = binding.EmailET.text.toString()
        user.name = binding.nameET.text.toString()
        user.stars = 0f
        user.starNum = 0

        mAuth?.createUserWithEmailAndPassword(email,password)?.addOnCompleteListener{
            binding.progressBar.visibility = View.GONE
            if(it.isSuccessful){
                Toast.makeText(activity, "вы зарегестрировались",Toast.LENGTH_SHORT).show()
                myRef?.child(it.result?.user?.uid!!)?.setValue(user)
                uploadPhoto(user, it.result?.user?.uid!!)
                findNavController().navigate(R.id.action_newCreaterFragment_to_LogInFragment)
            }else{
                Toast.makeText(activity, "вы не зарегестрировались, попробуйте еще раз",Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener{

            binding.progressBar.visibility = View.GONE
            Toast.makeText(activity, it.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewCreaterFragment().apply {
                arguments = Bundle().apply {

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
            if (resultCode == RESULT_OK) {
                actualProImagePath = imageFilePath
                Glide.with(this).load(imageFilePath).apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.1f).into(addPhoto)
            }
        }

        if (requestCode == INTENT_CHOOSE_PHOTO_FROM_GALLERY) {
            if (resultCode == RESULT_OK) {
                try {
                    activity?.let{context->
                        val imageUri = data!!.data
                        val imageStream: InputStream? = context.getContentResolver().openInputStream(imageUri!!)
                        val selectedImage = BitmapFactory.decodeStream(imageStream)
                        actualProImagePath = getPathFromURI(imageUri)

                        Glide.with(this).asBitmap().load(selectedImage).apply(RequestOptions.circleCropTransform()).thumbnail(0.1f).into(addPhoto)

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
                Log.d("newCreaterFragment.kt", it.toString())
            }
        }
    }
}
