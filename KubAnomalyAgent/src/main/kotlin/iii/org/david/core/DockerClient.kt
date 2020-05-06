package iii.org.david.core


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder

import interfaces.DockerClientApi

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import structure.DavidAgentInformation

import structure.HDData

import utils.ExternalProcessHandleError
import java.io.InputStream
import java.nio.file.Files

import java.nio.file.Paths


class DockerClient :DockerClientApi {


    var loggers: Logger = LogManager.getLogger(DockerClient::class.java!!.getName())

    var config =  DefaultDockerClientConfig.createDefaultConfigBuilder()
    .withDockerHost("unix:///var/run/docker.sock")
    .build()

    var dockerClient = DockerClientBuilder.getInstance(config)
    .build()
    override fun getDAVIDAgentList(): HashSet<DavidAgentInformation>{
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun getDockerversion(){
        val version = dockerClient.versionCmd().exec()
        loggers.info("version "+version.version)
    }
    override fun listimage():Boolean{
        try {
            var listimage = dockerClient.listImagesCmd().exec()
            listimage.forEach { it -> println(it.repoTags[0]) }
        }catch (e:Exception){
            loggers.info("listIamge Failed: "+e.toString())
            return false
        }
        return true
    }
    override fun loadImage(imagepath:String,imagename:String):String{
        try {
            loggers.info("Begin to Load Image")
            var uploadStream :InputStream = Files.newInputStream(Paths.get(imagepath))
            dockerClient.loadImageCmd(uploadStream).exec()

            var image :Image?= findImageWithId(imagename, dockerClient.listImagesCmd().exec())
            loggers.info("Load Image Complete")
            val imageId = if (image != null) image.id else "No"
            return imageId
        }catch (e:Exception){
            loggers.error("Load Image Failed "+e.toString())
        }
        return "No"
    }
    override fun findImageWithId(id: String, images: List<Image>): Image? {
        for (image in images) {
            if (id == image.repoTags[0]) {
                return image
            }
        }
        return null
    }

    override fun listContainerID(Off:Boolean):List<Container>{
        var containers = dockerClient.listContainersCmd().withShowAll(Off).exec()
        return containers
    }

    override fun containerResource():ArrayList<HDData>{

        val objectMapper= ObjectMapper().registerModule(KotlinModule())
        var containerResourcesData = arrayListOf<HDData>()
        var queryStatusCommand="timeout 6 docker stats --no-stream --format \"{{.Container}}: {{.CPUPerc}} : {{.MemUsage}} : {{.MemPerc}} : {{.NetIO}} : {{.BlockIO}} ::\"  "
        var queryStatusCommandrunner = ExternalProcessHandleError()
        try {
            loggers.info("Ready to start container $queryStatusCommand")
            queryStatusCommandrunner.exec(queryStatusCommand,true,true,30000)
        }catch (e:Exception){
            loggers.error("Start Contaner error: "+e.toString())
        }

        parseResourceDataForTer(queryStatusCommandrunner.outputMsg,containerResourcesData)
        loggers.info("StdOut: "+queryStatusCommandrunner.outputMsg)
        loggers.info("errorOut: "+queryStatusCommandrunner.errorMsg)
       // parseResourceData(containerResourcesData)
       // val account = objectMapper.writeValue(json, HDData::class.java)

        return containerResourcesData
    }

    fun findContainerIdWithImage(imagename:String):String{
        var containerId = "No"
        var containers = dockerClient.listContainersCmd().exec()
        for(eachContianer:Container in containers){
            if(eachContianer.image.contains(imagename)){
                containerId= eachContianer.id
            }
        }
        return containerId
    }

    fun startContainer(imagename:String,startcommand:String):String{
        var containerId :String ="No"
        var dockerunner = ExternalProcessHandleError()
        try {
            loggers.info("Ready to start container $startcommand")
            dockerunner.exec(startcommand,true,true,120000)
        }catch (e:Exception){
            loggers.error("Start Contaner error: "+e.toString())
            return "Error"
        }
        loggers.info("StdOut: "+dockerunner.outputMsg)
        loggers.info("errorOut: "+dockerunner.errorMsg)
        containerId = findContainerIdWithImage(imagename)
        loggers.info("Start Complete ID is $containerId")
        return containerId.substring(0,11)
    }

    fun removeImage(imagename: String):Boolean{
        loggers.info("Begin to Remove Image: "+imagename)
        try {
            dockerClient.removeImageCmd(imagename).exec()
        }catch (e:Exception){
            loggers.info("Remove Iamge Error: "+e.toString())
            return false
        }
        loggers.info(" Remove Image: $imagename Compelete")
        return true
    }
    fun stopAndRemoveContainer(contianernamae:String):Boolean{
        loggers.info("Begin to remove Container $contianernamae")
        try {
            dockerClient.stopContainerCmd(contianernamae).withTimeout(2).exec()
            dockerClient.removeContainerCmd(contianernamae).exec()
        }catch (e:Exception){
            loggers.info("Container Error: "+e.toString())
            return false
        }
        loggers.info("Container remove complete $contianernamae")
        return true
    }
    fun parseResourceDataForTer(data:String,resourceData:ArrayList<HDData>):Boolean{
        var unfilterData = arrayListOf<HDData>()
        loggers.info(data)
        var eachContainer = data.split("::")
        eachContainer.forEach {
            var eachInfo = it.split(":")
            var counter = 0
            var containerName :String = ""
            var cpu :String = ""
            var MEMUSAGE :String = ""
            var NETIO :String = ""
            var BLOCKIO :String = ""
            var MEMUSAGEPercentage :String = ""
            eachInfo.forEach {
                var content = it.replace(" ","")

                if(content.length>0){
                    when(counter%7){
                        0 -> {
                            containerName =content
                            //loggers.info("CONTAINER ${containerName}")
                        }
                        1 ->{
                            cpu = content
                           // loggers.info("CPU% ${cpu}")
                        }
                        2 -> {
                            MEMUSAGE = content
                           // loggers.info("MEMUSAGE/LIMITMEM% ${MEMUSAGE}")
                        }
                        3 -> {
                            MEMUSAGEPercentage = content
                           // loggers.info("MEMUSAGE/LIMITMEM% ${MEMUSAGEPercentage}")
                        }
                        4 -> {
                            NETIO = content
                           // loggers.info("NETI/O ${NETIO}")
                        }
                        5 -> {
                            BLOCKIO = content
                            //loggers.info("BLOCKI/O ${BLOCKIO}")
                        }
                    }
                    if((counter%6)==5){
                        var eachData = HDData("",containerName,cpu,MEMUSAGE,MEMUSAGEPercentage,NETIO,BLOCKIO)
                        loggers.info("Each Data $eachData")
                        if(!containerName.contains("CONTAINER")){
                            unfilterData.add(eachData)
                        }

                    }
                    counter ++
                }
            }
        }

        //Filter same container data
        unfilterData.forEach {
            if(finddata(it.containerName,resourceData)){

            }else{
                resourceData.add(it)
            }
        }
        return true
    }
  
    fun finddata(dataWantToFind:String,dataList:ArrayList<HDData>):Boolean{
        var result = false
        dataList.forEach {
            if(it.containerName.contains(dataWantToFind)){
                result =true
            }
        }
        return result
    }
}


