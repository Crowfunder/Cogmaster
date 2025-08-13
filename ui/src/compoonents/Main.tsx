import { type Component } from "solid-js";
import Visualizer from "./Visualizer.jsx";
import D3 from "./D3.jsx";

const Main: Component = () => {
  
  return (
    <div>
      {/* <D3 height={500} width={500} margin={2} data={[{a:1, b: 'test1'}, {a:1, b: 'test1'}, {a:3, b: 'test3'}]} value={(x) => x.a}/> */}
      <div class="flex justify-center">
        <Visualizer />
      </div>
    </div>
  );
};

export default Main;
