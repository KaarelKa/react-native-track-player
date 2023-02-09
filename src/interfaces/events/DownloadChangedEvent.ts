import { VideoData } from './VideoData';

export interface DownloadChangedEvent {
  completedDownloads: VideoData[];
  activeDownloads: VideoData[];
  failedDownloads: VideoData[];
}
