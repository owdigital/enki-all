FROM python:3
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

WORKDIR /app
COPY requirements-image.txt /app
COPY setup.py /app
COPY test/ereshkigal/*.py /app/test/ereshkigal/
RUN python3 -m venv VENV
RUN VENV/bin/pip install -r requirements-image.txt
RUN VENV/bin/pip install -e .

# from https://github.com/joyzoursky/docker-python-chromedriver/blob/master/py3/py3.6-xvfb-selenium/Dockerfile

RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list'

# install things
RUN apt-get update && \
    apt-get install -y google-chrome-stable unzip xvfb && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean
RUN wget -O /tmp/chromedriver.zip http://chromedriver.storage.googleapis.com/`curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE`/chromedriver_linux64.zip
RUN unzip /tmp/chromedriver.zip chromedriver -d /usr/local/bin/ && \
    rm /tmp/chromedriver.zip

# set display port and dbus env to avoid hanging
ENV DISPLAY=:99
ENV DBUS_SESSION_BUS_ADDRESS=/dev/null

# selenium installed by requirements-image.txt. in VENV

ENV BASIC_USERNAME=enki
ENV BASIC_PASSWORD=
ENV ENKI_SERVER_URL=http://enki
ENV HYDRA_URLS=
ENV BANK_URLS="http://bank-a https://bank-b"
WORKDIR /app
COPY wait-for-it.sh /bin/wait-for-it
RUN chmod +x /bin/wait-for-it
ENTRYPOINT . VENV/bin/activate && pytest --junitxml=junit-report.xml --capture=no -vvv -m "$(PYTEST_FILTER)"
