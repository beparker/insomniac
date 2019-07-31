package com.beparker.insomniac.fragment


import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beparker.insomniac.R
import com.beparker.insomniac.addTo
import com.beparker.insomniac.db.Package
import com.beparker.insomniac.packageDatabase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_package.view.*

class PackageRecyclerViewAdapter(
    private val items: MutableList<Package>
) : RecyclerView.Adapter<PackageRecyclerViewAdapter.ViewHolder>() {

    private val compositeDisposable = CompositeDisposable()

    fun dispose() = compositeDisposable.dispose()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_package, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.checkBox.isChecked = item.enabled
        holder.contentView.text = item.label

        try {
            val icon = holder.imageView.context.packageManager.getApplicationIcon(item.name)
            holder.imageView.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            Completable.fromAction {
                packageDatabase.packageDao().delete(item)
            }
                .subscribeOn(Schedulers.io())
                .subscribe().addTo(compositeDisposable)
        }

        with(holder.checkBox) {
            setOnCheckedChangeListener { _, isChecked ->
                Completable.fromAction {
                    item.enabled = isChecked
                    packageDatabase.packageDao().update(item)
                }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
                    .addTo(compositeDisposable)
            }
        }

        with(holder.view) {
            setOnClickListener {
                holder.checkBox.toggle()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Package>) {
        Single.fromCallable {
            DiffUtil.calculateDiff(PackageDiffCallback(newItems))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { diffResult ->
                diffResult.dispatchUpdatesTo(this)
                items.clear()
                items.addAll(newItems)
            }.addTo(compositeDisposable)
    }

    inner class PackageDiffCallback(private val newItems: List<Package>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return items[oldItemPosition].name == newItems[newItemPosition].name
        }

        override fun getOldListSize(): Int {
            return items.size
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return items[oldItemPosition].name == newItems[newItemPosition].name
        }
    }

    class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val checkBox: CheckBox = mView.checkbox
        val contentView: TextView = mView.content
        val imageView: ImageView = mView.image
        val view = mView

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}
