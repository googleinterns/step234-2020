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

let taskTemplate;
let mdcSnackbar;
let taskLoadingProgressBar;

$(document).ready(init);

function init() {
  taskLoadingProgressBar = new mdc.linearProgress.MDCLinearProgress(document.querySelector('#progress-bar'));
  hideDismissedInfo();
  initCheckboxChangeHandlers();
  compileTaskTemplate();
  initSnackbar();
  loadTasks();
  loadCalendar();
}

/**
 * Compiles the handlebars template representing a task.
 */
function compileTaskTemplate() {
  const taskTemplateElement = document.getElementById("task-template").innerHTML;
  taskTemplate = Handlebars.compile(taskTemplateElement);
}

/**
 * Inits the snackbar component.
 */
function initSnackbar() {
  mdcSnackbar = new mdc.snackbar.MDCSnackbar($("#snackbar-result")[0]);
}

function hideDismissedInfo() {
  if (localStorage.getItem("closedInfo")) {
    $("#info").hide();
  }
}

function initCheckboxChangeHandlers() {
  $("#toggle-all").on("change", handleEmptySelection);
  $("#task-list").on("change", "input[type=checkbox]", handleEmptySelection);
}

function hideInfo() {
  $("#info").hide();
  localStorage.setItem("closedInfo", "true");
}

function loadTasks() {
  taskLoadingProgressBar.open();
  fetch("/load_tasks")
      .then(getJsonIfOk)
      .then(renderTasks)
      .catch(handleNetworkError);
}

function renderTasks(tasks) {
  taskLoadingProgressBar.close();
  $("#task-list").empty();
  $("#empty-message").toggle(!tasks.length);
  if (tasks.length > 0) {
    tasks.forEach(renderSingleTask);
  }
  initDropdowns();
  handleEmptySelection();
}

/**
 * Compiles the task template with the actual task data and
 * adds it to the task list.
 */
function renderSingleTask(task) {
  $("#task-list").append(taskTemplate(task));
}

/**
 * Initializes the dropdowns selection components.
 */
function initDropdowns() {
  $(".mdc-select").each(
      function() {
        const mdcSelect = new mdc.select.MDCSelect(this);
        $(this).data("mdcSelect", mdcSelect);
      });
}

function handleEmptySelection() {
  const isAnyChecked = $("input:checkbox[name='taskId']:checked").length > 0;
  $("#schedule-button").prop("disabled", !isAnyChecked);
  if (!isAnyChecked) {
    $("#toggle-all").prop("checked", false);
  }
}

/**
 * Provides feedback to the user that tasks were scheduled, and
 * reports if there are problems.
 */
function updateView(result) {
  $("#scheduling-progress").hide();
  $("#schedule-button").show();
  showResultMessage(result);
  loadTasks();
}

/**
 * Shows the result of scheduling.
 */
function showResultMessage(result) {
  $("#snackbar-result-text").text(result.message);
  mdcSnackbar.open();
}

function schedule() {
  $("#schedule-button").prop("disabled", true);
  $("#schedule-button").hide();
  $("#scheduling-progress").show();
  const formContent = new FormData($("#schedule-form")[0]);
  daterange = $("#daterange").val();
  [startDate, endDate] = daterange.split(' - ');
  formContent.append("startDate", startDate.trim());
  formContent.append("endDate", endDate.trim());
  appendDurations(formContent);
  postData("/schedule", new URLSearchParams(formContent).toString())
      .then(getJsonIfOk)
      .then(updateView)
      .then(refreshCalendar)
      .catch(handleNetworkError);
}

/**
 * Appends to the form data the selected values for the duration of each task.
 */
function appendDurations(formData) {
  $("input:checkbox[name='taskId']:checked").each(
      function () {
        const durationSelect = $(this).closest(".task").find(".task-duration");
        const mdcDurationSelect = $(durationSelect).data("mdcSelect");
        formData.append("taskDuration", mdcDurationSelect.value);
      }
  );
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

/**
 * Toggles all tasks checkboxes.
 */
function toggleAll() {
  const toggleStatus = $("#toggle-all").prop("checked");
  $("#task-list input[type=checkbox]").prop("checked", toggleStatus);
}
