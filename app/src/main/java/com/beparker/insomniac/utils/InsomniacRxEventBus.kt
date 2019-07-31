package com.beparker.insomniac

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

val rxEventBus = InsomniacRxEventBus()

class InsomniacRxEventBus {

    private val disposableMap: MutableMap<InsomniacEventConsumer, Disposable> = ConcurrentHashMap()
    private val publishSubject = PublishSubject.create<InsomniacEvent>()

    fun post(event: InsomniacEvent) {
        if (publishSubject.hasObservers()) {
            publishSubject.onNext(event)
        }
    }

    @Synchronized
    fun subscribe(subscribeOn: Scheduler, observeOn: Scheduler, consumer: InsomniacEventConsumer) {
        if (subscribed(consumer)) {
            disposableMap[consumer]
        } else if (!subscribed(consumer)) {
            val disposable = publishSubject
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe {consumer.accept(it)}

            disposableMap[consumer] = disposable
        }
    }

    fun subscribed(consumer: InsomniacEventConsumer) : Boolean = disposableMap.containsKey(consumer)

    fun unsubscribe(onNext: InsomniacEventConsumer) {
        subscribed(onNext).apply {
            disposableMap.remove(onNext).let { it?.dispose() }
        }
    }

    fun hasActiveSubject() = disposableMap.isNotEmpty()
}

interface InsomniacEvent

interface InsomniacEventConsumer : Consumer<InsomniacEvent> {
    override fun accept(v: InsomniacEvent) {}
}