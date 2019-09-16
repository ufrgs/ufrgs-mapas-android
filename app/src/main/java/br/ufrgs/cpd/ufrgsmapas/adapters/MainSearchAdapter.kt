package br.ufrgs.cpd.ufrgsmapas.adapters

import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.ufrgs.cpd.ufrgsmapas.R
import br.ufrgs.cpd.ufrgsmapas.models.Building
import kotlinx.android.synthetic.main.item_search.view.*
import com.lapism.searchview.SearchItem



/**
 * Created by Theo on 31/07/17.
 */
class MainSearchAdapter() : RecyclerView.Adapter<MainSearchAdapter.SearchViewHolder>(), Parcelable {

    var list : List<Building>
    var itemClickCompletion: ((Building)->Unit)? = null

    constructor(parcel: Parcel) : this() {
        list = arrayListOf()
    }

    constructor(completion: (Building)->Unit) : this() {
        list = arrayListOf()
        itemClickCompletion = completion
    }

    init {
        list = arrayListOf()
    }

    fun setData(data: List<Building>) {
        if (list == null) {
            list = data
            notifyDataSetChanged()
        } else {
            val previousSize = list.size
            val nextSize = data.size
            list = data
            if (previousSize == nextSize && nextSize != 0)
                notifyItemRangeChanged(0, previousSize)
            else if (previousSize > nextSize) {
                if (nextSize == 0)
                    notifyItemRangeRemoved(0, previousSize)
                else {
                    notifyItemRangeChanged(0, nextSize)
                    notifyItemRangeRemoved(nextSize - 1, previousSize)
                }
            } else {
                notifyItemRangeChanged(0, previousSize)
                notifyItemRangeInserted(previousSize - 1, nextSize)
            }
        }
    }

    override fun onBindViewHolder(holder: SearchViewHolder?, position: Int) {
        holder?.bindSearch(list[position], this.itemClickCompletion)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long = list[position].codPredioUfrgs.toLong()

    class SearchViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var itemClickCompletion: ((Building)->Unit)? = null
        var building: Building? = null

        fun bindSearch(building : Building, completion: ((Building)->Unit)?) {
            this.itemClickCompletion = completion
            this.building = building

            itemView.text.text = building.codPredioUfrgs.toString() + " - " + building.nomePredio
            itemView.setOnClickListener {

                if (completion != null) {
                    completion!!(building)
                }
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(list)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainSearchAdapter> {
        override fun createFromParcel(parcel: Parcel): MainSearchAdapter {
            return MainSearchAdapter(parcel)
        }

        override fun newArray(size: Int): Array<MainSearchAdapter?> {
            return arrayOfNulls(size)
        }
    }
}