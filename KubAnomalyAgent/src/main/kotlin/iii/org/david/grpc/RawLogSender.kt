package iii.org.david.grpc

import com.fasterxml.jackson.databind.ObjectMapper

import interfaces.DavidCommunicatorServiceGrpc
import interfaces.ListRawLogStringWithIp

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger



//Thread use to Send Raw Log to Master
class RawLogSender:Runnable {
    var logger: Logger = LogManager.getLogger(RawLogSender::class.java.getName())
    lateinit var davidCommunicatorServiceGrpcStub: DavidCommunicatorServiceGrpc.DavidCommunicatorServiceBlockingStub
    lateinit var agentIP: String
    lateinit var rawLogList: ArrayList<ArrayList<String>>
    fun init(agentip:String,rawlog:ArrayList<ArrayList<String>>,stub:DavidCommunicatorServiceGrpc.DavidCommunicatorServiceBlockingStub):Boolean{
        this.agentIP = agentip
        this.rawLogList = rawlog
        this.davidCommunicatorServiceGrpcStub=stub
        return true
    }

    override fun run() {

        val amountOfInterval = (rawLogList.size/7)+1
        logger.info("amountOfInterval $amountOfInterval")
        var count=0
        //Send Raw Log Chunk By Chunk
        rawLogList.forEach {
            val rawLogStruct = ListRawLogStringWithIp.newBuilder()
            it.forEach {
                val objectmapper = ObjectMapper()
                val rawLogs = objectmapper.writeValueAsString(it)
                rawLogStruct.addRawlogstring(rawLogs)
            }
            rawLogStruct.ip = agentIP
            val listRawLog = rawLogStruct.build()
            davidCommunicatorServiceGrpcStub.sendRawLogDataToMaster(listRawLog)
            count++
            if(amountOfInterval%count==0){
                logger.info("Delay 1 Sec")
               Thread.sleep(1000)
            }

        }
        logger.info("Thread-ID: ${Thread.currentThread().id}  Send LogRawDataToMaster Complete")
        return
    }
}