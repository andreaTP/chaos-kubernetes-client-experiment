all: build push

build:
	docker build --build-arg APP_NAME=checker . -t ${CHECKER_IMAGE}
	docker build --build-arg APP_NAME=control . -t ${CONTROL_IMAGE}

push:
	docker push ${CHECKER_IMAGE}
	docker push ${CONTROL_IMAGE}

build-std:
	docker build --build-arg APP_NAME=checker . -t andreatp/fabric8-it-checker-app:latest
	docker build --build-arg APP_NAME=control . -t andreatp/fabric8-it-control-app:latest

push-std:
	docker push andreatp/fabric8-it-checker-app:latest
	docker push andreatp/fabric8-it-control-app:latest
