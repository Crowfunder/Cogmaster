import { type Component } from "solid-js";
import ConfigSearchForm from "./ConfigSearchForm.jsx";
import { IndexApiVersion } from "../util/constants.jsx";
import Toggleable from "./Toggleable.jsx";
import ConfigFileNamesDisplay from "./ConfigFileNamesDisplay.jsx";
import ConfigPathsForm from "./ConfigPaths.jsx";
import ConfigPathsByFile from "./ConfigPathsByFile.jsx";
import SearchNamesForm from "./SearchNamesForm.jsx";
import StatsForm from "./StatsForm.jsx";

const Main: Component = () => {
  return (
    <div class="px-52 py-2">
      <div class="w-full grid grid-cols-2 border gap-8 py-4 px-2 ">
        <h3 class="text-2xl mx-auto">V1</h3>
        <h3 class="text-2xl mx-auto">V2</h3>
        <hr class="col-span-full" />
        <h3 class="text-2xl mx-auto col-span-full">index/info/config/names</h3>
        <Toggleable label={`Config Names `}>
          <ConfigFileNamesDisplay version={IndexApiVersion.v1} />
        </Toggleable>
        <Toggleable label={`Config Names `}>
          <ConfigFileNamesDisplay version={IndexApiVersion.v2} />
        </Toggleable>
        <hr class="col-span-full" />
        <h3 class="text-2xl mx-auto col-span-full">info/stats</h3>
        <Toggleable label={`Stats `}>
          <StatsForm version={IndexApiVersion.v1} />
        </Toggleable>
        <Toggleable label={`Stats `}>
          <StatsForm version={IndexApiVersion.v2} />
        </Toggleable>
        <hr class="col-span-full" />
        <h3 class="text-2xl mx-auto col-span-full">
          index/config/fileName?path=query
        </h3>
        <Toggleable label="Config Search ">
          <ConfigSearchForm version={IndexApiVersion.v1} />
        </Toggleable>
        <Toggleable label="Config Search">
          <ConfigSearchForm version={IndexApiVersion.v2} />
        </Toggleable>
        <hr class="col-span-full" />
        <h3 class="text-2xl mx-auto col-span-full">index/info/config/paths</h3>
        <Toggleable label="All Paths">
          <ConfigPathsForm version={IndexApiVersion.v1} />
        </Toggleable>
        <Toggleable label="All Paths">
          <ConfigPathsForm version={IndexApiVersion.v2} />
        </Toggleable>
        <hr class="col-span-full" />
        <h3 class="text-2xl mx-auto col-span-full">index/info/config/map</h3>
        <Toggleable label="All Paths By File">
          <ConfigPathsByFile version={IndexApiVersion.v1} />
        </Toggleable>
        <Toggleable label="All Paths By File">
          <ConfigPathsByFile version={IndexApiVersion.v2} />
        </Toggleable>
        <hr class="col-span-full" />
        <h3 class="text-2xl mx-auto col-span-full">
          info/search/names?tradeable=query
        </h3>
        <Toggleable label="Name Search">
          <SearchNamesForm version={IndexApiVersion.v1} />
        </Toggleable>
        <Toggleable label="Name Search">
          <SearchNamesForm version={IndexApiVersion.v2} />
        </Toggleable>
      </div>
    </div>
  );
};

export default Main;
