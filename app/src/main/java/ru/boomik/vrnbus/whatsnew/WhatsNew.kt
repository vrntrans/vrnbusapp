package io.github.tonnyl.whatsnew

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.boomik.vrnbus.whatsnew.adapter.ItemsAdapter
import io.github.tonnyl.whatsnew.item.WhatsNewItem
import io.github.tonnyl.whatsnew.util.PresentationOption
import ru.boomik.vrnbus.R


/**
 * Created by lizhaotailang on 30/11/2017.
 */
class WhatsNew : DialogFragment() {

    var mItems: Array<WhatsNewItem>? = null
    var presentationOption: PresentationOption = PresentationOption.IF_NEEDED
    var titleText: CharSequence = "What's New"
    var titleColor: Int = Color.parseColor("#000000")
    var itemTitleColor: Int? = null
    var itemContentColor: Int? = null
    var buttonBackground: Int = Color.parseColor("#000000")
    var buttonText: String = "Continue"
    var buttonTextColor: Int = Color.parseColor("#FFEB3B")
    var windowInsets: WindowInsetsCompat? = null

    private val TAG = "WhatsNew"

    companion object {
        @JvmField
        val ARGUMENT = "argument"

        private val LAST_VERSION_CODE = "LAST_VERSION_CODE"
        private val LAST_VERSION_NAME = "LAST_VERSION_NAME"

        @JvmStatic
        fun newInstance(vararg items: WhatsNewItem): WhatsNew {
            val bundle = Bundle()
            bundle.putParcelableArray(ARGUMENT, items)
            return WhatsNew().apply { arguments = bundle }
        }

        @JvmStatic
        fun newInstance(items: List<WhatsNewItem>): WhatsNew {
            val bundle = Bundle()
            bundle.putParcelableArray(ARGUMENT, items.toTypedArray())
            return WhatsNew().apply { arguments = bundle }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { mItems = it.getParcelableArray(ARGUMENT) as Array<WhatsNewItem>? }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.whatsnew_main, container, false)

        // The title text view.
        with(view.findViewById<TextView>(R.id.titleTextView)) {
            text = titleText
            setTextColor(titleColor)
        }

        // The recycler view.
        with(view.findViewById<RecyclerView>(R.id.itemsRecyclerView)) {
            if (mItems != null && context != null) {
                layoutManager = LinearLayoutManager(context)
                adapter = ItemsAdapter(mItems!!, requireActivity()).apply {
                    itemContentColor?.let { this.contentColor = it }
                    itemTitleColor?.let { this.titleColor = it }
                }
                (adapter as ItemsAdapter).notifyDataSetChanged()
            }
        }

        // The button.
        with(view.findViewById<Button>(R.id.button)) {
            text = buttonText
            setTextColor(buttonTextColor)
            setBackgroundColor(buttonBackground)
            setOnClickListener { dismiss() }
        }


        // Make the dialog fullscreen.
        val window = dialog?.window!!
        window.setBackgroundDrawableResource(R.color.background)
        if (windowInsets != null) windowInsets?.let {
            window.decorView.setPadding(it.systemWindowInsetLeft, it.systemWindowInsetTop, it.systemWindowInsetRight, it.systemWindowInsetBottom)
        }
        else window.decorView.setPadding(0, 0, 0, 0)
        with(window.attributes) {
            gravity = Gravity.BOTTOM
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        // Animate.
        window.setWindowAnimations(R.style.WhatsNewDialogAnimation)

        return view
    }

    fun presentAutomatically(activity: AppCompatActivity) : Boolean {

        when (presentationOption) {
            PresentationOption.DEBUG -> {
                show(activity.supportFragmentManager, TAG)
            }
            PresentationOption.NEVER -> {
                return false
            }
            else -> {
                // Obtain the last version code from sp.
                val lastVersionCode = PreferenceManager.getDefaultSharedPreferences(activity)
                        .getInt(LAST_VERSION_CODE, 0)


                var nowVersionCode = 0

                var lastFirstNumOfVersionName = 0
                var nowFirstNumOfVersionName = 0
                var lastSecondNumOfVersionName = 0
                var nowSecondNumOfVersionName = 0

                try {
                    var tmp = ""
                    activity.packageManager
                            .getPackageInfo(activity.packageName, 0)
                            .let {
                                nowVersionCode = it.versionCode
                                tmp = it.versionName
                            }



                    // Obtain the first two numbers of current version name.
                    tmp.split("\\.".toRegex())
                            .filter { it.isNotEmpty() && !it.isBlank() }
                            .apply {
                                if (size >= 1) {
                                    nowFirstNumOfVersionName = this[0].toInt()
                                }

                                if (size >= 2) {
                                    nowSecondNumOfVersionName = this[1].filter { it.isDigit() }.toInt()
                                }
                            }

                    // Obtain the first two numbers of last version name.
                    (PreferenceManager.getDefaultSharedPreferences(activity)
                            .getString(LAST_VERSION_NAME, "") ?: "")
                            .split("\\.".toRegex())
                            .filter { it.isNotEmpty() && !it.isBlank() }
                            .apply {
                                if (size >= 1) {
                                    lastFirstNumOfVersionName = this[0].toInt()
                                }

                                if (size >= 2) {
                                    lastSecondNumOfVersionName = this[1].toInt()
                                }
                            }

                    if (presentationOption == PresentationOption.ALWAYS) {
                        if (nowVersionCode >= 0 && nowVersionCode > lastVersionCode) {

                            // Show the dialog.
                            show(activity.supportFragmentManager, TAG)
                            // Save the latest version code to sp.
                            PreferenceManager.getDefaultSharedPreferences(activity)
                                    .edit()
                                    .putInt(LAST_VERSION_CODE, nowVersionCode)
                                    .apply()
                            return true
                        }
                    } else { // presentationOption == PresentationOption.IF_NEEDED
                        if (((nowFirstNumOfVersionName >= 0 && nowFirstNumOfVersionName > lastFirstNumOfVersionName)
                                        || (nowSecondNumOfVersionName >= 0 && nowSecondNumOfVersionName > lastSecondNumOfVersionName))
                                && (nowVersionCode >= 0 && lastVersionCode >= 0 && nowVersionCode > lastVersionCode)) {


                            // Save the latest version name to sp.
                            PreferenceManager.getDefaultSharedPreferences(activity)
                                    .edit()
                                    .putInt(LAST_VERSION_CODE, nowVersionCode)
                                    .putString(LAST_VERSION_NAME, "$nowFirstNumOfVersionName.$nowSecondNumOfVersionName")
                                    .apply()

                            // Show the dialog.
                            if (lastVersionCode>0) {
                                show(activity.supportFragmentManager, TAG)
                                return true
                            }

                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }
}