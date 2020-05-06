package iii.org.david.agent

import com.beust.klaxon.Klaxon
import iii.api.DAVIDReciver
import iii.api.DAVIDSender
import iii.api.MQHandlerAPI
import iii.org.david.agent.interfaces.AgentWorker
import iii.org.david.config.Configure

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import structure.AgentMessage


/**
 * Created by jimmyhuang on 2018/7/23.
 */
class DavidAgent(private val mqHandler: MQHandlerAPI,
                 private val clientName: String): AgentWorker {


    var logger : Logger = LogManager.getLogger(DavidAgent::class.java)

    private lateinit var davidLogCollector: AgentWorker
    private var isFinish: Boolean = false
    private var port: String = Configure.davidMasterPort


    private fun sendHealthyMessage():Boolean{

        val healthyMessage = AgentMessage(Configure.getDavidAgentIP(),
                this.clientName,"I am Healthy")
        try {
        /*    sender.getConntction()
            sender.send(JSONObject(healthyMessage))
            sender.closeConnection()*/
            mqHandler.send(Configure.Queue, JSONObject(healthyMessage),
                    Configure.MessageLabel, -1)
            logger.info("sendHealthyMessage Complete")
        }catch (e:Exception){
            logger.error("sendHealthyMessage error $e")
            return false
        }
        return true
    }

    private fun doDavidHandShaking():Boolean{
        logger.info("Begin to Handshaking with DAVID Master")
        logger.info("MqURL -> ${Configure.getMQURL()} ")
        logger.info("ClientID -> ${this.clientName}")
        logger.info("David Agent -> ${Configure.getDavidAgentIP()}")
        logger.info("Master IP -> ${Configure.getDAVIDMasterIP()}")
        val message = AgentMessage(Configure.getDavidAgentIP(), this.clientName)
        var isSuccess = false
        try{

            mqHandler.send(Configure.Queue, JSONObject(message),
                    Configure.MessageLabel, -1)

            logger.info("Waiting HandShaking Message")
           val serverResponse = mqHandler.receive(Configure.Queue,
                   "MessageLabel='$clientName'", 60)!!

            logger.info("HandShaking Response Message -> $serverResponse")

            if(serverResponse.has("ip")) {
                val agentMessage:AgentMessage =
                        Klaxon().parse<AgentMessage>(serverResponse.toString())!!

                if(agentMessage.message.contains("Hello")) {
                    agentMessage.message.indexOf("is")

                    //port = agentMessage.message.substring(agentMessage.message.indexOf("is"))
                    logger.info("DavidHandShaking is success.")
                    isSuccess = true
                }
            }
        }catch (e:Exception){
            logger.error("DavidHandShaking Exception -> $e")
            e.stackTrace.forEach { logger.error("\t $it") }
            isSuccess = false
        }finally {
            if(isSuccess)
                logger.info("Handshaking with DAVID Master Complete")
            return isSuccess
        }
    }


    override fun shutdown() {
        Configure.agentStatus = "shutdown"
        this.davidLogCollector.shutdown()
        this.isFinish = true
    }

    override fun run() {
        //program  will wait the handshaking complete

        runBlocking {
            while (!doDavidHandShaking()){
                logger.error("HandShaking with david master failure.")
                logger.error("Waiting 10s to reconnection.")
                Thread.sleep(10000)
            }
        }

        Configure.agentStatus = "Running"
        //Start to Collect Data
        GlobalScope.launch {
            davidLogCollector = LoggerCollector(port, Configure.getDavidAgentIP())
            davidLogCollector.run()
            //val dacidCollectRunner = Thread(davidLogCollector)
            //dacidCollectRunner.start()
        }

        runBlocking {
            while (!isFinish){
                logger.info("Sending DavidAgent HealthStatus every 6 sec")
                //handle with DavidAgent Health
                sendHealthyMessage()
                Thread.sleep(6000)
            }
        }

    }

}