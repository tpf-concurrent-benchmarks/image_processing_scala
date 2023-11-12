init:
	docker swarm init
.PHONY: init

CONTAINERS = $(shell ls ./containers)
NODES = $(shell ls ./containers | grep -v common | grep -v rabbitmq)

compile:
	for container in $(CONTAINERS); do \
		cd ./containers/$$container && sbt compile && cd ../..; \
	done
.PHONY: compile

jar: common_publish_local
	docker compose -f docker-compose-compilation.yaml up --build
	docker compose -f docker-compose-compilation.yaml down
	for node in $(NODES); do \
  		cp ./compilation/$$node/scala-3.3.1/$$node.jar ./containers/$$node/$$node.jar; \
	done
.PHONY: jar

jar_local:
	for node in $(NODES); do \
  		cd ./containers/$$node && sbt assembly && cd ../.. && \
  		cp ./containers/$$node/target/scala-3.3.1/$$node.jar ./containers/$$node/$$node.jar; \
	done
.PHONY: jar_local

build: jar_local
	for node in $(CONTAINERS); do \
  		docker rmi image_processing_scala_$$node -f; \
  		docker build -t image_processing_scala_$$node -f ./containers/$$node/Dockerfile ./containers/$$node; \
	done
.PHONY: build

build_rabbitmq:
	docker build -t rostov_rabbitmq ./containers/rabbitmq/
.PHONY: build_rabbitmq

run_rabbitmq:
	docker stack deploy -c docker-compose-rabbitmq.yaml rabbitmq
.PHONY: run_rabbitmq

down_rabbitmq:
	if docker stack ls | grep -q rabbitmq; then \
		docker stack rm rabbitmq; \
	fi
.PHONY: down_rabbitmq

run_graphite: down_graphite
	docker stack deploy -c docker-compose-graphite.yaml graphite
.PHONY: run_graphite

down_graphite:
	if docker stack ls | grep -q graphite; then \
		docker stack rm graphite; \
	fi
.PHONY: down_graphite

setup: init compile build build_rabbitmq
.PHONY: setup

_deploy:
	mkdir -p graphite
	mkdir -p grafana_config
	mkdir -p shared
	mkdir -p shared/input
	mkdir -p shared/output
	MY_UID="$(shell id -u)" MY_GID="$(shell id -g)" docker stack deploy -c docker-compose.yaml ip_scala
.PHONY: _deploy

deploy: remove build down_rabbitmq down_graphite
	make _deploy
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

run_manager_tests:
	cd ./containers/manager && sbt test
	cd ../..
.PHONY: run_manager_tests

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
	cd ./containers/common && sbt publishLocal && cd ../..
.PHONY: common_publish_local

# Server specific

REMOTE_WORK_DIR = ip_scala/image_processing_scala

upload_jars: jar
	for node in $(NODES); do \
  		scp containers/$$node/$$node.jar efoppiano@atom.famaf.unc.edu.ar:$$REMOTE_WORK_DIR/containers/$$node; \
	done
.PHONY: upload_jars

## Use *_remote if you are running them from your local machine
## Do not use those that start with _
_build_remote:
	for node in $(NODES); do \
  		docker rmi image_processing_scala_$$node -f && \
  		docker build -t image_processing_scala_$$node -f ./containers/$$node/Dockerfile ./containers/$$node; \
	done
.PHONY: _build_remote

build_remote: upload_jars
	ssh efoppiano@atom.famaf.unc.edu.ar 'cd $(REMOTE_WORK_DIR) && make _build_remote'
.PHONY: build_remote

_deploy_remote:
	mkdir -p graphite
	mkdir -p grafana_config
	mkdir -p shared
	mkdir -p shared/input
	rm -rf shared/output || true
	mkdir -p shared/output
	MY_UID="$(shell id -u)" MY_GID="$(shell id -g)" docker stack deploy -c docker-compose-server.yaml ip_scala
.PHONY: _deploy_remote

deploy_remote: remove_remote build_remote
	ssh efoppiano@atom.famaf.unc.edu.ar 'cd $(REMOTE_WORK_DIR) && make _deploy_remote'
.PHONY: deploy_remote

_remove_remote:
	if docker stack ls | grep -q ip_scala; then \
			docker stack rm ip_scala; \
	fi
.PHONY: _remove_remote

remove_remote:
	ssh efoppiano@atom.famaf.unc.edu.ar 'cd $(REMOTE_WORK_DIR) && make _remove_remote'
.PHONY: remove_remote

## Tunneling

tunnel_rabbitmq:
	ssh -L 15672:127.0.0.1:15672 efoppiano@atom.famaf.unc.edu.ar
.PHONY: tunnel_rabbitmq

tunnel_graphite:
	ssh -L 8080:127.0.0.1:8080 efoppiano@atom.famaf.unc.edu.ar
.PHONY: tunnel_graphite

tunnel_cadvisor:
	ssh -L 8888:127.0.0.1:8888 efoppiano@atom.famaf.unc.edu.ar
.PHONY: tunnel_cadvisor

tunnel_grafana:
	ssh -L 8081:127.0.0.1:8081 efoppiano@atom.famaf.unc.edu.ar
.PHONY: tunnel_grafana