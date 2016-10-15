import com.typesafe.config.{ConfigFactory, Config}
import nl.about42.configutils.Reader

val configFiles = List("SampleTree.json", "application")

def readConfigFiles(configFiles: List[String] ): Config = configFiles match {
  case (head :: tail) =>
    ConfigFactory.parseResources( head).withFallback(readConfigFiles(tail))
  case (_) => ConfigFactory.empty()
}

val config = readConfigFiles(configFiles )

config

val reader = new Reader()

reader.getAllConfigs

