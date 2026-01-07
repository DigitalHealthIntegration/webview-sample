---

# Android WebView Host (Orin Integration)

This Android application serves as a lightweight native wrapper for the Orin Camera Aggregator web application. Its primary responsibility is to bridge the native user input with the web-based recording workflow and manage the application lifecycle based on network signals.

## Purpose

This app acts as the mobile host for the Orin WebView-based workflow. It **does not** implement recording logic, file handling, or backend communication directly; it delegates these tasks to the web application running within the WebView.

## Core Responsibilities

1. **Launch** the Orin web frontend.
2. **Pass** a `userId` into the recording session.
3. **Detect** session completion via network interception.
4. **Reset** the UI state for the next user.

## WebView Behavior

### 1. Session Launch

The app initiates a session by loading the web frontend with a user-provided identifier passed as a query parameter.

* **URL Pattern:** `http://<host>:4173/setup?userId=<USER_ID>`
* **Input:** The `userId` is supplied via a native input field on the start screen.

### 2. Session Termination

The web application signals the end of a workflow by sending a background network request. The native app listens for this specific signal to close the view.

* **Trigger Endpoint:** `POST http://<host>:4000/finish-session`
* **Mechanism:** The web app uses `fetch()` to send this request.

### 3. Native Interception Logic

Since `/finish-session` is a background POST request (and not a navigation event), the standard `shouldOverrideUrlLoading` will not catch it. Instead, the app uses **`WebViewClient.shouldInterceptRequest(...)`**.

**Logic Flow:**

1. **Monitor:** The `WebViewClient` monitors all outgoing requests.
2. **Detect:** When a request matching the `/finish-session` path is detected:
* The app **hides** the WebView.
* The app **navigates** back to the initial start screen.
* A minimal `200 OK` response is returned to the WebView to prevent console errors.



3. **Lifecycle:** The app **does not exit**; it resets to allow a new `userId` entry.

## UI Flow

1. **Start:** App launches to a native input screen.
2. **Input:** User enters `userId`.
3. **Action:** User taps **"Open Session"**.
4. **Active:** WebView opens in fullscreen mode; user completes the web workflow.
5. **Finish:** `/finish-session` is intercepted.
6. **Reset:** User is immediately returned to the native start screen.

---

# Orin API & Integration Documentation
This document outlines the architecture, file management, and integration points for the Orin Camera Aggregator application.
## System Overview
This application is designed to function as a *WebView* embedded within a native mobile host application. It acts as a local controller for connected hardware (Android, Hyperspectral and Realsense cameras), managing recording sessions and data integrity.
### WebView Integration
The application runs a web frontend that communicates with local Python backend services.
* *Session Termination:*
The specific route /finish-session signals the end of the user workflow. The native mobile wrapper must listen for navigation to this route to trigger the closing of the WebView.
## Recording API
The system uses a synchronized start/stop mechanism across connected devices.
### 1. Start Recording
* *Method:* POST
* *Behavior:*
* Accepts a user ID or session identifier.
* *UUID Generation:* To ensure file uniqueness and prevent overwriting, the backend automatically appends a random UUID to the provided identifier.
* *Locking:* Creates a lock file (e.g., .lock.android) to prevent concurrent recording requests.
### 2. Stop Recording
* *Method:* POST
* *Behavior:*
* Stops the camera pipelines safely.
* *Keepalive:* Browser safety protocols use fetch with keepalive: true to ensure stop commands are sent even if the WebView is closed immediately.
* *Cleanup:* Removes lock files upon successful stop.
## File System & Paths
Storage paths are strictly defined via container orchestration.
### Configuration
* *Source:* docker-compose.yml
* *Environment Variable:* OUTPUT_PATH
* *Mechanism:* The application writes to a specific directory defined in the Docker Compose configuration. This directory is mounted as a volume to ensure persistence outside the container.
## Data Integrity & Hashing
To ensure data reliability during the transfer to the cloud, the system employs a pre-upload hashing strategy.
### Naming Convention
Files are generated using a composite naming structure:
[USER_ID]-[UUID]_[SUFFIX].[EXTENSION]
*Example Output:*
text
-rw-r--r-- 1 root root   397854 Dec 26 11:19 ASDSKLAJDAKS-b74711d8-1a71-45e1-8702-385bbe1f6c19_android_gray_blurred_web.mp4
-rw-r--r-- 1 root root  6335301 Dec 26 11:19 ASDSKLAJDAKS-b74711d8-1a71-45e1-8702-385bbe1f6c19.mp4.enc
### Hash Verification
1. *Generation:* Once a recording is finalized, the system calculates a cryptographic hash of the file.
2. *Transmission:* This hash is sent to the Upload Service alongside the file metadata before the upload begins.
3. *Verification:* After the file is uploaded, the Upload Service recalculates the hash of the received file and compares it against the provided hash to confirm that no data corruption occurred during transfer.
---

https://github.com/user-attachments/assets/3c15855c-fbef-4d2d-8c0a-906f77f1f7e0


### Webview Demo
