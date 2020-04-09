package ru.maluginp.statesteps.steps

import android.os.Bundle
import android.util.Log
import io.reactivex.Single
import ru.maluginp.statesteps.DetailsFragment
import ru.maluginp.statesteps.MainActivity
import ru.maluginp.statesteps.R

// Handle error
interface Step {
    // async process
    fun begin()
    fun commit(): CommitResult
    fun rollback()
}


//typealias StepReducer = (bundle: Bundle?) -> Bundle

typealias RxJavaCallback = (Bundle?) -> Unit

sealed class CommitResult {
    object Done : CommitResult()
    object NextStep: CommitResult()
}

class RxJavaStep(
    private val parentFlow: Flow,
    private val key: String
) : Step {

    override fun begin() {
        Log.d("Flow", "RxJava $key begin")
        Single.just("test")
            .subscribe { t ->  parentFlow.onAsyncNext(null) }
    }

    override fun commit(): CommitResult {
        Log.d("Flow", "RxJava $key commit")
        return CommitResult.Done
    }

    override fun rollback() {
        Log.d("Flow", "RxJava $key Rollback")
    }
}

interface ScreenStep : Step {
}

class ConditionalStep(
    private val parentFlow: Flow,
    private val initialize: ConditionalStep.() -> Unit
) : Step {
    var activated: Boolean = false
    var nextStep: Step? = null

    fun activate(activate: Boolean, step: Step) {
        if (!activated) {

            if (activate) {
                activated = true
                nextStep = step
            }

        }
    }

    override fun begin() {
        initialize()

        nextStep?.begin()

        parentFlow.next(null)
    }

    override fun commit(): CommitResult {
        return nextStep?.commit() ?: CommitResult.Done
    }

    override fun rollback() {
        nextStep?.rollback()
    }


}


class FlowStep(private val initialize: FlowStep.() -> Unit) : Flow(), Step  {
    init {
        Log.d("Flow", "Initialze Flow Step")
        initialize()
    }

    override fun begin() {

    }

    override fun commit(): CommitResult {
        next()

        val completed = isCompleted

        return if (completed) {
            CommitResult.Done
        } else {
            CommitResult.NextStep
        }
    }

    override fun rollback() {
        steps.map { it.rollback() }
    }

}

class OpenDetailsScreenStep(private val activity: MainActivity, private val key: String) : ScreenStep {
    override fun begin() {
        Log.d("Flow","Begin $key")

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.flContainer, DetailsFragment.newInstance(), "Details")
            .addToBackStack("Details")
            .commitAllowingStateLoss()
    }

    override fun commit(): CommitResult {
        Log.d("Flow","Commit $key")
        return CommitResult.Done
    }

    override fun rollback() {
        Log.d("Flow","Rollback $key")
    }
}

class NothingStep : Step {
    override fun begin() {
    }

    override fun commit(): CommitResult {
        return CommitResult.Done
    }

    override fun rollback() {
    }
}

abstract class Flow {
    protected val state: Bundle = Bundle()
    protected val steps: MutableList<Step> = mutableListOf()
    private var position = 0

    fun Screen(step: ScreenStep) {
        steps.add(step)
    }

    open fun RxJava(key: String) {
        steps.add(RxJavaStep(this, key))
    }

    fun Conditional(block: ConditionalStep.() -> Unit) {
        val step = ConditionalStep(this, block)
        steps.add(step)
    }

    private var currentStep: Step? = null

    fun next(bundle: Bundle? = null) {
        Log.d("Flow", "Flow next($position)")

        if (currentStep != null) {
            val res = currentStep?.commit()

            when (res) {
                is CommitResult.NextStep -> return
                is CommitResult.Done -> position++
            }
        }

        if (position >= steps.size) {
            return
        }

        currentStep = steps[position]
        currentStep?.begin()

        // get current step
        // reduce for state
        // increment up
        // call process

        // handle error?
        // if completed to call onCompleted
    }

    fun prev() {
        if (position <= -1) {
            return
        }

        if (position >= steps.size) {
            position = steps.size - 1
        }

        steps[position].rollback()

        position--
    }

    fun onAsyncNext(bundle: Bundle?) {
        next(bundle)
    }

    val isCompleted: Boolean
        get() = position >= steps.size
}


class CreditFlow(private val activity: MainActivity) : Flow() {

    init {
        RxJava("Rx1")

        Screen(OpenDetailsScreenStep(activity, "Key1"))

        Conditional {
            activate(true, FlowStep{
                RxJava("Rx2.1")
                Screen(OpenDetailsScreenStep(activity,"Key2"))
                Screen(OpenDetailsScreenStep(activity,"Key3"))

                Conditional {
                    activate(true, FlowStep {
                        RxJava("Rx3.1")
                        Screen(OpenDetailsScreenStep(activity,"Key4"))

                        Conditional {
                            activate(true, FlowStep {
                                Screen(OpenDetailsScreenStep(activity, "Key5"))
                                RxJava("Rx4.1")
                            })
                        }

                    })
                }


//                RxJava("Rx2.2")
            })

        }

        RxJava("Rx5")

        Screen(OpenDetailsScreenStep(activity,"Key6"))

        RxJava("Rx6")
    }
}

