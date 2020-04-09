package ru.maluginp.statesteps.core

import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer

abstract class UseCase<in Input, Output> {

    abstract fun buildObservable(input: Input): Observable<Output>

    fun execute(observer: Observer<Output>, input: Input) {
        buildObservable(input).subscribe(observer)
    }
}