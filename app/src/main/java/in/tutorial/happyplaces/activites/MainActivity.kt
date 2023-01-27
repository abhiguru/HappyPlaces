package `in`.tutorial.happyplaces.activites

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `in`.tutorial.happyplaces.adapters.HappyPlacesAdapter
import `in`.tutorial.happyplaces.database.DatabaseHandler
import `in`.tutorial.happyplaces.databinding.ActivityMainBinding
import `in`.tutorial.happyplaces.models.HappyPlaceModel
import `in`.tutorial.happyplaces.utils.SwipeToDeleteCallback
import `in`.tutorial.happyplaces.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    var binding:ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this, AddHappyPlace::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQ_CODE)
        }
        getHappyPlacesListFromLocalDB()
    }
    private fun setupHappyPlacesRecyclerView(
        happyPlaceList: ArrayList<HappyPlaceModel>){
        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
        binding?.rvHappyPlacesList?.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter
        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity,
                    viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQ_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

        val delSwipeHandler = object: SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getHappyPlacesListFromLocalDB()
            }
        }
        val delItemTouchHelper = ItemTouchHelper(delSwipeHandler)
        delItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)
    }
    private fun getHappyPlacesListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getHappyPlacesList = dbHandler.getHappyPlacesList()
        if (getHappyPlacesList.size > 0) {
            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailText?.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        }else{
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailText?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQ_CODE){
            if(resultCode == Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }
        }
    }
    companion object{
        var ADD_PLACE_ACTIVITY_REQ_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}