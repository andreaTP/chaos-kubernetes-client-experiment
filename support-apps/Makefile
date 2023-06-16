all: build push

build:
	docker build --build-arg APP_NAME=checker --build-arg HTTP_CLIENT=${HTTP_CLIENT} . -t ${CHECKER_IMAGE}
	docker build --build-arg APP_NAME=control --build-arg HTTP_CLIENT=${HTTP_CLIENT} . -t ${CONTROL_IMAGE}

push:
	docker push ${CHECKER_IMAGE}
	docker push ${CONTROL_IMAGE}

build-local:
	docker build --build-arg APP_NAME=checker . -t andreatp/fabric8-it-checker-app:latest
	docker build --build-arg APP_NAME=control . -t andreatp/fabric8-it-control-app:latest

push-local:
	docker push andreatp/fabric8-it-checker-app:latest
	docker push andreatp/fabric8-it-control-app:latest
