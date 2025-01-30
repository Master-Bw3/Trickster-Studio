// @refresh reload
import { mount, StartClient } from "@solidjs/start/client";
import { registerAllFragmentTypes } from "./fragment/old-garbage/Fragment";

mount(() => <StartClient />, document.getElementById("app")!);