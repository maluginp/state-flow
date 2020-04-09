package ru.maluginp.statesteps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import ru.maluginp.statesteps.steps.CreditFlow
import ru.maluginp.statesteps.steps.Flow

class MainActivity : AppCompatActivity(), DetailsCallback {
    private lateinit var flow: Flow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flow = CreditFlow(this@MainActivity)
        flow.flow()


        findViewById<TextView>(R.id.btnRestart)?.setOnClickListener {
            Log.d("Flow", "Restart Flow")

            flow = CreditFlow(this@MainActivity)
            flow.flow() // Workaround
        }

        findViewById<TextView>(R.id.btnNext)?.setOnClickListener {
            flow.next()
        }

        findViewById<TextView>(R.id.btnPrev)?.setOnClickListener {
            flow.prev()
        }

    }

    override fun onFinish() {
        supportFragmentManager.popBackStack()
        flow.next()
    }
}
