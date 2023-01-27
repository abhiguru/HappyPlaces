package `in`.tutorial.happyplaces.activites

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import `in`.tutorial.happyplaces.R
import `in`.tutorial.happyplaces.database.DatabaseHandler
import `in`.tutorial.happyplaces.databinding.ActivityAddHappyPlaceBinding
import `in`.tutorial.happyplaces.models.HappyPlaceModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlace : AppCompatActivity(), View.OnClickListener {
    var binding: ActivityAddHappyPlaceBinding? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage:Uri? = null
    private var mLat : Double = 0.0
    private var mLong : Double = 0.0
    private var mHappyPlaceDetails : HappyPlaceModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails =
                intent.getParcelableExtra<HappyPlaceModel>(MainActivity.EXTRA_PLACE_DETAILS)
        }
        dateSetListener = DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()
        if(mHappyPlaceDetails!=null){
            supportActionBar?.title = "Edit Happy Place"
            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.desc)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLat = mHappyPlaceDetails!!.latitude
            mLong = mHappyPlaceDetails!!.longitude
            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
            binding?.btnSave?.text = "Update"
        }
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@AddHappyPlace,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture from camera")
                pictureDialog.setItems(pictureDialogItems){
                    dialog, which ->
                    when(which){
                        0-> choosePhotoFromGallery()
                        1-> takePhotoFromCam()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save->{
                when{
                    binding?.etTitle?.text.isNullOrEmpty()->{
                        Toast.makeText(this@AddHappyPlace, "Enter Title", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text.isNullOrEmpty()->{
                        Toast.makeText(this@AddHappyPlace, "Enter Desc", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty()->{
                        Toast.makeText(this@AddHappyPlace, "Enter Location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null ->{
                        Toast.makeText(this@AddHappyPlace, "Enter Image", Toast.LENGTH_SHORT).show()
                    }else ->{

                        var id:Int = 0
                        if(mHappyPlaceDetails!=null){
                            id = mHappyPlaceDetails!!.id
                        }

                        val happyPlaceModel = HappyPlaceModel(
                            id,
                            binding?.etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLat,
                            mLong
                        )
                        val dbHandler = DatabaseHandler(this)
                    var resultCode:Int = -1
                        if(mHappyPlaceDetails == null){
                            resultCode = dbHandler.addHappyPlace(happyPlaceModel).toInt()
                        }else {
                            resultCode = dbHandler.updateHappyPlace(happyPlaceModel)
                        }
                        if(resultCode>0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                }
                }

            }
        }
    }
    companion object{
        private const val GALLERY = 1
        private const val CAM = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream:OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data!=null){
                    val contentURI = data.data
                    try {
                        val selImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        binding?.ivPlaceImage?.setImageBitmap(selImageBitmap)
                        saveImageToInternalStorage = saveImageToInternalStorage(selImageBitmap)
                        Log.e("Saved Image", " Path:: $saveImageToInternalStorage")
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }else if(requestCode == CAM){
                val thumb:Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumb)
                Log.e("Saved Image", " Path:: $saveImageToInternalStorage")
                binding?.ivPlaceImage!!.setImageBitmap(thumb)
            }
        }
    }
    private fun takePhotoFromCam(){
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report?.areAllPermissionsGranted() == true){
                    val galIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galIntent, CAM)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialogForPermission()
            }
        }).onSameThread().check()
    }
    private fun choosePhotoFromGallery(){
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report?.areAllPermissionsGranted() == true){
                    val galIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialogForPermission()
            }
        }).onSameThread().check()
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this).setMessage(""+
                "Please enabler permissions under App Settings")
            .setPositiveButton("Go to Settings"){
                _, _->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){

                }
            }.setNegativeButton("Cancel"){dialog, _->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }
}