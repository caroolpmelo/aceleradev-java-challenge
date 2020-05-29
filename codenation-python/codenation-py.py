# -*- coding: utf-8 -*-
import os
import sys
import json
import time
import string
import hashlib
import requests
from numbers import Number


OBJ = {}
FILENAME = "answer.json"
URL = "https://api.codenation.dev/v1/challenge/dev-ps/"
PARAMS = {"token": "bdb8061c657acc21727f3ac92c6c057c3269f9dd"}


def decryptStr():
    alphabet = string.ascii_lowercase
    result = ""

    for letter in OBJ["cifrado"]:
        if letter in alphabet:
            pos = alphabet.find(letter)
            pos = (pos - OBJ["numero_casas"]) % 26
            result += alphabet[pos]
        else:
            result += letter
    OBJ.update({"decifrado": result})


def generateSha1():
    OBJ.update({"resumo_criptografico": hashlib.sha1(
        OBJ["decifrado"].encode('ascii')).hexdigest()})


def createFile():
    with open(FILENAME, "x") as file:
        json.dump(OBJ, file)


def getReq():
    resp = requests.get(url=URL+"generate-data", params=PARAMS).json()
    OBJ.update(resp)


def postReq():
    file = open(FILENAME, 'rb')
    up = {'answer': file}
    postReq = requests.post(url=URL+"submit-solution", params=PARAMS, files=up)

    print(postReq.json())

    file.close()
    os.remove(FILENAME)


def main():
    getReq()
    if (OBJ is not None):
        decryptStr()
        generateSha1()
        createFile()
        postReq()
    else:
        getReq()


if __name__ == "__main__":
    main()
