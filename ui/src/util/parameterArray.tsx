export interface ParameterArray {
  [key: string]: ParameterValue;
}

export interface ParameterValue {
  value: any;
  nested: boolean;
}

export interface Path {
  path: string | null;
}
