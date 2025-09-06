import {
  createResource,
  createSignal,
  type Component,
} from "solid-js";
import TreeViewer, {  type HoverableGraphNode } from "./TreeViewer.jsx";

const Visualizer: Component = () => {
  const [inputValue, setInputValue] = createSignal("");
  const [searchQuery, setSearchQuery] = createSignal<string | null>(null);

  async function fetchProperties(
    query: string | null
  ): Promise<{ status: number; body: HoverableGraphNode[] } | null> {
    const url = `/api/v1/parseableGraph/all`;
    const awaited = await fetch(url);
    return { status: awaited.status, body: await awaited.json() };
  }
  
  const [response, { refetch }] = createResource(searchQuery, fetchProperties);

  const handleSearch = () => {
    const lastSearch = searchQuery();
    setSearchQuery(inputValue());
    if (lastSearch === searchQuery()) refetch();
  };

  return (
    <div class="text-white border-b sm:border-0">
      <h1 class="text-2xl">Properties</h1>
      <div class="flex flex-col gap-4">
          <button
          class="hover:cursor-pointer p-2 border border-black bg-blue-500 hover:bg-blue-600 active:bg-blue-900 disabled:bg-gray-900 rounded-xs max-w-xs"
            disabled={response.loading}
            onClick={handleSearch}
          >
            Load
          </button>
        <TreeViewer data={response()?.body} loading={response.loading} />
      </div>
    </div>
  );
};

export default Visualizer;