package iii.org.david.daemons



import iii.org.david.grpc.ContainerBehaviorCollectorServer


class ContainerBehaviorCollectorDaemon: DAVIDDaemon() {
    val logger: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(ContainerBehaviorCollectorDaemon::class.java.getName())

    override fun init(): Boolean {
        logger.info("ContainerBehaviorCollectorDaemon Port is $port")
        return true
    }

    override fun listen(): Boolean{
        logger.info("ContainerBehaviorCollectorDaemon start to listen")
        this.server = io.grpc.ServerBuilder.forPort(port.toInt()).addService(ContainerBehaviorCollectorServer()).build()
        this.server.start()
        return true
    }

    override fun close(): Boolean{
        this.server.shutdownNow()
        return true
    }

}