import { ErrorBoundary, Match, Switch, type Component, type Resource } from "solid-js";
import { getErrorMessage } from "../util/error.jsx";
import { ConfigEntry } from "../util/configEntry.jsx";
import { SearchResultData } from "../util/searchState.jsx";
import SearchResultDisplayV1 from "./SearchResultDisplayV1.jsx";
import SearchResultDisplayV2 from "./SearchResultDisplayV2.jsx";
import { IndexApiVersion } from "../util/constants.jsx";

interface SearchResultProps {
  searchResource: Resource<SearchResultData | null>;
  class?: string;
  version: IndexApiVersion;
}

const SearchResult: Component<SearchResultProps> = (props) => {
  function parseConfigResponse(response: SearchResultData): ConfigEntry | null {
    if (!response || response.status !== 200) {
      return null;
    }
    try {
      return JSON.parse(response.text) as ConfigEntry;
    } catch (error) {
      console.error("Failed to parse config response:", error);
      return null;
    }
  }

  const parsedData = () => {
    const data = props.searchResource();
    return data ? parseConfigResponse(data) : null;
  };

  return (
    <ErrorBoundary fallback="Errored SearchResult component">
      <div
        class={`w-full m-1 p-1 border border-gray-500 inset-1 ${
          props.class || ""
        }`}
      >
        <Switch fallback={<div>Unhandled state</div>}>
          <Match when={props.searchResource.loading}>Loading...</Match>

          <Match when={props.searchResource.error}>
            <div class="text-red-600">
              {getErrorMessage(props.searchResource.error)}
            </div>
          </Match>

          <Match when={props.searchResource()}>
            <Switch
              fallback={
                <div>
                  Error {props.searchResource()?.status} getting result
                </div>
              }
            >
              <Match when={props.searchResource()?.status === 404}>
                Not Found
              </Match>
              <Match when={props.searchResource()?.status === 200}>
                {props.version === IndexApiVersion.v1 ? (
                  <SearchResultDisplayV1 data={props.searchResource()!} />
                ) : (
                  <SearchResultDisplayV2 data={props.searchResource()!} />
                )}
              </Match>
            </Switch>
          </Match>
        </Switch>
      </div>
    </ErrorBoundary>
  );
};

export default SearchResult;