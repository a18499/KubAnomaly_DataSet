package iii.org.david.daemons

import iii.api.DAVIDSender
import iii.org.david.config.Configure


import iii.org.david.grpcservice.UIControlServer
import io.grpc.Server
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors

class DavidUIControllerDaemon: DAVIDDaemon() {
    val logger : Logger = LogManager.getLogger(DavidUIControllerDaemon::class.java)
    var agentpool = Executors.newFixedThreadPool(1)
    lateinit var agentSender :DAVIDSender
    lateinit var clientID :String
    override fun init(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close(): Boolean {
        logger.info("ContainerInformationCollectorDaemon Start to Shutdown")
        this.server.shutdownNow()
        return true
    }
    fun init(agentsender:DAVIDSender,clientid:String):Boolean{
        logger.info("Port $port")
       // var davidagent = DAVIDAgent()
       // davidagent.init(agentsender,clientid)
       // agentpool.submit(davidagent)
        agentSender=agentsender
        clientID = clientid
        logger.info("DavidUIControllerDaemon init complete")
        return true
    }

    override fun listen():Boolean{
        try {
            val uiControlServer = UIControlServer()
            uiControlServer.init(agentpool,this.agentSender,this.clientID)
            this.server = io.grpc.ServerBuilder.forPort(port.toInt())
                    .addService(uiControlServer).build()
            this.server.start()

            logger.info("DavidUIControllerDaemon Start to listen")
        }catch (e:Exception){
           logger.error("DavidUIControllerDaemon Error $e")
        }

        return true
    }

}