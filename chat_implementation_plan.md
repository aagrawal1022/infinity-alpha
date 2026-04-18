# End-to-End Implementation Plan: Inf-Alpha Web Interface

This document dictates the rollout strategy to create a modern, premium frontend Web Application that will dynamically consume our Spring Boot `/chat` and `/chat/stream` endpoints. 

Per the system guidelines, we will build an incredibly aesthetic interface completely out of **Vite + React** and **Vanilla CSS**.

## Architecture

### 1. Project Initialization
We will scaffold a brand new frontend application inside `/inf-alpha/frontend/` using Vite.
- **Framework:** React + JavaScript
- **Styling:** Vanilla CSS (CSS Variables for dynamic dark themes, glassmorphism utilities).

### 2. Provider & Model Architecture (Integration)
We will fetch the available models organically from the backend so the UI never needs hard-coded configuration.
- **Endpoint:** `GET http://localhost:8080/api/v1/models`
- **Output:** A customized dropdown in the header allowing the user to select logic between the "Paid" vs "Free" tiers and picking specific models.

### 3. The `/chat` Execution Flow
We will implement two distinct interaction patterns based on a toggle switch in the UI (Sync vs. Stream):

#### Scenario A: Synchronous Chat (`/api/v1/chat`)
1. User submits input.
2. React State applies an optimistic UI update (shows a loading animation/spinner).
3. We execute a standard `fetch()` POST request.
4. UI waits for the entire JSON payload to return, then displays it instantly.

#### Scenario B: Streaming Chat (`/api/v1/chat/stream`)
1. User submits input.
2. We execute a `fetch()` POST request with `Accept: text/event-stream`.
3. Instead of parsing JSON, we will grab the `response.body.getReader()`.
4. We utilize a `TextDecoder("utf-8")` inside a `while (true)` loop to read binary chunks as they stream in from the LLM.
5. The React State updates *character-by-character*, creating the smooth "typing" visualization you see in ChatGPT/Claude.

## Proposed Changes

---

### UI Core Framework

#### `frontend/index.css`
The central nervous system of our UI. It will contain:
- Deep, sleek dark-mode variables.
- Fluid background gradients.
- Reusable "Glass" classes (`backdrop-filter: blur(16px)`).
- Custom animated keyframes (pulse, gradient-shift).

#### `frontend/src/App.jsx`
The central routing and state management component. Holds the array of `messages` and the `currentModel`.

#### `frontend/src/components/ChatLayout.jsx`
The master flex-grid. Displays the Model Toggle on top, Chat History in the middle, and the Input Box locked to the bottom.

---

### Service Layer

#### `frontend/src/services/api.js`
All interactions with `localhost:8080` will route through this file to keep React components clean.
- `fetchModels()`
- `sendSyncChat()`
- `sendStreamChat(callback)` -> Contains the complex Javascript Generator logic for reading HTTP byte-streams.

## Verification Plan

### Automated Tests
- Running `npm run dev` to boot the application.
- Ensuring React compiles without errors.

### Manual Verification
- Validate the UI renders a high-quality glassmorphic aesthetic.
- Ask the AI a long question using `gemini-free` and visually confirm that the SSE text stream appears character by character on the screen without lagging.
