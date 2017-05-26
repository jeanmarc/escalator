
import nl.about42.experiments.foodsl._


val farmer: Farmer = new Farmer()

farmer.make("apple")

val food: Food = farmer.vegetarian().make("apple").withEnergy(80)

food.show()

val otherFarmer = farmer make "apple" withEnergy 65 vegetarian

otherFarmer.show()

val otherFarmer2 = Farmer.apply() make "pear"

otherFarmer2.show()

farmer make "blueberry"

val myFood: Food = Farmer() make "blueberry" withEnergy 123 vegetarian

val my2ndFood: Food = Farmer() vegetarian() withEnergy 123  make "blueberry"

myFood.show()

my2ndFood.show()

//val food: Food = Farmer make "apple" withEnergy 80.0
