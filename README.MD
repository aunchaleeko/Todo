# Akka Http

### Run with SBT
Use `sbt run` command

---------------------------------------------------
### Add the task to the list
Using CURL:

curl -X POST \
'http://127.0.0.1:9090/todo/tasks/{{subject}}' \
  -H 'Content-Type: application/json' \
  -d '{
	"detail" : "create doc",
	"status" : "done"

}'

### view all item in the list
Using CURL:
curl -X GET \
  http://127.0.0.1:9090/todo/tasks

### view a single
Using CURL:
curl -X GET \
  'http://127.0.0.1:9090/todo/tasks/{{subject}}'

### delete a task from the list
Using CURL:
curl -X DELETE \
  'http://localhost:9090/todo/delete/{{subject}}'


### update task
Using CURL:
curl -X PUT \
  'http://127.0.0.1:9090/todo/tasks/{{subject}}' \
  -H 'Content-Type: application/json' \
  -d '{
	"detail" : "to day is to day",
	"status" : "done"

}'

--------------------------------------------------




