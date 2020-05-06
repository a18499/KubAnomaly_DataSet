package iii.org.david.core

import iii.api.DAVIDSender

import iii.org.david.config.Configure
import iii.org.david.daemons.DavidUIControllerDaemon
import iii.org.david.vmenv.SetupVmEnv

import org.apache.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.json.JSONObject
import org.mockito.Mock
import structure.AgentMessage
import utils.ExternalProcessHandleError
import java.io.File
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths

import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashSet

class DAVIDAgentMain {
    var logger: Logger = LogManager.getLogger(DAVIDAgentMain::class.java)
    var agentmessager = DAVIDSender()
    val clientID = UUID.randomUUID().toString()+"-DAVDIAgent"

    var agentID =HashSet<String>()
    //For test
    @Mock
    var workerPool = Executors.newFixedThreadPool(3)

    //High performance foreach
    inline fun <reified T> List<T>.foreach(crossinline invoke:(T) -> Unit):
            Unit{
        val size =size
        var i=0
        while (i<size){
            invoke(get(i))
            i++
        }
    }

    fun showBanner(){
        logger.info(
                "  ___   ___   _____ ___   _                _   \n" +
                " |   \\ /_\\ \\ / /_ _|   \\ /_\\  __ _ ___ _ _| |_ \n" +
                " | |) / _ \\ V / | || |) / _ \\/ _` / -_) ' \\  _|\n" +
                " |___/_/ \\_\\_/ |___|___/_/ \\_\\__, \\___|_||_\\__|\n" +
                "                             |___/             \n" +
                "" +
                "ver 0.1.6.10")
    }

    fun getHostIP():String{
        var result = "192.168.0.1"
        if(Configure.getVMVersion()){
            try {
                val socket = DatagramSocket()
                socket.connect(InetAddress.getByName("8.8.8.8"),10022)
                result = socket.localAddress.hostAddress
                logger.info("IP is $result")
            }catch (e:Exception){
                logger.error("get Host IP error: " + e)
            }
        }else{
            val printenv =  ExternalProcessHandleError()
            printenv.exec("printenv > env.txt",true,true,3000)
            val outputString = printenv.outputMsg
            val lines = Files.readAllLines(Paths.get("env.txt"))

            lines.foreach {
                println(it)
                val split = it.split("=")
                if(split[0].equals("DOCKERHOST")){
                    result = split[1]
                }
            }
        }

        return result
    }

    //Run this function used to get agent's container ID
    fun gatherAgentInformation():Boolean{
        val externalprocess = ExternalProcessHandleError()
        val gatherCommand = "docker ps > ./AgentInformation.txt"
        println("Comaand $gatherCommand")
        externalprocess.exec(gatherCommand,true,true,30000)
        logger.info("Output ${externalprocess.outputMsg}")
        logger.info("Error Output ${externalprocess.errorMsg}")

        val agentInformation = Files.readAllLines(Paths.get("AgentInformation.txt"))

        agentInformation.forEach {

            if(it.contains("CONTAINER")){

            }else{
                val information = it.split("   ")
                var containerId = ""
                var count=0
                information.forEach {

                    if(count==0){
                        containerId = it
                    }else if(it.contains("davidwithgui")){
                        println("add $containerId")
                        agentID.add(containerId)
                    }else if(it.contains("davidonlycli")){
                        println("add $containerId")
                        agentID.add(containerId)
                    }
                    count++
                }
            }
        }
        println(agentID.size)
        Configure.AgentID = agentID
        //Setting Agent IP
        Configure.setDavidAgentIP(getHostIP())
        logger.info("David Agent ContainerID Size = ${ Configure.AgentID.size}")
        return true
    }

    //This function initial some param
    // @param nickname use to get user input from UI
    fun init(nickname:String):Boolean{

        showBanner()
        Configure.init()
        Configure.setDavidAgentIP(nickname)
        //MQ
        logger.info("Init Queue")
        logger.info("sender clientID: ${clientID}-Sender")
        logger.info("Queue: ${Configure.Queue}")
        logger.info("MQURL: ${Configure.getMQURL()}")
        logger.info("MessageLabel: ${Configure.MessageLabel}")
        try{
            agentmessager.create(clientID+"-Sender",Configure.MessageLabel,Configure.getMQURL(),Configure.Queue)
            agentmessager.getConntction()
        }catch (e:Exception){
           logger.error("Connect to MQ Failed $e")
            return false
        }

        logger.info("DAVID Agent init Complete")
        return true
    }



    fun startAgent():Boolean{
        logger.info("DAVID Agent Start")


        //Check Agent Version
        if (Configure.getVMVersion()){
            logger.info("This is VM version")
            val envChecker = SetupVmEnv()
            envChecker.init()
            envChecker.checkingVmEnv()
            envChecker.updateRule()
        }

        if(!Configure.getVMVersion()){
            gatherAgentInformation()
        }

        logger.info("Begin to Send Message to DAVID Maser ")
        logger.info("MQURL ${Configure.getMQURL()} ")
        logger.info("ClientID: ${this.clientID}")
        logger.info("UI Mode: ${Configure.getUIOption()}")


        logger.info("David Agent ${Configure.getDavidAgentIP()}")
        logger.info( "Master IP ${Configure.getDAVIDMasterIP()}")
        val message = AgentMessage(Configure.getDavidAgentIP(),this.clientID)

        try{
            agentmessager.send(JSONObject(message))
            agentmessager.closeConnection()
        }catch (e:Exception){
            logger.error("Unknown Exception ${e}")
            return false
        }

        val uiControllerDaemon = DavidUIControllerDaemon()
        logger.info("uiControllerDaemon Starting")

        uiControllerDaemon.init(this.agentmessager,this.clientID)
        uiControllerDaemon.listen()

        logger.info("startAgent Start Complete")
        while (true){
            Thread.sleep(20000)
        }

    }


}

fun main(args: Array<String>) {
   val context = LogManager.getContext(false) as LoggerContext
   val logconfig = File("./Config/log4j-DAVIDAgent.xml")
   context.setConfigLocation(logconfig.toURI())
   org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF)
   Configure.init()
    // not yet implment
   //var ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"

   //var ValidHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$"



    val masterIP=""
   //var masterIP =args[0]

   val david = DAVIDAgentMain()
   david.init(masterIP)
   david.startAgent()

}