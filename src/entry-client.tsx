// @refresh reload
import { mount, StartClient } from "@solidjs/start/client";
import { registerAllFragmentTypes } from "./fragment/Fragment";

mount(() => <StartClient />, document.getElementById("app")!);