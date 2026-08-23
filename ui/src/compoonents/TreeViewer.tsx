import {
  createEffect,
  createSignal,
  For,
  Show,
  type Component,
} from "solid-js";
import * as d3 from "d3";

export interface GraphNode {
  path: string;
  children: GraphNode[];
}

export interface HoverableGraphNode extends GraphNode {
  isHovered: boolean;
  isClicked: boolean;
  children: HoverableGraphNode[];
}

const TreeViewer: Component<{
  data: HoverableGraphNode[] | undefined;
  loading?: boolean;
}> = (props) => {
  const firstElement = () =>
    props.data?.find((x) => x.children.some((y) => true));

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
  const [clickedNode, setClickedNode] = createSignal<
    HoverableGraphNode | undefined
  >(undefined);

  // Pan and zoom event handlers
  const handleMouseDown = (e: MouseEvent) => {
    setIsDragging(true);
    setLastMousePos({ x: e.clientX, y: e.clientY });
    if (canvasRef && !hoveredNode()) {
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

  // Add click handler
  const handleClick = (e: MouseEvent) => {
    if (isDragging()) return; // Don't handle clicks during drag

    if (hoveredNode()) {
      hoveredNode()!.isClicked = true;
      setClickedNode(hoveredNode())!.isClicked = true;
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
    if (showDebug) console.log("draw frame");
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
        // Calculate canvas coordinates once if we have mouse position
        let canvasX: number, canvasY: number, radiusSquared: number;
        if (mousePos) {
          canvasX = (mousePos.x - currentTransform.x) / currentTransform.scale;
          canvasY = (mousePos.y - currentTransform.y) / currentTransform.scale;
          radiusSquared = Math.pow(8 / currentTransform.scale, 2);
        }

        let newHoveredNode: HoverableGraphNode | undefined = undefined;
        // Draw nodes and hover detect
        treeData.each((node) => {
          ctx.beginPath();
          const nodeRadius = 6 / currentTransform.scale; // Adjust node size for zoom
          ctx.arc(node.x!, node.y!, nodeRadius, 0, 2 * Math.PI);

          node.data.isHovered = false;
          if (clickedNode() != node.data) node.data.isClicked = false;
          // Hit test during drawing
          if (mousePos) {
            const dx = canvasX - node.x!;
            const dy = canvasY - node.y!;
            if (dx * dx + dy * dy <= radiusSquared) {
              node.data.isHovered = true;
              newHoveredNode = node.data;
            }
          }

          // Determine fill color based on state
          let fillColor = "#69b3a2"; // Default color
          if (node.data.isClicked) {
            fillColor = "#ff8c00"; // Orange for clicked
          } else if (node.data.isHovered) {
            fillColor = "#ffffff"; // White for hovered
          }

          ctx.fillStyle = fillColor;
          ctx.fill();
        });
        setHoveredNode(newHoveredNode);

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
      canvasRef.addEventListener("click", handleClick);

      // Wheel event for zooming
      canvasRef.addEventListener("wheel", handleWheel);

      // Initialize centered view
      setTransform({ x: 200, y: 200, scale: 1 });

      // Cleanup function
      return () => {
        canvasRef?.removeEventListener("mousedown", handleMouseDown);
        window.removeEventListener("mousemove", handleMouseMove);
        window.removeEventListener("mouseup", handleMouseUp);
        canvasRef?.removeEventListener("click", handleClick);
        canvasRef?.removeEventListener("wheel", handleWheel);
      };
    }
  });

  const showDebug = false;

  return (
    <div>
      <div class="flex flex-row justify-center">
        <div class="flex flex-col gap-2">
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
            Pan: Click and drag | Zoom: Mouse wheel | Click: Select node |
            Scale: {transform().scale.toFixed(2)}x
          </div>
        </div>
      </div>
      <Show when={showDebug}>
        <div class="mt-4 text-sm ml-4">
          Roots: {props.data?.length || 0} | Total: {getAllNodes().length}
          <br />
          Root: x={tree()?.x}, y={tree()?.y}
          <br />
          Transform: x={transform().x.toFixed(1)}, y=
          {transform().y.toFixed(1)}, scale={transform().scale.toFixed(2)},
          <br />
          Mouse: x= {currentMousePos()?.x.toFixed(2)} y=
          {currentMousePos()?.y.toFixed(2)}
          <For each={getAllNodes()}>
            {(node) => (
              <div class={node.data.isHovered ? "font-bold" : ""}>
                Path: {node.data.path} - x: {node.x}, y: {node.y} - Clicked:{" "}
                {node.data.isClicked ? "Yes" : "No"}
              </div>
            )}
          </For>
        </div>
      </Show>
      <div>
        <b>Hovered:</b>
        <Show when={hoveredNode()}>
          {(node) => <span>Path: {node().path}</span>}
        </Show>
      </div>
      <div>
        <b>Clicked:</b>
        <Show when={clickedNode()}>
          {(node) => <span>Path: {node().path}</span>}
        </Show>
      </div>
    </div>
  );
};

export default TreeViewer;
