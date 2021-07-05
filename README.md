# ViewBindingOwner

[![Version](https://img.shields.io/maven-central/v/io.github.teobaranga.viewbindingowner/core?style=flat&label=core)][core]
[![Version](https://img.shields.io/maven-central/v/io.github.teobaranga.viewbindingowner/fragment?style=flat&label=fragment)][fragment]
[![Version](https://img.shields.io/maven-central/v/io.github.teobaranga.viewbindingowner/activity?style=flat&label=activity)][activity]
[![License](https://img.shields.io/github/license/teobaranga/viewbindingowner?style=flat)][license]

ViewBindingOwner is a library that makes it easier to work with ViewBinding. There are currently two main libraries providing Kotlin delegates which allow creating and accessing ViewBinding objects. The implementation is loosely based on the `ViewModelStoreOwner` interface from Jetpack.

---

- [Libraries](#libraries)
  - [Fragment](#fragment)
  - [Activity](#activity)
  - [Core](#core)
- [Installation](#installation)
- [Contributing](#contributing)

## Libraries

### Fragment

The fragment library allows accessing ViewBinding by either creating a view or binding to an existing one.

The `FragmentViewBindingProvider` class is able to inflate and bind ViewBinding through reflection which makes it easier to introduce ViewBinding in projects where it's preferred to inflate the view manually in `onCreateView`.

The library works best when it's used in a base Fragment class, eg:

```kotlin
abstract class BaseFragment : Fragment(), ViewBindingOwner {

    protected open val binding: ViewBinding? = null

    override val viewBindingProvider = FragmentViewBindingProvider()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBindingProvider.onCreateView(inflater, container)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBindingProvider.onDestroyView()
    }
}
```

If you don't have enough control over the view creation, for example in `PreferenceFragment`s, you can also bind after the view was created, in `onViewCreated`:
```kotlin
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBindingProvider.onViewCreated(view)
    }
``` 


Subclasses can then use a one-liner to declare a `binding` object and access it in `onViewCreated`:
```kotlin
class MyFragment : BaseFragment() {

    override val binding by viewBinding<MyFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // All the binding views can be accessed safely at this point
    }
}
```

### Activity

The activity library is very similar to the fragment one. One difference is that the activity binding provider only allows inflating the binding and not binding to an existing view. This is also designed to be used mainly in a base Activity such as:

```kotlin
abstract class BaseActivity : AppCompatActivity(), ViewBindingOwner {

    protected open val binding: ViewBinding? = null

    override val viewBindingProvider = ActivityViewBindingProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBindingProvider.onCreate(layoutInflater)
        binding?.let {
            setContentView(it.root)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBindingProvider.onDestroy()
    }
}
```

Once the base activity is set up, the binding can safely be accessed in `onCreate`:

```kotlin
class MyActivity : BaseActivity() {

    override val binding by viewBinding<MyActivityBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // All the binding views can be accessed safely at this point
    }
}
``` 

### Core

The core library contains the interfaces that need to be implemented in order to give ViewBinding provider functionality to an existing class. This project offers some implementations for fragments and activities but in theory this could be extended to other things such as widgets or custom views.


## Installation

Add the dependency:

```kotlin
dependencies {
    implementation("io.github.teobaranga.viewbindingowner:fragment:1.0.0") // for the Fragment + Core APIs
    implementation("io.github.teobaranga.viewbindingowner:activity:1.0.0") // for the Activity + Core APIs
    implementation("io.github.teobaranga.viewbindingowner:core:1.0.0") // for the Core APIs only
}
```

Make sure ViewBinding is enabled inside the module's build script:

```kotlin
android {
    buildFeatures {
        viewBinding = true
    }
}
```

## Contributing

Pull requests are always welcome! Anything from the design of the library to adding new features is up for discussion.

## License

```
Copyright 2021 Teo Baranga.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```

[core]: https://search.maven.org/artifact/io.github.teobaranga.viewbindingowner/core
[fragment]: https://search.maven.org/artifact/io.github.teobaranga.viewbindingowner/fragment
[activity]: https://search.maven.org/artifact/io.github.teobaranga.viewbindingowner/activity
[license]: ./LICENSE.md
