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
let selectOptionsTemplate;
let mdcSnackbar;
let taskLoadingProgressBar;

$(document).ready(init);

function init() {
  taskLoadingProgressBar = new mdc.linearProgress.MDCLinearProgress(document.querySelector('#progress-bar'));
  hideDismissedInfo();
  initCheckboxChangeHandlers();
  compileTemplates();
  fillSelects();
  initSnackbar();
  loadTasks();
  loadCalendar();
  // Todo: add onchange listener to working hours form, save entries to localStorage
  // Also read entries from localStorage
}

/**
 * Compiles the handlebars template representing a task.
 */
function compileTemplates() {
  const taskTemplateElement = document.getElementById("task-template").innerHTML;
  const selectOptionsTemplateElement = document.getElementById("select-options-template").innerHTML;
  taskTemplate = Handlebars.compile(taskTemplateElement);
  selectOptionsTemplate = Handlebars.compile(selectOptionsTemplateElement);
}

const START_HOUR = "09";
const START_MIN = "00";
const END_HOUR = "18";
const END_MIN = "00";

function addZeroBefore(n) {
  return (n < 10 ? '0' : '') + n;
}

function fillSelects() {
  const minutes = {values: ["00", "15", "30", "45"]};
  const hours = {values: Array.from(Array(24).keys()).map((hour) => addZeroBefore(hour))};

  minOptions = selectOptionsTemplate(minutes);
  hourOptions = selectOptionsTemplate(hours);

  $(".min-select").each((index, element) => $(element).append(minOptions));
  $(".hour-select").each((index, element) => $(element).append(hourOptions));
  $("#start-hour li[data-value=" + START_HOUR + " ]").addClass("mdc-list-item--selected");
  $("#start-min li[data-value=" + START_MIN + " ]").addClass("mdc-list-item--selected");
  $("#end-hour li[data-value=" + END_HOUR + " ]").addClass("mdc-list-item--selected");
  $("#end-min li[data-value=" + END_MIN + " ]").addClass("mdc-list-item--selected");

  initSelects();
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
      .then(checkResponse)
      .then(getJson)
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
  initSelects();
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
function initSelects() {
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
  showScheduleButton();
  showResultMessage(result.message);
  loadTasks();
}

/**
 * Shows the schedule button and hides the scheduling loader.
 */
function showScheduleButton() {
  $("#scheduling-progress").hide();
  $("#schedule-button").show();
}

/**
 * Shows the result of scheduling.
 */
function showResultMessage(message) {
  $("#snackbar-result-text").text(message);
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
  formData = appendWorkingHours(formContent); // JavaScript seems pass by reference, maybe = is unnecessary, but increases readability
  postData("/schedule", new URLSearchParams(formContent).toString())
      .then(checkResponse)
      .then(getJson)
      .then(updateView)
      .then(refreshCalendar)
      .catch(restoreScheduleButton)
      .catch(handleNetworkError);
}

/**
 * Restores the schedule button as before clicking it.
 */
function restoreScheduleButton() {
  showScheduleButton();
  handleEmptySelection();
}

function appendWorkingHours(formData) {
  formData.append("startHour", $("#start-hour").data("mdcSelect").value);
  formData.append("startMin", $("#start-min").data("mdcSelect").value);
  formData.append("endHour", $("#end-hour").data("mdcSelect").value);
  formData.append("endMin", $("#end-min").data("mdcSelect").value);
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

/**
 * Checks the status code of the response. If it is ok, then it returns the response,
 * otherwise it shows and throws an error.
 */
function checkResponse(response) {
  if (!response.ok) {
    if (response.status === 400) {
      getJson(response)
          .then((result) => {
            return result.message;
          })
          .then(showResultMessage);
    } else {
      let message;
      if (response.status > 400 && response.status <= 499) {
        message = "Client error: " + response.statusText;
      } else if (response.status >= 500 && response.status <= 599) {
        message = "Server error";
      } else {
        message = "Error";
      }
      showResultMessage(message);
    }
    throw new Error(`Error detected: ${response.status} (${response.statusText})`);
  }
  return response;
}

function getJson(response) {
  return response.json();
}

/**
 * Loads the user's email and sets the source
 * of the calendar iframe.
 */
function loadCalendar() {
  fetch("/user")
      .then(checkResponse)
      .then(getJson)
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
