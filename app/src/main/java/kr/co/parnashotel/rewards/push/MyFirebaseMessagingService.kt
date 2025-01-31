package kr.co.parnashotel.rewards.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.co.parnashotel.rewards.common.SharedData
import kr.co.parnashotel.rewards.common.Utils

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 토큰 재생성
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data: Map<String?, String?> = remoteMessage.data
        val notification = PushNotification()
        Utils.Log("data ==> : $data")
        notification.generateNotification(applicationContext, data)
    }

    // 토큰 재생성
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SharedData.setSharedData(applicationContext, SharedData.PUSH_TOKEN, token)
    }
}