#
#   This file is part of Enki.
#  
#   Copyright © 2016 - 2019 Oliver Wyman Ltd.
#  
#   Enki is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#  
#   Enki is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#  
#   You should have received a copy of the GNU General Public License
#   along with Enki.  If not, see <https://www.gnu.org/licenses/>
#


import os
from flask import Flask, render_template

app = Flask(__name__)


@app.route("/")
def index():
    return render_template('suite.html', zone=os.environ.get('ZONE'))


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8080)
