---
description: "Use when editing the React frontend in react-ui/frontend. Covers the legacy React 16 JavaScript app, routing, axios usage, local CSS, and keeping frontend changes aligned with the API gateway."
name: "React Frontend Guidance"
applyTo: "**/*.js, **/*.css, **/package.json"
---
# React Frontend Guidance

- Keep frontend changes inside `react-ui/frontend` unless the task clearly requires backend or gateway updates.
- Preserve the existing stack: React 16, JavaScript, `react-router-dom` v4, axios, `react-bootstrap`, and `react-materialize`.
- Match the current component style in the touched area. This codebase uses class components in multiple places, so do not refactor to hooks, TypeScript, or a new state library unless explicitly requested.
- Keep styling local and incremental. Reuse the existing component-local CSS structure instead of introducing a new styling system.
- Route API traffic through the existing frontend proxy and API gateway rather than hardcoding direct service URLs in UI components.
- If a UI change depends on backend behavior, keep the gateway contract and the consuming component in sync.
- Verify UI changes with the existing frontend build path when practical, and avoid unnecessary package upgrades or tooling churn.
- Do not edit generated frontend build output under `react-ui/frontend/build` or Spring Boot output under `target/`.