
build:
	docker build --build-arg APP_NAME=checker . -t andreatp/fabric8-it-checker-app:latest
	docker build --build-arg APP_NAME=control . -t andreatp/fabric8-it-control-app:latest

push:
	docker push andreatp/fabric8-it-checker-app:latest
	docker push andreatp/fabric8-it-control-app:latest
