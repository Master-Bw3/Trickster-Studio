import { MetaProvider, Title } from "@solidjs/meta";
import { Route, Router } from "@solidjs/router";
import { FileRoutes } from "@solidjs/start/router";
import { Suspense } from "solid-js";
import Editor from "./routes/Editor";

export default function App() {
  return (
    <Router>
      <Route path="/" component={Editor} />
    </Router>
  );
}
