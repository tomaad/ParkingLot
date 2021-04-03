# ParkingLot

## Sample Input File
```
Create_parking_lot 6
Park KA-01-HH-1234 driver_age 21
Park PB-01-HH-1234 driver_age 21
Slot_numbers_for_driver_of_age 21
Park PB-01-TG-2341 driver_age 40
Slot_number_for_car_with_number PB-01-HH-1234
Leave 2
Park HR-29-TG-3098 driver_age 39
Vehicle_registration_number_for_driver_of_age 18
```

## Sample Output File
```
Created parking of 6 slots
Car with vehicle registration number "KA-01-HH-1234" has been parked at slot number 1
Car with vehicle registration number "PB-01-HH-1234" has been parked at slot number 2
Vehicles with driver age = 21 : [KA-01-HH-1234,PB-01-HH-1234]
Slots for drivers with age = 21: [1,2]
Car with vehicle registration number "PB-01-TG-2341" has been parked at slot number 3
2
Slot number 2 vacated, the car with vehicle registration number "PB-01-HH-1234" left the space, the driver of the car was of age 21
Car with vehicle registration number "HR-29-TG-3098" has been parked at slot number 2
Vehicles with driver age = 18 : []
```


## System Requirements
1. JAVA JDK 1.8(or higher) should be installed (Guide here: https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html)
2. Maven should be installed (Guide here: https://maven.apache.org/install.html)
3. Git should be installed to clone the repo from github remote repo (Guide here: https://github.com/git-guides/install-git#:~:text=To%20install%20Git%2C%20navigate%20to,installation%20by%20typing%3A%20git%20version%20.)

## Steps to run the application:
1. Open terminal and type following commands
2. cd <directory path where git repo should be cloned> e.g. cd ~
3. git clone https://github.com/tomaad/ParkingLot.git
4. cd ParkingLot 
5. mvn clean install assembly:single
6. java -jar target/Parking-1.0-SNAPSHOT-jar-with-dependencies.jar "<INPUT_FILE_NAME>". Note: INPUT_FILE_NAME is the path to the text file contaning commands to run. Assumption: file path doesn't have spaces. If INPUT_FILE_NAME is not passed in the arguments, then start typing the commands directly in the terminal.
7. All the commands in the INPUT_FILE_NAME will be run sequentially. After all the commands run successfully, app will wait for commands in terminal.
8. Type Commands in the terminal to run on Parking Lot.
9. Type ```quit``` to exit the terminal

## Output Messages for edge cases

Message | Meaning
------- | --------
**Parking Lot is Full!** | `Park` command is run and the parking slot is full
**The vehicle with ID '%s' is already parked at Slot with ID '%s'! Duplicate Registration Id! Call Police!** | `Park` command is run and the registration ID of the new vehicle is already available in parking records i.e.a vehicle is already parked with the same registration ID
**Slot with id '%s' is vacant!** | `Leave` command is run for a slot which is already vacant
**Slots for drivers with age = %s: [%s]** | `Slot_numbers_for_driver_of_age command` is run. <br><br>When this command is run, first the list of registration ID of vehicles with driver age is printed then the list of slots is printed in the same order e.g.<br><br> <ul> <li> ```Vehicles with driver age = 21 : [KA-01-HH-1234,PB-01-HH-1234]``` <li> ```Slots for drivers with age = 21: [1,2]``` </ul><br> The lists are printed in the increasing order of slot at which the vehicle is parked
**Vehicles with driver age = %s : [%s]** | `Vehicle_registration_number_for_driver_of_age` is run <br>The lists are printed in the increasing order of slot at which the vehicle is parked
**Car with Registration ID \"%s\" does not exist in the Parking Lot!** | `Slot_number_for_car_with_number` is run with a registration ID and no vehicle is parked with that registration ID



