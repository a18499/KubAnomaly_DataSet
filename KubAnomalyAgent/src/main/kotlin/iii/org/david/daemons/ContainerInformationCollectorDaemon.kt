package iii.org.david.daemons



import iii.org.david.grpc.ContainerInformationCollectorServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ContainerInformationCollectorDaemon: DAVIDDaemon() {

    val logger : Logger = LogManager.getLogger(ContainerInformationCollectorDaemon::class.java.getName())

    override fun init(): Boolean{
        logger.info("Port $port")
        return true
    }

    override fun listen(): Boolean{
        logger.info("ContainerInformationCollectorDaemon Start to listen")
        this.server = io.grpc.ServerBuilder.forPort(port.toInt())
                .addService(ContainerInformationCollectorServer()).build()
        this.server.start()
        return true
    }

    override fun close(): Boolean{
        logger.info("ContainerInformationCollectorDaemon Start to Shutdown")
        this.server.shutdownNow()
        return true
    }
}