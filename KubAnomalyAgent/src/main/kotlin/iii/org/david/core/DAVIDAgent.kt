package iii.org.david.core

import com.beust.klaxon.Klaxon
import iii.api.DAVIDReciver
import iii.api.DAVIDSender
import iii.org.david.config.Configure
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import structure.AgentMessage

class DAVIDAgent :Runnable{
    lateinit var agentSender: DAVIDSender
    lateinit var clientID: String
    var connectionConfirmFlag = false
    var logger: Logger = LogManager.getLogger(DAVIDAgent::class.java)
    fun init(agentsender: DAVIDSender, clientid:String): Boolean{
        this.agentSender = agentsender
        this.clientID = clientid
        logger.info("DAVIDAgent init complete")
        return true
    }

    fun sendHelloMessage(){
        logger.info("Begin to Send Message to DAVID Maser ")
        logger.info("MQURL ${Configure.getMQURL()} ")
        logger.info("ClientID: ${this.clientID}")
        logger.info("David Agent IP: ${Configure.getDavidAgentIP()}")
        logger.info( "Master IP ${Configure.getDAVIDMasterIP()}")
        val message = AgentMessage(Configure.getDavidAgentIP(),this.clientID)

        try{
            agentSender.getConntction()
            agentSender.send(JSONObject(message))
            agentSender.closeConnection()

        }catch (e:Exception){
            logger.error("Unknown Exception ${e}")

        }
        logger.info("sendHelloMessage Complete")
    }

    //Send Message to Master every 20 sec
    fun sendHealthyMessage():Boolean{

        val healthyMessage = AgentMessage(Configure.getDavidAgentIP(),this.clientID,"I am Healthy")
        try {
            agentSender.getConntction()
            agentSender.send(JSONObject(healthyMessage))
            agentSender.closeConnection()
            logger.info("sendHealthyMessage Complete")
        }catch (e:Exception){
            logger.error("sendHealthyMessage error $e")
            return false
        }
        return true
    }

    fun mainProcess():Boolean{

        sendHelloMessage()
        logger.info("Create DAVIDReciver ClientID: ${this.clientID}")
        val messageReciver = DAVIDReciver()
        messageReciver.create(this.clientID,"MessageLabel='${this.clientID}'",Configure.getMQURL(),Configure.Queue)
        messageReciver.getConnection()
        while (true){

            logger.info("check switch status ${Configure.agentSwitch}")
            if(!Configure.agentSwitch){
                logger.info("Shut Downing DAVIDAgent")
                break
            }

            Configure.agentStatus="Running"
            logger.info("waiting Message")
            messageReciver.Timer = 20000
            logger.info("Timer ${messageReciver.Timer}")
            messageReciver.recive()

            val serverResponse = messageReciver.ReciveMessage
            logger.info("Server Response Message ${serverResponse}")
            if(serverResponse.has("ip")){
                val agentMessage = Klaxon().parse<AgentMessage>(serverResponse.toString())
                logger.info("ServerMessage: ${agentMessage!!.message}")
                if(agentMessage.message.contains("Hello")){
                    agentMessage.message.indexOf("is")
                    //val port = agentMessage.message.substring(agentMessage.message.indexOf("is"))
                    //logger.info("port $port")
                    logger.info("Confirm")
                    //Start to Collect Data
                    if(!connectionConfirmFlag){
                        val davidLogCollector  = DAVIDLogCollector()
                        davidLogCollector.init("2001",Configure.getDavidAgentIP())
                        val dacidCollectRunner= Thread(davidLogCollector)
                        dacidCollectRunner.start()
                    }
                    connectionConfirmFlag = true
                }
                //reset Message to none
                messageReciver.ReciveMessage = JSONObject()
            }else{
                logger.info("There is no message")
                if(!connectionConfirmFlag){
                    logger.info("Resend HelloMessage")
                    sendHelloMessage()
                    continue
                }

                logger.info("Sending Healthy Information")
                sendHealthyMessage()

            }
        }

        logger.info("DavidAgent is going to shutdown")
        logger.info("Closing connection")
        messageReciver.closeConnection()
        agentSender.closeConnection()
        Configure.agentStatus="Stop"
        return true
    }

    override fun run() {
        mainProcess()
        return
    }

}