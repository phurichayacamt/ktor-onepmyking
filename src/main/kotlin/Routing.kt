package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Task(val id: Int, val content: String , val isDone: Boolean)
@Serializable
data class TaskRequest( val content: String , val isDone: Boolean)

object taskRepository{
    val tasks = mutableListOf<Task>(
        Task(id = 1, content = "Learn Ktor", isDone = true),
        Task(id = 2, content = "Build a REST API", isDone = false),
        Task(id = 3, content = "Write Unit Tests", isDone = false)
    )
}

fun getAll(): List<Task> = taskRepository.tasks
fun add(task: Task)
{
    taskRepository.tasks.add(task)
}
fun update(id: Int, updatedTask: Task){
    val index = taskRepository.tasks.indexOfFirst { it.id == id }
    if (index != -1) {
        taskRepository.tasks[index] = updatedTask
    }
}
fun delete(id: Int){
    taskRepository.tasks.removeIf { it.id == id }
}

fun Application.configureRouting() {
    routing {
        route("/task") {
            get {
                if (taskRepository.tasks.isNotEmpty()) {
                    call.respond(taskRepository.tasks)
                } else {
                    call.respondText("No task found", status = HttpStatusCode.NotFound)
                }
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                val task = taskRepository.tasks.find { it.id == id }
                if (task != null) {
                    call.respond(task)
                } else
                    call.respondText("Task not found", status = HttpStatusCode.NotFound)
            }

            post {
                val request = call.receive<TaskRequest>()
                val newId = (taskRepository.tasks.maxByOrNull { it.id }?.id ?: 0) + 1
                val task = Task(id = newId, content = request.content, isDone = request.isDone)
                taskRepository.tasks.add(task)
                println("Task list now: ${taskRepository.tasks}")
                call.respondText("Created", status = HttpStatusCode.Created)
            }
            put("/{id}") {
                val id = call.parameters["id"]!!.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                    return@put
                }
                val request = call.receive<TaskRequest>()
                val index = taskRepository.tasks.indexOfFirst { it.id == id }
                if (index != -1) {
                    val updatedTask = Task(id = id!!, content = request.content, isDone = request.isDone)
                    taskRepository.tasks[index] = updatedTask
                    call.respondText("Updated", status = HttpStatusCode.OK)
                } else
                    call.respondText("Task not found", status = HttpStatusCode.NotFound)

            }
            delete("/{id}") {
                val id = call.parameters["id"]!!.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid ID", status = HttpStatusCode.BadRequest)
                    return@delete
                }
                val removeTask = taskRepository.tasks.removeIf { it.id == id }
                if (removeTask) {
                    call.respondText("Deleted", status = HttpStatusCode.OK)
                } else
                    call.respondText("Deleted", status = HttpStatusCode.NotFound)
            }
        }


    }


}