FORMAT_WORKER_REPLICAS = 2
RESOLUTION_WORKER_REPLICAS = 2
SIZE_WORKER_REPLICAS = 2
REMOTE_WORK_DIR = ip_scala/image_processing_scala
CONTAINERS_FILES = $(shell find containers -type f -not -path "*/target/*")
CONTAINERS = $(shell ls ./containers)
NODES = $(shell ls ./containers | grep -v common | grep -v rabbitmq)
SERVER_USER = efoppiano
SERVER_HOST = atom.famaf.unc.edu.ar

init:
	mkdir -p .make
	docker swarm init
.PHONY: init

.make/jar_native: $(CONTAINERS_FILES)
	for node in $(NODES); do \
  		cd ./containers/$$node && sbt assembly && cd ../.. && \
  		cp ./containers/$$node/target/scala-3.3.1/$$node.jar ./containers/$$node/$$node.jar; \
	done
	touch .make/jar_native

.make/jar_dockerized: $(CONTAINERS_FILES)
	docker compose -f docker-compose-compilation.yaml up --build
	docker compose -f docker-compose-compilation.yaml down
	for node in $(NODES); do \
  		cp ./compilation/$$node/scala-3.3.1/$$node.jar ./containers/$$node/$$node.jar; \
	done
	touch .make/jar_dockerized

jar: common_publish_local
	if command -v sbt; then \
  		make .make/jar_native; \
	else \
		make .make/jar_dockerized; \
	fi

.make/build: $(CONTAINERS_FILES)
	for node in $(CONTAINERS); do \
  		docker rmi image_processing_scala_$$node -f || true; \
  		docker build -t image_processing_scala_$$node -f ./containers/$$node/Dockerfile ./containers/$$node; \
	done
	touch .make/build

build: jar .make/build

build_rabbitmq:
	if ! docker images | grep -q rostov_rabbitmq; then \
		docker build -t rostov_rabbitmq ./containers/rabbitmq/; \
	fi
.PHONY: build_rabbitmq

run_rabbitmq: build_rabbitmq
	docker stack deploy -c docker/rabbitmq.yaml rabbitmq
.PHONY: run_rabbitmq

down_rabbitmq:
	if docker stack ls | grep -q rabbitmq; then \
		docker stack rm rabbitmq; \
	fi
.PHONY: down_rabbitmq

setup: init build build_rabbitmq
.PHONY: setup

_common_folders:
	mkdir -p graphite
	mkdir -p grafana_config
	mkdir -p shared
	mkdir -p shared/input
	rm -rf shared/formatted || true
	mkdir -p shared/formatted
	rm -rf shared/scaled || true
	mkdir -p shared/scaled
	rm -rf shared/output || true
	mkdir -p shared/output

deploy: remove down_rabbitmq build build_rabbitmq _common_folders
	until FORMAT_WORKER_REPLICAS=$(FORMAT_WORKER_REPLICAS) \
		RESOLUTION_WORKER_REPLICAS=$(RESOLUTION_WORKER_REPLICAS) \
		SIZE_WORKER_REPLICAS=$(SIZE_WORKER_REPLICAS) \
		docker stack deploy \
		-c docker/rabbitmq.yaml \
		-c docker/common.yaml \
		-c docker/local.yaml ip_scala; do sleep 1; done
.PHONY: deploy

deploy_jars: remove down_rabbitmq build build_rabbitmq _common_folders
	until FORMAT_WORKER_REPLICAS=$(FORMAT_WORKER_REPLICAS) \
		RESOLUTION_WORKER_REPLICAS=$(RESOLUTION_WORKER_REPLICAS) \
		SIZE_WORKER_REPLICAS=$(SIZE_WORKER_REPLICAS) \
		docker stack deploy \
		-c docker/rabbitmq.yaml \
		-c docker/common.yaml \
		-c docker/server.yaml ip_scala; do sleep 1; done
.PHONY: deploy_jars

remove:
	if docker stack ls | grep -q ip_scala; then \
            docker stack rm ip_scala; \
	fi
.PHONY: remove

manager_logs:
	docker service logs -f ip_scala_manager
.PHONY: manager_logs

