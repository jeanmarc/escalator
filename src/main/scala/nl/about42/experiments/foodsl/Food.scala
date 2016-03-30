package nl.about42.experiments.foodsl

/**
  * Created by Jean-Marc van Leerdam on 2016-03-29
  */

class Food( name: String, nutritionalValue: Double, vegetarian: Boolean) {

  def show(): Unit = vegetarian match {
      case true => println(s"I am a vegetarian ${name} with ${nutritionalValue} calories")
      case _    => println(s"I am a ${name} with ${nutritionalValue} calories")
    }

}
