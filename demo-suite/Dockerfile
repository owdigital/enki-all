FROM python:3-alpine3.6
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

RUN apk add --no-cache build-base python3-dev
COPY requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -r /app/requirements.txt

ENV PYTHONUNBUFFERED 1
ENV FLASK_DEBUG 0

WORKDIR /app
COPY . /app

ENTRYPOINT gunicorn -b 0.0.0.0:80 app:app --chdir=/app
