all_args=("$@")
if [ "$1" = 'idserver' ]; then
    echo "We are starting idserver with argument: "
    echo ${all_args[@]:1}
    java -jar /p2/target/IdServer.jar ${all_args[@]:1}
elif [ "$1" = 'idclient' ]; then
    echo "We are starting idclient with argument"
    echo ${all_args[@]:1}
    java -jar /p2/target/IdClient.jar ${all_args[@]:1}
fi