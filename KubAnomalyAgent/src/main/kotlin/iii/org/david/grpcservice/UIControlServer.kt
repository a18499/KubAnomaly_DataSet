package iii.org.david.grpcservice

import com.google.protobuf.Empty

import iii.api.DAVIDSender
import iii.api.MQHandlerAPI
import iii.org.david.agent.DavidAgent

import iii.org.david.config.Configure

import iii.org.david.interfaces.DavidAgentUIControl
import interfaces.BooleanStrut
import interfaces.StringStruct
import interfaces.UIControlServiceGrpc
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.concurrent.ExecutorService

class UIControlServer:DavidAgentUIControl,UIControlServiceGrpc.UIControlServiceImplBase(){
    var logger : Logger = LogManager.getLogger(UIControlServer::class.java)
    lateinit var agentPool: ExecutorService
    var agentThread:Thread? = null
    lateinit var agentSender : DAVIDSender
    lateinit var clientID:String
    lateinit var agentWorker :DavidAgent
    fun init(agentpool:ExecutorService, agentsender:DAVIDSender, clientid:String):Boolean{
        this.agentPool = agentpool
        this.agentSender = agentsender
        this.clientID=clientid
        logger.info("UIControlServer :  clientID:$clientID")
        logger.info("Checking for none UI version")
        if(Configure.getUIOption().equals("false")){
            logger.info("Start Agent automatically")
            logger.info("Setting agentSwitch")
            Configure.agentSwitch=true
            logger.info("Start DAVID Agent ")
            GlobalScope.launch {
                val mqHandler = MQHandlerAPI("Agent-$clientID", Configure.getMQURL())
                agentWorker = DavidAgent(mqHandler, clientID)
                agentWorker.run()
            }
           // agentPool.submit(agentWorker)
            /*val davidagent = DAVIDAgent()
            davidagent.init(agentSender,clientID)
            agentPool.submit(davidagent)*/
        }

        logger.info("UIControlServer init complete")
        return true
    }
    override fun start(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getstatus(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {
        synchronized(Configure.agentSwitch) {
            if (this.agentThread != null && (this.agentThread!!.isAlive)) {
                logger.info("Already Started ")
                val response = BooleanStrut.newBuilder().setScucces(true).build()
                responseObserver.onNext(response)
                responseObserver.onCompleted()

            }else {
                logger.info("Setting ")
                Configure.agentSwitch = true
                /* logger.info("Begin to Send Message to DAVID Maser ")
        logger.info("MQURL ${Configure.getMQURL()} ")
        logger.info("ClientID: ${this.clientID}")
        logger.info("David Agent ${Configure.getDavidAgentIP()}")
        logger.info( "Master IP ${Configure.getDAVIDMasterIP()}")
        val message = AgentMessage(Configure.getDavidAgentIP(),this.clientID)

        try{
            agentmessager.send(JSONObject(message))
        }catch (e:Exception){
            logger.error("Unknown Exception ${e}")
            return false
        }
        agentmessager.closeConnection()*/
                logger.info("Start DAVID Agent ")
                /*val davidagent = DAVIDAgent()
                davidagent.init(agentSender, clientID)

                if(this.agentThread == null ){
                    this.agentThread = Thread(davidagent)
                    this.agentThread!!.start()
                }else if(this.agentThread != null && (!this.agentThread!!.isAlive)){
                    this.agentThread = Thread(davidagent)
                    this.agentThread!!.start()
                }
                //agentPool.submit(davidagent)*/

                GlobalScope.launch {

                   /* agentSender.create("$clientID-Sender",
                            Configure.MessageLabel, Configure.getMQURL(), Configure.Queue)
                    agentReceiver.create(clientID,
                            "MessageLabel='$clientID'", Configure.getMQURL(), Configure.Queue)*/
                    val mqHandler = MQHandlerAPI("Agent-$clientID", Configure.getMQURL())
                    agentWorker = DavidAgent(mqHandler, clientID)
                    agentWorker.run()
                }

                val response = BooleanStrut.newBuilder().setScucces(true).build()
                responseObserver.onNext(response)
                responseObserver.onCompleted()
            }
        }
    }

    override fun stop(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {

        if(!Configure.agentSwitch){
            logger.info("already shutdown")
            val response = BooleanStrut.newBuilder().setScucces(true).build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
            return
        }
        logger.info("Setting flag")
        Configure.agentSwitch = false
        logger.info("Stopping DavidAgent")

        while (Configure.agentStatus == "Running"){
            Thread.sleep(500)
        }

        logger.info("Stopping DavidAgent Complete")
       /* if(this.agentThread != null){
            this.agentThread = null
        }
        */
        agentWorker.shutdown()
        val response = BooleanStrut.newBuilder().setScucces(true).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getStatus(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {
        var statusFlag = false
        if(Configure.agentStatus == "Running"){
            statusFlag = true
        }
        val reponse :BooleanStrut = BooleanStrut.newBuilder().setScucces(statusFlag).build()
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()
    }

    override fun setHostIP(request: StringStruct, responseObserver: StreamObserver<StringStruct>) {
        logger.info("Setting Host IP: ${request.result}")
        Configure.setDavidAgentIP(request.result)
        val reponse :StringStruct = StringStruct.newBuilder().setResult(Configure.getDavidAgentIP()).build()
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()
    }
}