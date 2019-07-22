FROM python:3
WORKDIR /tests

COPY wait-for-it.sh /usr/bin/wait-for-it
RUN chmod +x /usr/bin/wait-for-it && mkdir /data/

COPY integration/requirements.txt /tests
RUN pip install -r requirements.txt

COPY clj/enki_agent/bletchley/data/* /data/

COPY integration/*.py /tests
CMD wait-for-it --timeout=120 agent:3010 -- wait-for-it --timeout=120 agent2:3010 -- pytest --junitxml=integration.xml --capture=no -v