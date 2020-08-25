// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

$(document).ready(init);

function init() {
  loadTasks();
}

function loadTasks() {
  fetchData("/load_tasks")
      .then((tasks) => renderTasks(tasks))
      .catch((status) => console.error(status));
}

function renderTasks(tasks) {
  $("task-list").empty();
  tasks.forEach(renderSingleTask);
}

function renderSingleTask(task) {
  id = task.id;
  newTask = $("<p></p>", {"id": id});
  checkBox = $("<input>", {"name": "taskId","value": id, "type": "checkbox"});
  checkBox.appendTo(newTask);
  newTask.append(task.title);
  newTask.addClass("task");
  newTask.appendTo("#task-list");
}

/**
 * Provides feedback to the user that tasks were scheduled, and
 * reports if there are problems.
 */
function updateView(results) {
  alert(results.message);
}

function schedule() {
  const formContent = new FormData($("#task-list")[0]);
  postData("/schedule", new URLSearchParams(formContent).toString())
      .then((results) => updateView(results)); //This is still returning a promise
}

function fetchData(url) {
  return fetch(url)
      .then((response) => getJsonIfOk(response))
      .catch((error) => handleNetworkError(error));
}


function postData(url, data) {
  return fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data
      }
  )
      .then((response) => getJsonIfOk(response))
      .catch((error) => handleNetworkError(error));
}

function handleNetworkError(exception) {
  console.error("There was a network error that prevented the completion of the request.");
  console.error(exception);
}

function getJsonIfOk(response) {
  if (!response.ok) {
    console.error('Server error detected: ' + response.status);
    console.error(response.statusText);
    throw response.status;
  } else {
    return response.json();
  }
}

