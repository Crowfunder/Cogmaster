import * as d3 from "d3";
import { createSignal, For } from "solid-js";

type ChartProps<T extends Record<string, any>> = {
  width: number;
  height: number;
  margin: number;
  data: T[];
  value: (d: T) => number;
};

const D3 = <T extends Record<string, any>>(p: ChartProps<T>) => {
  const pieGenerator = d3.pie<T>().value(p.value);
  const parsedData = pieGenerator(p.data);

  const radius = Math.min(p.width, p.height) / 2 - p.margin;

  const arcGenerator = d3
    .arc<d3.PieArcDatum<T>>()
    .innerRadius(0)
    .outerRadius(radius);

  const colorGenerator = d3
    .scaleSequential(d3.interpolateWarm)
    .domain([0, p.data.length]);

  const arcs = parsedData.map((d, i) => ({
    path: arcGenerator(d),
    data: d.data,
    color: colorGenerator(i),
  }));

  return (
    <div class="flex justify-center">
      D3 Test
      <svg
        width={`${p.width}`}
        height={`${p.height}`}
        class="m-auto border border-black mt-3"
        viewBox={`${-p.width/2} ${-p.height/2} ${p.width} ${p.height}`}
      >
        {/* <g transform={`translate(${p.width * 2},${p.height * 2}`}> */}
          <For each={arcs}>
            {(d) => (
              <path
                d={d.path ?? undefined}
                fill={d.color}
                class="transition hover:scale-105"
              />
            )}
          </For>
        {/* </g> */}
      </svg>
    </div>
  );
};

export default D3;
