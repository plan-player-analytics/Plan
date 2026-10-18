export type VersionInfo =
    | {
          currentVersion: string;
          updateAvailable: false;
      }
    | {
          currentVersion: string;
          updateAvailable: true;
          newVersion: string;
          downloadUrl: string;
          changelogUrl: string;
          isRelease: boolean;
      };
