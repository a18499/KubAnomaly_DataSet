package iii.org.david.grpc


import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Empty
import iii.org.david.config.Configure
import iii.org.david.core.DockerBehavierCollector


import iii.org.david.interfaces.structure.ContainerLog
import iii.org.david.pool.RawLogNamePool

import interfaces.*
import io.grpc.stub.StreamObserver

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


import java.util.*


class ContainerBehaviorCollectorServer:ContainerBehaviorCollectServiceGrpc.ContainerBehaviorCollectServiceImplBase() {
    private val logger: Logger = LogManager.getLogger(ContainerBehaviorCollectorServer::class.java.getName())
    private var containerBehaiverCollector = DockerBehavierCollector()
    private var normalizeLog = ArrayList<ContainerLog>()
    override fun init(request: StringStruct, responseObserver: StreamObserver<BooleanStrut>) {
        val uuid = UUID.randomUUID()
        containerBehaiverCollector.init(uuid.toString())
        logger.info("ContainerBehavierCollectorServer init Complete")
        val reponse :BooleanStrut = BooleanStrut.newBuilder().setScucces(true).build()
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()
    }

    override fun collect(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {
        logger.info("Reset Log")

        normalizeLog.clear()
        logger.info("Begin Collect")
        logger.info("Looger will start record 10 sec")
        val logName = UUID.randomUUID().toString()+".json"
        containerBehaiverCollector.init(logName)
        containerBehaiverCollector.collect()

        RawLogNamePool.putLogIntoPool(logName)

        val reponse :BooleanStrut = BooleanStrut.newBuilder().setScucces(true).build()
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()
    }

    override fun killMonitorProcess(request: Empty?, responseObserver: StreamObserver<BooleanStrut>?) {

    }

    override fun getMonitorLogs(request: Empty, responseObserver: StreamObserver<ListContainerLog>) {

       // val result = normalizeLog
        val result = containerBehaiverCollector.normalizeLog
        val response = ListContainerLog.newBuilder()

        if (result.isEmpty()){
            logger.info("Data is Empty")
        }else{

            result.forEach outer@{
                //Filter Agent Rawlog with mutiple containerID
                val eachContianerLog = it
                Configure.AgentID.forEach {
                    if(eachContianerLog.containerid.contains(it)){

                    }else{
                        val objectMapper = ObjectMapper()
                        val containerLogString = objectMapper.writeValueAsString(it)
                        response.addContainerLog(containerLogString)
                        return@outer
                    }
                }
            }
        }
        responseObserver.onNext(response.build())
        responseObserver.onCompleted()
    }

    override fun deinit(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {
        logger.info("Deinit has no function")
        val reponse :BooleanStrut = BooleanStrut.newBuilder().setScucces(true).build()
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()

    }

    override fun discoveryLiveContainer(request: Empty, responseObserver: StreamObserver<ContainerList>) {

        val liveContainers = containerBehaiverCollector.discoveryLiveContainer()
        logger.info("valid container num ${liveContainers.size}")
        val response = ContainerList.newBuilder()
        //Transform to grpc format
        liveContainers.forEach {
            val objectmapper = ObjectMapper()
            val containerstring = objectmapper.writeValueAsString(it)
            response.addContainer(containerstring)
        }
        responseObserver.onNext(response.build())
        responseObserver.onCompleted()
    }


}