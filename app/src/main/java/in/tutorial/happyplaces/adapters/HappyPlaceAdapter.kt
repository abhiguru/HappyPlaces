package `in`.tutorial.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import `in`.tutorial.happyplaces.activites.AddHappyPlace
import `in`.tutorial.happyplaces.activites.MainActivity
import `in`.tutorial.happyplaces.database.DatabaseHandler
import `in`.tutorial.happyplaces.databinding.ItemHappyPlaceBinding
import `in`.tutorial.happyplaces.models.HappyPlaceModel

open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    class ViewHolder(binding: ItemHappyPlaceBinding):
        RecyclerView.ViewHolder(binding.root){
        val ivImage = binding?.ivPlaceImage
        val tvTitle = binding?.tvTitle
        val tvDesc = binding?.tvDescription
    }
    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHappyPlaceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.ivImage?.setImageURI(Uri.parse(model.image))
        holder.tvTitle?.text = model.title
        holder.tvDesc?.text = model.desc
        holder.itemView.setOnClickListener {
            if(onClickListener!=null){
                onClickListener!!.onClick(position, model)
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position: Int, model: HappyPlaceModel)
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    fun notifyEditItem(activity: Activity, position: Int, requestCode:Int){
        val intent = Intent(context, AddHappyPlace::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(list[position])
        if(isDeleted>0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
// END