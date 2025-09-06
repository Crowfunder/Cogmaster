import {
  Component,
  JSX,
  createSignal,
  ParentComponent,
  Show,
  Switch,
  Match,
} from "solid-js";

const Toggleable: Component<{
  children: JSX.Element;
  label: string;
  openByDefault?: boolean;
}> = (props) => {
  const [isVisible, setIsVisible] = createSignal(props.openByDefault ?? false);

  return (
    <div class="flex flex-col items-center">
      <button onClick={() => setIsVisible(!isVisible())}>
        <Switch>
          <Match when={isVisible()}>
            <div class="hover:text-white">▼ {props.label}</div>
          </Match>
          <Match when={!isVisible()}>
            <div class="hover:text-white">▶ {props.label}</div>
          </Match>
        </Switch>
      </button>
      <Show when={isVisible()}>{props.children}</Show>
    </div>
  );
};

export default Toggleable;
