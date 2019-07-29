import {Plugins} from '@capacitor/core';
import {IAppShortcutsPlugin, Shortcut} from './definitions';

const {AppShortcutsPlugin} = Plugins;

export class AppShortcuts implements IAppShortcutsPlugin {

    /**
     * Resolves promise, if dynamic shortcuts are supported
     */
    public supportsDynamic(): Promise<void> {
        return AppShortcutsPlugin.supportsDynamic();
    };

    /**
     * Resolves promise, if dynamic shortcuts are supported
     */
    public supportsPinned(): Promise<void> {
        return AppShortcutsPlugin.supportsPinned();
    };

    /**
     * Add a pinned shortcut
     */
    public addPinned(shortcut: Shortcut): Promise<void> {
        return AppShortcutsPlugin.addPinned(shortcut);
    };

    /**
     * Add dynamic shortcuts - inserted shortcuts always overwrite the preview ones
     */
    public setDynamic(shortcuts: Array<Shortcut>): Promise<void> {
        return AppShortcutsPlugin.setDynamic({shortcuts: shortcuts});
    };

    /**
     * Subscribe on new intent
     */
    public onNewIntent(): Promise<any> {
        return AppShortcutsPlugin.onNewIntent();
    };

    /**
     * Get intent
     */
    public getIntent(): Promise<any> {
        return AppShortcutsPlugin.getIntent();
    };
}
