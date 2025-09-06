import { createMemo, Show, type Component } from "solid-js";
import { ConfigEntry } from "../util/configEntry.jsx";
import { SearchResultData } from "../util/searchState.jsx";

interface SearchResultDisplayV1Props {
  data: SearchResultData;
  class?: string;
}

const SearchResultDisplayV1: Component<SearchResultDisplayV1Props> = (
  props
) => {
  const parsedData = createMemo(() => {
    if (!props.data || props.data.status !== 200) return null;
    try {
      return JSON.parse(props.data.text) as ConfigEntry;
    } catch (error) {
      console.error("Failed to parse V1 config response:", error);
      return null;
    }
  });

  return (
    <div class={"flex flex-col gap-0.5  " + props.class}>
      <h3 class="font-bold mb-2">Config Entry (V1)</h3>
      <Show when={parsedData()} fallback={<>Failed to parse config entry</>}>
        {(data) => (
          <>
            <div>
              <b>Name: </b>
              {data().name || "No name"}
            </div>
            <div>
              <b>Source Config:</b> {data().sourceConfig}
            </div>
            <div>
              <b>Implementation: </b>
              {data().implementationType}
            </div>
            <div>
              <b>Derived Implementation:</b> {data().derivedImplementationType}
            </div>

            <div>
              <b>Path:</b> {data().path?.path || "No path"}
            </div>
            <div>
              <b>Derived Path:</b>{" "}
              {data().derivedPath?.path || "No derived path"}
            </div>
          </>
        )}
      </Show>
    </div>
  );
};

export default SearchResultDisplayV1;
