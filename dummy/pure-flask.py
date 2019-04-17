from flask import Flask, request
import json
import pandas
from flask import Response
import numpy as np
import random
import time

db = pandas.read_csv("initial_database.csv")
db.phone = db.phone.map(str)

app = Flask("jsonrpc-demo")

def random_pause():
    n = 2000
    mu, sigma = n/2, 0.3 # mean and standard deviation
    s = np.random.normal(mu, sigma, n).tolist()[0]
    time.sleep(random.randint(int(s-s/20), int(s+s/20))/1000)


@app.route('/get/all')
def getall():
    global db
    random_pause()
    return json.loads(db.to_json(orient='records'))


@app.route('/get/email', methods=['GET'])
def get_byemail():
    global db
    email = request.args.get('email')
    random_pause()
    return Response(
        response=json.dumps(json.loads(db.loc[db['email'] == email].to_json(orient='records'))[0]),
        status=200,
        mimetype='application/json'
    )


@app.route('/get/name', methods=['GET'])
def get_byname():
    global db
    name = request.args.get('name')
    random_pause()
    return Response(
        response=json.dumps(json.loads(db.loc[db['name'] == name].to_json(orient='records'))[0]),
        status=200,
        mimetype='application/json'
    )


@app.route('/add', methods=['POST'])
def add():
    global db
    body = request.get_json()
    print(body)
    db = db.append({"name": body["name"], "status": body["status"], "email": body["email"], "surname": body["surname"],
                    "phone": body["phone"]},
                   ignore_index=True)
    db.to_csv("database.csv", index=False)
    random_pause()
    return Response(
        response=json.dumps(json.loads(db.loc[db['email'] == body["email"]].to_json(orient='records'))[0]),
        status=200,
        mimetype='application/json'
    )


app.run(host="localhost")
