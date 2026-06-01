# Tous les tests
mvn test

# Tests avec rapport de couverture
mvn clean test jacoco:report
C:\Users\anas0\Desktop\4ISI\SpringBoot\e-challenge-spring\target\site\jacoco\index.html

# Tests par catégorie
mvn test -Dtest=*ServiceTest

# Ouvre le rapport de couverture
start target/site/jacoco/index.html