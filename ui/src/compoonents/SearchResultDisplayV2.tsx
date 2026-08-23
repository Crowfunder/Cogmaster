import { createMemo, Show, type Component } from "solid-js";
import { SearchResultData } from "../util/searchState.jsx";
import { NewConfigEntry } from "../util/newConfigEntry.jsx";
import TreeViewer, { GraphNode, HoverableGraphNode } from "./TreeViewer.jsx";

interface SearchResultDisplayV2Props {
  data: SearchResultData;
  class?: string;
}

const SearchResultDisplayV2: Component<SearchResultDisplayV2Props> = (
  props
) => {
  const parsedData = createMemo(() => {
    if (!props.data || props.data.status !== 200) return null;
    try {
      return JSON.parse(props.data.text) as NewConfigEntry;
    } catch (error) {
      console.error("Failed to parse V2 config response:", error);
      return null;
    }
  });

  const treeViewOfData = createMemo(() => {
    if (parsedData() == null) {
      return undefined;
    }

    let leafEntryNode = parsedData()!;
    let rootGraphNode: HoverableGraphNode = {
      path: leafEntryNode.path.path!,
      children: [],
      isHovered: false,
    };
    while (leafEntryNode.parentReference != null) {
      leafEntryNode = leafEntryNode.parentReference.referencedEntry;
      rootGraphNode = {
        path: leafEntryNode.path.path!,
        children: [rootGraphNode],
        isHovered: false,
      };
    }
    return [rootGraphNode];
  });

  return (
    <div class={"flex flex-col gap-0.5  " + props.class}>
      <h3 class="font-bold mb-2">New Config Entry (V2)</h3>
      <Show
        when={parsedData()}
        fallback={<>Failed to parse new config entry</>}
      >
        {(data) => (
          <>
            <div>
              <b>Name:</b> {data().name || "No name"}
            </div>
            <div>
              <b>Config File:</b> {data().configFileName}
            </div>
            <div>
              <b>Implementation:</b> {data().implementationType}
            </div>
            <div>
              <b>Root Implementation:</b> {data().rootImplementationType}
            </div>
            <div>
              <b>Has Parent:</b> {data().parentReference ? "Yes" : "No"}
            </div>

            <div>
              <b>Path:</b> {data().path?.path || "No path"}
            </div>
            <TreeViewer data={treeViewOfData()} />
          </>
        )}
      </Show>
    </div>
  );
};

export default SearchResultDisplayV2;
