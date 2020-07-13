import smtplib
import flask
import pymongo
import datetime
import werkzeug
import DeepLearning
import cv2
import numpy as np
import pickle
import Predict
from skimage import io
from skimage import img_as_ubyte


app = flask.Flask(__name__)

localHost = "127.0.0.1"
port = "27017"
hostAndPort = localHost + ":" + port
entireMongoDBhost = "mongodb://" + hostAndPort + "/"

myclient = pymongo.MongoClient(entireMongoDBhost)
natureGoDatabase = myclient["NatureGo"]
userCollection = natureGoDatabase["User"]
HikesCollection = natureGoDatabase["Hikes"]
image_size = DeepLearning.image_size
predictClass = Predict.predictImage()


def preprocess(picture, size):
    data = []
    image = cv2.resize(picture, (size, size))
    data.append(image)
    data = np.array(data, dtype="float") / 255.0
    return data

def load_obj(name):
    with open(name + '.pickle', 'rb') as f:
        return pickle.load(f)

@app.route("/LoginCheck", methods=['GET', 'POST'])
def LoginCheck():

    jsondata = flask.request.get_json()
    if checkUserExistence(jsondata["Phone"]) == True:

        myQuery = { "Phone": jsondata["Phone"]}
        myDoc = userCollection.find(myQuery)
        for value in myDoc:
            if jsondata["Password"] == value["Password"]:
                return {"Status":value["Status"], "Score":value["Score"],"Phone":value["Phone"], "Name": value["Name"],
                        "Animals":value["Animals"]}
        return "False"
    return "False"

@app.route("/getAnimals",methods=['GET','POST'])
def getAnimals():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["loginPhone"]}
    mydoc = userCollection.find(myQuery)
    for value in mydoc:
        return value["Animals"]

@app.route("/getPhoto", methods=['GET','POST'])
def getPhoto():
    imagefile = flask.request.files['image']
    image = io.imread(imagefile)
    cv_image = img_as_ubyte(image)
    filename = werkzeug.utils.secure_filename(imagefile.filename)
    result = predictClass.predict(cv_image,image_size)
    animalName,accuracy = predictClass.fixPrint(result)

    return animalName



@app.route("/AddAnimal",methods=['GET','POST'])
def AddAnimal():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["loginPhone"]}
    newvaules = {"$set": {"Animals":jsondata["Animals"]}}
    userCollection.update(myQuery,newvaules)
    return "Animals Set"


@app.route("/RegisterNewUser", methods=['GET', 'POST'])
def RegisterNewUser():
    jsondata = flask.request.get_json()
    if checkUserExistence(jsondata["Phone"]) == True:
        return "PhoneExists"
    #newUserDict = {"Name":jsondata["Name"], "Family Name": jsondata["FamilyName"],"Gender":jsondata["Gender"], "Date":jsondata["Date"],"Email":jsondata["Email"] "Phone": , "Password": }

    date = jsondata["Date"]
    startList = date.split('/')
    date = datetime.datetime(int(startList[2]),int(startList[1]),int(startList[0]))
    jsondata["Date"] = date
    userCollection.insert(jsondata)
    return "Registered"

def calculateAVG(dictValues):

    totalTime = 0
    totalDistance = 0
    totalHikes = 0
    totalPhotos = 0
    for x in dictValues:
        totalTime += int(dictValues[x]["Duration"])
        totalDistance += float(dictValues[x]["totalDistance"])
        totalHikes+= 1
        totalPhotos += int(dictValues[x]["photoCount"])
    totalTime =  str(datetime.timedelta(seconds=totalTime))
    avrg = {}
    avrg["TotalDistance"] = totalDistance
    avrg["TotalTime"] = totalTime
    avrg["NumOfHikes"] = totalHikes
    avrg["NumOfPhotos"] = totalPhotos
    return avrg

def recoverEmail(jsonData):
    s = smtplib.SMTP('smtp.gmail.com', 587)
    s.starttls()
    s.login("NatureGoSCE@gmail.com", "sukablyat123")
    password = returnSpecificInfo([jsonData["Phone"],"Password"])
    message = 'Subject: {}\n\n{}'.format('Password Recovery',' Your Password is: ' + password)
    s.sendmail("NatureGoSCE@gmail.com",jsonData["Email"], message)

