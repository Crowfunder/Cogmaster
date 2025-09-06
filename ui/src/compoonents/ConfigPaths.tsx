import { createSignal, createResource, For, type Component, Show, Match, Switch } from "solid-js";
import { IndexApiVersion } from "../util/constants.jsx";

const ConfigPathsForm: Component<{
  class?: string;
  version: IndexApiVersion;
}> = (props) => {
  const [shouldFetch, setShouldFetch] = createSignal(false);

  const url = () => `api/${props.version}/index/info/config/paths`;

  async function fetchPaths(): Promise<string[]> {
    const response = await fetch(url());
    const data = await response.json();
    return data;
  }

  const [pathsResource, { refetch }] = createResource(shouldFetch, async () => {
    if (!shouldFetch()) return null;
    return await fetchPaths();
  });

  const handleFetch = () => {
    if (shouldFetch()) {
      refetch();
    } else {
    setShouldFetch(true);
    }
  };

  const isLoading = () => pathsResource.loading;

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
          {isLoading() ? "Loading..." : shouldFetch() ? "Reload Paths" : "Load All Paths"}
        </button>
      </div>

      {/* Results Display */}
      <div class="mt-4">
        <Switch>
          <Match when={pathsResource.loading}>
          <div class="text-center text-gray-300">Loading paths...</div>
          </Match>

          <Match when={pathsResource.error}>
          <div class="text-center text-red-400">
            Error loading paths: {String(pathsResource.error)}
          </div>
          </Match>

          <Match when={pathsResource()}>
          <div class="border border-gray-400 rounded bg-gray-800 p-4">
            <div class="text-sm text-gray-300 mb-2">
              Found {pathsResource()!.length} paths:
            </div>
            <div 
              class="max-h-[600px] overflow-y-auto border border-gray-600 rounded bg-gray-900 p-2"
            >
              <For each={pathsResource()}>
                {(path) => (
                  <div class="py-1 px-2 text-sm text-gray-100 hover:bg-gray-700 rounded  break-all">
                    {path}
                  </div>
                )}
              </For>
            </div>
          </div>
          </Match>

          <Match when={!shouldFetch()}>
          <div class="text-center text-gray-400">
            Click "Load All Paths" to fetch configuration paths
          </div>
          </Match>
        </Switch>
      </div>
    </div>
  );
};

export default ConfigPathsForm;