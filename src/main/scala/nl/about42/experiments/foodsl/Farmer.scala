package nl.about42.experiments.foodsl

/**
  * Created by Jean-Marc van Leerdam on 2016-03-29
  */

object Farmer {
  def apply(): Farmer = new Farmer

}


class Farmer(foodName: String = "", nutriValue: Double = 0.0, isVegetarian: Boolean = false) {

  def make(name: String) = new Farmer(name, nutriValue, isVegetarian)

  def vegetarian() = new Farmer(foodName, nutriValue, true)

  def withEnergy( nutritionalValue: Double) = new Farmer(foodName, nutritionalValue, isVegetarian)

  def apply(): Food = new Food(foodName, nutriValue, isVegetarian)

  //implicit def farmerProducesFood( farmer: Farmer): Food = new Food(farmer.foodName, farmer.nutriValue, farmer.isVegetarian)

}
