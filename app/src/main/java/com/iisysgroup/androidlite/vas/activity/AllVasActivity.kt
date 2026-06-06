package com.iisysgroup.androidlite.vas.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.iisysgroup.androidlite.*
import com.iisysgroup.androidlite.vas.VasAdapter
import com.iisysgroup.androidlite.vas.VasAllGenerator
import com.iisysgroup.androidlite.vas.VasBaseActivity
import com.iisysgroup.androidlite.vas.VasItems
import com.iisysgroup.androidlite.vas.activity.education.Waec.WaecActivity
import com.iisysgroup.androidlite.vas.activity.energy.Abuja.AbujaElectric
import com.iisysgroup.androidlite.vas.activity.energy.Eko.EkoElectric
import com.iisysgroup.androidlite.vas.activity.energy.Enugu.EnuguElectric
import com.iisysgroup.androidlite.vas.activity.energy.Ikeja.IkejaElectric
import com.iisysgroup.androidlite.vas.activity.energy.Kaduna.KadunaElectric
import com.iisysgroup.androidlite.vas.activity.energy.Kano.KanoElectric
import com.iisysgroup.androidlite.vas.activity.energy.PortHarcourt.PHElectric
import com.iisysgroup.androidlite.vas.airtime_and_data.AirtimeActivity
import com.iisysgroup.androidlite.vas.cable.Dstv
import com.iisysgroup.androidlite.vas.cable.Gotv
import com.iisysgroup.androidlite.vas.cable.startimes.Startimes
import kotlinx.android.synthetic.main.activity_all_vas.*
import org.jetbrains.anko.toast
import java.util.*

class AllVasActivity : VasBaseActivity(), VasAdapter.VasClickListener {
    lateinit var vasItems : ArrayList<VasItems>


    override fun onVasItemClick(vasItemsArrayList: ArrayList<VasItems>?, position: Int) {
        when (position){
            //Airtime
            0 -> airtime("MTNVTU")
            1 -> airtime("AirtelVTU")
            2 -> airtime("9mobileVTU")
            3 -> airtime("GloVTU")

            //Cable TV
            4 -> startActivity(Intent(this, Dstv::class.java))
            5 -> startActivity(Intent(this, Gotv::class.java))
            6 -> startActivity(Intent(this, Startimes::class.java))
            7 -> startActivity(Intent(this, ConsatTVOptions::class.java))

            //Education
            8 -> startActivity(Intent(this, WaecActivity::class.java))

            //Energy
            9 -> startActivity(Intent(this, EkoElectric::class.java))

            10 -> {}//startActivity(Intent(this, com.iisysgroup.androidlite.vas.activity.energy.Ibadan.Ibedc))
            11 -> startActivity(Intent(this, IkejaElectric::class.java))
            12 -> startActivity(Intent(this, AbujaElectric::class.java))

            13 -> startActivity(Intent(this, EnuguElectric::class.java))
            14 -> startActivity(Intent(this, KadunaElectric::class.java))

            15 -> startActivity(Intent(this, KanoElectric::class.java))
            16 -> startActivity(Intent(this, PHElectric::class.java))

            //Event
            17 -> toast("Afritickets coming soon")

            //Games
            18 -> toast("Golden chance coming soon")
            19 -> toast("Paddy bet coming soon")

            //Insurance
            20 -> startActivity(Intent(this, Leadway::class.java))
            21 -> startActivity(Intent(this, Cornerstone::class.java))

            //Internet Data
            22 -> toast("Smile coming soon")
            23 -> toast("Spectranet coming soon")
            24 -> toast("Swift coming soon")

            //Digital payments
            25 -> toast("Remita coming soon")
            26 -> toast("Mastercard coming soon")

        }
    }

    private fun airtime(provider : String){
        val intent = Intent(this, AirtimeActivity::class.java)
        intent.putExtra(AirtimeActivity.TAGS.AIRTIME_PURCHASE_KEY, "verified")
        intent.putExtra(AirtimeActivity.TAGS.AIRTIME_PURCHASE_PROVIDER_TYPE, provider.toUpperCase())

        startActivity(intent)
    }

    override fun setRecyclerView(): RecyclerView {

        return rv
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_all_vas)

        vasItems = VasAllGenerator.generateData(this)
        initializeRecyclerView(vasItems, this)

        setToolbar(toolbar)

    }
}
