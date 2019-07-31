package com.beparker.insomniac

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.transaction
import com.beparker.insomniac.fragment.PackageFragment
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.intentFor

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        supportFragmentManager.transaction {
            replace(R.id.fragment, PackageFragment.newInstance(), "PKG")
        }

        Completable.fromAction {
            loadInstalledApps()
        }
            .subscribeOn(Schedulers.io())
            .subscribe().addTo(compositeDisposable)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (AppMonitorService.running) menu?.add(getString(R.string.stop_service))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.title == getString(R.string.stop_service)) {
            stopService(intentFor<AppMonitorService>())
            item?.isVisible = false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
