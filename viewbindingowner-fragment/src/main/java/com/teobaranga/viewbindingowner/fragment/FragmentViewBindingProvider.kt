package com.teobaranga.viewbindingowner.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.teobaranga.viewbindingowner.ViewBindingOwner
import com.teobaranga.viewbindingowner.ViewBindingProvider
import java.lang.reflect.Method
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * [ViewBindingProvider] that knows how to create and destroy a [ViewBinding] instance for a view
 * associated with a fragment.
 *
 * Supports creation of a [ViewBinding] class either through [onCreateView] which creates the view and binds to it,
 * or through [onViewCreated] which binds to an existing view. Clearing out the binding should also be done using the
 * [onDestroyView] method.
 *
 * Attempting to access the [ViewBinding] provided by the delegate before calling one of the creation methods or after
 * calling [onDestroyView] will throw an [IllegalStateException].
 *
 * Example usage:
 * ```
 * class MyFragment : Fragment(), ViewBindingOwner {
 *
 *     private val viewBindingProvider = FragmentViewBindingProvider()
 *     private val binding by viewBinding<MyFragmentBinding>()
 *
 *     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
 *         viewBindingProvider.onCreateView(inflater, container)
 *         return binding.root
 *     }
 *
 *     override fun onDestroyView() {
 *         super.onDestroyView()
 *         viewBindingProvider.onDestroyView()
 *     }
 * }
 * ```
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class FragmentViewBindingProvider : ViewBindingProvider {

    private var binding: ViewBinding? = null

    private lateinit var delegate: FragmentViewBindingDelegate<ViewBinding>

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewBinding> get(viewBindingClass: KClass<T>): ReadOnlyProperty<ViewBindingOwner, T> {
        if (!::delegate.isInitialized) {
            delegate = FragmentViewBindingDelegate(viewBindingClass) as FragmentViewBindingDelegate<ViewBinding>
        }
        return delegate as ReadOnlyProperty<ViewBindingOwner, T>
    }

    /**
     * Creates the [ViewBinding] object by inflating the target view.
     *
     * This is meant to be used in the fragment's `onCreateView` method:
     *
     * ```
     * private val fragmentViewBindingProvider = FragmentViewBindingProvider()
     * private val binding by viewBinding<MyFragmentBinding>()
     *
     * override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
     *     fragmentViewBindingProvider.onCreateView(inflater, container)
     *     return binding.root
     * }
     * ```
     *
     * @param inflater the [LayoutInflater] object that can be used to inflate any views in the fragment
     * @param container if non-null, this is the parent view that the fragment's UI should be attached to. The fragment
     * should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @throws IllegalArgumentException if the [ViewBinding] delegate has not been initialised through the [get] method
     */
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?) {
        binding = requireDelegate().inflate(null, inflater, container, false) as ViewBinding
    }

    /**
     * Creates the [ViewBinding] object by binding to an existing view.
     *
     * This is meant to be used in the fragment's `onViewCreated` method for scenarios where the view creation is
     * being done by a superclass, eg. for Preference fragments:
     *
     * ```
     * private val fragmentViewBindingProvider = FragmentViewBindingProvider()
     * private val binding by viewBinding<MyFragmentBinding>()
     *
     * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
     *     super.onViewCreated(view, savedInstanceState)
     *     fragmentViewBindingProvider.onViewCreated(view)
     *     // binding can now be accessed
     *     ...
     * }
     * ```
     *
     * @param view the [View] that matches the [ViewBinding] subtype handled by this class
     * @throws IllegalArgumentException if the [ViewBinding] delegate has not been initialised through the [get] method
     */
    fun onViewCreated(view: View) {
        binding = requireDelegate().bind(null, view) as ViewBinding
    }

    /**
     * Clears the [ViewBinding] object.
     *
     * This is meant to be used in the fragment's `onDestroyView` method:
     *
     * ```
     * override fun onDestroyView() {
     *     super.onDestroyView()
     *     fragmentViewBindingProvider.onDestroyView()
     *     ...
     * }
     * ```
     */
    fun onDestroyView() {
        binding = null
    }

    private fun requireDelegate(): FragmentViewBindingDelegate<ViewBinding> {
        require(::delegate.isInitialized) {
            "The ViewBinding delegate needs to be initialized before calling onCreateView or onViewCreated."
        }
        return delegate
    }

    private inner class FragmentViewBindingDelegate<T : ViewBinding>(
        viewBindingClass: KClass<T>
    ) : ReadOnlyProperty<ViewBindingOwner, T> {

        /**
         * Method signature: `ViewBinding.inflate(inflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean)`
         */
        val inflate: Method = viewBindingClass.java.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )

        /**
         * Method signature: `ViewBinding.bind(view: View)`
         */
        val bind: Method = viewBindingClass.java.getDeclaredMethod("bind", View::class.java)

        override operator fun getValue(thisRef: ViewBindingOwner, property: KProperty<*>): T {
            @Suppress("UNCHECKED_CAST")
            return binding as? T ?: throw IllegalStateException(
                "ViewBinding accessed before the view was created or after it was destroyed."
            )
        }
    }
}
