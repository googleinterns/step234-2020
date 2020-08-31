/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$(document).ready(init);

function init() {
  loadTasks();
  loadCalendar();
}

function loadTasks() {
  fetch("/load_tasks")
      .then(getJsonIfOk)
      .then(renderTasks)
      .catch(handleNetworkError);
}

function renderTasks(tasks) {
  // TODO: check the size of tasks and show a warning if zero
  $("#task-list").empty();
  tasks.forEach(renderSingleTask);
}

function renderSingleTask(task) {
  id = task.id;
  newTask = $("<p></p>", {"id": id});
  checkBox = $("<input>", {"name": "taskId", "value": id, "type": "checkbox"});
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
  $("#message").html(results.message);
  loadTasks();
}

function schedule() {
  const formContent = new FormData($("#schedule-form")[0]);
  daterange = $("#daterange").val();
  [startDate, endDate] = daterange.split(' - ');
  formContent.append("startDate", startDate.trim());
  formContent.append("endDate", endDate.trim());
  postData("/schedule", new URLSearchParams(formContent).toString())
      .then(getJsonIfOk)
      .then(updateView)
      .then(refreshCalendar)
      .catch(handleNetworkError);
}

function postData(url, data) {
  return fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data
      }
  );
}

function handleNetworkError(exception) {
  console.error("There was a network error that prevented the completion of the request.");
  console.error(exception);
}

function getJsonIfOk(response) {
  if (!response.ok) {
    throw new Error(`Server error detected: ${response.status} (${response.statusText})`);
  } else {
    return response.json();
  }
}

/**
 * Handles response by checking it and converting it to text.
 */
function handleTextResponse(response) {
  if (!response.ok) {
    throw new Error("Response error while fetching data: " +
        response.status + " (" + response.statusText + ")");
  }
  return response.text();
}

/**
 * Loads the user's email and sets the source
 * of the calendar iframe.
 */
function loadCalendar() {
  fetch("/user")
    .then(getJsonIfOk)
    .then(setCalendar)
    .catch(handleNetworkError);
}

/**
 * Sets the source of the calendar iframe including
 * the email of the user.
 */
function setCalendar(user) {
  const calendarIframe = document.getElementById("calendar");
  calendarIframe.src =
      `https://calendar.google.com/calendar/embed?src=${user.email}&mode=WEEK`;
}

/**
 * Refreshes the calendar iframe.
 */
function refreshCalendar() {
  let calendarIframe = document.getElementById("calendar");
  // setting it’s src attribute to itself is the only cross-domain and cross-browser
  // solution to reload an iframe (https://stackoverflow.com/a/4062084)
  calendarIframe.src = calendarIframe.src;
}

