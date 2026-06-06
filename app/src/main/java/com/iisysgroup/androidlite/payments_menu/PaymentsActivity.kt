package com.iisysgroup.androidlite.payments_menu

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.iisysgroup.androidlite.*
import com.iisysgroup.androidlite.all_history.AllHistoryActivity
import com.iisysgroup.androidlite.login.LoginActivity
import com.iisysgroup.androidlite.login.securestorage.SecureStorage
import com.iisysgroup.androidlite.payments_menu.transfer.TransferAmountEntry
import com.iisysgroup.androidlite.payments_menu.transfer.TransferBankSelection
import com.iisysgroup.androidlite.utils.SharedPreferenceUtils
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert


/**
 * Created by Oladipo Siyanbola D on 5/7/2019.
 */

class PaymentsActivity : AppCompatActivity() {
    private lateinit var recyclerViewModel: RecyclerViewModel

    private lateinit var toolbar: Toolbar


    private lateinit var mRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payments)

        launch{
            SharedPreferenceUtils.setIsTerminalPrepped(this@PaymentsActivity, TermMagmActivity.TerminalUtils.isTerminalPrepped(this@PaymentsActivity, application as App))

        }


        recyclerViewModel = ViewModelProviders.of(this).get(RecyclerViewModel::class.java)
        mRecyclerView = findViewById(R.id.recyclerView)


        val gridLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        mRecyclerView.layoutManager = gridLayoutManager

        val adapter = MenuRecyclerAdapter()
        mRecyclerView.adapter = adapter

        toolbar = findViewById(R.id.toolbar1)

        setSupportActionBar(toolbar)


        val integerObserver = Observer<Int> { integer -> onRecyclerItemClick(integer!!) }
        recyclerViewModel.itemSelected.observe(this, integerObserver)
    }

    internal fun onRecyclerItemClick(position: Int) {
        when (position) {
            0 -> startActivity(Intent(this, PurchaseActivity::class.java))
            1 -> startActivity(Intent(this, LoginActivity::class.java))
//            2 -> {
//                val intent = Intent(this, TransferBankSelection::class.java)
//                intent.putExtra("transfer_type", TransferAmountEntry.TRANSACTION_TYPE.TRANSFER)
//                startActivity(intent)
//            }
//            3 -> {
//                val withdrawal = Intent(this, TransferBankSelection::class.java)
//                withdrawal.putExtra("transfer_type", TransferAmountEntry.TRANSACTION_TYPE.WITHDRAWAL)
//                startActivity(withdrawal)
//            }

            2 -> {
                alert {
                    title = "Help"
                    message = "1. Tap on the Menu on the top right corner\n" +
                            "2. Go to Settings, enter the password and Enter a valid Terminal ID\n" +
                            "3. Tap on Terminal Management and on Configure Terminal\n\n"+
                            "   www.iisysgroup.com \n      070-2255-4839\n"
                }.show()
            }

            //            5 -> startActivity(Intent(this, CashAdvance::class.java))
//            6 -> startActivity(Intent(this, CashBack::class.java))
//            5 -> startActivity(Intent(this, WalletBalance::class.java))

//            6 -> {
//                startActivity(Intent(this, AllHistoryActivity::class.java))
//            }
//            8 -> startActivity(Intent(this, RefundActivity::class.java))
//            9 -> startActivity(Intent(this, DigitalPayments::class.java))
        }
    }

    inner class MenuRecyclerAdapter : RecyclerView.Adapter<MyViewHolder>() {

//        internal var names = arrayOf("Purchase", "VAS", "Transfer", "Withdrawal", "Deposit", "Cash Advance", "Cash back", "Balance Enquiry", "Refund \u0026 Reversal", "Digital Payments")

        internal var names = arrayOf("Purchase", "Services",  "Help")

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val layoutInflater = LayoutInflater.from(this@PaymentsActivity)
            val view = layoutInflater.inflate(R.layout.item_menu_recycler, parent, false)

            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            when (position) {
                0 -> {
                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_light))
                    holder.imageView.setImageResource(R.drawable.ic_credit_card_black_24dp)
                }
                1 -> {
                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_dark))
                    holder.imageView.setImageResource(R.drawable.ic_extension_black_24dp)
                }
//                2 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_light))
//                    holder.imageView.setImageResource(R.drawable.ic_file_upload_black_24dp)
//
//                }
//
//                3 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_light))
//                    holder.imageView.setImageResource(R.drawable.ic_file_download_black_24dp)
//                }

                2 -> {
                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_light))
                    holder.imageView.setImageResource(R.drawable.ic_help_outline_black_24dp)
                }

                //                5 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_dark))
//                    holder.imageView.setImageResource(R.drawable.ic_cash_advance)
//                }

//                6 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_dark))
//
//                    holder.imageView.setImageResource(R.drawable.ic_cash_back)
//                }

//                5 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_dark))
//                    holder.imageView.setImageResource(R.drawable.ic_account_balance_wallet_black_24dp)
//                }
//
//                6 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_dark))
//                    holder.imageView.setImageResource(R.drawable.transactionhistory)
//                }

//                8 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_light))
//                    holder.imageView.setImageResource(R.drawable.ic_refund_24dp)
//                }
//
//                9 -> {
//                    holder.constraintLayout.setBackgroundColor(resources.getColor(R.color.recyclerview_dark))
//                    holder.imageView.setImageResource(R.drawable.ic_digital_payments)
                // }
            }


            holder.textView.text = names[position]

        }

        override fun getItemCount(): Int {
            return names.size
        }

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var textView: TextView
        internal var imageView: ImageView
        internal var constraintLayout: ConstraintLayout

        init {
            itemView.setOnClickListener(this)
            textView = itemView.findViewById(R.id.text)
            imageView = itemView.findViewById(R.id.imageView)
            constraintLayout = itemView.findViewById(R.id.constraint)
        }

        override fun onClick(view: View) {
            recyclerViewModel.setItemSelected(adapterPosition)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.payment_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_others -> {
                finish()
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }

//            R.id.action_sign_out -> {
//                signOut()
//                return true
//            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun signOut() {
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        sharedPreferences.edit().clear().apply()
//        SecureStorage.deleteAll()
//        SharedPreferenceUtils.setUserLoggedIn(this@PaymentsActivity, false)
//        startActivity(Intent(this, LoginActivity::class.java))
//        finish()
//
//    }
}
