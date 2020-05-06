package iii.org.david.daemons

import iii.org.david.config.Configure
import io.grpc.Server

abstract class DAVIDDaemon{
    lateinit var server: Server
    val port = Configure.dockerContainerdaemonPort

    abstract fun init(): Boolean
    abstract fun listen(): Boolean
    abstract fun close(): Boolean
}