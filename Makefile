.PHONY: help build package run-sample run-hot run-local-data run down logs

ENV_FILE ?= example.env
DOCKER_COMPOSE ?= docker-compose

help:
	@echo "Common developer commands:"
	@echo "  make build        - mvn clean package"
	@echo "  make package      - mvn package -DskipTests"
	@echo "  make run-sample   - run with sample data (docker-compose-sample-data.yml, ENV_FILE=$(ENV_FILE))"
	@echo "  make run-hot      - run with hot-reload WAR (docker-compose-hot.yml, ENV_FILE=$(ENV_FILE))"
	@echo "  make run-local-data - run against local cudl-data-source (docker-compose-local-cudl-data.yml, ENV_FILE=$(ENV_FILE))"
	@echo "  make run          - build app and start docker-compose with ENV_FILE=$(ENV_FILE)"
	@echo "  make down         - stop all docker-compose services for ENV_FILE=$(ENV_FILE)"
	@echo "  make logs         - tail dl-loading-ui logs via docker-compose"

build:
	mvn clean package

package:
	mvn package -DskipTests

run-sample: package
	$(DOCKER_COMPOSE) --env-file example.env -f docker-compose-sample-data.yml up

run-hot: package
	$(DOCKER_COMPOSE) --env-file $(ENV_FILE) -f docker-compose-hot.yml up

run-local-data: package
	$(DOCKER_COMPOSE) --env-file $(ENV_FILE) -f docker-compose-local-cudl-data.yml up

run: build
	$(DOCKER_COMPOSE) --env-file $(ENV_FILE) up --build

down:
	$(DOCKER_COMPOSE) --env-file $(ENV_FILE) down

logs:
	$(DOCKER_COMPOSE) --env-file $(ENV_FILE) logs -f dl-loading-ui
