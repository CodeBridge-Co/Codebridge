package za.co.eduvos.codebridge

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import za.co.eduvos.codebridge.databinding.ActivityMainBinding
import za.co.eduvos.codebridge.feature.dashboard.DashboardFragment
import za.co.eduvos.codebridge.feature.leaderboard.ProgressFragment
import za.co.eduvos.codebridge.feature.problems.ChallengesListFragment
import za.co.eduvos.codebridge.feature.profile.ProfileFragment
import za.co.eduvos.codebridge.feature.sandbox.SandboxFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    enum class Tab { HOME, CHALLENGES, PRACTICE, PROGRESS, PROFILE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        binding.tabHome.setOnClickListener { selectTab(Tab.HOME) }
        binding.tabChallenges.setOnClickListener { selectTab(Tab.CHALLENGES) }
        binding.tabPractice.setOnClickListener { selectTab(Tab.PRACTICE) }
        binding.tabProgress.setOnClickListener { selectTab(Tab.PROGRESS) }
        binding.tabProfile.setOnClickListener { selectTab(Tab.PROFILE) }

        if (savedInstanceState == null) selectTab(Tab.HOME)
    }

    /** Switch bottom-bar tab and clear the stage/detail back stack. */
    fun selectTab(tab: Tab) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment: Fragment = when (tab) {
            Tab.HOME -> DashboardFragment()
            Tab.CHALLENGES -> ChallengesListFragment()
            Tab.PRACTICE -> SandboxFragment()
            Tab.PROGRESS -> ProgressFragment()
            Tab.PROFILE -> ProfileFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .commit()
        highlight(tab)
    }

    /** Open a screen on top of the current one (stages, events list). */
    fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun highlight(tab: Tab) {
        val tabs = mapOf(
            Tab.HOME to binding.tabHome,
            Tab.CHALLENGES to binding.tabChallenges,
            Tab.PRACTICE to binding.tabPractice,
            Tab.PROGRESS to binding.tabProgress,
            Tab.PROFILE to binding.tabProfile
        )
        val activeColor = ContextCompat.getColor(this, R.color.cb_green)
        val inactiveColor = Color.parseColor("#8E8E93")

        tabs.forEach { (t, layout) ->
            val on = t == tab
            val targetColor = if (on) activeColor else inactiveColor

            if (t == Tab.PRACTICE) {
                val tv = layout.getChildAt(1) as? TextView
                tv?.setTextColor(targetColor)
            } else {
                val iv = layout.getChildAt(0) as? ImageView
                val tv = layout.getChildAt(1) as? TextView
                iv?.setColorFilter(targetColor)
                tv?.setTextColor(targetColor)
            }
        }
    }
}
