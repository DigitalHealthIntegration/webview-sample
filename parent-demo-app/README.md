# Parent Demo Webview Wrapper (React + Vite + Docker)

## 📌 Overview

This project is a **React-based Parent Wrapper Application** that embeds an external "Camera Aggregator" (or any other web app) inside an **iframe**, manages a session lifecycle, and securely receives messages from the embedded application using `postMessage`.

It serves as a **host application** that:

- Takes a **User ID**
- Launches a child application inside an iframe
- Passes the User ID as a query parameter
- Listens for messages from the child app:
  - `SESSION_DATA` → Displays session results
  - `FINISH_SESSION` → Closes the iframe

---

### Key File: `ParentDemo.tsx`

This is the **core application** that:

- Renders the UI
- Manages session state
- Launches the iframe
- Listens for messages from the embedded app

---

# 🐳 Running the App with Docker

## ✅ Prerequisites

Make sure you have:

- **Docker installed and running**

Check:

```sh
docker --version
```

---

## Step 1 — Build the Docker image

From the project root, run:

```sh
docker build -t parent-demo-app .
```

This will:

- Install dependencies inside a Node container
- Build the React app (`npm run build`)
- Serve it using Nginx in a lightweight Alpine container

---

## Step 2 — Run the container

```sh
docker run -d -p 8080:80 --name parent-demo-container parent-demo-app
```

Explanation:

- `-d` → Run in background
- `-p 8080:80` → Map container port 80 to your machine's port 8080
- `--name parent-demo-container` → Friendly container name

---

## Step 3 — Open in browser

Go to:

```
http://localhost:8080
```

You should see **the same Parent Demo UI**, now running inside Docker.

---

# 🐙 Docker Compose (Optional but Recommended)

Instead of manually building and running the container, you can use Docker Compose.

### `docker-compose.yml`

```yaml
version: "3.8"

services:
  parent-demo:
    build: .
    container_name: parent-demo-container
    ports:
      - "8080:80"
    restart: always
```

### Run with Docker Compose

```sh
docker-compose up --build -d
```

Then open:

```
http://localhost:8080
```

To stop it:

```sh
docker-compose down
```

---

# 📡 How the Iframe Communication Works

The Parent app listens for messages like:

### ✅ Session Data Message

```ts
{
  type: "SESSION_DATA",
  data: {
    userId: "USER_001",
    files: ["file1.jpg", "file2.jpg"],
    timestamp: "2025-01-20T10:00:00Z"
  }
}
```

### ✅ Finish Session Message

```ts
{
  type: "FINISH_SESSION"
}
```

The child app must send messages using:

```js
window.parent.postMessage(message, window.location.origin);
```

---

# 🔐 Security Notes

In production, you should:

- Strictly validate `event.origin` in `ParentDemo.tsx`
- Avoid `ALLOWALL` in Nginx headers
- Use HTTPS
- Restrict iframe permissions

---

# ✍️ Author

**Prince Kumar**
