import { ParameterArray, Path } from "./parameterArray.jsx";

export interface NewConfigEntryReference {
  referencedEntry: NewConfigEntry;
}

export interface NewConfigEntry {
  configFileName: string;
  path: Path;
  implementationType: string;
  parentReference: NewConfigEntryReference | null;
  entryParameters: ParameterArray;
  routedParameters: ParameterArray;

  // Computed properties that would come from getters
  rootImplementationType?: string;
  effectiveParameters?: ParameterArray;
  name?: string | null;
}
