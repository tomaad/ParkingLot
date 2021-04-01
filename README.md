# ParkingLot

#System Requirements
1. JAVA JDK 1.8(or higher) should be installed (Guide here: https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html)
2. Maven should be installed (Guide here: https://maven.apache.org/install.html)
3. Git should be installed to clone the repo from github remote repo (Guide here: https://github.com/git-guides/install-git#:~:text=To%20install%20Git%2C%20navigate%20to,installation%20by%20typing%3A%20git%20version%20.)

#Steps to run the application:
1. Open terminal and type following commands
2. cd <directory path where git repo should be cloned> e.g. cd ~
3. git clone https://github.com/tomaad/ParkingLot.git
4. cd ParkingLot 
5. mvn clean compile assembly:single
6. java -jar target/Parking-1.0-SNAPSHOT-jar-with-dependencies.jar "<INPUT_FILE_NAME>"

#Output Messages for edge cases
Output messages in edge cases:
- Parking Lot is Full!
- The vehicle with ID '%s' is already parked at Slot with ID '%s'! Duplicate Registration Id! Call Police!
- Slot with id '%s' is vacant!
- Slots for drivers with age = %s: [%s]
- Vehicles with driver age = %s : [%s]
- Car with Registration ID \"%s\" does not exist in the Parking Lot!

