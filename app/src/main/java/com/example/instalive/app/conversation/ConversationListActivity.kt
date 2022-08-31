package com.example.instalive.app.conversation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.views.BaseActivity
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.BuildConfig
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.databinding.ActivityConversationListBinding
import com.venus.dm.db.entity.ConversationsEntity
import kotlinx.android.synthetic.main.activity_conversation_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.activities.start
import timber.log.Timber

@ExperimentalStdlibApi
class ConversationListActivity : BaseActivity<ConversationListViewModel, ActivityConversationListBinding>() {

    private lateinit var messagesListAdapter: ConversationListAdapter

    private var isFirstRefresh: Boolean = true

    override fun initData(savedInstanceState: Bundle?) {
        BarUtils.setStatusBarLightMode(this, false)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.fb_direct_messages)
        viewModel.getConversationList()
        initList()
    }

    private fun initList() {
        messagesListAdapter =
            ConversationListAdapter(this, listOf(), object : ConversationListAdapter.OnConversationClickListener {
                override fun onConversationClicked(
                    conversationsEntity: ConversationsEntity,
                    position: Int
                ) {
                    RecentConversation.conversationsEntity = conversationsEntity
                    start<MessageActivity> {
//                        this.putExtra(Constants.EXTRA_CONVERSATION_ENTITY, conversationsEntity)
                    }
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
        messagesList.itemAnimator = null
        messagesList.setHasFixedSize(true)
        messagesList.adapter = messagesListAdapter
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.conversationsFlow.distinctUntilChanged().collectLatest {
                delay(1000)
                Timber.d("collectLatest")
                val oldList = messagesListAdapter.conversationList
                val newList = it
                val result = DiffUtil.calculateDiff(
                    ConversationListComparator(
                        newList,
                        oldList
                    ), false
                )
                withContext(Dispatchers.Main) {
                    messagesListAdapter.conversationList = it
                    result.dispatchUpdatesTo(messagesListAdapter)
                }
                delay(300)
                withContext(Dispatchers.Main) {
                    if (isFirstRefresh && messagesListAdapter.itemCount > 1) {
                        isFirstRefresh = false
                        messagesList.scrollToPosition(0)
                    }
                }
            }
        }
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