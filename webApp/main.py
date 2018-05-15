from flask import Flask, render_template, redirect, url_for
import json
import subprocess
from flask import request
import flask
import subprocess

app = Flask(__name__)


@app.route("/", methods=["POST", "GET"])
def index():

	data= json.loads(request.data)
	arg1= data["Emot"]
	arg2= data["Pact"]
	arg3= data["Mact"]
	arg4= data["Session"]


	cmd = subprocess.Popen(["Rscript","pro.R",arg1, arg2, arg3, arg4],stdout=subprocess.PIPE)
	out, cmd_err = cmd.communicate()

	print(out)

	return out;

if __name__ == '__main__':
	app.debug = True # shows the error log
	app.run(host='0.0.0.0') # this makes the web application accessible to any system over this network (useful for instant and parallal testing)
