package com.beparker.insomniac.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beparker.insomniac.R
import com.beparker.insomniac.db.PackageViewModel

class PackageFragment : Fragment() {

    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_package_list, container, false)
        val viewModel = ViewModelProviders.of(this).get(PackageViewModel::class.java)
        val adapter = PackageRecyclerViewAdapter(mutableListOf())

        if (view is RecyclerView) {
            recyclerView = view
            with(view) {
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
            }
        }

        viewModel.getPackages().observe(this, Observer {
            adapter.setItems(it)
        })

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        (recyclerView?.adapter as? PackageRecyclerViewAdapter)?.dispose()
    }

    companion object {
        @JvmStatic
        fun newInstance() = PackageFragment()
    }
}
