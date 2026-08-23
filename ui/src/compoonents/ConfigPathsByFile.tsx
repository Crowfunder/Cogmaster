import { createSignal, createResource, For, type Component, Match, Switch } from "solid-js";
import { IndexApiVersion } from "../util/constants.jsx";
import Toggleable from "./Toggleable.jsx";

const ConfigPathsByFile: Component<{
  class?: string;
  version: IndexApiVersion;
}> = (props) => {
  const [shouldFetch, setShouldFetch] = createSignal(false);

  const url = () => `api/${props.version}/index/info/config/map`;

  async function fetchConfigMap(): Promise<Record<string, string[]>> {
    const response = await fetch(url());
    const data = await response.json();
    return data;
  }

  const [configMapResource, { refetch }] = createResource(shouldFetch, async () => {
    if (!shouldFetch()) return null;
    return await fetchConfigMap();
  });

  const handleFetch = () => {
    if (shouldFetch()) {
      refetch();
    } else {
      setShouldFetch(true);
    }
  };

  const isLoading = () => configMapResource.loading;

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
          {isLoading() ? "Loading..." : shouldFetch() ? "Reload Config Map" : "Load Config Map"}
        </button>
      </div>

      <div class="mt-4">
        <Switch>
          <Match when={configMapResource.loading}>
            <div class="text-center text-gray-300">Loading config map...</div>
          </Match>

          <Match when={configMapResource.error}>
            <div class="text-center text-red-400">
              Error loading config map: {String(configMapResource.error)}
            </div>
          </Match>

          <Match when={configMapResource()}>
            <div class="border border-gray-400 rounded bg-gray-800 p-4">
              <div class="text-sm text-gray-300 mb-2">
                Found {Object.keys(configMapResource()!).length} config files:
              </div>
              <div 
                class="max-h-[600px] overflow-y-auto border border-gray-600 rounded bg-gray-900 p-2"
              >
                <For each={Object.entries(configMapResource()!)}>
                  {([filename, paths]) => (
                    <div class="mb-2">
                      <Toggleable label={`${filename} (${paths.length})`}>
                        <div class="ml-4 mt-2">
                        <For each={paths}>
                          {(path) => (
                            <div class="py-1 px-2 text-sm text-gray-100 hover:bg-gray-700 rounded  break-all">
                              {path}
                            </div>
                          )}
                        </For>
                      </div>
                      </Toggleable>
                    </div>
                  )}
                </For>
              </div>
            </div>
          </Match>

          <Match when={!shouldFetch()}>
            <div class="text-center text-gray-400">
              Click "Load Config Map" to fetch configuration mapping
            </div>
          </Match>
        </Switch>
      </div>
    </div>
  );
};

export default ConfigPathsByFile;