format_logs:
	docker service logs -f ip_scala_format_worker
.PHONY: format_logs

res_logs:
	docker service logs -f ip_scala_resolution_worker
.PHONY: res_logs

size_logs:
	docker service logs -f ip_scala_size_worker
.PHONY: size_logs

run_manager_local:
	cd ./containers/manager && LOCAL=true sbt -J-Xmx500M run
	cd ../..
.PHONY: run_manager_local

run_format_worker_local:
	cd ./containers/format_worker && LOCAL=true sbt -J-Xmx500M run
	cd ../..
.PHONY: run_format_worker_local

run_resolution_worker_local:
	cd ./containers/resolution_worker && LOCAL=true sbt -J-Xmx500M run
	cd ../..
.PHONY: run_resolution_worker_local

run_size_worker_local:
	cd ./containers/size_worker && LOCAL=true sbt -J-Xmx500M run
	cd ../..
.PHONY: run_size_worker_local

.make/common_publish_local: $(CONTAINERS_FILES)
	if command -v sbt; then \
		cd ./containers/common && sbt publishLocal; \
		cd ../..; \
	else \
		docker compose -f docker/common_compilation.yaml up --build; \
		docker compose -f docker/common_compilation.yaml down; \
	fi
	touch .make/common_publish_local

common_publish_local: .make/common_publish_local

# Server specific

.make/upload_jars: $(shell find containers -type f -name "*.jar")
	for node in $(NODES); do \
  		scp containers/$$node/$$node.jar efoppiano@atom.famaf.unc.edu.ar:${REMOTE_WORK_DIR}/containers/$$node; \
  	done
	touch .make/upload_jars

upload_jars: build .make/upload_jars

## Use *_remote if you are running them from your local machine

_build_remote:
	for node in $(NODES); do \
  		docker rmi image_processing_scala_$$node -f || true; \
  		docker build -t image_processing_scala_$$node -f ./containers/$$node/Dockerfile ./containers/$$node; \
	done
.PHONY: _build_remote

_deploy_remote: remove _common_folders _build_remote
	until FORMAT_WORKER_REPLICAS=$(FORMAT_WORKER_REPLICAS) \
		RESOLUTION_WORKER_REPLICAS=$(RESOLUTION_WORKER_REPLICAS) \
		SIZE_WORKER_REPLICAS=$(SIZE_WORKER_REPLICAS) \
		docker stack deploy \
		-c docker/rabbitmq.yaml \
		-c docker/common.yaml \
		-c docker/server.yaml ip_scala; do sleep 1; done

deploy_remote: upload_jars
	ssh $(SERVER_USER)@$(SERVER_HOST) 'cd $(REMOTE_WORK_DIR) && make _deploy_remote'
.PHONY: deploy_remote

remove_remote:
	ssh $(SERVER_USER)@$(SERVER_HOST) 'cd $(REMOTE_WORK_DIR) && make remove'
.PHONY: remove_remote

## Tunneling

tunnel_rabbitmq:
	ssh -L 15672:127.0.0.1:15672 $(SERVER_USER)@$(SERVER_HOST)
.PHONY: tunnel_rabbitmq

tunnel_graphite:
	ssh -L 8080:127.0.0.1:8080 $(SERVER_USER)@$(SERVER_HOST)
.PHONY: tunnel_graphite

tunnel_cadvisor:
	ssh -L 8888:127.0.0.1:8888 $(SERVER_USER)@$(SERVER_HOST)
.PHONY: tunnel_cadvisor

tunnel_grafana:
	ssh -L 8081:127.0.0.1:8081 $(SERVER_USER)@$(SERVER_HOST)
.PHONY: tunnel_grafana

# Cloud specific

# Requires the following env variables:
# - NFS_SERVER_IP
# - NFS_SERVER_PATH
deploy_cloud: remove
	mkdir -p graphite
	mkdir -p grafana_config
	until WORKER_REPLICAS=$(WORKER_REPLICAS) docker stack deploy \
 	-c docker/rabbitmq.yaml \
	-c docker/common.yaml \
	-c docker/cloud.yaml ip_scala; do sleep 1; done
.PHONY: deploy_cloud