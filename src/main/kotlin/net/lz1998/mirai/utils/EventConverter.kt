package net.lz1998.mirai.utils

import net.lz1998.mirai.alias.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.*

suspend fun BotEvent.toFrame(): BFrame? = when (this) {
    is GroupMessageEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is FriendMessageEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is MemberJoinEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is BotJoinGroupEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is MemberLeaveEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is MemberJoinRequestEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is BotInvitedJoinGroupRequestEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is NewFriendRequestEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    is FriendAddEvent -> this.toProtoMessage().toProtoFrame(this.bot.id)
    else -> null
}

suspend fun GroupMessageEvent.toProtoMessage(): BGroupMessageEvent {
    val sender = BGroupMessageSender.newBuilder()
            .setUserId(this.sender.id)
            .setNickname(this.sender.nick)
            .setCard(this.sender.nameCard)
            .setRole(this.sender.permission.name)
            .setTitle(this.sender.specialTitle)

    // 构造消息链
    val onebotMessage = this.message.toOnebotMessage()

    // 构造rawMessage
    val rawMessage = this.message.toRawMessage()

    // 构造GroupMessageEvent
    return BGroupMessageEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("message")
            .setMessageType("group")
            .setSubType("normal")
            .setMessageId(message.id)
            .setGroupId(group.id)
            .setUserId(this.sender.id)
            .addAllMessage(onebotMessage)
            .setRawMessage(rawMessage)
            .setSender(sender)
            .build()
}

suspend fun FriendMessageEvent.toProtoMessage(): BPrivateMessageEvent {
    val sender = BPrivateMessageSender.newBuilder()
            .setUserId(this.sender.id)
            .setNickname(this.sender.nick)

    // 构造消息链
    val onebotMessage = this.message.toOnebotMessage()

    // 构造rawMessage
    val rawMessage = this.message.toRawMessage()

    // 构造GroupMessageEvent
    return BPrivateMessageEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("message")
            .setMessageType("private")
            .setSubType("normal")
            .setMessageId(message.id)
            .setUserId(this.sender.id)
            .addAllMessage(onebotMessage)
            .setRawMessage(rawMessage)
            .setSender(sender)
            .build()
}

fun MemberJoinEvent.toProtoMessage(): BGroupIncreaseNoticeEvent {
    val subType = when (this) {
        is MemberJoinEvent.Invite -> "invite"
        is MemberJoinEvent.Active -> "approve"
        is MemberJoinEvent.Retrieve -> "retrieve"
    }
    return BGroupIncreaseNoticeEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("notice")
            .setNoticeType("group_increase")
            .setSubType(subType)
            .setGroupId(this.group.id)
            .setUserId(this.member.id)
            .build()
}

fun BotJoinGroupEvent.toProtoMessage(): BGroupIncreaseNoticeEvent {
    val subType = when (this) {
        is BotJoinGroupEvent.Invite -> "invite"
        else -> "approve"
    }
    val operatorId = when (this) {
        is BotJoinGroupEvent.Invite -> this.invitor.id
        else -> 0L
    }
    return BGroupIncreaseNoticeEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("notice")
            .setNoticeType("group_increase")
            .setSubType(subType)
            .setGroupId(this.group.id)
            .setOperatorId(operatorId)
            .setUserId(bot.id)
            .build()
}

fun MemberLeaveEvent.toProtoMessage(): BGroupDecreaseNoticeEvent {
    val operatorId: Long
    val subType = when (this) {
        is MemberLeaveEvent.Kick -> {
            operatorId = this.operator?.id ?: 0
            "kick"
        }
        is MemberLeaveEvent.Quit -> {
            operatorId = this.member.id
            "leave"
        }
    }
    return BGroupDecreaseNoticeEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("notice")
            .setNoticeType("group_decrease")
            .setSubType(subType)
            .setGroupId(this.group.id)
            .setUserId(this.member.id)
            .setOperatorId(operatorId)
            .build()
}

fun MemberJoinRequestEvent.toProtoMessage(): BGroupRequestEvent {
    return BGroupRequestEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("request")
            .setRequestType("group")
            .setSubType("add")
            .setGroupId(this.groupId)
            .setUserId(this.fromId)
            .setComment(this.message)
            .setFlag(this.eventId.toString())
            .build()
}

fun BotInvitedJoinGroupRequestEvent.toProtoMessage(): BGroupRequestEvent {
    return BGroupRequestEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("request")
            .setRequestType("group")
            .setSubType("invite")
            .setGroupId(this.groupId)
            .setUserId(this.invitorId)
            .setFlag(this.eventId.toString())
            .build()
}

fun NewFriendRequestEvent.toProtoMessage(): BFriendRequestEvent {
    return BFriendRequestEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("request")
            .setRequestType("friend")
            .setUserId(this.fromId)
            .setComment(this.message)
            .setFlag(this.eventId.toString())
            .build()
}

fun FriendAddEvent.toProtoMessage(): BFriendAddNoticeEvent {
    return BFriendAddNoticeEvent.newBuilder()
            .setTime(System.currentTimeMillis())
            .setSelfId(bot.id)
            .setPostType("notice")
            .setNoticeType("friend_add")
            .setUserId(this.friend.id)
            .build()
}

fun BGroupMessageEvent.toProtoFrame(botId: Long): BFrame = BFrame.newBuilder().setBotId(botId).setFrameType(BFrameType.GroupMessageEvent).setGroupMessageEvent(this).build()
fun BPrivateMessageEvent.toProtoFrame(botId: Long): BFrame = BFrame.newBuilder().setBotId(botId).setFrameType(BFrameType.PrivateMessageEvent).setPrivateMessageEvent(this).build()
fun BGroupIncreaseNoticeEvent.toProtoFrame(botId: Long): BFrame = BFrame.newBuilder().setBotId(botId).setFrameType(BFrameType.GroupIncreaseNoticeEvent).setGroupIncreaseNoticeEvent(this).build()
fun BGroupDecreaseNoticeEvent.toProtoFrame(botId: Long): BFrame = BFrame.newBuilder().setBotId(botId).setFrameType(BFrameType.GroupDecreaseNoticeEvent).setGroupDecreaseNoticeEvent(this).build()
fun BGroupRequestEvent.toProtoFrame(botId: Long): BFrame = BFrame.newBuilder().setBotId(botId).setFrameType(BFrameType.GroupRequestEvent).setGroupRequestEvent(this).build()
fun BFriendRequestEvent.toProtoFrame(botId: Long): BFrame = BFrame.newBuilder().setBotId(botId).setFrameType(BFrameType.FriendRequestEvent).setFriendRequestEvent(this).build()
fun BFriendAddNoticeEvent.toProtoFrame(botId: Long): BFrame = BFrame.newBuilder().setBotId(botId).setFrameType(BFrameType.FriendAddNoticeEvent).setFriendAddNoticeEvent(this).build()



