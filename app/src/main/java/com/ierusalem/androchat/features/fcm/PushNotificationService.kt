package com.ierusalem.androchat.features.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        /**
         * For authentication you should take new token, push it to server database, and then
         * you can reference to corresponding user account
         */
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        //Respond to received messages with custom notification if you want
    }

}