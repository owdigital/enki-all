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

import requests
import json
import pytest

def memoize(f):
    memo = {}
    def helper(x):
        if x not in memo:
            memo[x] = f(x)
        return memo[x]
    return helper

@memoize
def read_data(kind):
    return json.loads(open("/data/%s" % kind).read())

@memoize
def send_data(kind):
    return requests.post("http://agent:3010/data", json=read_data(kind))

@pytest.mark.parametrize("kind", ["metadata-assertion", "share-assertion", "pii-type", "sharing-purpose", "data-subject", "pii-data"])
def test_send_data(kind):
    res = send_data(kind)
    assert res.ok, res.text
    assert "type" in res.json(), res.text
    assert res.json()["type"] != "error", res.json()

@pytest.mark.parametrize("kind", ["data-subject", "pii-data"])
def test_get_data(kind):
    postRes = send_data(kind)
    assert postRes.ok, postRes.text
    assert postRes.json()["type"] == "consus", postRes.text
    assert "location" in postRes.json(), postRes.text

    getRes = requests.get("http://agent:3010/data?file=%s" % postRes.json()["location"])
    assert getRes.ok, getRes.text
    assert getRes.json()["type"] == "consus", getRes.text
    assert getRes.json()["data"] == read_data(kind)

    directory = postRes.json()["location"]
    directory = directory[:directory.rindex("/")]
    getRes = requests.get("http://agent:3010/data?dir=%s" % directory)
    assert getRes.ok, getRes.text
    assert getRes.json()["type"] == "consus", getRes.text
    assert "items" in getRes.json(), postRes.text
    items = getRes.json()["items"]
    if kind == "data-subject":
        name = "_info"
    elif kind == "pii-data":
        name = read_data(kind)["piiType"]
    else:
        raise Exception("Don't know how to handle %s" % kind)
    assert len(items) == 1, items
    assert items[0] == {"short": name, "long": postRes.json()["location"]}

def test_not_allowed():
    res = requests.get("http://agent:3010/data?file=upspin@in.labshift.io/test")
    assert res.status_code == 403
    assert "type" in res.json(), res.text
    assert res.json()["type"] == "error", res.text

def test_set_access():
    data = {
        "location": send_data("pii-data").json()["location"],
        "user": "test-agent2@test.labshift.io"
    }
    for _ in range(2): # Makes sure the read Access parsing works as well as the write
        res = requests.post("http://agent:3010/access", json=data)
        assert res.ok, res.text
        assert "type" in res.json(), res.text
        assert res.json()["type"] == "access", res.json()
        assert res.json()["users"] == ["test-agent1@test.labshift.io","test-agent2@test.labshift.io"], res.json()

def test_set_access_duplicate():
    sendRes = requests.post("http://agent:3010/data", json={
        "type": "pii-data",
        "id": "blah",
        "piiType": "surname",
        "subjectId": "alpha",
        "processorId": "beta",
        "value": "foo"
    })
    assert sendRes.ok, sendRes.ok
    data = {
        "location": sendRes.json()["location"],
        "user": "test-agent1@test.labshift.io"
    }
    res = requests.post("http://agent:3010/access", json=data)
    assert res.ok, res.text
    assert "type" in res.json(), res.text
    assert res.json()["type"] == "access", res.json()
    assert res.json()["users"] == ["test-agent1@test.labshift.io"], res.json()

def test_use_access():
    data = {
        "location": send_data("pii-data").json()["location"],
        "user": "test-agent2@test.labshift.io"
    }
    res = requests.post("http://agent:3010/access", json=data)
    assert res.ok, res.text

    getRes = requests.get("http://agent2:3010/data?file=%s" % send_data("pii-data").json()["location"])
    assert getRes.ok, getRes.text
    assert getRes.json()["type"] == "consus", getRes.text
    assert getRes.json()["data"] == read_data("pii-data")

def test_access_with_empty_location():
    data = {
        "location": None,
        "user": "test-agent2@test.labshift.io"
    }
    res = requests.post("http://agent:3010/access", json=data)
    assert "type" in res.json(), res.text
    assert res.json()["type"] == "error", res.json()
    assert res.json()["error"].find("nil - failed: string") != -1, res.json()

def test_access_with_number_values():
    data = {
        "location": 1,
        "user": 2
    }
    res = requests.post("http://agent:3010/access", json=data)
    assert "type" in res.json(), res.text
    assert res.json()["type"] == "error", res.json()
    assert res.json()["error"].find("2 - failed: string?") != -1, res.json()

def test_access_with_bad_location():
    data = {
        "location": "@",
        "user": "test-agent2@test.labshift.io"
    }
    res = requests.post("http://agent:3010/access", json=data)
    assert "type" in res.json(), res.text
    assert res.json()["type"] == "error", res.json()
    assert res.json()["error"].find("\"@\" - failed:") != -1, res.json()

def test_access_with_bad_user():
    data = {
        "location": "test-agent@test.lab/foo",
        "user": ""
    }
    res = requests.post("http://agent:3010/access", json=data)
    assert "type" in res.json(), res.text
    assert res.json()["type"] == "error", res.json()
    assert res.json()["error"].find("\"\" - failed:") != -1, res.json()