package nl.about42.configutils

import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConversions._
/**
  * Created by jml on 15-3-16.
  */
class Reader {
  val config = ConfigFactory.load("application")

  val dataConfig = config.getConfig("testdata")

  def getAllConfigs: Config = {
    readConfigFiles(getStringList("dataFiles"))
  }

  private def readConfigFiles(configFiles: List[String] ): Config = configFiles match {
    case (head :: tail) => ConfigFactory.parseResources(head).withFallback(readConfigFiles(tail))
    case (_) => ConfigFactory.empty()
  }

  private def getStringList( configKey: String): List[String] = {
    dataConfig.getStringList(configKey).toList
  }

}
