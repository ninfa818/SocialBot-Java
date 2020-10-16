package com.hostcart.socialbot.activities.main

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.droidninja.imageeditengine.ImageEditor
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.hostcart.socialbot.R
import com.hostcart.socialbot.activities.*
import com.hostcart.socialbot.activities.settings.SettingsActivity
import com.hostcart.socialbot.adapters.ChatsAdapter
import com.hostcart.socialbot.adapters.ViewPagerAdapter
import com.hostcart.socialbot.chatbot.chatavtivity.Chat_Activity
import com.hostcart.socialbot.fragments.*
import com.hostcart.socialbot.interfaces.FragmentCallback
import com.hostcart.socialbot.interfaces.StatusFragmentCallbacks
import com.hostcart.socialbot.job.DailyBackupJob
import com.hostcart.socialbot.job.SaveTokenJob
import com.hostcart.socialbot.job.SyncContactsDailyJob
import com.hostcart.socialbot.model.Post
import com.hostcart.socialbot.model.TextStatus
import com.hostcart.socialbot.model.constants.GroupEventTypes
import com.hostcart.socialbot.model.realms.Chat
import com.hostcart.socialbot.model.realms.CurrentUserInfo
import com.hostcart.socialbot.model.realms.GroupEvent
import com.hostcart.socialbot.model.realms.User
import com.hostcart.socialbot.services.CallingService
import com.hostcart.socialbot.services.FCMRegistrationService
import com.hostcart.socialbot.services.InternetConnectedListener
import com.hostcart.socialbot.services.NetworkService
import com.hostcart.socialbot.utils.*
import com.hostcart.socialbot.vmeet.VmeetMainActivity
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MainActivity : BaseActivity(), FabRotationAnimation.RotateAnimationListener
        , FragmentCallback, StatusFragmentCallbacks
        , PostFragment.OnListFragmentInteractionListener{
    var isInActionMode = false
    private var isInSearchMode = false

    private var fab: FloatingActionButton? = null
    private var textStatusFab: FloatingActionButton? = null

    private var toolbar: Toolbar? = null
    private var tvSelectedChatCount: TextView? = null
    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null

    private var users: RealmResults<User>? = null
    private var fireListener: FireListener? = null
    private var adapter: ViewPagerAdapter? = null
    private lateinit var rotationAnimation: FabRotationAnimation
    private var root: CoordinatorLayout? = null

    private var currentPage = 0

    private lateinit var viewModel: MainViewModel
    private var tabs = ArrayList<String>()

    private val postFragment = PostFragment()
    private val isHasMutedItem: Boolean
        get() {
            val selectedItems = selectedItems ?: return false
            for (chat in selectedItems) {
                if (chat.isMuted)
                    return true
            }
            return false
        }

    private val isHasGroupItem: Boolean
        get() {
            val selectedItems = selectedItems ?: return false

            for (chat in selectedItems) {
                val user = chat.user
                if (user.isGroupBool && user.group.isActive)
                    return true
            }
            return false
        }

    private val selectedItems: List<Chat>?
        get() = if (getAdapter() == null) {
            null
        } else getAdapter()!!.selectedChatForActionMode

    override fun enablePresence(): Boolean {
        return true
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            setContentView(R.layout.activity_main_dark)
            val window = getWindow()
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew))
            getWindow().navigationBarColor = ContextCompat.getColor(this, R.color.colorNew)
        } else {
            setContentView(R.layout.activity_main_light)
            val window = getWindow()
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite))
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val userInfoManager = UserInfoManager()
        userInfoManager.loadAddedMeList()

        init()

        viewModel = ViewModelProviders.of(this, MainViewModelFactory()).get(MainViewModel::class.java)

        setSupportActionBar(toolbar)

        rotationAnimation = FabRotationAnimation(this)

        fireListener = FireListener()
        startServices()

        users = RealmHelper.getInstance().listOfUsers

        fab!!.setOnClickListener {
            AppUtils.gEditPost = false
            val i = Intent(this@MainActivity, NewPostActivity::class.java)
            when (currentPage) {
                1 -> startActivity(Intent(this@MainActivity, NewChatActivity::class.java))
                2 -> startActivity(Intent(this@MainActivity, NewCallActivity::class.java))
                0 -> startActivityForResult(i, 5555)
            }
        }

        textStatusFab!!.setOnClickListener { startActivityForResult(Intent(this, TextStatusActivity::class.java), REQUEST_CODE_TEXT_STATUS) }

        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            //onSwipe or tab change
            override fun onPageSelected(position: Int) {
                val prevValue = tabs.get(tabs.size-1).toInt()
                if( !prevValue.equals(position) )
                    tabs.add(position.toString())

                currentPage = position
                if (isInSearchMode)
                    exitSearchMode()
                if (isInActionMode)
                    exitActionMode()

                when (position) {
                    1 -> {
                        fab!!.show()
                        val fragment = adapter!!.getItem(1) as BaseFragment
                        addMarginToFab(fragment.isVisible && fragment.isAdShowing)
                        animateFab(R.drawable.ic_chat_white)
                    }
                    3 -> { // status camera
//                        fab!!.hide()
                    }

                    2 -> {
                        fab!!.show()
                        val fragment = adapter!!.getItem(2) as BaseFragment
                        addMarginToFab(fragment.isVisible && fragment.isAdShowing)
                        animateFab(R.drawable.ic_phone)
                    }
                    1 -> {
//                        fab!!.hide()
                    }
                    0 -> {
                        fab!!.show()
                        val fragment = adapter!!.getItem(0) as BaseFragment
                        addMarginToFab(fragment.isVisible && fragment.isAdShowing)
                        animateFab(R.drawable.post_icon)
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) { }
        })

        //revert status fab to starting position
        textStatusFab!!.addOnHideAnimationListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                textStatusFab!!.animate().y(fab!!.y).start()

            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        //save app ver if it's not saved before
        if (!SharedPreferencesManager.isAppVersionSaved()) {
            FireConstants.usersRef.child(FireManager.getUid()!!).child("ver").setValue(AppVerUtil.getAppVersion(this)).addOnSuccessListener { SharedPreferencesManager.setAppVersionSaved(true) }
        }


        //start sinch client for the first time to save the id and start receiving calls
        if (!SharedPreferencesManager.isSinchConfigured()) {
            val serviceIntent = Intent(this, CallingService::class.java)
            serviceIntent.putExtra(IntentUtils.START_SINCH, true)
            startService(serviceIntent)
        }

        if (!SharedPreferencesManager.isCurrentUserInfoSaved()) {
            RealmHelper.getInstance().saveObjectToRealm(CurrentUserInfo(FireManager.getUid(), FireManager.getPhoneNumber()))
            SharedPreferencesManager.setCurrentUserInfoSaved(true)
        }

