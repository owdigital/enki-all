MODULE = common
SHELL=/bin/bash

include ../build/common.mk

install: target/common-$(COMMON_VERSION).jar
	lein install

target/common-$(COMMON_VERSION).jar: $(shell find {src,bletchley} -type f)
	java -version
	lein uberjar

compile: target/common-$(COMMON_VERSION).jar

clean:
	rm -rf ../work/common
	lein clean