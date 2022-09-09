package com.example.instalive.app.conversation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.BarUtils
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.BuildConfig
import com.example.instalive.R
import com.example.instalive.app.base.InstaBaseActivity
import com.example.instalive.databinding.ActivityConversationListBinding
import com.example.instalive.utils.marsToast
import com.venus.dm.db.entity.ConversationsEntity
import kotlinx.android.synthetic.main.activity_conversation_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.activities.start
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.positiveButton
import splitties.alertdialog.appcompat.titleResource
import timber.log.Timber

@ExperimentalStdlibApi
class ConversationListActivity : InstaBaseActivity<ConversationListViewModel, ActivityConversationListBinding>() {

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
        viewModel.errorInfo.observe(this, {
            marsToast(it.second.toString())
        })
        viewModel.loadingStatsLiveData.observe(this, {
            loadingAnimContainer?.isVisible = it == StatusEvent.LOADING
        })
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
                    conversationLongClick(conversationsEntity)
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

    private fun conversationLongClick(conversationsEntity: ConversationsEntity) {
        val builder = AlertDialog.Builder(this@ConversationListActivity)
        val adapter =
            ArrayAdapter<String>(
                this@ConversationListActivity,
                android.R.layout.simple_list_item_1
            )
        adapter.add(
            if (conversationsEntity.isPin == 0) getString(R.string.fb_pin) else getString(
                R.string.fb_unpin
            )
        )
        adapter.add(
            if (conversationsEntity.mute == 0) getString(R.string.fb_mute) else getString(
                R.string.fb_unmute
            )
        )
        adapter.add(getString(R.string.fb_delete))
        adapter.add(getString(R.string.fb_cancel))
        builder.setAdapter(adapter) { dialog, witch ->
            Timber.d("witch:$witch")
            when (witch) {
                0 -> {
                    if (conversationsEntity.isPin == 0) {
                        viewModel.pinConversation(
                            conversationsEntity.conversationId,
                            result = {})
                    } else {
                        viewModel.unpinConversation(
                            conversationsEntity.conversationId,
                            result = {})
                    }
                }
                1 -> {
                    if (conversationsEntity.mute == 0) {
                        viewModel.muteOrUnmute(conversationsEntity.conversationId, 1, {

                        }, null)
                    } else {
                        viewModel.muteOrUnmute(conversationsEntity.conversationId, 0, null, {

                        })
                    }
                }
                2 -> {
                    alertDialog {
                        titleResource = R.string.title_delete_conversation
                        cancelButton()
                        positiveButton(R.string.delete) {
                            if (conversationsEntity.isPin == 1) {
                                viewModel.unpinConversation(
                                    conversationsEntity.conversationId,
                                    result = {
                                        deleteConversation(conversationsEntity.conversationId)
                                        if (messagesListAdapter.currentID == conversationsEntity.conversationId) {
                                            messagesListAdapter.currentID = ""
                                        }
                                    })
                            } else {
                                deleteConversation(conversationsEntity.conversationId)
                                if (messagesListAdapter.currentID == conversationsEntity.conversationId) {
                                    messagesListAdapter.currentID = ""
                                }
                            }
                        }
                    }.show()
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deleteConversation(conId: String) {
        viewModel.deleteConversation(conId, false)
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