import { createSignal, createResource, type Component } from "solid-js";
import SearchResult from "./SearchResult.jsx";
import { IndexApiVersion } from "../util/constants.jsx";
import { SearchResultData } from "../util/searchState.jsx";

const ConfigSearchForm: Component<{
  class?: string;
  version: IndexApiVersion;
}> = (props) => {
  const [configFileName, setConfigFileName] = createSignal("");
  const [queryPath, setQueryPath] = createSignal("");
  const [shouldSearch, setShouldSearch] = createSignal(false);

  const url = () => `api/${props.version}/index/config/${configFileName()}`;

  async function fetchProperties(): Promise<SearchResultData> {
    const fullUrl = `${url()}?path=${queryPath()}`;
    const response = await fetch(fullUrl);
    const text = await response.text();
    return { status: response.status, text: text };
  }

  const [searchResource, { refetch }] = createResource(
    shouldSearch,
    async () => {
      if (!shouldSearch()) return null;
      return await fetchProperties();
    }
  );

  const handleSearch = () => {
    if (!queryPath().trim() || !configFileName().trim()) {
      return;
    }

    shouldSearch() ? refetch() : setShouldSearch(true);
  };

  const isLoading = () => searchResource.loading;

  return (
    <div class={`flex-col gap-10 w-full p-4 text-white ${props.class || ""}`}>
      <div class="flex flex-col gap-6 items-center">
        <input
          class="border border-black bg-white text-black w-full p-1"
          type="text"
          placeholder="Config filename"
          value={configFileName()}
          onInput={(e) => {
            setConfigFileName(e.target.value);
          }}
        />
        <input
          class="border border-black bg-white text-black w-full p-1"
          type="text"
          placeholder="Query path"
          value={queryPath()}
          onInput={(e) => {
            setQueryPath(e.target.value);
          }}
        />
        <button
          classList={{
            "p-2 border border-black rounded-xs transition-colors": true,
            "bg-blue-500 hover:bg-blue-600 active:bg-blue-900 hover:cursor-pointer":
              !isLoading(),
            "bg-gray-400 cursor-not-allowed opacity-50": isLoading(),
          }}
          disabled={isLoading()}
          onClick={handleSearch}
        >
          {isLoading() ? "Searching..." : "Search"}
        </button>
      </div>

      <SearchResult searchResource={searchResource} version={props.version} />
    </div>
  );
};

export default ConfigSearchForm;
