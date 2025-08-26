import {
  createEffect,
  createResource,
  createSignal,
  For,
  Show,
  type Component,
} from "solid-js";
import * as d3 from "d3";

interface GraphNode {
  path: string;
  children: GraphNode[];
}
type GraphNodeResponse = GraphNode[];

interface HoverableGraphNode extends GraphNode {
  isHovered: boolean;
  children: HoverableGraphNode[];
}

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

  const firstElement = () =>
    response()?.body.find((x) => x.children.some((y) => true));

  const hierarchy = () => {
    const root = firstElement();
    return !root ? null : d3.hierarchy(root, (x) => x.children);
  };

  const tree = () => {
    const hierarchyRoot = hierarchy();
    if (!hierarchyRoot) return null;
    const nodes = hierarchyRoot.count();
    const treeLayout = d3
      .tree<HoverableGraphNode>()
      .size([(nodes.value ?? 10) * 15, (nodes.value ?? 10) * 15]);
    return treeLayout(hierarchyRoot);
  };

  // Helper function to collect all nodes recursively
  const getAllNodes = () => {
    const treeData = tree();
    if (!treeData) return [];

    const nodes: d3.HierarchyPointNode<HoverableGraphNode>[] = [];
    treeData.each((node) => {
      nodes.push(node);
    });
    return nodes;
  };

  const [transform, setTransform] = createSignal({
    x: 0,
    y: 0,
    scale: 1,
  });
  const [isDragging, setIsDragging] = createSignal(false);
  const [lastMousePos, setLastMousePos] = createSignal({ x: 0, y: 0 });

  let canvasRef: HTMLCanvasElement | undefined;

  const [currentMousePos, setCurrentMousePos] = createSignal<{
    x: number;
    y: number;
  } | null>(null);
  const [hoveredNode, setHoveredNode] = createSignal<
    HoverableGraphNode | undefined
  >(undefined);

  // Pan and zoom event handlers
  const handleMouseDown = (e: MouseEvent) => {
    setIsDragging(true);
    setLastMousePos({ x: e.clientX, y: e.clientY });
    if (canvasRef) {
      canvasRef.style.cursor = "grabbing";
    }
  };

  const handleMouseMove = (e: MouseEvent) => {
    const rect = canvasRef!.getBoundingClientRect();
    setCurrentMousePos({
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    });

    if (!isDragging()) return;

    const deltaX = e.clientX - lastMousePos().x;
    const deltaY = e.clientY - lastMousePos().y;

    setTransform((prev) => ({
      ...prev,
      x: prev.x + deltaX,
      y: prev.y + deltaY,
    }));

    setLastMousePos({ x: e.clientX, y: e.clientY });
  };

  const handleMouseLeave = () => {
    setHoveredNode(undefined);
    if (canvasRef) {
      canvasRef.style.cursor = "default";
      canvasRef.title = "";
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
    if (canvasRef) {
      canvasRef.style.cursor = "grab";
    }
  };

  const handleWheel = (e: WheelEvent) => {
    e.preventDefault();

    const rect = canvasRef!.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    const scaleFactor = e.deltaY > 0 ? 0.9 : 1.1;
    const newScale = Math.max(
      0.1,
      Math.min(5, transform().scale * scaleFactor)
    );

    // Zoom towards mouse position
    const scaleRatio = newScale / transform().scale;
    const newX = mouseX - (mouseX - transform().x) * scaleRatio;
    const newY = mouseY - (mouseY - transform().y) * scaleRatio;

    setTransform({
      x: newX,
      y: newY,
      scale: newScale,
    });
  };

  // Reset view function
  const resetView = () => {
    setTransform({ x: 200, y: 200, scale: 1 });
  };

  // draw frame
  createEffect(() => {
    console.log("draw frame");
    if (canvasRef && tree()) {
      const ctx = canvasRef.getContext("2d");
      if (ctx) {
        const currentTransform = transform();

        // Clear canvas
        ctx.clearRect(0, 0, 400, 400);

        // Save context and apply transform
        ctx.save();
        ctx.translate(currentTransform.x, currentTransform.y);
        ctx.scale(currentTransform.scale, currentTransform.scale);

        // Draw nodes and links
        const treeData = tree()!;

        // Draw links first
        ctx.strokeStyle = "#999";
        ctx.lineWidth = 1 / currentTransform.scale; // Adjust line width for zoom
        treeData.links().forEach((link) => {
          ctx.beginPath();
          ctx.moveTo(link.source.x!, link.source.y!);
          ctx.lineTo(link.target.x!, link.target.y!);
          ctx.stroke();
        });

        const mousePos = currentMousePos();
        let newHoveredNode: HoverableGraphNode | undefined = undefined;
        // Calculate canvas coordinates once if we have mouse position
        let canvasX: number, canvasY: number, radiusSquared: number;
        if (mousePos) {
          canvasX = (mousePos.x - currentTransform.x) / currentTransform.scale;
          canvasY = (mousePos.y - currentTransform.y) / currentTransform.scale;
          radiusSquared = Math.pow(8 / currentTransform.scale, 2);
        }
        // Draw nodes and hover detect
        ctx.fillStyle = "#69b3a2";
        treeData.each((node) => {
          ctx.fillStyle = "#69b3a2";
          ctx.beginPath();
          const nodeRadius = 5 / currentTransform.scale; // Adjust node size for zoom
          ctx.arc(node.x!, node.y!, nodeRadius, 0, 2 * Math.PI);

          node.data.isHovered = false;
          // Hit test during drawing
          if (mousePos) {
            const dx = canvasX - node.x!;
            const dy = canvasY - node.y!;
            if (dx * dx + dy * dy <= radiusSquared) {
              node.data.isHovered = true;
              newHoveredNode = node.data;
              ctx.fillStyle = "#ffffff";
            }
          }

          ctx.fill();
        });

        // Restore context
        ctx.restore();
      }
    }
  });

  // Set up event listeners when canvas is ready
  createEffect(() => {
    if (canvasRef) {
      canvasRef.style.cursor = "grab";

      // Mouse events for panning
      canvasRef.addEventListener("mousedown", handleMouseDown);
      canvasRef.addEventListener("mousemove", handleMouseMove);
      canvasRef.addEventListener("mouseup", handleMouseUp);
      canvasRef.addEventListener("mouseleave", handleMouseLeave);

      // Wheel event for zooming
      canvasRef.addEventListener("wheel", handleWheel);

      // Initialize centered view
      setTransform({ x: 200, y: 200, scale: 1 });

      // Cleanup function
      return () => {
        canvasRef?.removeEventListener("mousedown", handleMouseDown);
        window.removeEventListener("mousemove", handleMouseMove);
        window.removeEventListener("mouseup", handleMouseUp);
        canvasRef?.removeEventListener("wheel", handleWheel);
      };
    }
  });

  const showDebug = false;
  return (
    <div class="text-white border-b sm:border-0">
      <h1 class="text-2xl">Properties</h1>
      <div class="flex flex-row justify-center">
        <div class="flex flex-col gap-2">
          <button
            class="hover:cursor-pointer p-2 border border-black bg-blue-500 hover:bg-blue-600 active:bg-blue-900 disabled:bg-gray-900 rounded-xs"
            disabled={response.loading}
            onClick={handleSearch}
          >
            Load
          </button>
          <button
            class="hover:cursor-pointer p-2 border border-black bg-green-500 hover:bg-green-600 active:bg-green-900 rounded-xs text-sm"
            onClick={resetView}
          >
            Reset View
          </button>
        </div>
        <div class="ml-4">
          <canvas
            ref={canvasRef}
            width={400}
            height={400}
            class="border border-black"
          />
          <div class="text-xs mt-2 text-gray-300">
            Pan: Click and drag | Zoom: Mouse wheel | Scale:{" "}
            {transform().scale.toFixed(2)}x
          </div>
        </div>
        <Show when={showDebug}>
        <div class="mt-4 text-sm ml-4">
          Roots: {response()?.body?.length || 0} | Total: {getAllNodes().length}
          <br />
          Root: x={tree()?.x}, y={tree()?.y}
          <br />
          Transform: x={transform().x.toFixed(1)}, y={transform().y.toFixed(1)},
          scale={transform().scale.toFixed(2)},
          <br />
          Mouse: x= {currentMousePos()?.x.toFixed(2)} y=
          {currentMousePos()?.y.toFixed(2)}
          <For each={getAllNodes()}>
            {(node) => (
              <div class={node.data.isHovered ? "font-bold" : ""}>
                Path: {node.data.path} - x: {node.x}, y: {node.y}
              </div>
            )}
          </For>
        </div>
        </Show>
      </div>
    </div>
  );
};

export default Visualizer;
