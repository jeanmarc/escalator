package nl.about42.configutils

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by jml on 16-3-16.
  */
class ReaderTest extends WordSpec with Matchers{

  "Reader object" should {
    "read a complete configuration" in {
      val reader = new Reader()

      val result = reader.getAllConfigs

      result.getString("name").shouldBe("")
      if (result.hasPath("data")){
        result.getString("data").shouldBe("")
      }
      val children = result.getConfigList("children")

      children.size().shouldBe(3)

      val apis = children.get(0)

      apis.getString("name").shouldBe("apis")

      val foobar = apis.getConfigList("children").get(0).getConfigList("children").get(0)

      foobar.getString("name").shouldBe("foobar")
      foobar.getString("data").shouldBe("""{"some":"data"}""")

    }

  }
}
