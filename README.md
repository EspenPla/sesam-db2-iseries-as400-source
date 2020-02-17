# db2-iseries-as400
IBM DB2 iSeries AS400 source for Sesam.io powered applications

Supports fetching data from DB2 iSeries. 

this service works on port 8080 and has one endpoint GET `/datasets/<TABLE NAME>/entities`
with parameters id=<PRIMARY KEY FIELD NAME> & lm=<LAST MODIFIED FIELD NAME>

Use parameter since=<DATE FOR FILTERING> if you want to limit your result. 
If no since value is provided from Sesam, the MS will set since to 0.

Example url:
`/datasets/<TABLE NAME>/entities?id=<PRIMARY KEY>&lm=<LAST MODIFIED>&since=20200101`

### environment variables needed
* **DB2_HOSTNAME** - hostname or IP to DB2 iSeries AS400 instance 
* **DB2_DBNAME** - database name 
* **DB2_USERNAME** - username
* **DB2_PASSWORD** - password

### System setup 
```json
{
  "_id": "db2-test",
  "type": "system:microservice",
  "docker": {
    "environment": {
      "DB2_DBNAME": "<DB NAME>",
      "DB2_HOSTNAME": "<DB HOST OR IP>",
      "DB2_PASSWORD": "<DB PASSWORD>",
      "DB2_USERNAME": "<DB USER>"
    },
    "image": "sesamcommunity/db2-iseries-as400:<VERSION>",
    "memory": 512,
    "port": 8080
  }
}

```

### Pipe setup  

```json
{
  "_id": "db2-test-pipe",
  "type": "pipe",
  "source": {
    "type": "json",
    "system": "db2-test",
    "url": "/datasets/<TABLE NAME>/entities?id=<PRIMARY KEY FIELD NAME>&lm=<LAST MODIFIED FIELD NAME>"
  },
  "transform": {
    "type": "dtl",
    "rules": {
      "default": [
        ["copy", "*"]
      ]
    }
  }
}
```
# db2-iseries-as400
