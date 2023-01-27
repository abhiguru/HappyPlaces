package `in`.tutorial.happyplaces.activites

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import `in`.tutorial.happyplaces.R
import `in`.tutorial.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import `in`.tutorial.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    var binding: ActivityHappyPlaceDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        var happyPlaceDetailModel : HappyPlaceModel? = null
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel =
                (intent.getParcelableExtra<HappyPlaceModel>(MainActivity.EXTRA_PLACE_DETAILS)!!)
        }
        if(happyPlaceDetailModel != null){
            setSupportActionBar(binding?.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title
            binding?.toolbarHappyPlaceDetail?.setNavigationOnClickListener {
                onBackPressed()
            }
            binding?.ivPlaceImage?.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            binding?.tvDescription?.text = happyPlaceDetailModel.desc
            binding?.tvLocation?.text = happyPlaceDetailModel.location
        }
    }
}