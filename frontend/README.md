# ResumeLens frontend

React + TypeScript + Vite client for the ResumeLens API. The application uses the supplied shadcn preset (`b27GcrRo`) and provides the landing upload experience, evidence dashboard, history, model diagnostics, settings, and architecture views.

```bash
npm install
npm run dev
```

During development, Vite proxies `/api` to `http://localhost:8080`. To point directly to a different API origin, set `VITE_API_BASE_URL` before running Vite.

```bash
npm run build
```

The root [`README.md`](../README.md) contains complete project setup, environment variables, API documentation, and model configuration.
