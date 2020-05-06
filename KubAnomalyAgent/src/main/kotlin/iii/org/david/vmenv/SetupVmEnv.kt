package iii.org.david.vmenv

import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.ExternalProcess
import utils.ExternalProcessHandleError

class SetupVmEnv {
    var logger: Logger = LogManager.getLogger(SetupVmEnv::class.java)
    val checkingCommand = "sudo falco --version"
    fun init():Boolean{

        logger.info("SetupVmEnv init complete")

        return true
    }

    fun checkingVmEnv():Boolean{
        logger.info("Begin checking vm environment")
        val falcoChecker = ExternalProcessHandleError()
        falcoChecker.exec(checkingCommand,true,true,3000)
        val terminalMsg = falcoChecker.outputMsg
        val terminalMsgError = falcoChecker.errorMsg
        logger.info("terminalMsg: $terminalMsg")
        logger.info("terminalMsgError: $terminalMsgError")
        if(terminalMsgError.toString().contains("falco command not found")||terminalMsgError.toString().contains("command")){
            logger.info("Env don't have falco ")
            logger.info("Begin to install falco ")
            installsysdigAndFalco()
        }else{
            logger.info("Checking OK")
            return true
        }
        return true
    }

    fun installsysdigAndFalco():Boolean{
        val falcoInstaller = ExternalProcessHandleError()

        logger.info("exec install script")
        falcoInstaller.exec("sudo ./install_falco.sh",true,true,300000)
        logger.info("Script result ${falcoInstaller.outputMsg}")
        logger.info("Script result error ${falcoInstaller.errorMsg}")
        falcoInstaller.exec(checkingCommand,true,true,30000)

        logger.info("Checking version ${falcoInstaller.outputMsg}")
        if(falcoInstaller.errorMsg!=null){
            logger.error("Eror ${falcoInstaller.errorMsg}")
        }
        logger.info("Falco install complete")

        return true
    }

    fun updateRule():Boolean{
        val falcoRulePath = "/etc/falco/"
        val ruleMover = ExternalProcessHandleError()

        ruleMover.exec("sudo cp falco.yaml $falcoRulePath",true,true,30000)
        if(ruleMover.errorMsg != null){
            logger.error("move falco config file Error: ${ruleMover.errorMsg}")
        }
        ruleMover.exec("sudo cp falco_rules.yaml $falcoRulePath",true,true,30000)
        if(ruleMover.errorMsg != null){
            logger.error("move falco rules Error: ${ruleMover.errorMsg}")
        }

        logger.info("update rule complete")
        return true
    }

    fun removeFalco(){
        val falcoUninstaller = ExternalProcessHandleError()
        falcoUninstaller.exec("sudo apt-get remove falco ",true,true,30000)
    }
}