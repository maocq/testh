#!/bin/bash
set -e
source ./functions.sh

case=$1

url_reposity=$(jq -r ".url_reposity" "config.json")
user=$(jq -r ".user" "config.json")
key=$(jq -r ".key" "config.json")
key_name=$(jq -r ".key_name" "config.json")
instance_type=$(jq -r ".instance_type" "config.json")
security_group=$(jq -r ".security_group" "config.json")
image_id=$(jq -r ".image_id" "config.json")


ip=$(jq -r ".ip" ".tmp/ip/$case.json")

if [ ! -f ".tmp/ip/$case-tests.json" ]; then
    tests_ip=$(start_simple_instance "$case-tests" "$image_id" "$instance_type" "$user" "$key" "$key_name" "$security_group")
else
    tests_ip=$(jq -r ".ip" ".tmp/ip/$case-tests.json")  
fi

command="docker restart \$(docker ps -a -q)"
execute_remote_command "$command" "$ip" "$user" "$key" > /dev/tty
wait_http "http://$ip:8080/api/hello"
sleep 1

cp "../jmeter/JMeterBenchmark.jmx" ".tmp/work/JMeterBenchmark-$case.jmx"


config_jmeter_file="../jmeter/config-jmeter.json"

duration_seconds=$(jq -r ".duration_seconds" "$config_jmeter_file")
ramp_up_period_seconds=$(jq -r ".ramp_up_period_seconds" "$config_jmeter_file")
latency=$(jq -r ".latency" "$config_jmeter_file")
hello_number_users=$(jq -r ".hello_number_users" "$config_jmeter_file")
get_http_number_users=$(jq -r ".get_http_number_users" "$config_jmeter_file")
get_https_number_users=$(jq -r ".get_https_number_users" "$config_jmeter_file")
get_pool_http1_number_users=$(jq -r ".get_pool_http1_number_users" "$config_jmeter_file")
get_pool_http2_number_users=$(jq -r ".get_pool_http2_number_users" "$config_jmeter_file")
query_number_users=$(jq -r ".query_number_users" "$config_jmeter_file")

sed -i -e "s/_SERVICE_IP_/$ip/g" \
    -e "s/_DURATION_SECONDS_/$duration_seconds/g" \
    -e "s/_RAMP_UP_PERIOD_SECONDS_/$ramp_up_period_seconds/g" \
    -e "s/_LATENCY_/$latency/g" \
    -e "s/_HELLO_NUMBER_USERS_/$hello_number_users/g" \
    -e "s/_GET_HTTP_NUMBER_USERS_/$get_http_number_users/g" \
    -e "s/_GET_HTTPS_NUMBER_USERS_/$get_https_number_users/g" \
    -e "s/_GET_POOL_HTTP1_NUMBER_USERS_/$get_pool_http1_number_users/g" \
    -e "s/_GET_POOL_HTTP2_NUMBER_USERS_/$get_pool_http2_number_users/g" \
    -e "s/_QUERY_NUMBER_USERS_/$query_number_users/g" \
    ".tmp/work/JMeterBenchmark-$case.jmx"


upload_file $tests_ip ".tmp/work/JMeterBenchmark-$case.jmx" "JMeterBenchmark.jmx" $user $key

out=$(execute_remote_command "sudo rm -rf report && sudo rm -f result-jmeter.csv && mkdir -p report" "$tests_ip" "$user" "$key")
execute_remote_command "docker run --rm -i -v \${PWD}:\${PWD} -w \${PWD} justb4/jmeter:latest -n -t JMeterBenchmark.jmx -l result-jmeter.csv -e -o report" "$tests_ip" "$user" "$key" > /dev/tty
out=$(execute_remote_command "tar -zcvf report.tar.gz report" "$tests_ip" "$user" "$key")


download_file $tests_ip "report.tar.gz" ".tmp/results/report-$case.tar.gz" $user $key