//        if (Build.VERSION.SDK_INT > VERSION_CODES.LOLLIPOP_MR1) {
//            val pkg = packageName
//            val pm = getSystemService(PowerManager::class.java)
//            if (!pm.isIgnoringBatteryOptimizations(pkg) && !SharedPreferencesManager.isDoNotShowBatteryOptimizationAgain()) {
//                showBatteryOptimizationDialog()
//            }
//        }
    }

//    private fun showBatteryOptimizationDialog() {
//        val dialog = IgnoreBatteryDialog(this)
//        dialog.setOnDialogClickListener(object : IgnoreBatteryDialog.OnDialogClickListener {
//            override fun onCancelClick(checkBoxChecked: Boolean) {
//                SharedPreferencesManager.setDoNotShowBatteryOptimizationAgain(checkBoxChecked)
//            }
//
//            override fun onOk() {
//                val intent = Intent()
//                intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
//                startActivity(intent)
//            }
//        })
//        dialog.show()
//    }


    //start CameraActivity
    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(IntentUtils.CAMERA_VIEW_SHOW_PICK_IMAGE_BUTTON, true)
        intent.putExtra(IntentUtils.IS_STATUS, true)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    //animate FAB with rotation animation
    @SuppressLint("RestrictedApi")
    private fun animateFab(drawable: Int) {
        val animation = rotationAnimation.start(drawable)
        fab!!.startAnimation(animation)
    }

    private fun animateTextStatusFab(show: Boolean) {
        if (show) {
            textStatusFab!!.show()
            textStatusFab!!.animate().y(fab!!.top - DpUtil.toPixel(70f, this)).start()
        } else {
            textStatusFab!!.hide()
            textStatusFab!!.layoutParams = fab!!.layoutParams
        }
    }

    override fun fetchStatuses() {
        users?.let {
            viewModel.fetchStatuses(it)
        }
    }

    private fun startServices() {
        if (!Util.isOreoOrAbove()) {
            startService(Intent(this, NetworkService::class.java))
            startService(Intent(this, InternetConnectedListener::class.java))
            startService(Intent(this, FCMRegistrationService::class.java))
        } else {
            if (!SharedPreferencesManager.isTokenSaved())
                SaveTokenJob.schedule(this, null)

//            SetLastSeenJob.schedule(this)
            UnProcessedJobs.process(this)
        }
        //sync contacts for the first time
        if (!SharedPreferencesManager.isContactSynced()) {
            ServiceHelper.startSyncContacts(this@MainActivity)
        }
        //schedule daily job to sync contacts
        SyncContactsDailyJob.schedule()
        //schedule daily job to backup messages
        DailyBackupJob.schedule()
    }

    private fun init() {
        fab = findViewById(R.id.open_new_chat_fab)
        toolbar = findViewById(R.id.toolbar)
        tvSelectedChatCount = findViewById(R.id.tv_selected_chat)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        textStatusFab = findViewById(R.id.text_status_fab)
        root = findViewById(R.id.root)

        tabs.add("0")

        initTabLayout()

        //prefix for a bug in older APIs
        fab!!.bringToFront()
    }

    private fun initTabLayout() {
        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter!!.addFragment(postFragment)
        adapter!!.addFragment(FragmentChats())
        adapter!!.addFragment(CallsFragment())
        adapter!!.addFragment(MoreFragment())

        viewPager!!.adapter = adapter
        viewPager!!.offscreenPageLimit = 1
        tabLayout!!.setupWithViewPager(viewPager)

        initListView()

        setUnreadCount()
        setupTabIcons()

        startTimer()
    }

    private fun startTimer() {
        Timer("schedule", true).schedule(2000, 300) {
            setUnreadCount()
            setupTabIcons()
        }
    }

    private fun setUnreadCount(): Int {
        val count = RealmHelper.getInstance().unreadMessagesCount
        unreadCount[1] = count.toInt()
        return count.toInt()
    }

    internal var tabTitle = arrayOf("Wall", "Chats", "Calls", "More")
    internal var unreadCount = intArrayOf(0, 0, 0, 0)
    val viewList = ArrayList<View>()

    private fun initListView() {
        var customView : View? = null
        for (i in 0..4) {
            if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
                customView = layoutInflater.inflate(R.layout.custom_tab_dark, null)
            } else {
                customView = layoutInflater.inflate(R.layout.custom_tab_light, null)
            }

            viewList.add(customView)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareTabView(pos: Int): View {
        var customView : View? = null
        var tv_title : TextView? = null
        var tv_count : TextView? = null

        customView = viewList.get(pos)

        tv_title = customView.findViewById<View>(R.id.tv_title) as TextView?
        tv_count = customView.findViewById<View>(R.id.tv_count) as TextView?

        tv_title!!.setText(tabTitle[pos])
        if (unreadCount[pos] > 0) {
            tv_count!!.visibility = View.VISIBLE
            tv_count.text = "" + unreadCount[pos]
        } else
            tv_count!!.visibility = View.GONE

        return customView
    }

    private fun setupTabIcons() {
        for (i in tabTitle.indices) {
            this@MainActivity.runOnUiThread({
                tabLayout!!.getTabAt(i)!!.setCustomView(prepareTabView(i))
            })
        }
    }

    override fun onPause() {
        super.onPause()
        fireListener!!.cleanup()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            menuInflater.inflate(R.menu.menu_main_dark, menu)
        } else {
            menuInflater.inflate(R.menu.menu_main_light, menu)
        }
        val menuItem = menu.findItem(R.id.search_item)

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }
            //exit search mode on searchClosed
            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                exitSearchMode()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_delete -> deleteItemClicked()
            R.id.menu_item_mute -> muteItemClicked()
            R.id.settings_item -> settingsItemClicked()
            R.id.search_item -> searchItemClicked()
            R.id.new_group_item -> createGroupClicked()
            R.id.go_profile_item -> {
                val intent = Intent(this@MainActivity, MyProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.exit_group_item -> exitGroupClicked()
            R.id.invite_item -> startActivity(IntentUtils.getShareAppIntent(this@MainActivity))
            R.id.new_broadcast_item -> {
                val intent = Intent(this@MainActivity, NewGroupActivity::class.java)
                intent.putExtra(IntentUtils.IS_BROADCAST, true)
                startActivity(intent)
            }
            R.id.meet_item -> {
                val intent = Intent(this@MainActivity, VmeetMainActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun exitGroupClicked() {
        if (!NetworkHelper.isConnected(this))
            return

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.confirmation)
                .setMessage(R.string.exit_group)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { _, _ ->
                    val selectedItems = selectedItems ?: return@OnClickListener
                    for (chat in selectedItems) {
                        GroupManager.exitGroup(chat.chatId, FireManager.getUid()) {
                            RealmHelper.getInstance().exitGroup(chat.chatId)
                            val groupEvent = GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.USER_LEFT_GROUP, null)
                            groupEvent.createGroupEvent(chat.user, null)
                        }
                    }
                    exitActionMode()
                })
                .show()
    }

    private fun createGroupClicked() {
        startActivity(Intent(this, NewGroupActivity::class.java))
    }

    private fun searchItemClicked() {
        startActivity(Intent(this, Chat_Activity::class.java))
    }

    private fun updateMutedIcon(menuItem: MenuItem?, isMuted: Boolean) {
        menuItem?.setIcon(if (isMuted) R.drawable.ic_volume_up else R.drawable.ic_volume_off)
    }

    private fun settingsItemClicked() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun muteItemClicked() {
        val selectedItems = selectedItems ?: return
        for (chat in selectedItems) {
            if (chat.isMuted) {
                RealmHelper.getInstance().setMuted(chat.chatId, false)
            } else {
                RealmHelper.getInstance().setMuted(chat.chatId, true)
            }
        }
        exitActionMode()
    }

    private fun deleteItemClicked() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.confirmation)
                .setMessage(R.string.delete_conversation_confirmation)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { _, _ ->
                    val selectedItems = selectedItems ?: return@OnClickListener
                    for (chat in selectedItems) {
                        RealmHelper.getInstance().deleteChat(chat.chatId)
                    }
                    exitActionMode()
                })
                .show()
    }


    @SuppressLint("SetTextI18n")
    fun addItemToActionMode(itemsCount: Int) {
        tvSelectedChatCount!!.text = itemsCount.toString() + ""

        if (itemsCount > 1) {
            if (isHasMutedItem)
                setMenuItemVisibility(false)
            else
                updateMutedIcon(toolbar!!.menu.findItem(R.id.menu_item_mute), false)//if there is no muted item then the user may select multiple chats and mute them all in once
        } else if (itemsCount == 1) {
            val adapter = getAdapter()
            if (adapter != null) {
                val isMuted = adapter.selectedChatForActionMode[0].isMuted
                //in case if it's hidden before
                setMenuItemVisibility(true)
                updateMutedIcon(toolbar!!.menu.findItem(R.id.menu_item_mute), isMuted)
            }
        }
        updateGroupItems()
    }

    private fun setMenuItemVisibility(b: Boolean) {
        if (toolbar!!.menu != null && toolbar!!.menu.findItem(R.id.menu_item_mute) != null)
            toolbar!!.menu.findItem(R.id.menu_item_mute).isVisible = b
    }

    private fun areAllOfChatsGroups(): Boolean {
        var b = false

        val selectedItems = selectedItems ?: return false
        for (chat in selectedItems) {
            val user = chat.user
            if (user.isGroupBool && user.group.isActive)
                b = true
            else {
                return false
            }
        }
        return b
    }


    fun onActionModeStarted() {
        if (isInSearchMode)
            exitSearchMode()

        if (!isInActionMode) {
            toolbar!!.menu.clear()
            if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                toolbar!!.inflateMenu(R.menu.menu_action_chat_list_dark)
            } else {
                toolbar!!.inflateMenu(R.menu.menu_action_chat_list_light)
            }
        }

        val selectedItems = selectedItems
        if (selectedItems != null) {
            updateMutedIcon(toolbar!!.menu.findItem(R.id.menu_item_mute), selectedItems[0].isMuted)
        }

        updateGroupItems()

        isInActionMode = true
        tvSelectedChatCount!!.visibility = View.VISIBLE
    }

    private fun updateGroupItems() {
        val deleteItem = toolbar!!.menu.findItem(R.id.menu_item_delete)
        if (deleteItem != null) {
            if (isHasGroupItem) {
                toolbar!!.menu.findItem(R.id.menu_item_delete).isVisible = false
                toolbar!!.menu.findItem(R.id.exit_group_item).isVisible = areAllOfChatsGroups()
            } else {
                toolbar!!.menu.findItem(R.id.menu_item_delete).isVisible = true
            }
        }
    }

    fun exitActionMode() {
        if (getAdapter() != null)
            getAdapter()!!.exitActionMode()
        isInActionMode = false
        tvSelectedChatCount!!.visibility = View.GONE
        toolbar!!.menu.clear()
        if (SharedPreferencesManager.getThemeMode() == AppUtils.THEME_DARK) {
            toolbar!!.inflateMenu(R.menu.menu_main_dark)
        } else {
            toolbar!!.inflateMenu(R.menu.menu_main_light)
        }
        invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        if (isInActionMode)
            exitActionMode()
        else if (isInSearchMode)
            exitSearchMode()
        else {
            val callsFragment = adapter!!.getItem(2) as CallsFragment
            if (!callsFragment.isActionModeNull)
                callsFragment.exitActionMode()
            else {
                if(tabs.count() > 1) {
                    tabs.remove(tabs[tabs.size-1])
                    viewPager!!.setCurrentItem(tabs.get(tabs.size-1).toInt())
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    //start user profile (Dialog-Like Activity)
    fun userProfileClicked(user: User) {
        val intent = Intent(this, ProfilePhotoDialog::class.java)
        intent.putExtra(IntentUtils.UID, user.uid)
        startActivity(intent)
    }

    fun exitSearchMode() {
        isInSearchMode = false
        val baseFragment = adapter?.getItem(currentPage) as? BaseFragment
        baseFragment?.onSearchClose()
    }

    private fun getAdapter(): ChatsAdapter? {
        if (adapter == null) return null
        val fragmentChats = adapter!!.getItem(1) as FragmentChats
        return fragmentChats.adapter
    }

    override fun onRotationAnimationEnd(drawable: Int) {
        fab!!.setImageResource(drawable)
        animateTextStatusFab(drawable == R.drawable.ic_photo_camera)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        val statusFragment = adapter?.getItem(4) as StatusFragment
        val postFragment = adapter?.getItem(0) as PostFragment

        if (requestCode == CAMERA_REQUEST) {
            postFragment?.onCameraActivityResult(resultCode, data)
//            statusFragment?.onCameraActivityResult(resultCode, data)
        } else if (requestCode == ImageEditor.RC_IMAGE_EDITOR && resultCode == Activity.RESULT_OK) {
            val imagePath: String = data!!.getStringExtra(ImageEditor.EXTRA_EDITED_PATH)
            postFragment?.onImageEditSuccess(imagePath)
//            statusFragment?.onImageEditSuccess(imagePath)
        } else if (requestCode == REQUEST_CODE_TEXT_STATUS && resultCode == Activity.RESULT_OK) {
            val textStatus = data!!.getParcelableExtra<TextStatus>(IntentUtils.EXTRA_TEXT_STATUS)
            postFragment?.onTextStatusResult(textStatus)
//            statusFragment?.onTextStatusResult(textStatus)
        } else if( requestCode == 5555 && resultCode == 1123 ) {
            postFragment?.setNewPost(data)
        }
    }


    override fun addMarginToFab(isAdShowing: Boolean) {
        val layoutParams = fab!!.layoutParams as CoordinatorLayout.LayoutParams
        val v = if (isAdShowing) DpUtil.toPixel(95f, this) else resources.getDimensionPixelSize(R.dimen.fab_margin).toFloat()

        layoutParams.bottomMargin = v.toInt()

        fab!!.layoutParams = layoutParams
        fab?.clearAnimation()
        fab?.animation?.cancel()

        animateTextStatusFab(isAdShowing)
    }

    override fun openCamera() {
        startCamera()
    }

    override fun openTextStatus() {
        startActivityForResult(Intent(this, TextStatusActivity::class.java), REQUEST_CODE_TEXT_STATUS)
    }

    override fun startTheActionMode(callback: ActionMode.Callback) {
        startActionMode(callback)
    }

    companion object {
        private const val CAMERA_REQUEST = 9514
        private const val REQUEST_CODE_TEXT_STATUS = 9145
    }

    override fun onListFragmentInteraction(item: Post?) {
    }

}


