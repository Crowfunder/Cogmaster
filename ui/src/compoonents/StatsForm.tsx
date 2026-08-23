import { createSignal, createResource, For, type Component, Match, Switch } from "solid-js";
import { IndexApiVersion } from "../util/constants.jsx";

const StatsForm: Component<{
  class?: string;
  version: IndexApiVersion;
}> = (props) => {
  const [shouldFetch, setShouldFetch] = createSignal(false);

  const url = () => `api/${props.version}/index/info/stats`;

  async function fetchStats(): Promise<Record<string, number>> {
    const response = await fetch(url());
    const data = await response.json();
    return data;
  }

  const [statsResource, { refetch }] = createResource(shouldFetch, async () => {
    if (!shouldFetch()) return null;
    return await fetchStats();
  });

  const handleFetch = () => {
    if (shouldFetch()) {
      // If already fetched, refetch
      refetch();
    } else {
      // First time, trigger the fetch
      setShouldFetch(true);
    }
  };

  const isLoading = () => statsResource.loading;

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
          {isLoading() ? "Loading..." : shouldFetch() ? "Reload Stats" : "Load Stats"}
        </button>
      </div>

      {/* Results Display */}
      <div class="mt-4">
        <Switch>
          <Match when={statsResource.loading}>
            <div class="text-center text-gray-300">Loading stats...</div>
          </Match>

          <Match when={statsResource.error}>
            <div class="text-center text-red-400">
              Error loading stats: {String(statsResource.error)}
            </div>
          </Match>

          <Match when={statsResource()}>
            <div class="border border-gray-400 rounded bg-gray-800 p-4">
              <h3 class="font-bold mb-2">Index Statistics</h3>
              <div class="flex flex-col gap-0.5">
                <For each={Object.entries(statsResource()!)}>
                  {([statName, statValue]) => (
                    <div>
                      <b>{statName}:</b> {statValue.toLocaleString()}
                    </div>
                  )}
                </For>
              </div>
            </div>
          </Match>

          <Match when={!shouldFetch()}>
            <div class="text-center text-gray-400">
              Click "Load Stats" to fetch index statistics
            </div>
          </Match>
        </Switch>
      </div>
    </div>
  );
};

export default StatsForm;