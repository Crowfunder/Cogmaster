
export type SearchResultData = {
  status: number;
  text: string;
} | null;

export type SearchState<TData,TError> = 
  | { state: 'idle' }
  | { state: 'loading' }
  | { state: 'success'; data: TData }
  | { state: 'error'; error: TError };
