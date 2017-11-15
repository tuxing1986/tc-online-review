#!/bin/sh

SERVICE="ap-sample"

# Project root directory of the micro service.
SOURCE_ROOT=$1
# dev|qa|prod
CONFIG=$2
# version
VER=`date "+%Y%m%d%H%M"`


# Application version name to be deployed in Elastic Beanstalk
APPVER="${SERVICE}-${CONFIG}-${VER}"

# Docker resistory
REGISTRY="appiriodevops"
REPO="${SERVICE}-microservice"
TAG="${REPO}:${CONFIG}.${VER}"
IMAGE="${REGISTRY}/${TAG}"

# The key of the profile for AWS CLI configuration
AWS_PROFILE="tc-${CONFIG}"

# S3 Bucket
AWS_S3_BUCKET="appirio-platform-${CONFIG}"
AWS_S3_KEY="services/docker/${TAG}"

# Elastic Beanstalk
#   Application name
AWS_EB_APP="${SERVICE}-microservice"
#   Environment name
AWS_EB_ENV=${SERVICE}



WORK_DIR=$SOURCE_ROOT/build
DOCKER_DIR=$WORK_DIR/docker
if [ ! -d $DOCKER_DIR ]; then
    mkdir -p $DOCKER_DIR
fi

function handle_error() {
  RET=$?
  if [ $RET -ne 0 ]; then
    echo "ERROR: $1"
    exit $RET
  fi
}

echo "***** start deploying the ${SERVICE} service to the ${CONFIG} environment *****"

cd $WORK_DIR

echo "copying application jar"
cp $SOURCE_ROOT/target/tech.core.sample.dropwizard.jar $DOCKER_DIR/${SERVICE}.jar

echo "copying configuration file"
cp $SOURCE_ROOT/target/classes/config.yml $DOCKER_DIR/config.yml

# checking domain
# prod -> topcoder.com, qa -> topcoder-qa.com, dev -> topcoder-dev.com
APPDOMAIN=`cat $DOCKER_DIR/config.yml | grep "authDomain" | sed -e 's/authDomain: //g'`
echo "[CHECK THIS IS CORRECT] application domain: ${APPDOMAIN}"

echo "copying environment-specific resources"
cp $WORK_DIR/$CONFIG/* $DOCKER_DIR/
cp $WORK_DIR/newrelic.yml $DOCKER_DIR/

echo "building docker image: ${IMAGE}"
sudo docker build -t $IMAGE $DOCKER_DIR
handle_error "docker build failed."

echo "pushing docker image: ${IMAGE}"
sudo docker push $IMAGE
handle_error "docker push failed."

echo "creating Dockerrun.aws.json from template."
DOCKERRUN="${WORK_DIR}/eb/Dockerrun.aws.json"
DR_TMP="${DOCKERRUN}.template"
if [ "$CONFIG" = "qa" ]; then
    DR_TMP="${WORK_DIR}/qa/Dockerrun.aws.json.template"
fi
cat $DR_TMP | sed -e "s/@IMAGE@/${TAG}/g" > $DOCKERRUN
cat $DOCKERRUN

echo "pushing Dockerrun.aws.json to S3: ${AWS_S3_BUCKET}/${AWS_S3_KEY}"
aws s3api put-object --profile $AWS_PROFILE --bucket "${AWS_S3_BUCKET}" --key "${AWS_S3_KEY}" --body $DOCKERRUN
handle_error "aws s3api put-object failed."

echo "creating new application version $APPVER in ${AWS_EB_APP} from s3:${AWS_S3_BUCKET}/${AWS_S3_KEY}"
aws elasticbeanstalk create-application-version --profile $AWS_PROFILE --application-name $AWS_EB_APP --version-label $APPVER --source-bundle S3Bucket="$AWS_S3_BUCKET",S3Key="$AWS_S3_KEY"
handle_error "aws elasticbeanstalk create-application-version failed."

echo "updating elastic beanstalk environment ${AWS_EB_ENV} with the version ${APPVER}."
# assumes beanstalk app for this service has already been created and configured
aws elasticbeanstalk --profile $AWS_PROFILE update-environment --environment-name $AWS_EB_ENV --version-label $APPVER
handle_error "aws elasticbeanstalk pdate-environment failed."
