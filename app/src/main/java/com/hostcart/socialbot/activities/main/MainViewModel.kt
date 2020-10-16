package com.hostcart.socialbot.activities.main

import androidx.lifecycle.MutableLiveData
import com.hostcart.socialbot.common.ScopedViewModel
import com.hostcart.socialbot.common.extensions.toDeffered
import com.hostcart.socialbot.job.DeleteStatusJob
import com.hostcart.socialbot.model.TextStatus
import com.hostcart.socialbot.model.constants.StatusType
import com.hostcart.socialbot.model.realms.Status
import com.hostcart.socialbot.model.realms.User
import com.hostcart.socialbot.utils.FireConstants
import com.hostcart.socialbot.utils.RealmHelper
import com.hostcart.socialbot.utils.TimeHelper
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainViewModel(uiContext: CoroutineContext) : ScopedViewModel(uiContext) {

    private val realmHelper = RealmHelper.getInstance()
    val statusLiveData = MutableLiveData<Unit>()
    var lastSyncTime = 0L

    companion object {
        const val WAIT_TIME = 15000
    }

    fun fetchStatuses(users: List<User>) {
        if (lastSyncTime == 0L || System.currentTimeMillis() - lastSyncTime > WAIT_TIME) {
            launch {
                try {
                    val statusesIds = mutableListOf<String>()
                    fetchImageAndVideosStatuses(users, statusesIds)
                    fetchTextStatuses(users, statusesIds)
                    realmHelper.deleteDeletedStatusesLocally(statusesIds)
                    lastSyncTime = System.currentTimeMillis()
                    updateUi()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateUi() {
        statusLiveData.value = Unit
    }

    private fun handleStatus(dataSnapshot: DataSnapshot, statusesIds: MutableList<String>) {
        if (dataSnapshot.value != null) {
            //get every status
            for (snapshot in dataSnapshot.children) {
                val userId = snapshot.ref.parent!!.key
                val statusId = snapshot.key
                val status = snapshot.getValue(Status::class.java)
                status!!.statusId = statusId
                status.userId = userId

                if (status.type == StatusType.TEXT) {
                    val textStatus = snapshot.getValue(TextStatus::class.java)
                    textStatus!!.statusId = statusId!!
                    status.textStatus = textStatus
                }

                statusesIds.add(statusId!!)
                //check if status is exists in local database , if not save it
                if (realmHelper.getStatus(status.statusId) == null) {
                    realmHelper.saveStatus(userId, status)
                    //schedule a job after 24 hours to delete this status locally
                    DeleteStatusJob.schedule(userId, statusId)
                }
            }
        }
    }

    private suspend fun fetchImageAndVideosStatuses(users: List<User>, statusesIds: MutableList<String>) {
        val timeBefore24Hours = TimeHelper.getTimeBefore24Hours()
        val jobs = mutableListOf<Deferred<DataSnapshot>>()
        val job = async {
            for (user in users) {
                if (user.uid == null) {
                    continue
                }
                val query = FireConstants.statusRef.child(user.uid)
                        .orderByChild("timestamp")
                        .startAt(timeBefore24Hours.toDouble())
                val dataSnapshot = query.toDeffered()
                jobs.add(dataSnapshot)
            }
        }

        job.await()
        val datasnapshots = jobs.awaitAll()
        datasnapshots.forEach {
            handleStatus(it, statusesIds)
        }
    }

    private suspend fun fetchTextStatuses(users: List<User>, statusesIds: MutableList<String>) {
        val timeBefore24Hours = TimeHelper.getTimeBefore24Hours()
        val jobs = mutableListOf<Deferred<DataSnapshot>>()
        val job = async {
//            for (user in users) {
//                val query = FireConstants.textStatusRef.child(user.uid)
//                        .orderByChild("timestamp")
//                        .startAt(timeBefore24Hours.toDouble())
//
//                val dataSnapshot = query.toDeffered()
//                jobs.add(dataSnapshot)
//            }
        }

        job.await()
        val datasnapshots = jobs.awaitAll()
        datasnapshots.forEach {
            handleStatus(it, statusesIds)
        }
    }

}


