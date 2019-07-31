package com.beparker.insomniac.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beparker.insomniac.packageDatabase

class PackageViewModel : ViewModel() {
    private var packages: LiveData<List<Package>> = MutableLiveData()
    private var enabledPackages: LiveData<List<Package>> = MutableLiveData()



    fun getPackages(): LiveData<List<Package>> {
        return packages
    }

    fun getEnabledPackages(): LiveData<List<Package>> {
        return enabledPackages
    }

   init {
       packages = packageDatabase.packageDao().getAll()
       enabledPackages = packageDatabase.packageDao().getEnabled()
   }
}