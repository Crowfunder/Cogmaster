import {
  createSignal,
  createResource,
  For,
  type Component,
  Match,
  Switch,
} from "solid-js";
import { IndexApiVersion } from "../util/constants.jsx";

const SearchNamesForm: Component<{
  class?: string;
  version: IndexApiVersion;
}> = (props) => {
  const [shouldFetch, setShouldFetch] = createSignal(false);
  const [tradeable, setTradeable] = createSignal(false);

  const url = () =>
    `api/${props.version}/index/info/search/names?tradeable=${tradeable()}`;

  async function fetchSearchNames(): Promise<string[]> {
    const response = await fetch(url());
    const data = await response.json();
    return data;
  }

  const [searchNamesResource, { refetch }] = createResource(
    shouldFetch,
    async () => {
      if (!shouldFetch()) return null;
      return await fetchSearchNames();
    }
  );

  const handleFetch = () => {
    if (shouldFetch()) {
      refetch();
    } else {
      setShouldFetch(true);
    }
  };

  const isLoading = () => searchNamesResource.loading;

  return (
    <div class={`flex-col gap-10 p-4 text-white ${props.class || ""}`}>
      <div class="flex flex-col gap-6 items-center">
        <div class="flex items-center gap-2">
          <input
            type="checkbox"
            id="tradeable-checkbox"
            checked={tradeable()}
            onChange={(e) => setTradeable(e.target.checked)}
            class="w-4 h-4"
          />
          <label for="tradeable-checkbox" class="text-sm">
            Tradeable only
          </label>
        </div>

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
          {isLoading()
            ? "Loading..."
            : shouldFetch()
            ? "Reload Search Names"
            : "Load Search Names"}
        </button>
      </div>

      {/* Results Display */}
      <div class="mt-4">
        <Switch>
          <Match when={searchNamesResource.loading}>
            <div class="text-center text-gray-300">Loading search names...</div>
          </Match>

          <Match when={searchNamesResource.error}>
            <div class="text-center text-red-400">
              Error loading search names: {String(searchNamesResource.error)}
            </div>
          </Match>

          <Match when={searchNamesResource()}>
            <div class="border border-gray-400 rounded bg-gray-800 p-4">
              <div class="text-sm text-gray-300 mb-2">
                Found {searchNamesResource()!.length}{" "}
                {tradeable() ? "tradeable" : ""} search names:
              </div>
              <div class="max-h-[600px] overflow-y-auto border border-gray-600 rounded bg-gray-900 p-2">
                <For each={searchNamesResource()}>
                  {(name) => (
                    <div class="py-1 px-2 text-sm text-gray-100 hover:bg-gray-700 rounded break-all">
                      {name}
                    </div>
                  )}
                </For>
              </div>
            </div>
          </Match>

          <Match when={!shouldFetch()}>
            <div class="text-center text-gray-400">
              Click "Load Search Names" to fetch entry names
            </div>
          </Match>
        </Switch>
      </div>
    </div>
  );
};

export default SearchNamesForm;
