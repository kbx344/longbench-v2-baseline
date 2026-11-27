rm -rf core-starter/target/
mvn clean install -DskipTests
docker build -t apps.horsecoder.com:5000/s-3ba9123a0ba90ea7728cb9a568a66b73-core:latest -f ./Dockerfile.prod .
docker push apps.horsecoder.com:5000/s-3ba9123a0ba90ea7728cb9a568a66b73-core:latest
curl -X POST "http://apps.horsecoder.com:32871/task" -H "accept: application/json" -H "Content-Type: application/json" -d "{ \"image\": \"apps.horsecoder.com:5000/s-3ba9123a0ba90ea7728cb9a568a66b73-core:latest\", \"name\": \"3ba9123a0ba90ea7728cb9a568a66b73-core\", \"namespace\": \"horsecoder-test\", \"path\": \"/core/\", \"port\": 39910}"
