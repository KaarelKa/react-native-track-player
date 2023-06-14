export interface VideoData {
  identifier: string;
  title: string;
  imageName: string;
  state: DownloadState;
  stringURL: string;
  path?: string;
}

enum DownloadState {
  UNKNOWN,
  PREFETCHING,
  WAITING,
  RUNNING,
  NO_CONNECTION,
  PAUSED,
  COMPLETED,
  CANCELED,
  FAILED,
  KEYLOADED,
}
