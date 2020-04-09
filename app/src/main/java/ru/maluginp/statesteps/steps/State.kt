package ru.maluginp.statesteps.steps

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.view.*
import ru.maluginp.statesteps.DetailsFragment
import ru.maluginp.statesteps.MainActivity
import ru.maluginp.statesteps.R

// Handle error
interface Step {
    // async process
    fun begin()
    fun commit() // How to check step is processed or not, async process
    fun rollback()
}


//typealias StepReducer = (bundle: Bundle?) -> Bundle


interface ScreenStep : Step {
}

class OpenDetailsScreenStep(private val activity: MainActivity, private val key: String) : ScreenStep {
    override fun begin() {
        Log.d("Flow","Begin $key")

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.flContainer, DetailsFragment.newInstance(), "Details")
            .addToBackStack("Details")
            .commitAllowingStateLoss()
    }

    override fun commit() {
        Log.d("Flow","Commit $key")
    }

    override fun rollback() {
        Log.d("Flow","Rollback $key")
    }
}

// Back ?

//typealias WhenInFlow = WhenFlow.() -> Unit
////typealias
//
//class WhenFlow {
//    fun Check(activate: Boolean, check: WhenInFlow) {
//        if (activate) {
//            check()
//        }
//    }
//}

//interface INode {
//    var prev: INode?
//    var next: INode?
//    val step: ScreenStep
//}
//
//data class Node(
//    override var prev: INode?,
//    override var next: INode?,
//    override val step: ScreenStep
//) : INode
//
//data class _Condition (
//    val cond:(() -> Boolean),
//    val node: INode
//)
//
//class ConditionalNode(
//    private var conds: List<_Condition>,
//    override var prev: INode?,
//) : INode {
//
//    override val step: ScreenStep = NothingScreenStep()
//
//    override var next: INode?
//        get() = conds.firstOrNull { it.cond() }?.node
//        set(value) {
//
//        }
//}

abstract class Flow {
    protected val state: Bundle = Bundle()
    private val steps: MutableList<ScreenStep> = mutableListOf()
    private var position = -1

    abstract fun flow()

//    fun When(block: WhenFlow.() -> Unit) {
//        val flow = WhenFlow()
//
//        flow.block()
//
//    }

    fun Screen(step: ScreenStep) {
        steps.add(step)
//
//        val node = Node(cursorNode, null, step)
//        cursorNode.next = node
//        cursorNode = node
    }

//    fun Screen(step: ScreenStep, reducer: StepReducer) {
//
//    }


    // step -> step ->

    // steps ->

    fun next(bundle: Bundle? = null) {
        Log.d("Flow", "Flow next($position)")

        if (position == steps.size) {
            steps[position-1].commit()
        }

        position++

        when {
            position == 0 -> steps[position].begin()
            position > 0 && position < steps.size -> {
                steps[position-1].commit()
                steps[position].begin()
            }
            else -> {
                Log.d("Flow", "Miss next, onCompleted")
                return@next
            }
        }




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

    val isCompleted: Boolean
        get() = position >= steps.size
}

class CreditFlow(private val activity: MainActivity) : Flow() {
    override fun flow() {
        Screen(OpenDetailsScreenStep(activity, "Key1"))

        Screen(OpenDetailsScreenStep(activity,"Key2"))
    }
}

//class CreditFlow : Flow() {
//    fun flow() {
//        Screen(OpenDetailsScreenStep())
//
//        When {
//            Check(state.getBoolean("test")) {
//                Screen(OpenDetailsScreenStep())
//            }
//
//            Check (state.getBoolean("test2")) {
//                Screen(OpenDetailsScreenStep())
//            }
//
//            Check(true) {
//                Screen(OpenDetailsScreenStep())
//            }
//        }
//    }
////    state = InitialState,
//
////    ScreenStep(
////
////        reducer { output -> state }
////    ),
////
////    RxJavaStep(
////    )
////
////    WhenFlow (
////        state -> WhenFlow (
////            Step (reducer -> ),
////            Step (reducer -> )
////        ),
////        state -> Y_Step (
////            Step ()
////        ),
////        state -> Z_Step (
////           Step ()
////        )
////    )
////
////    ScreenStep(
////
////        reducer { }
////    ),
////
////    onCompleted() {
////
////    }
//}
