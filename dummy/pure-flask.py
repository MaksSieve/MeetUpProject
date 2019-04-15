from flask import Flask, request
import json
import pandas

db = pandas.read_csv("database.csv")

app = Flask("jsonrpc-demo")


@app.route('/get/all')
def getall():
    return json.loads(db.to_json(orient='records'))


@app.route('/get/email', methods=['GET'])
def get_byemail():
    email = request.args.get('email')
    return db.loc[db['email'] == email].to_json(orient='records')


@app.route('/get/name', methods=['GET'])
def get_byname():
    name = request.args.get('name')
    return db.loc[db['name'] == name].to_json(orient='records')


@app.route('/add', methods=['POST'])
def add():
    global db
    body = request.get_json()
    db = db.append({"name": body["name"], "status": body["status"], "email": body["email"], "surname": body["surname"], "phone": body["phone"]},
                   ignore_index=True)
    db.to_csv("database.csv", index=False)
    return db.loc[db['email'] == body["email"]].to_json(orient='records')


app.run(host="localhost")
