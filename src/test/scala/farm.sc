
import nl.about42.experiments.foodsl._


val farmer: Farmer = new Farmer()

farmer.make("apple")

val food: Food = farmer.vegetarian().make("apple").withEnergy(80).apply()

food.show()

val otherFarmer = farmer make "apple" withEnergy 65 vegetarian

otherFarmer.apply().show()

val otherFarmer2 = Farmer.apply() make "pear"

otherFarmer2.apply().show()


//val food: Food = Farmer make "apple" withEnergy 80.0
