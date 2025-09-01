package personal.cx.point.ui.download

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import personal.cx.point.R

class DownloadAdapter(private val context: Context, private val items: List<Item>) : BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(num: Int): Any {
        return items[num]
    }

    override fun getItemId(num: Int): Long {
        return items[num].id
    }

    override fun getView(num: Int, view: View?, group: ViewGroup?): View {
        var view_handle = view
        val holder: ViewHolder
        if(view_handle == null){
            view_handle = LayoutInflater.from(context).inflate(R.layout.list_download_item, group, false)
            holder = ViewHolder(
                view_handle.findViewById(R.id.imageIcon) as ImageView,
                view_handle.findViewById(R.id.textName) as TextView,
                view_handle.findViewById(R.id.textInfo) as TextView
            )
            view_handle.tag = holder
        }
        else{
            holder = view_handle.tag as ViewHolder
        }

        holder.imageIcon.setImageResource(items[num].icon)
        holder.textName.text = items[num].name
        holder.textInfo.text = items[num].info

        return view_handle as View
    }

    private class ViewHolder(val imageIcon: ImageView, val textName: TextView, val textInfo: TextView)
}

data class Item (
    val id: Long,
    val icon: Int,
    val name: String,
    val info: String,
    val locate: String
)