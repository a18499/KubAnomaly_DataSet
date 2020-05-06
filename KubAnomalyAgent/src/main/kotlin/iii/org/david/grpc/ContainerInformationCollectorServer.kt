package iii.org.david.grpc
/*
* This Program used to collect container hardware information.
* Is a grpc api call by other process or other machine
*
* */
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.Empty
import iii.org.david.interfaces.core.DockerInformationCollector
import interfaces.BooleanStrut
import interfaces.ContainerInformationCollectServiceGrpc
import interfaces.ListContainerHDInformation
import interfaces.StringStruct
import io.grpc.stub.StreamObserver


class ContainerInformationCollectorServer:ContainerInformationCollectServiceGrpc.ContainerInformationCollectServiceImplBase() {
    private val logger: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(ContainerInformationCollectorServer::class.java.getName())
    private val informationCollector = DockerInformationCollector()

    override fun init(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {
        informationCollector.init()
        logger.info("ContainerInformationCollectorServer init Complete")
        val response :BooleanStrut = BooleanStrut.newBuilder().setScucces(true).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun deinit(request: Empty?, responseObserver: StreamObserver<BooleanStrut>?) {


    }

    override fun getIP(request: Empty, responseObserver: StreamObserver<StringStruct>) {
        val ip = informationCollector.getIP()
        val ipString = StringStruct.newBuilder().setResult(ip).build()

        responseObserver.onNext(ipString)
        responseObserver.onCompleted()

    }

    override fun getResource(request: Empty, responseObserver: StreamObserver<ListContainerHDInformation>) {
        val informationResources = informationCollector.getResource()
        val response = ListContainerHDInformation.newBuilder()

        if(informationResources.isEmpty()){
            logger.info("Resource is empty")
        }else{
            //Transfer to string data from data class
            informationResources.forEach {
                val objectmapper = ObjectMapper()
                val hdinformation = objectmapper.writeValueAsString(it)
                logger.info("Add ContainerHDInformation ${hdinformation}")
                response.addContainerHDInformation(hdinformation)
            }
        }

        responseObserver.onNext(response.build())
        responseObserver.onCompleted()
    }




}
