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
      .then((tasks) => renderTasks(tasks));
}

function renderTasks(tasks) {
  tasks.forEach(renderSingleTask);
}

function renderSingleTask(task) {
  id = task.id;
  newTask = $("<p></p>", {"id": id});
  checkBox = $("<input>", {"name": id, "type": "checkbox"});
  checkBox.appendTo(newTask);
  newTask.append(task.title);
  newTask.addClass("task");
  newTask.appendTo("#task-list");
}

function schedule() {
  const formContent = new FormData($("#task-list")[0]);
  postData("/load_events", new URLSearchParams(formContent).toString()); //This is still returning a promise
}

function fetchData(url) {
  return fetch(url)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network error detected');
        }
        return response.json();
      })
      .catch((error) => handleFetchError(error));
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
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network error detected');
        }
        return response.json();
      })
      .catch((error) => handleFetchError(error));
}

function handleFetchError(exception) {
  console.error(exception);
}
