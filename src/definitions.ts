//@ts-ignore
declare module "@capacitor/core" {
    interface PluginRegistry {
        AppShortcuts: IAppShortcutsPlugin;
    }
}

export interface IAppShortcutsPlugin {

    /**
     * Resolves promise, if dynamic shortcuts are supported
     */
    iSupported(): Promise<ShortcutSupport>;

    /**
     * Add a pinned shortcut
     */
    addPinned(shortcut: Shortcut): Promise<void>;

    /**
     * Add dynamic shortcuts - inserted shortcuts always overwrite the preview ones
     */
    setDynamic(shortcut: Array<Shortcut>): Promise<void>;

    /**
     * Get the data of the shortcut icon, that opened the app (activity)
     * This function fails, if the app was launched via press on the app icon
     */
    onHomeIconPressed(): Promise<any>;
}

export interface Shortcut {
    /**
     * Unique identifier of the shortcut (can be defined randomly)
     */
    id: string;

    /**
     * Title of the app shortcut
     */
    title: string;

    /**
     * Subtitle of the app shortcut
     */
    subtitle: string;

    /**
     * iOS only: Icon presets
     */
    icon?: IOSShortcutIcon

    /**
     * Android only: Base 64 encoded icon for the shortcut. DEFAULT: AppIcon
     */
    iconBitmap?: string;

    /**
     * Android: filename w/o extension of an icon that resides on res/drawable-* (hdpi,mdpi..)
     * iOS: Can be used to provide your own icon. It must be a valid name of an icon template in your Assets catalog.
     */
    iconFromResource?: string;
}

export interface ShortcutSupport {
    /**
     * True, if the device supports app shortcuts
     */
    appShortcuts: boolean;

    /**
     * Android only: True, if the device supports pinned shortcuts;
     */
    pinnedShortcuts: boolean;
}

export enum IOSShortcutIcon {
    Compose = 'Compose',
    Play = 'Play',
    Pause = 'Pause',
    Add = 'Add',
    Location = 'Location',
    Search = 'Search',
    Share = 'Share',
    Prohibit = 'Prohibit',
    Contact = 'Contact',
    Home = 'Home',
    MarkLocation = 'MarkLocation',
    Favorite = 'Favorite',
    Love = 'Love',
    Cloud = 'Cloud',
    Invitation = 'Invitation',
    Confirmation = 'Confirmation',
    Mail = 'Mail',
    Message = 'Message',
    Date = 'Date',
    Time = 'Time',
    CapturePhoto = 'CapturePhoto',
    CaptureVideo = 'CaptureVideo',
    Task = 'Task',
    TaskCompleted = 'TaskCompleted',
    Alarm = 'Alarm',
    Bookmark = 'Bookmark',
    Shuffle = 'Shuffle',
    Audio = 'Audio',
    Update = 'Update',
}
