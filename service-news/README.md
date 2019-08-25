# HPI-Cloud Service News

Provides access to news from various sources, e.g. HPI News and HPImgzn.

This service implements a server for [NewsService](https://github.com/HPI-de/hpi-cloud-apis/blob/dev/hpi/cloud/news/v1test/news_service.proto). See [service-news-crawler] for a list of crawled sources.


## Run

```sh
# Build docker container
docker build -f ./service-news/Dockerfile -t hpi-cloud/service-news:0.0.1 .

# Run
docker run \
  -e HPI_CLOUD_PORT=<port> \
  -p <port>:<port>
  -e HPI_CLOUD_COUCHBASE_NODES=<nodes> \
  -e HPI_CLOUD_COUCHBASE_USERNAME=<username> \
  -e HPI_CLOUD_COUCHBASE_PASSWORD=<password> \
  hpi-cloud/service-news:0.0.1
```

Parameters:
- `port`: Port to expose the service on. Default: "50051"
- `nodes`: Comma-separated list of Couchbase node to connect to. Default: "127.0.0.1" (`CouchbaseAsyncCluster.DEFAULT_HOST`)
- `username`: Username to connect to Couchbase
- `password`: Password to connect to Couchbase


[service-news-crawler]: https://github.com/HPI-de/hpi-cloud/tree/dev/service-news-crawler
