#! /bin/bash
set +x

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

export CHECKER_IMAGE=$(echo ttl.sh/$(uuidgen)-checker:10h | tr '[:upper:]' '[:lower:]')
export CONTROL_IMAGE=$(echo ttl.sh/$(uuidgen)-control:10h | tr '[:upper:]' '[:lower:]')
export HTTP_CLIENT="jdkhttp"

(cd $SCRIPT_DIR/support-apps && make all)

cd $SCRIPT_DIR
mvn verify

echo "If the control and checker apps are not changed you can re-use the containers:"
echo "export CHECKER_IMAGE=${CHECKER_IMAGE}"
echo "export CONTROL_IMAGE=${CONTROL_IMAGE}"
echo "mvn verify"
