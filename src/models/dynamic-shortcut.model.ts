export interface PinnedShortcut {
    /**
     * Unique identifier of the shortcut (can be defined randomly)
     */
    id: string;

    /**
     * 'Short description'
     */
    shortLabel: string;

    /**
     * Longer string describing the shortcut
     */
    longLabel: string;

    /**
     * Base 64 encoded icon for the shortcut. DEFAULT: AppIcon
     */
    iconBitmap?: 'string';

    /**
     * filename w/o extension of an icon that resides on res/drawable-* (hdpi,mdpi..)
     */
    iconFromResource?: string;

    /**
     * content for the intent
     */
    intent: {
        action: 'android.intent.action.RUN',
        categories: [
            'android.intent.category.TEST', // Built-in Android category
            'MY_CATEGORY' // Custom categories are also supported
        ],
        flags: 67108864,
        data: 'myapp://path/to/launch?param=value', // Must be a well-formed URI
        extras: {
            'android.intent.extra.SUBJECT': 'Hello world!', // Built-in Android extra (string)
            'MY_BOOLEAN': true, // Custom extras are also supported (boolean, number and string only)
        }
    }
}
