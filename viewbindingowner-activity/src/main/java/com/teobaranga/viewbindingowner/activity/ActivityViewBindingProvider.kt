package com.teobaranga.viewbindingowner.activity

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.teobaranga.viewbindingowner.ViewBindingOwner
import com.teobaranga.viewbindingowner.ViewBindingProvider
import java.lang.reflect.Method
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * [ViewBindingProvider] that knows how to create and destroy a [ViewBinding] instance for a view
 * associated with an activity.
 *
 * Supports creation of a [ViewBinding] class through the [onCreate] method which creates the view and binds to it.
 * Clearing out the binding should be done using the [onDestroy] method once the activity is destroyed.
 *
 * Attempting to access the [ViewBinding] provided by the delegate before calling [onCreate] or after calling
 * [onDestroy] will throw an [IllegalStateException].
 *
 * Example usage:
 * ```
 * class MyActivity : AppCompatActivity(), ViewBindingOwner {
 *
 *     private val viewBindingProvider = ActivityViewBindingProvider()
 *     private val binding by viewBinding<MyActivityBinding>()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         viewBindingProvider.onCreate(layoutInflater)
 *         setContentView(binding.root)
 *     }
 *
 *     override fun onDestroy() {
 *         super.onDestroy()
 *         viewBindingProvider.onDestroy()
 *     }
 * }
 * ```
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ActivityViewBindingProvider : ViewBindingProvider {

    private var binding: ViewBinding? = null

    private lateinit var delegate: ActivityViewBindingDelegate<ViewBinding>

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewBinding> get(viewBindingClass: KClass<T>): ReadOnlyProperty<ViewBindingOwner, T> {
        if (!::delegate.isInitialized) {
            delegate = ActivityViewBindingDelegate(viewBindingClass) as ActivityViewBindingDelegate<ViewBinding>
        }
        return delegate as ReadOnlyProperty<ViewBindingOwner, T>
    }

    /**
     * Creates the [ViewBinding] object by inflating the target view.
     *
     * This is meant to be used in the activity's `onCreate` method:
     *
     * ```
     * private val viewBindingProvider = ActivityViewBindingProvider()
     * private val binding by viewBinding<MyActivityBinding>()
     *
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     super.onCreate(savedInstanceState)
     *     viewBindingProvider.onCreate(layoutInflater)
     *     setContentView(binding.root)
     * }
     * ```
     *
     * @param inflater the [LayoutInflater] object that can be used to inflate the binding view
     * @throws IllegalArgumentException if the [ViewBinding] delegate has not been initialised through the [get] method
     */
    fun onCreate(inflater: LayoutInflater) {
        binding = requireDelegate().inflate(null, inflater) as ViewBinding
    }

    /**
     * Clears the [ViewBinding] object.
     *
     * This is meant to be used in the activity's `onDestroy` method:
     *
     * ```
     * override fun onDestroy() {
     *     super.onDestroy()
     *     viewBindingProvider.onDestroy()
     *     ...
     * }
     * ```
     */
    fun onDestroy() {
        binding = null
    }

    private fun requireDelegate(): ActivityViewBindingDelegate<ViewBinding> {
        require(::delegate.isInitialized) {
            "The ViewBinding delegate needs to be initialized before calling onCreateView."
        }
        return delegate
    }

    private inner class ActivityViewBindingDelegate<T : ViewBinding>(
        viewBindingClass: KClass<T>
    ) : ReadOnlyProperty<ViewBindingOwner, T> {

        /**
         * Method signature: `ViewBinding.inflate(inflater: LayoutInflater)`
         */
        val inflate: Method = viewBindingClass.java.getDeclaredMethod("inflate", LayoutInflater::class.java)

        override operator fun getValue(thisRef: ViewBindingOwner, property: KProperty<*>): T {
            @Suppress("UNCHECKED_CAST")
            return binding as? T ?: throw IllegalStateException(
                "ViewBinding accessed before the view was created or after it was destroyed."
            )
        }
    }
}
