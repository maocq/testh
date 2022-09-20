#!/bin/bash
set -e
source ./functions.sh

mkdir -p .tmp/instances
mkdir -p .tmp/ip
mkdir -p .tmp/results
mkdir -p .tmp/work

aws sts get-caller-identity

url_reposity=$(jq -r ".url_reposity" "config.json")
user=$(jq -r ".user" "config.json")
key=$(jq -r ".key" "config.json")
key_name=$(jq -r ".key_name" "config.json")
instance_type=$(jq -r ".instance_type" "config.json")
security_group=$(jq -r ".security_group" "config.json")
image_id=$(jq -r ".image_id" "config.json")
http_pool_size=$(jq -r ".http_pool_size" "config.json")


case=$1
stacks=("java-imperative-ms" "java-reactive-ms" "go-ms" "elixir-ms")
if [[ ! " ${stacks[*]} " =~ " ${case} " ]]; then
    echo "Invalid option: $case"
	exit
fi


db_ip=$(start "$case-db" "db" "$image_id" "$instance_type" "$user" "$key" "$key_name" "$security_group")


configuration=$(echo "printf 'DATABASE_IP="$db_ip"\nHTTP_POOL_SIZE="$http_pool_size"\n' > /tmp/env.list")
ip=$(start "$case" "$case" "$image_id" "$instance_type" "$user" "$key" "$key_name" "$security_group" "$configuration")

echo "http://$ip:8080/api/hello" > /dev/tty