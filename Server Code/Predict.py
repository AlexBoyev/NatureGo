#Importing Libraries

import numpy as np

import cv2
import DeepLearning
import pickle
import numpy as np
from keras.models import load_model
import tensorflow as tf
import keras
from tensorflow.python.keras.backend import set_session
from tensorflow.python.keras.models import load_model

class predictImage:

    def __init__(self):
        self.sess = tf.Session()
        self.graph = tf.get_default_graph()
        set_session(self.sess)
        self.model = load_model("Model.h5")
        self.model.load_weights("Model_weights.h5")
        self.animalClasses = self.load_obj("animalClasses")

    def load_obj(self,name ):
        with open(name + '.pickle', 'rb') as f:
            return pickle.load(f)

    def returnModel(self):
        return self.model

    def fixPrint(self,result):
        animalClasses = self.load_obj("animalClasses")
        label_index = np.argmax(result)
        acc = np.max(result)
        animalName = list(animalClasses.keys())[list(animalClasses.values()).index(label_index)]
        return animalName,str(acc)


# get path till implementation
    def preprocess(self,picture,size):
        data = []
        image = cv2.resize(picture,(size,size))
        data.append(image)
        data = np.array(data,dtype="float") / 255.0
        return data


    def predict(self,picture,image_size):
        with self.graph.as_default():
            set_session(self.sess)
            data_test = self.preprocess(picture,image_size)
            prediction = self.model.predict(data_test,batch_size=1)
            return prediction

    def modelHolder(self):
        return self.model

    def animalClassesHolder(self):
        return self.animalClasses

    def returnGraph(self):
        return self.graph

    def returnSess(self):
        return self.sess

#for animal in os.listdir("Testset"):
#   predictions = predict(cv2.imread("Testset" + '/' + animal),DeepLearning.image_size)
#   fixPrint(predictions,animal.split('.')[0])

