import {AndroidPinnedShortCut} from '../../src/core/app-shortcuts/models/android-app-shortcut.model';
import {PinnedShortcut} from './models/dynamic-shortcut.model';

declare module "@capacitor/core" {
    interface PluginRegistry {
        AppShortcuts: AppShortcutsPlugin;
    }
}

export interface AppShortcutsPlugin {
    echo(options: { value: string }): Promise<{ value: string }>;

    /**
     * Resolves promise, if dynamic shortcuts are supported
     */
    supportsDynamic(): Promise<void>;

    /**
     * Resolves promise, if dynamic shortcuts are supported
     */
    supportsPinned(): Promise<void>;

    /**
     * Add a pinned shortcut
     */
    addPinned(shortcut: PinnedShortcut): Promise<void>;

    /**
     * Add dynamic shortcuts - inserted shortcuts always overwrite the preview ones
     */
    setDynamic(shortcut: PinnedShortcut): Promise<void>;

    /**
     * Subscribe on new intent
     */
    onNewIntent(): Promise<any>;

    /**
     * Get intent
     */
    getIntent(): Promise<any>;
}
