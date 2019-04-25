# Quartz Demo 

This demo uses an in-memory job store. For production we'd want to use a JDBC backend, in clustered mode. 

Try it out: modify the timestamp to trigger at in `test.http` and fire a POST request. If all goes well, you should see a log line at the requested time with the test msg. 


### Build & Run
```sh
make
make run
```
