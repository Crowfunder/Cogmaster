import { ParameterArray, Path } from "./parameterArray.jsx";

export interface ConfigEntry {
  implementationType: string;
  derivedImplementationType: string;
  path: Path;
  derivedPath: Path;
  sourceConfig: string;
  parameters: ParameterArray;
  routedParameters: ParameterArray;
  derivedParameters: ParameterArray;
  
  // Computed properties that would come from getters
  effectiveImplementation?: string;
  name?: string | null;
  derived?: boolean;
}

