from flask import Flask, request
import json
import pandas
from flask import Response

db = pandas.read_csv("database.csv")
db.phone = db.phone.map(str)

app = Flask("jsonrpc-demo")


@app.route('/get/all')
def getall():
    global db
    return json.loads(db.to_json(orient='records'))


@app.route('/get/email', methods=['GET'])
def get_byemail():
    global db
    email = request.args.get('email')
    return Response(
        response=json.dumps(json.loads(db.loc[db['email'] == email].to_json(orient='records'))[0]),
        status=200,
        mimetype='application/json'
    )


@app.route('/get/name', methods=['GET'])
def get_byname():
    global db
    name = request.args.get('name')
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
    return Response(
        response=json.dumps(json.loads(db.loc[db['email'] == body["email"]].to_json(orient='records'))[0]),
        status=200,
        mimetype='application/json'
    )


app.run(host="localhost")
