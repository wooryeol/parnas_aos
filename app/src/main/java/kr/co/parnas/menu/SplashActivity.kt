package kr.co.parnas.menu

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.*
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import kr.co.parnas.R
import kr.co.parnas.common.Define
import kr.co.parnas.common.PopupFactory
import kr.co.parnas.common.SharedData
import kr.co.parnas.net.ApiClientService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    lateinit var mContext: Context
    //최초 진입인지 여부
    //최초 진입일땐 권한 체크를 하지 않고 설정 페이지에서 돌아올때 체크하기 위해
    var isFirst = true
    //백그라운드 푸시 받았을때 대응
    var mUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_splash)

        mContext = this

        //백그라운드에서 푸시오는 경우
        val bundle = intent.extras
        if(bundle != null){
            mUrl = bundle.getString("Em_Index")
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            SharedData.setSharedData(mContext, SharedData.DEVICE_TOKEN, task.result)
            askNotificationPermission()
        }

        //26 오레오 이상 채널등록
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(getString(R.string.channel_id))
            //채널이 null일때만 생성
            if (channel == null)
                oreoSetChannel()
        }
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            requestAppInfo()
        } else {
            confirmPlease()
        }
    }

    private fun askNotificationPermission() {
        // 알림 권한을 확인
        val notificationManager = NotificationManagerCompat.from(mContext)
        // Android 13 이상에서 런타임 알림 권한 요청
        if (Build.VERSION.SDK_INT >= 33) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (notificationManager.areNotificationsEnabled()) {
                    requestAppInfo()
                    // 퍼미션 허용 되어있음
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    //사용자가 권한을 거부했던 경우
                    confirmPlease()
                } else {
                    // 권한을 직접 요청
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }else{
            requestAppInfo()
        }
    }

    private fun confirmPlease(){
        PopupFactory.drawSystemPopup(this, "알림설정을 하지 않으면 앱을 사용할 수 없습니다.\n설정창으로 가시겠습니까?", "설정으로 이동", "앱 종료",
            object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    if (msg.what == Define.EVENT_OK) {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationSettingOreo(mContext)
                        } else {
                            notificationSettingOreoLess(mContext)
                        }
                        try {
                            isFirst = false
                            startActivity(intent)
                        }catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                        }
                    }else{
                        finish()
                    }
                }
            })
    }

    //오레오 채널 생성
    private fun oreoSetChannel() {
        //26 오레오 이상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = getString(R.string.channel_id)

            //알림 채널
            val notiChannel = NotificationChannel(
                channel,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notiChannel.enableLights(true)
            notiChannel.lightColor = Color.BLUE
            notiChannel.enableVibration(true)
            notiChannel.description = getString(R.string.channel_desc)
            notiChannel.name = getString(R.string.channel_name)
            val notiUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            notiChannel.setSound(notiUri, audioAttributes)
            notiChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notiChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notiChannel)
        }
    }

    private fun nextPage(){
        val handler = Handler(Looper.getMainLooper())
        val r = Runnable {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            mUrl.let {
                intent.putExtra("index", it)
            }
            startActivity(intent)
        }
        handler.postDelayed(r, 1000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun notificationSettingOreo(context: Context): Intent {
        return Intent().also { intent ->
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun notificationSettingOreoLess(context: Context): Intent {
        return Intent().also { intent ->
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo?.uid)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    override fun onResume() {
        super.onResume()

        if(!isFirst) {
            val notificationManager = NotificationManagerCompat.from(mContext)
            if (notificationManager.areNotificationsEnabled()) {
                requestAppInfo()
                // 퍼미션 허용 되어있음
            } else {
                askNotificationPermission()
            }
        }
    }

    /*
	 * 앱 정보 보내기
	 */
    private fun requestAppInfo() {
        val token = SharedData.getSharedData(mContext, SharedData.DEVICE_TOKEN, "")
        val push = SharedData.getSharedData(mContext, SharedData.PUSH_SERVICE, "Y")
        val url = getString(R.string.base_url)
        val service = ApiClientService.retrofitString.create(ApiClientService::class.java)
        val call = service.requestAppInfo(url, token, "A", push)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                nextPage()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                nextPage()
            }
        })
    }
}