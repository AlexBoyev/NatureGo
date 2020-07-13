#Importing Libraries
import os
from tqdm import tqdm
import numpy as np
from sklearn.utils import class_weight
from sklearn import preprocessing
from keras import optimizers
from keras.preprocessing.image import ImageDataGenerator
from keras.models import load_model
import itertools
from keras.layers import Dense, Activation, Conv2D, BatchNormalization, MaxPooling2D, Dropout, Flatten
from keras.models import Sequential
from sklearn.metrics import classification_report
from keras.utils import np_utils
from sklearn.model_selection import train_test_split
import matplotlib.pylab as plt
from sklearn.metrics import confusion_matrix

import cv2
import random
import pickle

global globalDictValues,image_size,num_of_classes,leClasses,animalClasses,dataGlobal,labelsGlobal,historyGlobal,x_testGlobal,y_testGlobal
globalDictValues = {}
num_of_classes = 0
leClasses = 0
animalClasses = 0
dataGlobal = 0
labelsGlobal = 0
historyGlobal = 0
x_testGlobal = 0
y_testGlobal = 0
#Variables
image_size = 210
batch = 32
epochs = 100
learning_rate = 0.01
momentum = 0.9
dataset = "DatasetFixed"

def save_obj(obj, name ):
    with open(name+'.pickle', 'wb') as f:
        pickle.dump(obj, f)

def load_obj(name ):
    with open(name + '.pickle', 'rb') as f:
        return pickle.load(f)

def loadData():
    animals = np.load("data.npy")
    labels = np.load("labels.npy")
    return animals, labels

def splitData(data,labels):
    global num_of_classes
    x_train, x_test, y_train, y_test = train_test_split(data, labels, stratify=labels,
                                                        test_size=0.25)
    class_weights = class_weight.compute_class_weight('balanced',
                                                      np.unique(y_train),
                                                      y_train)
    d1 = dict()
    for i in range(num_of_classes):
        d1[i] = class_weights[i]

    return x_train, x_test, y_train, y_test,d1

def normalize(data,labels):
    labels = np.array(labels)
    # Label Encode labels
    le = preprocessing.LabelEncoder()
    labels = le.fit_transform(labels)
    res = get_integer_mapping(le)
    #Normalize
    data = np.array(data, dtype="float32") / 255.0
    global dataGlobal,labelsGlobal
    labelsGlobal = labels
    dataGlobal = data
    np.save("data",data)
    np.save("labels",labels)
    save_obj(res,"animalClasses")
    #save_obj(le,"leClasses")
    global leClasses,animalClasses
    leClasses = le
    animalClasses = res
    return data,labels,le,res

#Get File path
def filesall(path):
    global globalDictValues
    filename = []
    all_folder = os.listdir(path)
    for f in all_folder:
        mylist = os.listdir(path+'/'+f)
        filename.extend([path+'/'+f+'/'+s for s in mylist])
        globalDictValues[f] = mylist
    random.seed(42)
    random.shuffle(filename)
    return filename

#Read Images
def readimages(images_path_all):
    global globalDictValues,num_of_classes
    data = []
    label = []
    random.seed(42)
    random.shuffle(images_path_all)
    for imagePath in tqdm(images_path_all):
        image = cv2.imread(imagePath)
        image = cv2.resize(image, (image_size, image_size))
        data.append(image)
        label.append(imagePath.split('/')[1])

    max_key = max(globalDictValues, key=lambda x: len(set(globalDictValues[x])))
    keys = set(globalDictValues.keys())
    num_of_classes = len(keys)
    for key in keys.difference(max_key):
        if len(globalDictValues[key]) < len(globalDictValues[max_key]):
            for i in range(len(globalDictValues[max_key]) - len(globalDictValues[key])):
                image = random.choice(globalDictValues[key])
                image = cv2.imread("Dataset/" + key + "/" + image)
                image = cv2.resize(image, (image_size, image_size))
                data.append(image)
                label.append(key)
    globalDictValues.clear()
    return data, label

def get_integer_mapping(le):
    '''
    Return a dict mapping labels to their integer values
    from an SKlearn LabelEncoder
    le = a fitted SKlearn LabelEncoder
    '''
    res = {}
    for cl in le.classes_:
        res.update({cl:le.transform([cl])[0]})

    return res




def toCategorical(y_test,y_train,num_classes):
    y_train = np_utils.to_categorical(y_train, num_classes)
    y_test = np_utils.to_categorical(y_test, num_classes)
    return y_train, y_test


