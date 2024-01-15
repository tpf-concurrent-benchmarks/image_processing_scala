FORMAT_WORKER_REPLICAS = 2
RESOLUTION_WORKER_REPLICAS = 2
SIZE_WORKER_REPLICAS = 2
REMOTE_WORK_DIR = ip_scala/image_processing_scala
CONTAINERS = $(shell ls ./containers)
NODES = $(shell ls ./containers | grep -v common | grep -v rabbitmq)
SERVER_USER = efoppiano
SERVER_HOST = atom.famaf.unc.edu.ar

init:
	docker swarm init
.PHONY: init

build:
	for node in $(CONTAINERS); do \
		docker rmi image_processing_scala_$$node -f || true; \
		docker build -t image_processing_scala_$$node -f ./containers/$$node/Dockerfile ./containers; \
	done
.PHONY: build

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
	mkdir -p shared/formatted
	mkdir -p shared/scaled
	mkdir -p shared/output

	mv shared/formatted shared/formatted.old || true
	rm -rf shared/formatted &
	mkdir -p shared/scaled

	mv shared/scaled shared/scaled.old || true
	rm -rf shared/scaled &
	mkdir -p shared/scaled

	mv shared/output shared/output.old || true
	rm -rf shared/output &
	mkdir -p shared/output
.PHONY: _common_folders

deploy: remove down_rabbitmq build build_rabbitmq _common_folders
	until \
	FORMAT_WORKER_REPLICAS=$(FORMAT_WORKER_REPLICAS) \
	RESOLUTION_WORKER_REPLICAS=$(RESOLUTION_WORKER_REPLICAS) \
	SIZE_WORKER_REPLICAS=$(SIZE_WORKER_REPLICAS) \
	docker stack deploy \
	-c docker/rabbitmq.yaml \
	-c docker/common.yaml \
	-c docker/server.yaml ip_scala; \
	do sleep 1; done
.PHONY: deploy

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

common_publish_local:
	cd ./containers/common && sbt publishLocal
	cd ../..
.PHONY: common_publish_local

# Server specific

## Use *_remote if you are running them from your local machine

deploy_remote:
	ssh $(SERVER_USER)@$(SERVER_HOST) 'cd $(REMOTE_WORK_DIR) && make deploy'
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

_mount_nfs:
	mkdir -p shared
	sudo mount -o rw,intr $(NFS_SERVER_IP):/$(NFS_SERVER_PATH) ./shared
.PHONY: _mount_nfs

# Requires the following env variables:
# - NFS_SERVER_IP
# - NFS_SERVER_PATH
deploy_cloud: remove
	NFS_SERVER_IP=$(NFS_SERVER_IP) NFS_SERVER_PATH=$(NFS_SERVER_PATH) make _mount_nfs
	sudo make _common_folders
	mkdir -p graphite
	mkdir -p grafana_config
	until \
	FORMAT_WORKER_REPLICAS=$(FORMAT_WORKER_REPLICAS) \
	RESOLUTION_WORKER_REPLICAS=$(RESOLUTION_WORKER_REPLICAS) \
	SIZE_WORKER_REPLICAS=$(SIZE_WORKER_REPLICAS) \
	NFS_SERVER_IP=$(NFS_SERVER_IP) \
	NFS_SERVER_PATH=$(NFS_SERVER_PATH) \
	sudo -E docker stack deploy \
	-c docker/rabbitmq.yaml \
	-c docker/common.yaml \
	-c docker/cloud.yaml ip_scala; do sleep 1; done
.PHONY: deploy_cloud