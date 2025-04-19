import { createResource, createSignal, Show, type Component } from "solid-js";
import { getErrorMessage } from "../util/error.jsx";
import Search from "./Search.jsx";

const Properties: Component = () => {
  const [inputValue, setInputValue] = createSignal("");
  const [searchKey, setSearchKey] = createSignal(true);
  const [searchQuery, setSearchQuery] = createSignal<string | null>(null);

  // Create a fetch function that properly uses the current searchQuery
  async function fetchProperties(query: string) {
    if (query === null) return null;
    const url = `/api/v1/properties/key?q=${query}`;
    return await fetch(url);
  }

  // Create the resource with the searchQuery signal as the dependency
  const [getKey] = createResource(searchQuery, fetchProperties);

  const handleSearch = () => {
    // Update the searchQuery signal to trigger the fetch
    setSearchQuery(inputValue());
  };

  return (
    <div class="text-white border-b sm:border-0">
      <h1 class="text-2xl">Properties</h1>
      <div class="flex flex-col sm:gap-20 gap-2 sm:flex-row">
        <Search
          
          name="Look up by key"
          url="/api/v1/properties/key?q="
        />
        <Search name="Look up by value" url="/api/v1/properties/value?q=" />
      </div>
    </div>
  );
};

export default Properties;
