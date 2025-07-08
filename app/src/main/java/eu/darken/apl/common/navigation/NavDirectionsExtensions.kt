package eu.darken.apl.common.navigation

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections

fun NavDirections.navVia(pub: MutableLiveData<in NavDirections>) = pub.postValue(this)
