import {
  createSignal,
  createResource,
  Component,
  ErrorBoundary,
  For,
  Match,
  Switch,
} from "solid-js";
import { IndexApiVersion } from "../util/constants.jsx";

const ConfigFileNamesDisplay: Component<{
  version: IndexApiVersion;
  class?: string;
}> = (props) => {
  const [shouldFetch, setShouldFetch] = createSignal(false);

  const url = () => `api/${props.version}/index/info/config/names`;

  async function fetchNames(): Promise<{ status: number; body: string[] }> {
    const response = await fetch(url());
    return { status: response.status, body: await response.json() };
  }

  const [configNamesResource, { refetch }] = createResource(shouldFetch, async () => {
    if (!shouldFetch()) return null;
    return await fetchNames();
  });

  const handleFetch = () => {
    if (shouldFetch()) {
      refetch();
    } else {
      setShouldFetch(true);
    }
  };

  const isLoading = () => configNamesResource.loading;

  return (
    <div class={`flex-col gap-10 p-4 text-white ${props.class || ""}`}>
      <div class="flex flex-col gap-6 items-center">
        <button
          classList={{
            "p-2 border border-black rounded-xs transition-colors": true,
            "bg-blue-500 hover:bg-blue-600 active:bg-blue-900 hover:cursor-pointer":
              !isLoading(),
            "bg-gray-400 cursor-not-allowed opacity-50": isLoading(),
          }}
          disabled={isLoading()}
          onClick={handleFetch}
        >
          {isLoading() ? "Loading..." : shouldFetch() ? "Reload Config Names" : "Load Config Names"}
        </button>
      </div>

      {/* Results Display */}
      <div class="mt-4">
      <ErrorBoundary fallback="Errored Config Names Search">
        <Switch fallback={<div>Unhandled state</div>}>
            <Match when={configNamesResource.loading}>
              <div class="text-center text-gray-300">Loading config names...</div>
            </Match>

            <Match when={configNamesResource.error}>
              <div class="text-center text-red-400">
                Error loading config names: {String(configNamesResource.error)}
              </div>
          </Match>

            <Match when={configNamesResource()}>
              <div class="border border-gray-400 rounded bg-gray-800 p-4">
                <div class="text-sm text-gray-300 mb-2">
                  Found {configNamesResource()!.body.length} config names:
                </div>
                <div 
                  class="max-h-[600px] min-w-[400px] overflow-y-auto border border-gray-600 rounded bg-gray-900 p-2"
                >
                  <For each={configNamesResource()!.body}>
                    {(item) => (
                      <div class="py-1 px-2 text-sm text-gray-100 hover:bg-gray-700 rounded">
                        {item}
                      </div>
                    )}
            </For>
                </div>
              </div>
            </Match>

            <Match when={!shouldFetch()}>
              <div class="text-center text-gray-400">
                Click "Load Config Names" to fetch configuration names
              </div>
          </Match>
        </Switch>
      </ErrorBoundary>
    </div>
    </div>
  );
};

export default ConfigFileNamesDisplay;