@app.route("/PasswordRecover", methods=['GET', 'POST'])
def PasswordRecover():
    jsondata = flask.request.get_json()
    if checkUserExistence(jsondata["Phone"]) == True:
        if returnSpecificInfo([jsondata["Phone"],"Email"]) == jsondata["Email"]:
            recoverEmail(jsondata)
            return "Recovered"
    return "Invalid"

@app.route("/StatisticsByDatesAVG", methods=['GET', 'POST'])
def StatisticsByDatesAVG():
    jsondata = flask.request.get_json()
    startDate = jsondata["StartDate"]
    startList = startDate.split('/')
    start = datetime.datetime(int(startList[2]),int(startList[1]),int(startList[0]))
    endDate = jsondata["EndDate"]
    startList = endDate.split('/')
    end = datetime.datetime(int(startList[2]),int(startList[1]),int(startList[0]))
    schoolName = jsondata["SchoolName"]
    className = jsondata["Class"]
    city = jsondata["City"]
    dictValues = {}
    dictValues2 = {}
    i = 0
    j = 0
    myDoc = HikesCollection.find({'Date': {'$gte': start, '$lte': end}})
    for x in myDoc:
        date = x["Date"]
        startList = str(date).split(' ')
        startList = startList[0].split('-')
        date = startList[2] + "." + startList[1] + "." + startList[0]
        x["Date"] = date
        x.pop("_id")
        dictValues2[str(j)] = x
        j += 1
        if schoolName != "":
            if x["SchoolName"] == schoolName:
                dictValues[str(i)] = x
                i += 1
        if className != "":
            if x["Class"] == className and x not in dictValues.values():
                dictValues[str(i)] = x
                i += 1
        if city != "":
            if x["City"] == city and x not in dictValues.values():
                dictValues[str(i)] = x
                i += 1


    if len(dictValues) == 0 and schoolName == "" and className == "" and city == "":
        avrg = calculateAVG(dictValues2)
        avrg["CORRECT"] = dictValues2
    else:
        avrg =calculateAVG(dictValues)
        avrg["CORRECT"] = dictValues

    avrg["KEY"] = "DatesSummery"
    return flask.jsonify(avrg)



@app.route("/SaveHike", methods=['GET', 'POST'])
def SaveHike():
    jsondata = flask.request.get_json()
    date = jsondata["Date"]
    startList = date.split(' ')
    date = startList[0].split('.')
    date = datetime.datetime(int(date[2]),int(date[1]),int(date[0]))
    jsondata["Date"] = date
    jsondata["Time"] = startList[2]
    HikesCollection.insert(jsondata)
    return "True"

@app.route("/GetAllStatistics", methods=['GET', 'POST'])
def GetAllStatistics():
    statDict = {}
    totalTime = 0
    totalDistance = 0
    totalHikes = 0
    totalPhotos = 0
    for x in HikesCollection.find({}, {"HikeName": 0, '_id': 0,"Date": 0}):
        totalTime += int(x["Duration"])
        totalDistance += float(x["totalDistance"])
        totalHikes+= 1
        totalPhotos += int(x["photoCount"])
    totalTime =  str(datetime.timedelta(seconds=totalTime))
    statDict["KEY"] = "All_Statistics"
    statDict["NumOfPhotos"] = totalPhotos
    statDict["NumOfHikes"] = totalHikes
    statDict["TotalTime"] = totalTime
    statDict["TotalDistance"] = totalDistance
    return flask.jsonify(statDict)

@app.route("/GetUserDetails", methods=['GET', 'POST'])
def GetUserDetails():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["Phone"]}
    myDoc = userCollection.find(myQuery)
    if myDoc.count() == 1:
        for x in myDoc:
            x.pop('_id')
            date = x["Date"]

            startList = str(date).split(' ')

            x["Date"] = startList[0]
        return flask.jsonify(x)
    else:
        return "false"

