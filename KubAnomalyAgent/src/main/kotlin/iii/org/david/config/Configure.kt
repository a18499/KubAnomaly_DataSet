package iii.org.david.config

import com.typesafe.config.ConfigFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class Configure constructor(var configurePath: String) {

    var config = ConfigFactory.parseFile(File(configurePath))
    val DAVIDMasterIP: String
    val MQURL: String
    var DavidAgentIP: String
    var UI: String
    var VMVersion: String
    init {
        this.UI = this.config.getString("UI")
        //this.config = this.config.resolve()
        this.DAVIDMasterIP = this.config.getString("DAVIDMASTERIP")
        //print("this.DAVIDMasterIP: ${this.DAVIDMasterIP}")
        this.MQURL = this.config.getString("MQ_URL")
        //print("this.MQ_URL: ${this.MQURL}")
        this.DavidAgentIP = this.config.getString("DAVIDAGENTIP")
        this.VMVersion = this.config.getString("VM")
    }

    companion object {
        lateinit var configure: Configure

        fun init() {
            if (Files.exists(Paths.get("./Config/configure.conf"))) {
                configure = Configure("./Config/configure.conf")
            } else {
                println("Configure Not Found")
            }

        }

        fun getUIOption(): String {
            return this.configure.UI
        }

        fun getDAVIDMasterIP(): String {
            return this.configure.DAVIDMasterIP
        }

        fun getMQURL(): String {
            return this.configure.MQURL
        }

        fun getDavidAgentIP(): String {
            return this.configure.DavidAgentIP
        }

        fun setDavidAgentIP(ip: String) {
            this.configure.DavidAgentIP = ip
        }

        fun getVMVersion():Boolean{
            return this.configure.VMVersion.toBoolean()
        }

        val davidMasterPort = "20001"
        val dockerContainerdaemonPort = "23001"
        val dockerContainerInformationPort = "23002"
        val dockerRawLogCollectPort = "23003"
        val davidControllerPort = "23005"
        val MessageLabel = "AgentMaster"

        val Queue = "Agent"
        var AgentID = HashSet<String>()
        var agentSwitch = false
        var agentStatus = ""
    }
}