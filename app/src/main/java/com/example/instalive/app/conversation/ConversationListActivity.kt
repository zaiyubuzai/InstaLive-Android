package com.example.instalive.app.conversation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.views.BaseActivity
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.BuildConfig
import splitties.alertdialog.appcompat.*
import com.example.instalive.R
import com.example.instalive.databinding.ActivityConversationListBinding
import com.venus.dm.db.entity.ConversationsEntity
import kotlinx.android.synthetic.main.activity_conversation_list.*
import kotlinx.coroutines.launch

@ExperimentalStdlibApi
class ConversationListActivity : BaseActivity<ConversationListViewModel, ActivityConversationListBinding>() {

    private lateinit var messagesListAdapter: ConversationListAdapter

    override fun initData(savedInstanceState: Bundle?) {
        BarUtils.setStatusBarLightMode(this, true)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.fb_direct_messages)
        initList()
    }

    private fun initList() {
        messagesListAdapter =
            ConversationListAdapter(this, listOf(), object : ConversationListAdapter.OnConversationClickListener {
                override fun onConversationClicked(
                    conversationsEntity: ConversationsEntity,
                    position: Int
                ) {

                }

                override fun onConversationLongClicked(conversationsEntity: ConversationsEntity) {

                }

                override fun onAvatarClicked(
                    conversationsEntity: ConversationsEntity,
                    position: Int
                ) {

                }

                override fun onCreateGroup() {

                }

            })
        messagesList.layoutManager = LinearLayoutManager(this)
        messagesList.adapter = messagesListAdapter
//        lifecycleScope.launch {
//            viewModel.getConversationFlow().collectLatest {
//                messagesListAdapter.submitData(it)
//            }
//        }
//        lifecycleScope.launch {
//            messagesListAdapter.loadStateFlow.distinctUntilChangedBy {
//                it.refresh
//            }.collect {
//                if (it.append.endOfPaginationReached || it.prepend.endOfPaginationReached) {
//                    messagesList.scrollToPosition(0)
//                }
//                val list = messagesListAdapter.snapshot()
//                if (list.items.isEmpty()) {
//                    messagesList.isVisible = false
//                    messageEmpty.isVisible = true
//                } else {
//                    messagesList.isVisible = true
//                    messageEmpty.isVisible = false
//                }
//            }
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
//        R.id.connect -> {
//            MarsSocket.initSocket(true)
//            true
//        }
//        R.id.disconnect -> {
//            MarsSocket.releaseSocket()
//            true
//        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun initViewModel(): ConversationListViewModel {
        return getActivityViewModel(ConversationListViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (BuildConfig.DEBUG) {
//            menuInflater.inflate(R.menu.menu_message_debug, menu)
        }
        return true
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.activity_conversation_list,
            viewModel
        )
    }
}