@app.route("/UpdateUserDetails", methods=['GET', 'POST'])
def UpdateUserDetails():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["loginPhone"]}
    if (checkUserExistence(jsondata["Phone"]) == True) and (jsondata["loginPhone"] !=jsondata["Phone"]):
        return "PhoneExists"

    date = jsondata["Date"]
    startList = date.split('/')
    date = datetime.datetime(int(startList[2]),int(startList[1])+1,int(startList[0]))

    newvalues = {"$set": {"Phone": jsondata["Phone"],"Name": jsondata["Name"],"Family Name": jsondata["Family Name"],
                 "Gender": jsondata["Gender"], "Date": date,"Email": jsondata["Email"],
                 "Password": jsondata["Password"],"SchoolName": jsondata["SchoolName"],"Class": jsondata["Class"],
                 "City": jsondata["City"],"Status": jsondata["Status"]}}
    userCollection.update(myQuery,newvalues)
    return "Updated"

@app.route("/Promote", methods=['GET', 'POST'])
def Promote():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["Phone"]}
    if checkUserExistence(jsondata["Phone"]) == True:
        newValues = {"$set": {"Status": "Teacher"}}
        userCollection.update(myQuery,newValues)
        return "StatusUpdated"
    return "Faild"

@app.route("/Demote", methods=['GET', 'POST'])
def Demote():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["Phone"]}
    if checkUserExistence(jsondata["Phone"]) == True:
        newValues = {"$set": {"Status": "User"}}
        userCollection.update(myQuery,newValues)
        return "StatusUpdated"
    return "Faild"

@app.route("/Delete", methods=['GET', 'POST'])
def Delete():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["Phone"]}
    if checkUserExistence(jsondata["Phone"]) == True:
        userCollection.delete_one(myQuery)
        return "User deleted"
    return "Faild"

@app.route("/ScoreUpdate", methods=['GET', 'POST'])
def ScoreUpdate():
    jsondata = flask.request.get_json()
    myQuery = {"Phone": jsondata["loginPhone"]}
    newvalues = {"$set": {"Score": jsondata["Score"]}}
    userCollection.update(myQuery,newvalues)
    return "Updated"


@app.route("/Search", methods=['GET', 'POST'])
def Search():
    jsondata = flask.request.get_json()
    search = jsondata["SearchValue"]
    dictValues = {}
    for x in userCollection.find({},{"Password":0, '_id': 0}):
        for key in x:
            if x[key] == search:
                dictValues[x["Phone"]] = x
    return flask.jsonify(dictValues)

@app.route("/StatisticsByDates", methods=['GET', 'POST'])
def StatisticsByDates():
    jsondata = flask.request.get_json()
    startDate = jsondata["StartDate"]
    startList = startDate.split('/')
    start = datetime.datetime(int(startList[2]),int(startList[1]),int(startList[0]))
    endDate = jsondata["EndDate"]
    startList = endDate.split('/')
    end = datetime.datetime(int(startList[2]),int(startList[1]),int(startList[0]))
    schoolName = jsondata["SchoolName"]
    className = jsondata["Class"]
    city = jsondata["City"]
    dictValues = {}
    dictValues2 = {}
    dictValues["KEY"] = "ByDates"
    dictValues2["KEY"] = "ByDates"
    i = 0
    j = 0
    myDoc = HikesCollection.find({'Date': {'$gte': start, '$lte': end}})
    for x in myDoc:
        date = x["Date"]
        startList = str(date).split(' ')
        startList = startList[0].split('-')
        date = startList[2] + "." + startList[1] + "." + startList[0]
        x["Date"] = date
        x.pop("_id")
        dictValues2[str(j)] = x
        j += 1
        if schoolName != "":
            if x["SchoolName"] == schoolName:
                dictValues[str(i)] = x
                i += 1
        if className != "":
            if x["Class"] == className and x not in dictValues.values():
                dictValues[str(i)] = x
                i += 1
        if city != "":
            if x["City"] == city and x not in dictValues.values():
                dictValues[str(i)] = x
                i += 1
    if len(dictValues) == 1 and schoolName == "" and className == "" and city == "":
        return flask.jsonify(dictValues2)
    return flask.jsonify(dictValues)



def checkUserExistence(user):
    myQuery = {"Phone": user}
    myDoc = userCollection.find(myQuery)
    if myDoc.count() != 0:
        return True
    return False




def returnSpecificInfo(userList):

    myQuery = {"Phone": userList[0]}
    myDoc = userCollection.find(myQuery)
    for value in myDoc:
        return value[userList[1]]

app.run(host="0.0.0.0", port=5000, threaded = True,processes=1)




