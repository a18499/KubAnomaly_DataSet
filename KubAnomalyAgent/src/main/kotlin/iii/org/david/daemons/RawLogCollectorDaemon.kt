package iii.org.david.daemons

import iii.org.david.grpc.RawLogCollectorServer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class RawLogCollectorDaemon: DAVIDDaemon() {
    val logger : Logger = LogManager.getLogger(RawLogCollectorDaemon::class.java.getName())


    override fun init(): Boolean {
        logger.info("RawLogCollectorDaemon init complete")
        return true
    }

    override fun listen(): Boolean{
        logger.info("RawLogCollectorDaemon Start to listen")
        this.server = io.grpc.ServerBuilder.forPort(port.toInt())
                .addService(RawLogCollectorServer()).build()
        this.server.start()
        return true
    }

    override fun close(): Boolean {
        logger.info("RawLogCollectorDaemon is going to shutting")
        this.server.shutdown()
        return true
    }

}