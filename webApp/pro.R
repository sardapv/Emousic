# Importing the test
training_set = read.csv('finaldata.csv')
args<-commandArgs(TRUE)
#import library
library(randomForest)
set.seed(123)
#train classifier
classifier = randomForest(x = training_set[-5],
                          y = training_set$Song,
                          ntree = 50)
library(caTools)
#read input
test = data.frame(Emot=args[1],Pact=args[2],Mact=args[3],Session=args[4],Song='x')
#binding solution from my SO question
test <- rbind(training_set[1, ] , test)
test <- test[-1,]
#predict output
y_pred = predict(classifier, test[-5])
#print output
song = paste('{"', 'song": ','"',y_pred,'"',"}",sep="")
cat(song)
