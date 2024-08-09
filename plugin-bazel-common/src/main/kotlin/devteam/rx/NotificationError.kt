package devteam.rx

data class NotificationError<T>(val error: Exception) : Notification<T>(NotificationKind.OnError)