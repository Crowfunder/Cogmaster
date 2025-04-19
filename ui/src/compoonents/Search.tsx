import {
  createResource,
  createSignal,
  ErrorBoundary,
  Match,
  Switch,
  type Component,
} from "solid-js";
import { getErrorMessage } from "../util/error.jsx";

const Search: Component<{ name: string; url: string; class?: string }> = (
  props
) => {
  const [inputValue, setInputValue] = createSignal("");
  const [searchKey, setSearchKey] = createSignal(true);
  const [searchQuery, setSearchQuery] = createSignal<string | null>(null);

  async function fetchProperties(
    query: string | null
  ): Promise<{ status: number; text: string } | null> {
    if (query === null) return null;
    const url = `${props.url}${query}`;
    const awaited = await fetch(url);
    const text = await awaited.text();
    return { status: awaited.status, text: text };
  }

  const [getKey, { refetch }] = createResource(searchQuery, fetchProperties);
  const handleSearch = () => {
    const lastSearch = searchQuery();
    setSearchQuery(inputValue());
    if (lastSearch == searchQuery()) refetch();
  };

  return (
    <ErrorBoundary fallback="Errored Search component">
      <div class={`flex-col gap-10 p-4 text-white ${props.class}`}>
        <h2 class="text-md">{props.name}</h2>
        <div class="flex flex-col gap-6 items-center">
          <input
            class="border border-black bg-white text-black size-fit p-1"
            type="text"
            onInput={(e) => {
              setInputValue(e.target.value);
            }}
          ></input>
          <button
            class="hover:cursor-pointer p-2 border border-black bg-blue-500 hover:bg-blue-600 active:bg-blue-900 disabled:bg-gray-900 rounded-xs"
            disabled={getKey.loading}
            onClick={handleSearch}
          >
            Search
          </button>
          <div class=" w-full m-1 p-1 border border-gray-500 inset-1">
            <Switch fallback={<div> Unhandled state {getKey.state}</div>}>
              <Match when={getKey.state == "unresolved"}>
                Search something
              </Match>
              <Match when={getKey.loading}>Loading...</Match>
              <Match when={getKey.error != null}>
                <div class="text-red-600">{getErrorMessage(getKey.error)}</div>
              </Match>
              <Match when={getKey.state == "ready"}>
                <Switch
                  fallback={<div>Error {getKey()?.status} getting result </div>}
                >
                  <Match when={getKey()!.status == 404}>Not Found</Match>
                  <Match when={getKey()!.status == 200 && !!getKey()}>
                    {`${getKey()?.text}`}
                  </Match>
                </Switch>
              </Match>
            </Switch>
          </div>
        </div>
      </div>
    </ErrorBoundary>
  );
};

export default Search;
