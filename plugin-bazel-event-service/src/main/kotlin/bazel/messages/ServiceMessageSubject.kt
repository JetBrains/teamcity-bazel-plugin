package bazel.messages

import bazel.Event
import bazel.events.OrderedBuildEvent
import devteam.rx.Disposable
import devteam.rx.Observable
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

interface ServiceMessageSubject: Observer<Event<OrderedBuildEvent>>, Observable<ServiceMessage>, Disposable