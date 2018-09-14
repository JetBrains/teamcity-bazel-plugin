package bazel

import devteam.rx.Observable

interface BindableEventService<TEvent>: io.grpc.BindableService, Observable<Event<TEvent>>