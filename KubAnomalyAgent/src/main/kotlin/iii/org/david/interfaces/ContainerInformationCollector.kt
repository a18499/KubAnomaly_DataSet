package iii.org.david.interfaces

import iii.org.david.interfaces.structure.ContainerHDInformation


interface ContainerInformationCollector {
    fun init(): Boolean
    fun getResource(): ArrayList<ContainerHDInformation>
    fun getIP(): String
    fun deinit(): Boolean
}