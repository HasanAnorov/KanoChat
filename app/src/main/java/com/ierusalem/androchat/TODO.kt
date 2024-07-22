package com.ierusalem.androchat

/**
 * 1. Add handling on background
 * 2. Extract online chat features
 * */


/***
 *
 * package uz.safix.chess.data
 *
 * import kotlinx.coroutines.flow.Flow
 *
 * sealed interface Message {
 *
 *     data class TextMessage(
 *         val text: String
 *     ): Message
 *
 *     data class ImageMessage(
 *         val imageUrl: String
 *     ): Message
 *
 *     data class VoiceMessage(
 *         val voiceUrl: String
 *     ): Message
 * }
 *
 * enum class MessageType {
 *     TEXT, IMAGE, VOICE
 * }
 *
 * /**
 *  * chatId - Id used for chat
 *  * users - List of user ids
 *  * message - message attached to the user
 *  *
 *  *
 *  * { userId, List<Message> } = room
 *  *
 *  *
 *  * Table Message {
 *  *  type: MessageType,
 *  *  text: String?,
 *  *  imageUrl: String?,
 *  *  voiceUrl: String?
 *  * }
 *  *
 *  *
 *  *
 *  *
 *  * */
 *
 * data class MessageEntity(
 *     @PrimaryKey(autoGenerate = true) val id: Long = 0L,
 *     val type: MessageType,
 *     val text: String? = null,
 *     val imageUrl: String? = null,
 *     val voiceUrl: String? = null,
 *     val userId: Long,
 *     val chatId: Long,
 * )
 *
 * fun insertMessage(chatId: Long, userId: Long, message: Message) {
 *     val messageEntity = when(message) {
 *         is Message.ImageMessage -> MessageEntity(type = MessageType.IMAGE,  imageUrl = message.imageUrl, userId = userId, chatId = chatId)
 *         is Message.TextMessage -> MessageEntity(type = MessageType.IMAGE,  text = message.text, userId = userId, chatId = chatId)
 *         is Message.VoiceMessage -> MessageEntity(type = MessageType.IMAGE,  voiceUrl = message.voiceUrl, userId = userId, chatId = chatId)
 *     }
 *
 *     db.dao.insertMessage(messageEntity)
 * }
 *
 * fun getUserMessages(userId: Long, chatId: Long): Flow<PagingSource<List<MessageEntity>>> {
 *     SELECT * FROM MESSAGES WHERE USER_ID = :userId and CHAT_ID=:chatId
 *     Paging3
 * }
 * // A -> B => chat
 * // A -> C =>
 *
 * // A -> create   create chatId
 * // B device (A and 😎
 *
 *
 */

//package uz.safix.chess.data
//
//import kotlinx.coroutines.flow.Flow
//
//sealed interface Message {
//
//    data class TextMessage(
//        val text: String
//    ): Message
//
//    data class ImageMessage(
//        val imageUrl: String
//    ): Message
//
//    data class VoiceMessage(
//        val voiceUrl: String
//    ): Message
//}
//
//enum class MessageType {
//    TEXT, IMAGE, VOICE
//}
//
///**
// * chatId - Id used for chat
// * users - List of user ids
// * message - message attached to the user
// *
// *
// * { userId, List<Message> } = room
// *
// *
// * Table Message {
// *  type: MessageType,
// *  text: String?,
// *  imageUrl: String?,
// *  voiceUrl: String?
// * }
// *
// *
// *
// *
// * */
//
//data class MessageEntity(
//    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
//    val type: MessageType,
//    val text: String? = null,
//    val imageUrl: String? = null,
//    val voiceUrl: String? = null,
//    val userId: Long,
//    val chatId: Long,
//)
//
//fun insertMessage(chatId: Long, userId: Long, message: Message) {
//    val messageEntity = when(message) {
//        is Message.ImageMessage -> MessageEntity(type = MessageType.IMAGE,  imageUrl = message.imageUrl, userId = userId, chatId = chatId)
//        is Message.TextMessage -> MessageEntity(type = MessageType.IMAGE,  text = message.text, userId = userId, chatId = chatId)
//        is Message.VoiceMessage -> MessageEntity(type = MessageType.IMAGE,  voiceUrl = message.voiceUrl, userId = userId, chatId = chatId)
//    }
//
//    db.dao.insertMessage(messageEntity)
//}
//
//fun getUserMessages(userId: Long, chatId: Long): Flow<PagingSource<List<MessageEntity>>> {
//    SELECT * FROM MESSAGES WHERE USER_ID = :userId and CHAT_ID=:chatId
//    Paging3
//}
//// A -> B => chat
//// A -> C =>
//
//// A -> create   create chatId
//// B device (A and 😎