def buildModel(x_train,y_train,x_test,y_test,num_classes,d1,batch_size,epochs):
    chanDim = -1
    model = Sequential()
    model.add(Conv2D(32, (3, 3), padding="same", input_shape=(image_size, image_size, 3)))
    model.add(Activation('relu'))
    model.add(BatchNormalization(axis=chanDim))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))

    model.add(Conv2D(64, (3, 3), padding="same"))
    model.add(Activation('relu'))
    model.add(BatchNormalization(axis=chanDim))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))

    model.add(Conv2D(64, (3, 3), padding="same"))
    model.add(Activation('relu'))
    model.add(BatchNormalization(axis=chanDim))
    model.add(MaxPooling2D(pool_size=(2, 2)))


    model.add(Conv2D(128, (3, 3), padding="same"))
    model.add(Activation('relu'))
    model.add(BatchNormalization(axis=chanDim))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))

    model.add(Conv2D(128, (3, 3), padding="same"))
    model.add(Activation('relu'))
    model.add(BatchNormalization(axis=chanDim))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))

    model.add(Conv2D(128, (3, 3), padding="same"))
    model.add(Activation('relu'))
    model.add(BatchNormalization(axis=chanDim))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))

    model.add(Flatten())
    model.add(Dense(512))
    model.add(Activation('relu'))
    model.add(BatchNormalization())
    model.add(Dropout(0.25))
    model.add(Dense(num_classes, activation = 'softmax'))

    model.summary()

    #data augmentation
    datagen = ImageDataGenerator(
        featurewise_center=False,
        samplewise_center=False,
        featurewise_std_normalization=False,
        samplewise_std_normalization=False,
        zca_whitening=False,
        rotation_range=15,
        width_shift_range=0.1,
        height_shift_range=0.1,
        horizontal_flip=True,
        vertical_flip=False
        )
    datagen.fit(x_train)

    #Training
    sgd = optimizers.SGD(lr=learning_rate)
    model.compile(loss='categorical_crossentropy', optimizer=sgd, metrics = ['accuracy'])
    history = model.fit_generator(datagen.flow(x_train, y_train, batch_size=batch_size),
                        steps_per_epoch=x_train.shape[0] // batch_size, epochs=epochs,
                        verbose=1, validation_data=(x_test, y_test), class_weight=d1)
    model.save("Model.h5")
    model.save_weights("Model_weights.h5")
    return model,history


def plot_confusion_matrix(cm,
                          target_names,
                          title='Confusion matrix',
                          cmap=None,
                          normalize=True):


    accuracy = np.trace(cm) / np.sum(cm).astype('float')
    misclass = 1 - accuracy

    if cmap is None:
        cmap = plt.get_cmap('Blues')

    plt.figure(figsize=(8, 6))
    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title)
    plt.colorbar()

    if target_names is not None:
        tick_marks = np.arange(len(target_names))
        plt.xticks(tick_marks, target_names, rotation=45)
        plt.yticks(tick_marks, target_names)

    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]


    thresh = cm.max() / 1.5 if normalize else cm.max() / 2
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        if normalize:
            plt.text(j, i, "{:0.4f}".format(cm[i, j]),
                     horizontalalignment="center",
                     color="white" if cm[i, j] > thresh else "black")
        else:
            plt.text(j, i, "{:,}".format(cm[i, j]),
                     horizontalalignment="center",
                     color="white" if cm[i, j] > thresh else "black")


    plt.tight_layout()
    plt.ylabel('True label')
    plt.xlabel('Predicted label\naccuracy={:0.4f}; misclass={:0.4f}'.format(accuracy, misclass))
    plt.show()

def makeGraph(history):
    acc = history.history['accuracy']
    val_acc = history.history['val_accuracy']
    loss = history.history['loss']
    val_loss = history.history['val_loss']
    epochs = range(1, len(acc) + 1)

    plt.title('Training and validation accuracy')
    plt.plot(epochs, acc, 'red', label='Training acc')
    plt.plot(epochs, val_acc, 'blue', label='Validation acc')
    plt.legend()

    plt.figure()
    plt.title('Training and validation loss')
    plt.plot(epochs, loss, 'red', label='Training loss')
    plt.plot(epochs, val_loss, 'blue', label='Validation loss')

    plt.legend()

    plt.show()

def makeNPYFiles(path):
    animalCheck = os.path.isfile("data.npy")
    labelCheck = os.path.isfile("labels.npy")
    if not animalCheck or not labelCheck:
        train_image_names = filesall(path)
        data, labels = readimages(train_image_names)
        data, labels, le, res = normalize(data, labels)

def runModel(path,batch_size,epochs):
    history = 0
    animalCheck = os.path.isfile("data.npy")
    labelCheck = os.path.isfile("labels.npy")
    if not animalCheck or not labelCheck:
        train_image_names = filesall(path)
        data, labels = readimages(train_image_names)
        data, labels, le, res = normalize(data, labels)

    else:
        data, labels = loadData()


    load = os.path.isfile("Model.h5")
    if not load:
        x_train, x_test, y_train, y_test,animalDict = splitData(data,labels)
        num_of_classes = len(np.unique(labels))
        y_train,y_test = toCategorical(y_test,y_train,num_of_classes)
        model,history = buildModel(x_train,y_train,x_test,y_test,num_of_classes,animalDict,batch_size,epochs)
        global historyGlobal,x_testGlobal,y_testGlobal
        #save_obj(history,"history")
        #save_obj(x_test,"x_test")
        #save_obj(y_test,"y_test")
        historyGlobal = history
        x_testGlobal = x_test
        y_testGlobal =y_test

    else:
        model = load_model("Model.h5")
        model.load_weights("Model_weights.h5")
    return model


def startTrain():
    global historyGlobal,y_testGlobal,x_testGlobal,leClasses
    #makeNPYFiles("Dataset")
    model = runModel(dataset,batch,epochs)
    predictions = model.predict(x_testGlobal, batch_size=1)
    print(classification_report(y_testGlobal.argmax(axis=1), predictions.argmax(axis=1), target_names=list(leClasses.classes)))
    makeGraph(historyGlobal)
    predictions = [i.argmax() for i in predictions]
    y_true = [i.argmax() for i in y_testGlobal]
    cm = confusion_matrix(y_pred=predictions, y_true=y_true)
    plot_confusion_matrix(cm, normalize=True, target_names=leClasses.classes_)

#startTrain()
