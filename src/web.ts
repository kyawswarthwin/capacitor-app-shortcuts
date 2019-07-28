import { WebPlugin } from '@capacitor/core';
import { AppShortcutsPlugin } from './definitions';

export class AppShortcutsWeb extends WebPlugin implements AppShortcutsPlugin {
  constructor() {
    super({
      name: 'AppShortcuts',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const AppShortcuts = new AppShortcutsWeb();

export { AppShortcuts };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AppShortcuts);
